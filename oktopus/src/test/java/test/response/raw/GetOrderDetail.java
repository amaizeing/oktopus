package test.response.raw;

import io.github.amaizeing.oktopus.annotation.OktopusCacheKey;
import io.github.amaizeing.oktopus.annotation.OktopusCacheTtl;
import io.github.amaizeing.oktopus.annotation.OktopusDependOn;
import io.github.amaizeing.oktopus.annotation.OktopusRequestBodies;
import io.github.amaizeing.oktopus.annotation.OktopusRequestBody;
import io.github.amaizeing.oktopus.annotation.OktopusRequestHeader;
import io.github.amaizeing.oktopus.annotation.OktopusRequestHeaders;
import io.github.amaizeing.oktopus.annotation.OktopusRequestKey;
import io.github.amaizeing.oktopus.annotation.OktopusRequestUrl;
import io.github.amaizeing.oktopus.annotation.OktopusRequestUrls;
import io.github.amaizeing.oktopus.annotation.method.Get;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Get(onSuccess = GetOrderDetail.Response.class)
public class GetOrderDetail {

    @OktopusDependOn(GetToken.class)
    private GetToken.Response tokenResponse;

    @OktopusDependOn(GetOrder.class)
    private GetOrder.Response orderResponse;

    @OktopusRequestUrls
    public Map<Long, String> urls(String baseUrl) {
        final var orderDetailIds = orderResponse.getOrderDetailIds();
        return orderDetailIds.stream()
                .collect(Collectors.toMap(Function.identity(), id -> baseUrl + id));
    }

    @OktopusRequestHeader
    public Map<String, String> initHeader(String requestId) {
        return Map.of("Authorization", tokenResponse.getToken(),
                      "X-Request-Id", requestId);
    }

//    @OktopusRequestHeaders
//    public Map<Long, Map<String, String>> initHeaders(String requestId) {
//        final var orderDetailIds = orderResponse.getOrderDetailIds();
//        return orderDetailIds.stream()
//                .collect(Collectors.toMap(Function.identity(),
//                                          id -> Map.of("Authorization", tokenResponse.getToken(),
//                                                       "X-Request-Id", requestId)));
//    }

    @OktopusRequestBodies
    public Map<Long, String> body() {
        final var orderDetailIds = orderResponse.getOrderDetailIds();
        return orderDetailIds.stream().collect(Collectors.toMap(Function.identity(), id -> "body of: " + id));
    }

    @OktopusCacheKey
    public String cacheKey(@OktopusRequestKey long key, @OktopusRequestUrls Object url, @OktopusRequestHeader Object header, @OktopusRequestBodies Object body) {
//        System.out.println("-------- trigger cache key here: " + key + " " + url);
////        System.out.println("urls " + urls);
//        System.out.println("header " + header);
//        System.out.println("body " + body);
        return "order-detail-" + key;
    }

    @OktopusCacheTtl
    public Duration ttl(@OktopusRequestKey long key, @OktopusRequestUrl Object url, @OktopusRequestUrls Object urls, @OktopusRequestHeader Object header, @OktopusRequestHeaders Object headers) {
        System.out.println("-----trigger ttl method----");
        System.out.println("----- url ---- " + url);
        System.out.println("----- urls---- " + urls);
        System.out.println("----- header---- " + header);
        System.out.println("----- headers---- " + headers);
        System.out.println("----- key---- " + key);
        return Duration.ofSeconds(10);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Response {

        private long id;
        private String name;
        private int quantity;
        private String description;

    }

}
