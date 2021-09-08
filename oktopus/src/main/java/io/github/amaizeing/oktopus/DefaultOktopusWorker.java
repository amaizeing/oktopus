package io.github.amaizeing.oktopus;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class DefaultOktopusWorker implements OktopusWorker {

    private final ExecutorService executorService;

    DefaultOktopusWorker(int workerCount) {
        executorService = Executors.newFixedThreadPool(workerCount, new OktopusThreadFactory());
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
