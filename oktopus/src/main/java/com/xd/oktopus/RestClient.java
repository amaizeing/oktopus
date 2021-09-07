package com.xd.oktopus;

import java.util.concurrent.Future;

public interface RestClient {

    ClientResponse sync(RequestInfo requestInfo) throws Exception;

    Future<ClientResponse> async(RequestInfo requestInfo) throws Exception;

}
