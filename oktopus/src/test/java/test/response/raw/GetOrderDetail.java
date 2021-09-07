package test.response.raw;

import com.amaizeing.oktopus.annotation.OktopusDependOn;
import com.amaizeing.oktopus.annotation.OktopusRequestHeader;
import com.amaizeing.oktopus.annotation.OktopusRequestUrls;
import com.amaizeing.oktopus.annotation.method.GetRequest;
import lombok.Data;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@GetRequest(responseType = GetOrderDetail.OrderDetailResponse.class)
public class GetOrderDetail {

    @OktopusDependOn(GetToken.class)
    private GetToken.TokenResponse tokenResponse;

    @OktopusDependOn(GetOrder.class)
    private GetOrder.OrderResponse orderResponse;

    @OktopusRequestUrls
    public Map<Long, String> urls() {
        final var orderDetailIds = orderResponse.getOrderDetailIds();
        return orderDetailIds.stream()
                .collect(Collectors.toMap(Function.identity(), id -> String.format("http://localhost:9090/orders/%s/details/%s", orderResponse.getOrderId(), id)));
    }

    @OktopusRequestHeader
    public Map<String, String> initHeader() {
        return Map.of("x-token", tokenResponse.getToken());
    }

    @Data
    public static final class OrderDetailResponse {

        private long orderId;
        private long orderDetailId;
        private String description;

    }

}
