package com.xd.oktopus;

import com.xd.oktopus.util.ClassUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class OktopusConfig {

    private RestClient restClient;
    private OktopusWorker worker;

    private OktopusConfig() {
    }

    static OktopusConfig defaultConfig() {
        final var config = new OktopusConfig();
        config.restClient = new DefaultClient();
        config.worker = new DefaultOktopusWorker();
        return config;
    }

    public OktopusConfig restClient(Class<? extends RestClient> restClient) {
        this.restClient = ClassUtil.getInstance(restClient);
        return this;
    }

    public OktopusConfig worker(Class<? extends OktopusWorker> worker) {
        this.worker = ClassUtil.getInstance(worker);
        return this;
    }

    RestClient getRestClient() {
        return restClient;
    }

    OktopusWorker getWorker() {
        return worker;
    }


}
