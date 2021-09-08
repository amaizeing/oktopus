package test.response.wrap;

import io.github.amaizeing.oktopus.annotation.OktopusDependOn;
import io.github.amaizeing.oktopus.annotation.OktopusRequestHeader;
import io.github.amaizeing.oktopus.annotation.OktopusRequestUrl;
import io.github.amaizeing.oktopus.annotation.method.Get;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Get(onSuccess = GetOrder.WrapOrderResponse.class)
public class GetOrder {

    @OktopusDependOn(GetToken.class)
    private GetToken.WrapTokenResponse tokenResponse;

    @OktopusRequestUrl
    public String url(int orderId) {
        return "http://localhost:9090/wrap/orders/" + orderId;
    }

    @OktopusRequestHeader
    public Map<String, String> initHeader() {
        return Map.of("x-token", tokenResponse.getData().getToken());
    }

    public static final class WrapOrderResponse extends ResponseMessage<OrderResponse> {

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class OrderResponse {

        private long orderId;
        private List<Long> orderDetailIds;

    }

}
