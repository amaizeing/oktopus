package test.response.raw;

import com.xd.oktopus.annotation.OktopusDependOn;
import com.xd.oktopus.annotation.OktopusRequestHeader;
import com.xd.oktopus.annotation.OktopusRequestUrl;
import com.xd.oktopus.annotation.method.GetRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@GetRequest(responseType = GetOrder.OrderResponse.class)
public class GetOrder {

    @OktopusDependOn(GetToken.class)
    private GetToken.TokenResponse tokenResponse;

    @OktopusRequestUrl
    public String url(int orderId) {
        return "http://localhost:9090/orders/" + orderId;
    }

    @OktopusRequestHeader
    public Map<String, String> initHeader() {
        return Map.of("x-token", tokenResponse.getToken());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class OrderResponse {

        private long orderId;
        private List<Long> orderDetailIds;

    }

}
