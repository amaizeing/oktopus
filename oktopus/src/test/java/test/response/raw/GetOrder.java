package test.response.raw;

import io.github.amaizeing.oktopus.annotation.OktopusDependOn;
import io.github.amaizeing.oktopus.annotation.OktopusRequestHeader;
import io.github.amaizeing.oktopus.annotation.OktopusRequestUrl;
import io.github.amaizeing.oktopus.annotation.method.Get;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Get(onSuccess = GetOrder.Response.class)
public class GetOrder {

    @OktopusDependOn(GetToken.class)
    private GetToken.ResponseBody token;

    @OktopusRequestUrl
    public String url(String url) {
        return url;
    }

    @OktopusRequestHeader
    public Map<String, String> initHeader(String requestId) {
        return Map.of("Authorization", "Bearer " + token.getAccessToken(),
                      "X-Request-Id", requestId);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Response {

        private long id;
        private String status;
        private List<Long> orderDetailIds;
        private List<Long> shipmentIds;

    }

}
