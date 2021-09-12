# oktopus

---

## What is this?

Nowadays, Micro-services is trending and popular architecture to develop the big services. It brings a lot of advantages
when comparing to Monolithic architecture.

There is a variety of ways to separate a business to multi domains for applying Microservice. Example, we have an
E-commerce to order something we want. We can separate to 3 or 4 services:

- Order service
- Payment service
- Shipment service

Divide and conquer, it's always be like this. Some other services or 3rd-party wants to integrate with our system. They
want to get customer information, order detail... or a lot of combination things. We have a pattern to do it is
Aggregator. Aggregator stands between 3rd-party and our service, aggregate result before responding to client.

![](https://i.imgur.com/Ie0TzoE.png)

There are 4 APIs to get an order information:

- Request token.
- Using this token to query order.
- Using order id to query shipment detail and order detail.

![](https://i.imgur.com/MzOOAAv.png)

The steps are:

- Step 1: Call Get token API. Cache result if needed.
- Step 2: Using token to query order.
- Step 3: Using token (step 1) and order id (step 2) to query shipment and order detail.

Some notice when doing this:

- Control cache of token.
- Build logic to call API in sequence or parallel in specific cases.
- If any API get an error, stop the flow immediately.

It's ok if the flow is small. But what happen if has flow like this?

![](https://i.imgur.com/LegD0AL.png)

What APIs can be executed in parallel, what must be executed in order?

Oktopus will help us on that. It supports on building the APIs layers, caching result, we just define the request, add
annotation, execute and waiting for result. Great.

Under the hood, **Oktopus** using OkHttp to make a RESTful request to server and Jackson to serializer/deserializer JSON
object.

If you want to use other Http client or JSON serializer, **Oktopus** provide the way to customize it. You just create
your own ways and register with Oktopus config.

## Maven

**amaizeing-oktopus** has been developed by Java 11 and deployed to maven central repository, so you only need to add
following dependencies to the pom.xml.

**SNAPSHOT** version:

```xml

<dependency>
  <groupId>io.github.amaizeing</groupId>
  <artifactId>amizeing-oktopus</artifactId>
  <version>0.1.0</version>
</dependency>
```

Then, including below libraries version that you prefer:

```xml

<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-api</artifactId>
  <version>1.7.32</version>
</dependency>
<dependency>
<groupId>ch.qos.logback</groupId>
<artifactId>logback-classic</artifactId>
<version>1.2.5</version>
</dependency>
<dependency>
<groupId>com.squareup.okhttp3</groupId>
<artifactId>okhttp</artifactId>
<version>4.9.1</version>
</dependency>
<dependency>
<groupId>com.fasterxml.jackson.core</groupId>
<artifactId>jackson-databind</artifactId>
<version>2.12.5</version>
</dependency>
```

## Quick start

We go with the example above for getting order information:

- Get access token
- Get order information
- Get order detail (include many requests with different parameter)
- Get shipment information

### 1. Token request

Using Annotation to define the request with 4 mandatory:

- **Http method**: @Post(onSuccess = GetToken.ResponseBody.class)
- **Endpoint**: @OktopusRequestUrl
- **Request header**: @OktopusRequestHeader
- **Request body**: @OktopusRequestBody

In some case, we want to cache the result of request by user information. Just implement 2 methods with cache key and
time to live with following annotations. Of course the time to live can be defined yourself or getting from body
response. If so, we just add annotation @OktopusResponseBody before argument or even @OktopusRequestBody.

- **Key**: @OktopusCacheKey
- **Time to live**: @OktopusCacheTtl

```java

@Post(onSuccess = GetToken.ResponseBody.class)
public class GetToken {

    @OktopusRequestUrl
    public String url(String url) {
        return url;
    }

    @OktopusRequestHeader
    public Map<String, String> headers(String requestId) {
        return Map.of("X-Request-Id", requestId);
    }

    @OktopusRequestBody
    public RequestBody requestBody(String userName, String password) {
        return new RequestBody(userName, password);
    }

    @OktopusCacheKey
    public String cacheKey(@OktopusRequestBody RequestBody tokenRequest) {
        return tokenRequest.getUserName();
    }

    @OktopusCacheTtl(TimeUnit.SECONDS)
    public long cacheTtl(@OktopusResponseBody ResponseBody tokenResponse) {
        return tokenResponse.ttlInSeconds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class RequestBody {

        private String userName;
        private String password;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class ResponseBody {

        private String accessToken;
        private long ttlInSeconds;

    }

}
```

### 2. Get order

Now, get order will be depended on token request result. Just define a new variable with @DependOn.

```java

@Get(onSuccess = GetOrder.Response.class)
public class GetOrder {

    @OktopusDependOn(GetToken.class)
    private GetToken.ResponseBody token;

    @OktopusRequestUrl
    public String url(String url) {
        return url;
    }

    @OktopusRequestHeader
    public Map<String, String> initHeader(String requestId) {
        return Map.of("Authorization", "Bearer " + token.getAccessToken(),
                      "X-Request-Id", requestId);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Response {

        private long orderId;
        private String status;
        private List<Long> orderDetailIds;
        private List<Long> shipmentIds;

    }

}
```

### 3. Get order details

Next step, define Get order detail request which depended on token response and order response.

However, single order can include many order details. In this case, we can use annotation with singular or plural noun
based on your need:

- **Endpoint**: @OktopusRequestUrl or @OktopusRequestUrls
- **Request header**: @OktopusRequestHeader or @OktopusRequestHeaders
- **Request body**: @OktopusRequestBody or @OktopusRequestBodies

The response type of those methods with plural annotation is Map of key and result. In this case, request header of all
requests are the same so we can use singular annotation with @OktopusRequestHeader.

```java

@Get(onSuccess = GetOrderDetail.Response.class)
public class GetOrderDetail {

    @OktopusDependOn(GetToken.class)
    private GetToken.ResponseBody tokenResponse;

    @OktopusDependOn(GetOrder.class)
    private GetOrder.Response orderResponse;

    @OktopusRequestUrls
    public Map<Long, String> urls(String baseUrl) {
        final var orderDetailIds = orderResponse.getOrderDetailIds();
        return orderDetailIds.stream()
                .collect(Collectors.toMap(Function.identity(),
                                          id -> baseUrl + orderResponse.getOrderId()));
    }

    @OktopusRequestHeader
    public Map<String, String> initHeader(String requestId) {
        return Map.of("Authorization", "Bearer " + tokenResponse.getAccessToken(),
                      "X-Request-Id", requestId);
    }

    @Data
    public static final class Response {

        private long orderId;
        private long orderDetailId;
        private String description;

    }

}
```

### 4. Get order shipments

Do the same thing with get order detail.

```java

@Get(onSuccess = GetOrderShipment.Response.class)
public class GetOrderShipment {

    @OktopusDependOn(GetToken.class)
    private GetToken.ResponseBody tokenResponse;

    @OktopusDependOn(GetOrder.class)
    private GetOrder.Response orderResponse;

    @OktopusRequestUrls
    public Map<Long, String> urls(String baseUrl) {
        final var orderDetailIds = orderResponse.getOrderDetailIds();
        return orderDetailIds.stream()
                .collect(Collectors.toMap(Function.identity(),
                                          id -> baseUrl + orderResponse.getId()));
    }

    @OktopusRequestHeader
    public Map<String, String> initHeader(String requestId) {
        return Map.of("Authorization", "Bearer " + tokenResponse.getAccessToken(),
                      "X-Request-Id", requestId);
    }

    public static final class Response {

        private long id;
        private String driverName;
        private String licensePlate;
        private String phoneNumber;

    }

}
```

### 5. Build request flow

Creating request flow and adding request into it, prepare to execute.

```java
var requestId=UUID.randomUUID().toString();

        var tokenRequest=OktopusRequest.on(GetToken.class)
        .urlArgs("http://localhost:9090/login/")
        .headersArgs(requestId)
        .requestBodyArgs("dat.bui","123");

        var orderRequest=OktopusRequest.on(GetOrder.class)
        .headersArgs(requestId)
        .urlArgs("http://localhost:9090/orders/1");

        var orderDetailRequest=OktopusRequest.on(GetOrderDetail.class)
        .headersArgs(requestId)
        .urlArgs("http://localhost:9090/order-details/");

        var orderShipmentRequest=OktopusRequest.on(GetOrderShipment.class)
        .headersArgs(requestId)
        .urlArgs("http://localhost:9090/shipments/");

        var flow=OktopusFlow.register()
        .append(tokenRequest)
        .append(orderRequest)
        .append(orderDetailRequest)
        .append(orderShipmentRequest);
```

**Oktopus** will base on your defined request to build the request layer. With this example, we can have 3 layers:

- Layer 1: request token.
- Layer 2: request order.
- Layer 3: request order detail and shipment info.

**Note**:

- Requests in next layer will be executed after prev layer completed.
- Requests in single layer will be executed in parallel.
- If any request get error, the flow will try to stop executing.

![](https://i.imgur.com/82drUBf.png)

### 6. Run flow

Flow is async. The result of execute() is future error.

```java
Future<Optional<RequestError>>err=flow.execute();
```

### 7. Get response

Finally, after executing without any error, we need to get response to aggregate the result before responding to client.

```java
Optinal<RequestError> flowErr=err.get();
        if(err.isPresent()){
        log.error("Error on executing flow with request: {}",error.get().get().getRequest(),error.get().get().getException());
        return;
        }

final var tokenResponse=flow.getResponse(GetToken.class,GetToken.ResponseBody.class);
        log.info("Token response: {}",tokenResponse);

final GetOrder.Response orderResponse=flow.getResponse(GetOrder.class);
        log.info("Order response: {}",orderResponse);

final Map<Long, GetOrderDetail.Response>orderDetailResponses=flow.getResponse(GetOrderDetail.class);
        orderDetailResponses.forEach((orderDetailId,response)->log.info("Order detail response: {}",response));

final Map<Long, GetOrderDetail.Response>shipmentResponses=flow.getResponse(GetOrderShipment.class);
        shipmentResponses.forEach((shipmentId,response)->log.info("Shipment response: {}",response));
```

Finally, build the response based on business logic.
