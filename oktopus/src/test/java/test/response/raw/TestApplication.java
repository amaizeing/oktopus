package test.response.raw;

import com.amaizeing.oktopus.Oktopus;
import com.amaizeing.oktopus.OktopusFlow;
import com.amaizeing.oktopus.OktopusRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class TestApplication {

    public static void main(String[] args) {

        Oktopus.load();

        test();

    }

    static void test() {
        var tokenRequest = OktopusRequest.on(GetToken.class)
                .requestBodyArgs("dat.bui", "123");
        var orderRequest = OktopusRequest.on(GetOrder.class)
                .urlArgs(1);
        var orderDetailRequest = OktopusRequest.on(GetOrderDetail.class);

        var getOrderDetailFlow = OktopusFlow.register();
        getOrderDetailFlow.append(tokenRequest)
                .append(orderRequest)
                .append(orderDetailRequest);

        final var error = getOrderDetailFlow.sync();
        if (error.isPresent()) {
            log.error("Error on executing flow with request: {}", error.get().getRequest(), error.get().getException());
            return;
        }

        final var tokenResponse = getOrderDetailFlow.getResponse(GetToken.class, GetToken.TokenResponse.class);
        System.out.println(tokenResponse.getToken());

        final GetOrder.OrderResponse orderResponse = getOrderDetailFlow.getResponse(GetOrder.class);
        System.out.println(orderResponse.getOrderId());
        System.out.println(orderResponse.getOrderDetailIds());

        final Map<Long, GetOrderDetail.OrderDetailResponse> orderDetailResponses = getOrderDetailFlow.getResponse(GetOrderDetail.class);
        orderDetailResponses.forEach((k, v) -> {
            System.out.println(v.getOrderId() + " " + v.getOrderDetailId() + " " + v.getDescription());
        });

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
