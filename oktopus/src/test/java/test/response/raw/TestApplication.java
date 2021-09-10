package test.response.raw;

import io.github.amaizeing.oktopus.Oktopus;
import io.github.amaizeing.oktopus.OktopusFlow;
import io.github.amaizeing.oktopus.OktopusRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class TestApplication {

    public static void main(String[] args) throws InterruptedException {

        Oktopus.load();

        test();

        System.out.println("------------- round 2-------------");
        test();

        Thread.sleep(8000);
        System.out.println("------------- round 3-------------");
        test();
        Thread.sleep(2000);
        System.out.println("------------- round 4-------------");
        test();
    }

    @SneakyThrows
    static void test() {
        final var requestId = UUID.randomUUID().toString();

        var tokenRequest = OktopusRequest.on(GetToken.class)
                .urlArgs("http://localhost:9090/login/")
                .headersArgs(requestId)
                .requestBodyArgs("dat.bui", "123");

        var orderRequest = OktopusRequest.on(GetOrder.class)
                .headersArgs(requestId)
                .urlArgs("http://localhost:9090/orders/1");

        var orderDetailRequest = OktopusRequest.on(GetOrderDetail.class)
                .headersArgs(requestId)
                .urlArgs("http://localhost:9090/order-details/");

        var orderShipmentRequest = OktopusRequest.on(GetOrderShipment.class)
                .headersArgs(requestId)
                .urlArgs("http://localhost:9090/shipments/");

        var flow = OktopusFlow.register()
                .append(tokenRequest)
                .append(orderRequest)
                .append(orderDetailRequest)
                .append(orderShipmentRequest);

        final var error = flow.execute();
        if (error.get().isPresent()) {
            log.error("Error on executing flow with request: {}", error.get().get().getRequest(), error.get().get().getException());
            return;
        }

        final var tokenResponse = flow.getResponse(GetToken.class, GetToken.Response.class);
        log.info("Token response: {}", tokenResponse);

        final GetOrder.Response orderResponse = flow.getResponse(GetOrder.class);
        log.info("Order response: {}", orderResponse);

        final Map<Long, GetOrderDetail.Response> orderDetailResponses = flow.getResponse(GetOrderDetail.class);
        orderDetailResponses.forEach((orderDetailId, response) -> log.info("Order detail response: {}", response));

        final Map<Long, GetOrderShipment.Response> shipmentResponses = flow.getResponse(GetOrderShipment.class);
        shipmentResponses.forEach((shipmentId, response) -> log.info("Shipment response: {}", response));

    }

    static void testLayerDependence() {
        var request1 = OktopusRequest.on(RequestDependence.Request01.class);
        var request2 = OktopusRequest.on(RequestDependence.Request02.class);
        var request3 = OktopusRequest.on(RequestDependence.Request03.class);
        var request4 = OktopusRequest.on(RequestDependence.Request04.class);
        var request5 = OktopusRequest.on(RequestDependence.Request05.class);
        var request6 = OktopusRequest.on(RequestDependence.Request06.class);
        var request7 = OktopusRequest.on(RequestDependence.Request07.class);
        var request8 = OktopusRequest.on(RequestDependence.Request08.class);
        var request9 = OktopusRequest.on(RequestDependence.Request09.class);
        var request10 = OktopusRequest.on(RequestDependence.Request10.class);
        var request11 = OktopusRequest.on(RequestDependence.Request11.class);
        var request12 = OktopusRequest.on(RequestDependence.Request12.class);
        var request13 = OktopusRequest.on(RequestDependence.Request13.class);

        var requests = List.of(request1, request2, request3, request4, request5, request6,
                               request7, request8, request9, request10, request11, request12, request13);
        requests = new ArrayList<>(requests);
        Collections.shuffle(requests);
        OktopusFlow.register(requests.toArray(new OktopusRequest[]{}));
    }

}
