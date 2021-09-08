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

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Post(onSuccess = GetToken.ResponseBody.class)
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
    public RequestBody requestBody(String userName, String password) {
        return new RequestBody(userName, password);
    }

    @OktopusCacheKey
    public String cacheKey(@OktopusRequestBody RequestBody tokenRequest) {
        return tokenRequest.getUserName();
    }

    @OktopusCacheTtl(TimeUnit.SECONDS)
    public long cacheTtl(@OktopusResponseBody ResponseBody tokenResponse) {
        return tokenResponse.ttlInSeconds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class RequestBody {

        private String userName;
        private String password;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class ResponseBody {

        private String accessToken;
        private long ttlInSeconds;

    }

}
