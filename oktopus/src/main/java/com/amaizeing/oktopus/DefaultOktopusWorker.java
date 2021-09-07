package com.amaizeing.oktopus;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class DefaultOktopusWorker implements OktopusWorker {

    private final ExecutorService executorService;

    DefaultOktopusWorker() {
        executorService = Executors.newFixedThreadPool(25, new OktopusThreadFactory());
    }


    @Override
    public void execute(final Runnable runnable) {
        executorService.execute(runnable);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        return executorService.submit(callable);
    }

}
