package co.smartreceipts.android;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SameThreadExecutorService extends AbstractExecutorService {

    private volatile boolean isShutdown = false;

    @Override
    public void shutdown() {
        isShutdown = true;
    }

    @NonNull
    @Override
    public List<Runnable> shutdownNow() {
        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
        return isShutdown;
    }

    @Override
    public boolean isTerminated() {
        return isShutdown;
    }

    @Override
    public boolean awaitTermination(long timeout, @NonNull TimeUnit unit) throws InterruptedException {
        shutdown();
        return isTerminated();
    }

    @Override
    public void execute(@NonNull Runnable command) {
        command.run();
    }
}
