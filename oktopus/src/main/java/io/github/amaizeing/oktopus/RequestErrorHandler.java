package io.github.amaizeing.oktopus;

public interface RequestErrorHandler {

    void onRequestError(ClientRequest request, ClientResponse response);

}
