package test.response.wrap;

import com.xd.oktopus.annotation.OktopusDependOn;
import com.xd.oktopus.annotation.OktopusRequestHeader;
import com.xd.oktopus.annotation.OktopusRequestUrl;
import com.xd.oktopus.annotation.method.GetRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@GetRequest(responseType = GetOrder.WrapOrderResponse.class)
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
