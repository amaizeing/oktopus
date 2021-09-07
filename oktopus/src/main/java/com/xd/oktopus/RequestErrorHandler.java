package com.xd.oktopus;

public interface RequestErrorHandler {

    void onRequestError(ClientRequest request, ClientResponse response);

}
