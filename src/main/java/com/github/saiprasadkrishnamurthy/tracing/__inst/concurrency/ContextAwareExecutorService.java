package com.github.saiprasadkrishnamurthy.tracing.__inst.concurrency;

import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ContextAwareExecutorService<D extends ExecutorService> implements ExecutorService {

    private final D delegate;

    public ContextAwareExecutorService(D delegate) {
        this.delegate = delegate;
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return delegate.submit(wrap(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return delegate.submit(wrap(task), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return delegate.submit(wrap(task));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return delegate.invokeAll(wrapCollection(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.invokeAll(wrapCollection(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return delegate.invokeAny(wrapCollection(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.invokeAny(wrapCollection(tasks), timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        delegate.execute(wrap(command));
    }

    public D getDelegate() {
        return delegate;
    }

    /* Copied from https://github.com/project-ncl/pnc/blob/master/common/src/main/java/org/jboss/pnc/common
    /concurrent/MDCWrappers.java */

    private static Runnable wrap(final Runnable runnable) {
        final Map<String, String> context = MDC.getCopyOfContextMap();
        return () -> {
            Map previous = MDC.getCopyOfContextMap();
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            try {
                runnable.run();
            } finally {
                if (previous == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previous);
                }
            }
        };
    }

    private static <T> Callable<T> wrap(final Callable<T> callable) {
        final Map<String, String> context = MDC.getCopyOfContextMap();
        return () -> {
            Map previous = MDC.getCopyOfContextMap();
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            try {
                return callable.call();
            } finally {
                if (previous == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previous);
                }
            }
        };
    }

    private static <T> Consumer<T> wrap(final Consumer<T> consumer) {
        final Map<String, String> context = MDC.getCopyOfContextMap();
        return (t) -> {
            Map previous = MDC.getCopyOfContextMap();
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            try {
                consumer.accept(t);
            } finally {
                if (previous == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previous);
                }
            }
        };
    }

    private static <T> Collection<Callable<T>> wrapCollection(Collection<? extends Callable<T>> tasks) {
        Collection<Callable<T>> wrapped = new ArrayList<>();
        for (Callable<T> task : tasks) {
            wrapped.add(wrap(task));
        }
        return wrapped;
    }
}