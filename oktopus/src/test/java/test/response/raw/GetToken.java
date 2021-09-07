package test.response.raw;

import com.amaizeing.oktopus.Oktopus;
import com.amaizeing.oktopus.annotation.OktopusCacheKey;
import com.amaizeing.oktopus.annotation.OktopusCacheTtl;
import com.amaizeing.oktopus.annotation.OktopusRequestBody;
import com.amaizeing.oktopus.annotation.OktopusRequestHeader;
import com.amaizeing.oktopus.annotation.OktopusRequestUrl;
import com.amaizeing.oktopus.annotation.OktopusResponseBody;
import com.amaizeing.oktopus.annotation.method.PostRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@PostRequest(responseType = GetToken.TokenResponse.class)
public class GetToken {

    private static final Logger LOGGER = LoggerFactory.getLogger(Oktopus.class);

    @OktopusRequestUrl
    public String url() {
        return "http://localhost:9090/login/";
    }

    @OktopusRequestHeader
    public Map<String, String> headers() {
        return Map.of();
    }

    @OktopusRequestBody
    public TokenRequest requestBody(String userName, String password) {
        return new TokenRequest(userName, password);
    }

    @OktopusCacheKey
    public String cacheKey(@OktopusRequestBody TokenRequest tokenRequest) {
        return tokenRequest.getUserName();
    }

    @OktopusCacheTtl(TimeUnit.SECONDS)
    public long cacheTtl(@OktopusResponseBody GetToken.TokenResponse tokenResponse) {
        return tokenResponse.ttlInSeconds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class TokenRequest {

        private String userName;
        private String password;

        @Override
        public String toString() {
            return "TokenRequest{" +
                    "userName='" + userName + '\'' +
                    ", password='" + password + '\'' +
                    '}';
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class TokenResponse {

        private String token;
        private long ttlInSeconds;

        @Override
        public String toString() {
            return "TokenResponse{" +
                    "token='" + token + '\'' +
                    ", ttlInSeconds=" + ttlInSeconds +
                    '}';
        }

    }

}
