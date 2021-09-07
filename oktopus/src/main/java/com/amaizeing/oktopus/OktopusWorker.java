package com.amaizeing.oktopus;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface OktopusWorker {

    void execute(Runnable runnable);

    <T> Future<T> submit(Callable<T> callable);

}
