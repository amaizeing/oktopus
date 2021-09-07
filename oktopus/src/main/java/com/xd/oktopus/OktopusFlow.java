package com.xd.oktopus;

import com.xd.oktopus.exception.OktopusException;
import com.xd.oktopus.util.Validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class OktopusFlow {

    private final Map<Class<?>, Object> requestToResponse = new HashMap<>();
    private final Map<Integer, Set<OktopusRequest>> requestsLayerMap = new HashMap<>();
    private final Object lock = new Object();
    private final FlowState state = new FlowState();
    private final Set<OktopusRequest> flowRequests = new HashSet<>();
    private final Set<Class<?>> flowRequestClasses = new HashSet<>();

    private OktopusFlow() {
    }

    public static OktopusFlow register(OktopusRequest... requests) {
        final var flow = new OktopusFlow();
        for (var request : requests) {
            flow.append(request);
        }
        return flow;
    }

    public OktopusFlow append(OktopusRequest... requests) {
        for (var request : requests) {
            if (!flowRequestClasses.add(request.getRequestClass())) {
                throw new OktopusException("Duplicate request");
            }
            if (!flowRequests.add(request)) {
                throw new OktopusException("Duplicate request");
            }
        }
        return this;
    }

    public Optional<RequestError> sync() {
        buildRequestsLayers();
        synchronized (lock) {
            if (!state.isReady()) {
                throw new IllegalStateException("Flow state must be " + FlowState.State.READY + " for running");
            }
            state.running();
        }
        requestToResponse.clear();
        final var dependedMap = Oktopus.getDependedMap();
        for (int i = 0; i < requestsLayerMap.size(); i++) {
            var requests = requestsLayerMap.get(i);
            for (var request : requests) {
                final var dependedRequests = dependedMap.get(request.getRequestClass());
                final var dependedRequestToResponse = Validation.isNullOrEmpty(dependedRequests) ? null : dependedRequests.stream()
                        .collect(Collectors.toMap(Function.identity(), requestToResponse::get));
                final var response = Oktopus.syncSingleRequest(request, dependedRequestToResponse);
                if (response.getException() != null) {
                    return Optional.of(new RequestError(request.getRequestClass(), request, response.getException()));
                }
                requestToResponse.put(request.getRequestClass(), response.getData());
            }
        }
        state.complete();
        return Optional.empty();
    }

    public Future<Optional<RequestError>> async() {
        buildRequestsLayers();
        synchronized (lock) {
            if (!state.isReady()) {
                throw new IllegalStateException("Flow state must be " + FlowState.State.READY + " for running");
            }
            state.running();
        }
        requestToResponse.clear();
        return Oktopus.getInstance().getWorker().submit(() -> {
            final var dependedMap = Oktopus.getDependedMap();
            for (int i = 0; i < requestsLayerMap.size(); i++) {
                var requests = requestsLayerMap.get(i);
                final var futureResponses = new HashMap<OktopusRequest, Future<Response<Object>>>();
                for (var request : requests) {
                    final var dependedRequests = dependedMap.get(request.getRequestClass());
                    final var dependedRequestToResponse = Validation.isNullOrEmpty(dependedRequests) ? null : dependedRequests.stream()
                            .collect(Collectors.toMap(Function.identity(), requestToResponse::get));
                    final var response = Oktopus.asyncSingleRequest(request, dependedRequestToResponse);
                    futureResponses.put(request, response);
                }
                for (var e : futureResponses.entrySet()) {
                    final var response = e.getValue().get();
                    if (response.getException() != null) {
                        return Optional.of(new RequestError(e.getKey().getRequestClass(), e.getKey(), response.getException()));
                    }
                    requestToResponse.put(e.getKey().getRequestClass(), response.getData());
                }
            }
            state.complete();
            return Optional.empty();
        });
    }

    @SuppressWarnings("unchecked")
    public <T> T getResponse(Class<?> requestClass) {
        if (state.isComplete()) {
            return (T) requestToResponse.get(requestClass);
        }
        throw new IllegalStateException("Flow is not complete");
    }

    @SuppressWarnings("unchecked")
    public <T> T getResponse(Class<?> requestClass, Class<T> responseClass) {
        return getResponse(requestClass);
    }

    private void buildRequestsLayers() {
        synchronized (lock) {
            if (!state.isInitiate()) {
                return;
            }
            state.build();
        }
        final Map<Class<?>, OktopusRequest> classToRequest = flowRequests.stream()
                .collect(Collectors.toMap(OktopusRequest::getRequestClass, Function.identity()));
        final Set<Class<?>> allRequestClasses = classToRequest.keySet();
        for (var request : flowRequests) {
            int layer = findRequestLayer(request, classToRequest, allRequestClasses);
            var layerRequests = requestsLayerMap.computeIfAbsent(layer, k -> new HashSet<>());
            layerRequests.add(request);
        }
        state.ready();
    }


    private int findRequestLayer(OktopusRequest request, Map<Class<?>, OktopusRequest> classToRequest, Set<Class<?>> allRequestClasses) {
        final var dependedMap = Oktopus.getDependedMap();
        final var dependedRequests = dependedMap.get(request.getRequestClass());
        if (Validation.isNullOrEmpty(dependedRequests)) {
            return 0;
        }
        return dependedRequests.stream().mapToInt(dependedRequest -> {
            if (!allRequestClasses.contains(dependedRequest)) {
                throw new OktopusException("Not found depended request: " + dependedRequest.getName() + "on request: " + request.getRequestClass().getName() + " in flow");
            }
            return 1 + findRequestLayer(classToRequest.get(dependedRequest), classToRequest, allRequestClasses);
        }).max().orElseThrow(() -> new OktopusException("TODO"));
    }

}
