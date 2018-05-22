
package com.xstv.launcher.dev.blcokmonitor;

import java.util.concurrent.ThreadFactory;

/**
 * This is intended to only be used with a single thread executor.
 */
final class BlockCanarySingleThreadFactory implements ThreadFactory {

    private final String threadName;

    BlockCanarySingleThreadFactory(String threadName) {
        this.threadName = "BlockCanary-" + threadName;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, threadName);
    }
}
