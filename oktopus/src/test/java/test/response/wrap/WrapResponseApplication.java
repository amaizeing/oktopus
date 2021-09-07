package test.response.wrap;

import com.xd.oktopus.Oktopus;
import com.xd.oktopus.OktopusFlow;
import com.xd.oktopus.OktopusRequest;
import com.xd.oktopus.RetryConfig;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

@Slf4j
public class WrapResponseApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(WrapResponseApplication.class);

    public static void main(String[] args) {

        Oktopus.load();

        var flow = getOrderDetailFlow();
        async(flow);
    }

    static OktopusFlow getOrderDetailFlow() {
        final var retryConfig = RetryConfig.builder()
                .retryTimes(2)
                .backoffCoefficient(2)
                .initialInterval(Duration.ofMillis(500))
                .build();

        var tokenRequest = OktopusRequest.on(GetToken.class)
                .requestBodyArgs("dat.bui", "123")
                .timeout(Duration.ofMillis(2_000))
                .retryConfig(retryConfig)
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

    static void async(OktopusFlow flow) {
        final var error = flow.async();
        System.out.println("finish async...");

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
