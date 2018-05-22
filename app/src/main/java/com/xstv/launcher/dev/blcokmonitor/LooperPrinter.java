
package com.xstv.launcher.dev.blcokmonitor;

import android.os.SystemClock;
import android.util.Printer;

class LooperPrinter implements Printer {

    private static final int DEFAULT_BLOCK_THRESHOLD_MILLIS = 2000;

    private long mBlockThresholdMillis = DEFAULT_BLOCK_THRESHOLD_MILLIS;
    private long mStartTimeMillis = 0;
    private long mStartThreadTimeMillis = 0;
    private BlockListener mBlockListener = null;

    private boolean mStartedPrinting = false;

    public LooperPrinter(BlockListener blockListener, long blockThresholdMillis) {
        if (blockListener == null) {
            throw new IllegalArgumentException("blockListener should not be null.");
        }
        mBlockListener = blockListener;
        mBlockThresholdMillis = blockThresholdMillis;
    }

    public void setBlockThresholdMillis(int blockThresholdMillis) {
        mBlockThresholdMillis = blockThresholdMillis;
    }

    @Override
    public void println(String x) {
        if (!mStartedPrinting) {
            mStartTimeMillis = System.currentTimeMillis();
            mStartThreadTimeMillis = SystemClock.currentThreadTimeMillis();
            mStartedPrinting = true;
            startDump();
        } else {
            final long endTime = System.currentTimeMillis();
            mStartedPrinting = false;
            if (isBlock(endTime)) {
                notifyBlockEvent(endTime);
            }
            stopDump();
        }
    }

    private boolean isBlock(long endTime) {
        return endTime - mStartTimeMillis > mBlockThresholdMillis;
    }

    private void notifyBlockEvent(final long endTime) {
        // Log.d("BlockCanary", "notifyBlockEvent: " + endTime + " - " + mStartTimeMillis + ">" + mBlockThresholdMillis);
        final long startTime = mStartTimeMillis;
        final long startThreadTime = mStartThreadTimeMillis;
        final long endThreadTime = SystemClock.currentThreadTimeMillis();
        HandlerThread.getWriteLogFileThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                mBlockListener.onBlockEvent(startTime, endTime, startThreadTime, endThreadTime);
            }
        });
    }

    private void startDump() {
        if (null != BlockCanaryCore.get().threadStackSampler) {
            BlockCanaryCore.get().threadStackSampler.start();
        }

        if (null != BlockCanaryCore.get().cpuSampler) {
            BlockCanaryCore.get().cpuSampler.start();
        }
    }

    private void stopDump() {
        if (null != BlockCanaryCore.get().threadStackSampler) {
            BlockCanaryCore.get().threadStackSampler.stop();
        }

        if (null != BlockCanaryCore.get().cpuSampler) {
            BlockCanaryCore.get().cpuSampler.stop();
        }
    }
}
