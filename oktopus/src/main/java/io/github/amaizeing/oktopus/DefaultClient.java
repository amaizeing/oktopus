package io.github.amaizeing.oktopus;

import io.github.amaizeing.oktopus.exception.ClientRequestException;
import io.github.amaizeing.oktopus.util.JsonUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.concurrent.Future;

public class DefaultClient implements RestClient {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient CLIENT = new OkHttpClient();

    @Override
    public ClientResponse sync(final RequestInfo requestInfo) throws IOException {
        final var request = new Request.Builder()
                .url(requestInfo.getUrl());
        if (requestInfo.getHeader() != null) {
            requestInfo.getHeader().forEach((key, val) -> request.addHeader(String.valueOf(key), String.valueOf(val)));
        }
        final var method = requestInfo.getMethod();
        switch (method) {
            case GET: {
                request.get();
                break;
            }
            case DELETE: {
                request.delete();
                break;
            }
            case PUT:
            case POST: {
                if (requestInfo.getBody() != null) {
                    final var requestBody = RequestBody.create(JsonUtil.toJson(requestInfo.getBody(), null), JSON);
                    request.post(requestBody);
                }
                break;
            }
            case HEAD:
            case PATCH:
            case OPTIONS:
            case TRACE:
            default: {
                // TODO
                return new ClientResponse().setException(new OperationNotSupportedException("TODO: Implement later"));
            }
        }
        return makeRequest(requestInfo, request.build(), requestInfo.getErrorHandler());
    }

    @Override
    public Future<ClientResponse> async(final RequestInfo requestInfo) {
        return Oktopus.getInstance().getWorker().submit(() -> sync(requestInfo));
    }

    private ClientResponse makeRequest(RequestInfo requestInfo, Request request, RequestErrorHandler handler) throws IOException {
        final var response = CLIENT.newCall(request).execute();

        var clientResponse = new ClientResponse();
        clientResponse.setHttpStatusCode(response.code());
        final var body = response.body();
        if (!response.isSuccessful()) {
            if (body != null) {
                final var bytes = body.bytes();
                if (requestInfo.getResponseTypeOnFailure() == byte[].class) {
                    clientResponse.setBody(bytes);
                } else {
                    clientResponse.setBody(JsonUtil.toObject(bytes, requestInfo.getResponseTypeOnFailure()));
                }
            }
            if (handler != null) {
                final var clientRequest = new ClientRequest()
                        .setRequestBody(requestInfo.getBody())
                        .setHeader(requestInfo.getHeader())
                        .setMethod(requestInfo.getMethod())
                        .setUrl(requestInfo.getUrl());
                Oktopus.getInstance().getWorker().execute(() -> handler.onRequestError(clientRequest, clientResponse));
            }
            return new ClientResponse().setException(new ClientRequestException("Receiving unsuccessful response with code: "
                                                                                        + response.code() + ". "
                                                                                        + requestInfo.getMethod()
                                                                                        + ": " + requestInfo.getUrl()));
        }
        if (body != null) {
            final var bytes = body.bytes();
            if (requestInfo.getResponseTypeOnSuccess() == byte[].class) {
                clientResponse.setBody(bytes);
            } else {
                clientResponse.setBody(JsonUtil.toObject(bytes, requestInfo.getResponseTypeOnSuccess()));
            }
        }
        response.headers().forEach(pair -> clientResponse.addHeader(pair.getFirst(), pair.getSecond()));
        return clientResponse;
    }


}
