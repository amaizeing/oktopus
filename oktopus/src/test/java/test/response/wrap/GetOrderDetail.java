package test.response.wrap;

import io.github.amaizeing.oktopus.annotation.OktopusCacheKeys;
import io.github.amaizeing.oktopus.annotation.OktopusCacheTtl;
import io.github.amaizeing.oktopus.annotation.OktopusDependOn;
import io.github.amaizeing.oktopus.annotation.OktopusRequestBodies;
import io.github.amaizeing.oktopus.annotation.OktopusRequestBody;
import io.github.amaizeing.oktopus.annotation.OktopusRequestHeader;
import io.github.amaizeing.oktopus.annotation.OktopusRequestHeaders;
import io.github.amaizeing.oktopus.annotation.OktopusRequestUrls;
import io.github.amaizeing.oktopus.annotation.method.Get;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Get(onSuccess = GetOrderDetail.WrapOrderDetailResponse.class)
public class GetOrderDetail {

    @OktopusDependOn(GetToken.class)
    private GetToken.WrapTokenResponse tokenResponse;

    @OktopusDependOn(GetOrder.class)
    private GetOrder.WrapOrderResponse orderResponse;

    @OktopusRequestUrls
    public Map<Long, String> urls() {
        final var orderDetailIds = orderResponse.getData().getOrderDetailIds();
        final var orderId = orderResponse.getData().getOrderId();
        return orderDetailIds.stream()
                .collect(Collectors.toMap(Function.identity(), id -> String.format("http://localhost:9090/wrap/orders/%s/details/%s", orderId, id)));
    }

    @OktopusRequestHeader
    public Map<String, Object> initHeader() {
        return Map.of("x-token", tokenResponse.getData().getToken(),
                      "x-order-detail-id", 1);
    }

    @OktopusCacheKeys
    public Map<Long, String> cacheKeys() {
        return urls();
    }

    @OktopusCacheTtl(TimeUnit.MINUTES)
    public int ttl() {
        return 1;
    }

    public static final class WrapOrderDetailResponse extends ResponseMessage<OrderDetailResponse> {

    }

    @Data
    public static final class OrderDetailResponse {

        private long orderId;
        private long orderDetailId;
        private String description;

    }

}
