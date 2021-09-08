package test.response.raw;

import io.github.amaizeing.oktopus.annotation.OktopusDependOn;
import io.github.amaizeing.oktopus.annotation.OktopusRequestHeader;
import io.github.amaizeing.oktopus.annotation.OktopusRequestUrls;
import io.github.amaizeing.oktopus.annotation.method.Get;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Get(onSuccess = GetOrderShipment.Response.class)
public class GetOrderShipment {

    @OktopusDependOn(GetToken.class)
    private GetToken.ResponseBody tokenResponse;

    @OktopusDependOn(GetOrder.class)
    private GetOrder.Response orderResponse;

    @OktopusRequestUrls
    public Map<Long, String> urls(String baseUrl) {
        final var orderDetailIds = orderResponse.getOrderDetailIds();
        return orderDetailIds.stream()
                .collect(Collectors.toMap(Function.identity(),
                                          id -> baseUrl + orderResponse.getId()));
    }

    @OktopusRequestHeader
    public Map<String, String> initHeader(String requestId) {
        return Map.of("Authorization", "Bearer " + tokenResponse.getAccessToken(),
                      "X-Request-Id", requestId);
    }

    public static final class Response {

        private long id;
        private String driverName;
        private String licensePlate;
        private String phoneNumber;

    }

}
