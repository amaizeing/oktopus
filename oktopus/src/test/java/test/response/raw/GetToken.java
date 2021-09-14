package test.response.raw;

import io.github.amaizeing.oktopus.annotation.OktopusCacheKey;
import io.github.amaizeing.oktopus.annotation.OktopusCacheTtl;
import io.github.amaizeing.oktopus.annotation.OktopusRequestBody;
import io.github.amaizeing.oktopus.annotation.OktopusRequestHeader;
import io.github.amaizeing.oktopus.annotation.OktopusRequestUrl;
import io.github.amaizeing.oktopus.annotation.OktopusResponseBody;
import io.github.amaizeing.oktopus.annotation.method.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.Map;

@Post(onSuccess = GetToken.Response.class)
public class GetToken {

    @OktopusRequestUrl
    public String url(String url) {
        return url;
    }

    @OktopusRequestHeader
    public Map<String, String> headers(String requestId) {
        return Map.of("X-Request-Id", requestId);
    }

    @OktopusRequestBody
    public Request requestBody(String userName, String password) {
        return new Request(userName, password);
    }

    @OktopusCacheKey
    public String cacheKey(@OktopusRequestBody Request tokenRequest) {
        return tokenRequest.getUserName();
    }

    @OktopusCacheTtl
    public Duration cacheTtl(@OktopusResponseBody Response tokenResponse) {
        return Duration.ofSeconds(tokenResponse.ttlInSeconds);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Request {

        private String userName;
        private String password;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Response {

        private String token;
        private long ttlInSeconds;

    }

}
