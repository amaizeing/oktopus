package test.response.wrap;

import io.github.amaizeing.oktopus.Oktopus;
import io.github.amaizeing.oktopus.OktopusFlow;
import io.github.amaizeing.oktopus.OktopusRequest;
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
        run(flow);
    }

    static OktopusFlow getOrderDetailFlow() {
//        final var retryConfig = RetryConfig.builder()
//                .retryTimes(2)
//                .backoffCoefficient(2)
//                .initialInterval(Duration.ofMillis(500))
//                .build();

        var tokenRequest = OktopusRequest.on(GetToken.class)
                .requestBodyArgs("datbui", "123")
                .onServerError((request, response) -> {
                    final Map<String, Object> responseBody = response.getBody();
                    LOGGER.error("Fail to request token with user: {}. Code: {}. Message: {}",
                                 "invalid",
                                 response.getHttpStatusCode(),
                                 responseBody.get("error"));
                });

        var orderRequest = OktopusRequest.on(GetOrder.class)
                .urlArgs(1)
                .onServerError((request, response) -> {
                });

        var orderDetailRequest = OktopusRequest.on(GetOrderDetail.class)
                .onServerError((request, response) -> {
                });

        return OktopusFlow.register(tokenRequest, orderDetailRequest, orderRequest);
    }

    static void run(OktopusFlow flow) throws ExecutionException, InterruptedException {
        final var error = flow.execute();
        System.out.println("finish async...");
        var err = error.get();
        if (err.isPresent()) {
            LOGGER.error("EXCEPTION", err.get().getException());
            return;
        }

        final var tokenResponse = flow.getResponse(GetToken.class, GetToken.WrapTokenResponse.class);
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
