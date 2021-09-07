package test.response.wrap;

import com.amaizeing.oktopus.Oktopus;
import com.amaizeing.oktopus.OktopusFlow;
import com.amaizeing.oktopus.OktopusRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
public class WrapResponseApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(WrapResponseApplication.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Oktopus.load();

        var flow = getOrderDetailFlow();
        async(flow);
    }

    static OktopusFlow getOrderDetailFlow() {
//        final var retryConfig = RetryConfig.builder()
//                .retryTimes(2)
//                .backoffCoefficient(2)
//                .initialInterval(Duration.ofMillis(500))
//                .build();

        var tokenRequest = OktopusRequest.on(GetToken.class)
                .requestBodyArgs("dat.bui", "123")
                .onServerError((request, response) -> {
                    LOGGER.error("Exception", response.getException());
                });

        var orderRequest = OktopusRequest.on(GetOrder.class).urlArgs(1);
        var orderDetailRequest = OktopusRequest.on(GetOrderDetail.class);

        var getOrderDetailFlow = OktopusFlow.register();
        getOrderDetailFlow.append(orderRequest);
        getOrderDetailFlow.append(tokenRequest);
        getOrderDetailFlow.append(orderDetailRequest);
        return getOrderDetailFlow;
    }

    static void async(OktopusFlow flow) throws ExecutionException, InterruptedException {
        final var error = flow.async();
        System.out.println("finish async...");
        error.get();

        final GetToken.WrapTokenResponse tokenResponse = flow.getResponse(GetToken.class);
        System.out.println(tokenResponse.getData().getToken());
    }

    static void sync(OktopusFlow flow) {
        final var error = flow.sync();
        if (error.isPresent()) {
//            log.error("Error on executing flow with request: {}", error.get().getRequest(), error.get().getException());
            return;
        }

        final GetToken.WrapTokenResponse tokenResponse = flow.getResponse(GetToken.class);
        System.out.println(tokenResponse.getData().getToken());

        final GetOrder.WrapOrderResponse orderResponse = flow.getResponse(GetOrder.class);
        System.out.println(orderResponse.getData().getOrderId());
        System.out.println(orderResponse.getData().getOrderDetailIds());

        final Map<Long, GetOrderDetail.WrapOrderDetailResponse> orderDetailResponses = flow.getResponse(GetOrderDetail.class);
        orderDetailResponses.forEach((k, v) -> {
            final var response = v.getData();
            System.out.println(response.getOrderId() + " " + response.getOrderDetailId() + " " + response.getDescription());
        });

    }

}
