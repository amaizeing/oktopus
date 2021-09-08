package io.github.amaizeing.oktopus;

import io.github.amaizeing.oktopus.annotation.OktopusCacheKey;
import io.github.amaizeing.oktopus.annotation.OktopusCacheKeys;
import io.github.amaizeing.oktopus.annotation.OktopusCacheTtl;
import io.github.amaizeing.oktopus.annotation.OktopusCacheTtls;
import io.github.amaizeing.oktopus.annotation.OktopusDependOn;
import io.github.amaizeing.oktopus.annotation.OktopusRequestBodies;
import io.github.amaizeing.oktopus.annotation.OktopusRequestBody;
import io.github.amaizeing.oktopus.annotation.OktopusRequestHeader;
import io.github.amaizeing.oktopus.annotation.OktopusRequestHeaders;
import io.github.amaizeing.oktopus.annotation.OktopusRequestUrl;
import io.github.amaizeing.oktopus.annotation.OktopusRequestUrls;
import io.github.amaizeing.oktopus.annotation.OktopusResponseBody;
import io.github.amaizeing.oktopus.annotation.method.HttpMethod;
import io.github.amaizeing.oktopus.annotation.method.OktopusRequestType;
import io.github.amaizeing.oktopus.exception.OktopusAnnotationException;
import io.github.amaizeing.oktopus.exception.OktopusException;
import io.github.amaizeing.oktopus.util.ClassUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import org.atteo.classindex.ClassIndex;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public final class Oktopus {

    private static final Logger LOGGER = LoggerFactory.getLogger(Oktopus.class);

    private static final Oktopus INSTANCE = new Oktopus();

    private final Map<Class<?>, Map<Class<? extends Annotation>, Method>> requestClassToMethods = new HashMap<>();
    private final Map<Class<?>, RequestInstance> requestClassToRequestInfo = new HashMap<>();
    private final Map<Class<?>, Set<Class<?>>> directlyDependedMap = new HashMap<>();
    private final Map<Class<?>, Map<Class<?>, Field>> requestToDependedFieldMap = new HashMap<>();

    private RestClient restClient;
    private OktopusWorker worker;
    private boolean ready = false;
    private final Map<Class<? extends Annotation>, Set<Class<?>>> requestAnnotationToAcceptableResponses
            = Map.of(OktopusRequestUrl.class, Set.of(String.class),
                     OktopusRequestUrls.class, Set.of(Map.class),
                     OktopusRequestHeader.class, Set.of(Map.class),
                     OktopusRequestHeaders.class, Set.of(Map.class),
                     OktopusRequestBody.class, Set.of(),
                     OktopusRequestBodies.class, Set.of(Map.class),
                     OktopusCacheKey.class, Set.of(String.class),
                     OktopusCacheKeys.class, Set.of(Map.class),
                     OktopusCacheTtl.class, Set.of(Long.class, long.class, Integer.class, int.class, Short.class, short.class),
                     OktopusCacheTtls.class, Set.of(Map.class));

    private Oktopus() {
    }

    static Oktopus getInstance() {
        return INSTANCE;
    }

    OktopusWorker getWorker() {
        return worker;
    }

    static Map<Class<?>, Set<Class<?>>> getDependedMap() {
        return getInstance().directlyDependedMap;
    }

    public static void load() {
        load(OktopusConfig.defaultConfig());
    }

    @SneakyThrows
    public static Pair<Class<?>, Class<?>> getResponseType(Annotation httpMethodAnnotation) {
        var responseTypeOnSuccessMethod = httpMethodAnnotation.annotationType().getMethod("onSuccess");
        var responseTypeOnSuccess = (Class<?>) responseTypeOnSuccessMethod.invoke(httpMethodAnnotation);
        var responseTypeOnFailureMethod = httpMethodAnnotation.annotationType().getMethod("onFailure");
        var responseTypeOnFailure = (Class<?>) responseTypeOnFailureMethod.invoke(httpMethodAnnotation);
        return Pair.of(responseTypeOnSuccess, responseTypeOnFailure);
    }

    @SuppressWarnings("unchecked")
    public static void load(OktopusConfig config) {
        synchronized (getInstance()) {
            if (getInstance().ready) {
                throw new OktopusException("Oktopus already initiated");
            }
            getInstance().ready = true;
        }
        if (config.getRestClient() == null) {
            throw new OktopusException("Not found RestClient class on config");
        }
        if (config.getWorker() == null) {
            throw new OktopusException("Not found Worker class on config");
        }

        getInstance().restClient = config.getRestClient();
        getInstance().worker = config.getWorker();

        final var httpMethods = ClassIndex.getAnnotated(OktopusRequestType.class);
        for (Class<?> hm : httpMethods) {
            final var requests = ClassIndex.getAnnotated((Class<? extends Annotation>) hm);
            var httpMethod = hm.getAnnotation(OktopusRequestType.class).value();
            for (var request : requests) {
                final var annotationToMethod = new HashMap<Class<? extends Annotation>, Method>(getInstance().requestAnnotationToAcceptableResponses.size());
                var hmAnnotation = request.getAnnotation((Class<? extends Annotation>) hm);
                var responseTypes = getResponseType(hmAnnotation);
                final var methods = request.getDeclaredMethods();
                for (var method : methods) {
                    var annotations = method.getAnnotations();
                    for (var methodAnnotation : annotations) {
                        getInstance().requestAnnotationToAcceptableResponses.forEach((annotation, acceptableResponses) -> {
                            if (!annotation.isAssignableFrom(methodAnnotation.getClass())) {
                                return;
                            }
                            final var returnType = method.getReturnType();
                            if (!acceptableResponses.isEmpty() && acceptableResponses.stream().noneMatch(returnType::isAssignableFrom)) {
                                throw new IllegalArgumentException("Return type of method: " + method.getName() + "()" +
                                                                           " in request: " + request.getName() +
                                                                           " must be in 1 of following type(s): " + acceptableResponses);
                            }
                            putToMethodMap(request, annotationToMethod, annotation, method);
                        });
                    }
                }
                validateRequestMethod(annotationToMethod, request.getName());
                getInstance().requestClassToMethods.put(request, annotationToMethod);
                final var requestInstance = RequestInstance.builder()
                        .requestClass(request)
                        .responseTypeOnSuccess(responseTypes.getLeft())
                        .responseTypeOnFailure(responseTypes.getRight())
                        .httpMethod(httpMethod)
                        .singleUrl(annotationToMethod.containsKey(OktopusRequestUrl.class))
                        .build();
                getInstance().requestClassToRequestInfo.put(request, requestInstance);
            }
        }
        verifyCyclicDependsOn(getInstance().requestClassToMethods.keySet());
    }

    private static void verifyCyclicDependsOn(Set<Class<?>> requests) {
        final var dependSet = new HashSet<Pair<Class<?>, Class<?>>>();
        for (var request : requests) {
            verifyCyclicRequest(requests, request, new ArrayList<>(), dependSet);
        }
        dependSet.forEach(pair -> LOGGER.info("Request {} depends on request {}", pair.getLeft().getName(), pair.getRight().getName()));
        final Map<Class<?>, Set<Class<?>>> dependedMap = dependSet.stream()
                .collect(Collectors.groupingBy(Pair::getKey, Collectors.mapping(Pair::getRight, Collectors.toSet())));
        getInstance().directlyDependedMap.putAll(dependedMap);
    }

    private static void verifyCyclicRequest(Set<Class<?>> requests, Class<?> request, List<Class<?>> flow, Set<Pair<Class<?>, Class<?>>> dependSet) {
        var fields = request.getDeclaredFields();
        final var dependedRequestToField = new HashMap<Class<?>, Field>();
        for (var field : fields) {
            var dependOnAnnotation = field.getDeclaredAnnotation(OktopusDependOn.class);
            if (dependOnAnnotation == null) {
                continue;
            }
            if (Modifier.isStatic(field.getModifiers())) {
                throw new OktopusAnnotationException("Cannot using @" + OktopusDependOn.class.getSimpleName() + " on static field: " + field + " in request: " + request.getName());
            }
            var dependOnRequest = dependOnAnnotation.value();
            if (!requests.contains(dependOnRequest)) {
                throw new OktopusAnnotationException("Not found request " + dependOnRequest + " that request " + request.getName() + " depended on");
            }
            if (flow.contains(dependOnRequest)) {
                throw new OktopusAnnotationException("Found a cyclic @" + OktopusDependOn.class.getSimpleName() + " on with flow: " + flow);
            }
            final var fieldType = field.getType();
            final var dependOnRequestInstance = INSTANCE.requestClassToRequestInfo.get(dependOnRequest);
            if (dependOnRequestInstance.isSingleUrl()) {
                final var dependOnResponseType = INSTANCE.requestClassToRequestInfo.get(dependOnRequest).getResponseTypeOnSuccess();
                if (!fieldType.isAssignableFrom(dependOnResponseType)) {
                    throw new OktopusAnnotationException("Field: " + field.getName() + " of request: " + request.getName()
                                                                 + " has type: " + fieldType.getSimpleName()
                                                                 + " different with depended on request response type: " + dependOnResponseType.getSimpleName());
                }
            } else if (!fieldType.isAssignableFrom(Map.class)) {
                throw new OktopusAnnotationException("Field: " + field.getName() + " of request: " + request.getName()
                                                             + " has type: " + fieldType.getSimpleName()
                                                             + ". Must be in type: Map<K,V> because of depended request: " + dependOnRequestInstance.getRequestClass().getName()
                                                             + " is a list of requests");
            }
            dependedRequestToField.put(dependOnRequest, field);
            dependSet.add(Pair.of(request, dependOnRequest));
            flow.add(0, request);
            verifyCyclicRequest(requests, dependOnRequest, new ArrayList<>(flow), dependSet);
        }
        if (!dependedRequestToField.isEmpty()) {
            getInstance().requestToDependedFieldMap.put(request, dependedRequestToField);
        }
    }

    private static void putToMethodMap(final Class<?> request, final Map<Class<? extends Annotation>, Method> methodMap, final Class<? extends Annotation> key, final Method value) {
        if (methodMap.containsKey(key)) {
            throw new OktopusAnnotationException("Duplicate @" + key.getSimpleName() + " in request: " + request);
        }
        methodMap.put(key, value);
    }

    private static void validateRequestMethod(Map<Class<? extends Annotation>, Method> annotationToMethod, String requestClass) {
        var url = annotationToMethod.containsKey(OktopusRequestUrl.class) ? 1 : 0;
        var urls = annotationToMethod.containsKey(OktopusRequestUrls.class) ? 1 : 0;
        if ((url ^ urls) != 1) {
            throw new OktopusAnnotationException("Only accept 1 @" + OktopusRequestUrl.class.getSimpleName() +
                                                         " or @" + OktopusRequestUrls.class.getSimpleName() + " in request: " + requestClass);
        }
        var containTtl = annotationToMethod.containsKey(OktopusCacheTtl.class) ? 1 : 0;
        var containCacheKey = annotationToMethod.containsKey(OktopusCacheKey.class) ? 1 : 0;
        var containCacheKeys = annotationToMethod.containsKey(OktopusCacheKeys.class) ? 1 : 0;
        if (((containCacheKey | containCacheKeys) == 0) && containTtl != 0) {
            throw new OktopusAnnotationException("Missing @" + OktopusCacheKey.class.getSimpleName() +
                                                         " or @" + OktopusCacheKeys.class.getSimpleName() + " in request: " + requestClass);
        }
        if (((url == 1 && containCacheKey == 0) || (urls == 1 && containCacheKeys == 0)) && containTtl != 0) {
            throw new OktopusAnnotationException("Ambiguous between single or multiple request because of cacheKeys in request: " + requestClass);
        }
        var header = annotationToMethod.containsKey(OktopusRequestHeader.class);
        var headers = annotationToMethod.containsKey(OktopusRequestHeaders.class);
        if (header && headers) {
            throw new OktopusAnnotationException("Only accept 1 @" + OktopusRequestHeader.class.getSimpleName() +
                                                         " or @" + OktopusRequestHeaders.class.getSimpleName() + " in request: " + requestClass);
        }
        if (headers && url == 1) {
            throw new OktopusAnnotationException("Ambiguous between single or multiple request because of headers in request: " + requestClass);
        }
        var requestBody = annotationToMethod.containsKey(OktopusRequestBody.class);
        var requestBodies = annotationToMethod.containsKey(OktopusRequestBodies.class);
        if (requestBody && requestBodies) {
            throw new OktopusAnnotationException("Only accept 1 @" + OktopusRequestBody.class.getSimpleName() +
                                                         " or @" + OktopusRequestBodies.class.getSimpleName() + " in request: " + requestClass);
        }
        if (requestBodies && url == 1) {
            throw new OktopusAnnotationException("Ambiguous between single or multiple request because of requestBodies in request: " + requestClass);
        }
        if ((containCacheKey & containCacheKeys) == 1) {
            throw new OktopusAnnotationException("Only accept 1 @" + OktopusCacheKey.class.getSimpleName() +
                                                         " or @" + OktopusCacheKeys.class.getSimpleName() + " in request: " + requestClass);
        }
        if (containCacheKey == 1 && containTtl == 0) {
            throw new OktopusAnnotationException("Missing @" + OktopusCacheTtl.class.getSimpleName() + " in request: " + requestClass);
        }
        if (containCacheKeys == 1 && containTtl == 0) {
            throw new OktopusAnnotationException("Missing @" + OktopusCacheTtl.class.getSimpleName() + " in request: " + requestClass);
        }
    }

    @SuppressWarnings("unchecked")
    static <T> Response<T> syncSingleRequest(OktopusRequest oktopusRequest, Map<Class<?>, Object> dependedRequestToResponse) {
        final var requestInfoMap = getInstance().getRequestInfoMap(oktopusRequest, dependedRequestToResponse);
        final var futureMap = requestInfoMap.getRight()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    var requestInfo = e.getValue();
                    return getInstance().worker.submit(() -> {
                        try {
                            final var cacheKey = requestInfo.getCacheKey();
                            if (cacheKey == null) {
                                final var response = syncRequest(requestInfo);
                                // TODO: Retry if needed
                                if (response.getException() != null) {
                                    return new Response<T>().setException(response.getException());
                                }
                                return response;
                            }
                            final T cacheResponse = RequestCache.get(cacheKey);
                            if (cacheResponse != null) {
                                return cacheResponse;
                            }
                            final Response<T> response = syncRequest(requestInfo);
                            // TODO: Retry if needed
                            if (response.getException() != null) {
                                return new Response<T>().setException(response.getException());
                            }
                            final var annotationToValue = new HashMap<Class<? extends Annotation>, Object>();
                            annotationToValue.put(OktopusRequestUrl.class, requestInfo.getUrl());
                            annotationToValue.put(OktopusRequestUrls.class, requestInfo.getUrls());
                            annotationToValue.put(OktopusRequestHeader.class, requestInfo.getHeader());
                            annotationToValue.put(OktopusRequestBody.class, requestInfo.getBody());
                            annotationToValue.put(OktopusResponseBody.class, response.getData());
                            final var cacheTtlMethod = requestInfo.getCacheTtlMethod();
                            if (cacheTtlMethod == null) {
                                return response;
                            }
                            var cacheTtlArgs = getMethodArgs(cacheTtlMethod, annotationToValue);
                            final var ttl = cacheTtlMethod.invoke(requestInfo.getRequestInstance(), cacheTtlArgs);
                            final var timeUnit = cacheTtlMethod.getAnnotation(OktopusCacheTtl.class).value();
                            RequestCache.put(cacheKey, response, ttl, timeUnit);
                            return response;
                        } catch (Exception ex) {
                            return new Response<T>().setException(ex);
                        }
                    });
                }));

        try {
            if (Boolean.TRUE.equals(requestInfoMap.getLeft())) {
                final var futureOpt = futureMap.values().stream().findFirst();
                if (futureOpt.isPresent()) {
                    return (Response<T>) futureOpt.get().get();
                }
                return new Response<T>().setException(new OktopusException("Unexpected error..."));
            }
            final Response<Map<Object, T>> response = new Response<>();
            final Map<Object, T> resultMap = new HashMap<>();
            for (var entry : futureMap.entrySet()) {
                final var key = entry.getKey();
                final var value = (Response<T>) entry.getValue().get();
                if (value.getException() != null) {
                    response.setException(value.getException());
                    break;
                }
                resultMap.put(key, value.getData());
            }
            response.setData(resultMap);
            return (Response<T>) response;
        } catch (Exception ex) {
            return new Response<T>().setException(ex);
        }
    }

    static <T> Future<Response<T>> asyncSingleRequest(OktopusRequest oktopusRequest, Map<Class<?>, Object> dependedRequestToResponse) {
        return getInstance().getWorker().submit(() -> syncSingleRequest(oktopusRequest, dependedRequestToResponse));
    }

    @SuppressWarnings("unchecked")
    private static <T> Response<T> syncRequest(final RequestInfo requestInfo) {
        try {
            LOGGER.debug("Sending request to: {} with body: {}", requestInfo.getUrl(), requestInfo.getBody());
            final var responseInfo = getInstance().restClient.sync(requestInfo);
            LOGGER.debug("Receive response from: {}", requestInfo.getUrl());
            return new Response<T>()
                    .setData(responseInfo.getBody())
                    .setException(responseInfo.getException())
                    .setHeaders(responseInfo.getHeaders())
                    .setHttpStatusCode(responseInfo.getHttpStatusCode());
        } catch (Exception ex) {
            return new Response<T>().setException(ex);
        }
    }


    @NotNull
    private static Object[] getMethodArgs(final Method method, final Map<Class<? extends Annotation>, Object> annotationToValue) {
        var paramsAnnotations = method.getParameterAnnotations();
        var arguments = new Object[paramsAnnotations.length];
        for (int i = 0; i < paramsAnnotations.length; i++) {
            for (var annotation : paramsAnnotations[i]) {
                var type = annotation.annotationType();
                if (!annotationToValue.containsKey(type)) {
                    continue;
                }
                final var value = annotationToValue.get(type);
                arguments[i] = value;
                break;
            }
        }
        return arguments;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private Pair<Boolean, Map<?, RequestInfo>> getRequestInfoMap(OktopusRequest request, Map<Class<?>, Object> dependedRequestToResponseBody) {

        final var requestInstance = requestClassToRequestInfo.get(request.getRequestClass());
        if (requestInstance == null) {
            throw new OktopusException("Not found any request: " + request.getRequestClass().getName());
        }
        final var instance = ClassUtil.getInstance(requestInstance.getRequestClass());
        final var methods = requestClassToMethods.get(request.getRequestClass());

        if (dependedRequestToResponseBody != null) {
            dependedRequestToResponseBody.forEach((dependedRequest, response) -> {
                var dependedRequestToField = INSTANCE.requestToDependedFieldMap.get(requestInstance.getRequestClass());
                final var field = dependedRequestToField.get(dependedRequest);
                try {
                    field.setAccessible(true);
                    field.set(instance, response);
                } catch (IllegalAccessException e) {
                    throw new OktopusException(e);
                }
            });
        }

        final Object url;
        final Method urlMethod;
        if (requestInstance.isSingleUrl()) {
            urlMethod = methods.get(OktopusRequestUrl.class);
        } else {
            urlMethod = methods.get(OktopusRequestUrls.class);
        }
        url = urlMethod.invoke(instance, request.getUrlArgs());

        final var headersMethod = methods.get(OktopusRequestHeader.class);
        final var headers = headersMethod == null ? null : headersMethod.invoke(instance, request.getHeadersArgs());
        final var requestBodyMethod = methods.get(OktopusRequestBody.class);
        final var body = requestBodyMethod == null ? null : requestBodyMethod.invoke(instance, request.getBodyArgs());
        final var cacheKeyMethod = requestInstance.isSingleUrl() ? methods.get(OktopusCacheKey.class) : methods.get(OktopusCacheKeys.class);
        final Object cacheKey;
        if (cacheKeyMethod != null) {
            final var annotationToValue = new HashMap<Class<? extends Annotation>, Object>();
            if (requestInstance.isSingleUrl()) {
                annotationToValue.put(OktopusRequestUrl.class, url);
            } else {
                annotationToValue.put(OktopusRequestUrls.class, url);
            }
            annotationToValue.put(OktopusRequestHeader.class, headers);
            annotationToValue.put(OktopusRequestBody.class, body);
            var cacheKeyArgs = getMethodArgs(cacheKeyMethod, annotationToValue);
            cacheKey = cacheKeyMethod.invoke(instance, cacheKeyArgs);
        } else {
            cacheKey = null;
        }
        if (requestInstance.isSingleUrl()) {
            final var requestInfo = new RequestInfo().setUrl((String) url)
                    .setErrorHandler(request.getErrorHandler())
//                    .setTimeout(request.getTimeout())
//                    .setRetryConfig(request.getRetryConfig())
                    .setRequestInstance(instance)
                    .setBody(body)
                    .setHeader((Map<String, Object>) headers)
                    .setMethod(requestInstance.getHttpMethod())
                    .setResponseTypeOnSuccess(requestInstance.getResponseTypeOnSuccess())
                    .setResponseTypeOnFailure(requestInstance.getResponseTypeOnFailure())
                    .setCacheKey(cacheKey)
                    .setCacheTtlMethod(methods.get(OktopusCacheTtl.class));
            return Pair.of(true, Map.of(url, requestInfo));
        }
        final var cacheKeys = (Map<?, String>) cacheKey;
        final var requestInfoMap = ((Map<?, String>) url).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                                          e -> new RequestInfo().setUrl(e.getValue())
//                                                  .setTimeout(request.getTimeout())
//                                                  .setRetryConfig(request.getRetryConfig())
                                                  .setErrorHandler(request.getErrorHandler())
                                                  .setRequestInstance(instance)
                                                  .setBody(body)
                                                  .setHeader((Map<String, Object>) headers)
                                                  .setMethod(requestInstance.getHttpMethod())
                                                  .setResponseTypeOnSuccess(requestInstance.getResponseTypeOnSuccess())
                                                  .setResponseTypeOnFailure(requestInstance.getResponseTypeOnFailure())
                                                  .setCacheKey(cacheKeys == null ? null : cacheKeys.get(e.getKey()))
                                                  .setCacheTtlMethod(methods.get(OktopusCacheTtl.class))));
        return Pair.of(false, requestInfoMap);
    }

    @Getter
    @Builder
    private static final class RequestInstance {

        private final Class<?> requestClass;
        private final Class<?> responseTypeOnSuccess;
        private final Class<?> responseTypeOnFailure;
        private final HttpMethod httpMethod;
        private final boolean singleUrl;

    }

}
