package io.github.amaizeing.oktopus;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class OktopusConfig {

    private final RestClient restClient;
    private final OktopusWorker worker;


    static OktopusConfig defaultConfig() {
        return OktopusConfig.builder()
                .restClient(new DefaultClient())
                .worker(new DefaultOktopusWorker(50))
                .build();
    }

    RestClient getRestClient() {
        return restClient;
    }

    OktopusWorker getWorker() {
        return worker;
    }


}
