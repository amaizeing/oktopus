package io.github.amaizeing.oktopus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter(AccessLevel.MODULE)
public class OktopusConfig {

    private final RestClient restClient;
    private final OktopusWorker worker;
    private final CacheService cacheService;


    static OktopusConfig defaultConfig() {
        return OktopusConfig.builder()
                .restClient(new DefaultClient())
                .worker(new DefaultOktopusWorker(50))
                .cacheService(new DefaultCacheService())
                .build();
    }


}
