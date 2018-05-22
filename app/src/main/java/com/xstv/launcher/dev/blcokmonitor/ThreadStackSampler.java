
package com.xstv.launcher.dev.blcokmonitor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

class ThreadStackSampler extends Sampler {

    private static final LinkedHashMap<Long, String> mThreadStackEntries = new LinkedHashMap<Long, String>();
    private static final int DEFAULT_MAX_ENTRY_COUNT = 10;

    private int mMaxEntryCount = DEFAULT_MAX_ENTRY_COUNT;

    private Thread mThread;

    public ThreadStackSampler(Thread thread, long sampleIntervalMillis) {
        this(thread, DEFAULT_MAX_ENTRY_COUNT, sampleIntervalMillis);
    }

    public ThreadStackSampler(Thread thread, int maxEntryCount, long sampleIntervalMillis) {
        super(sampleIntervalMillis);
        mThread = thread;
        mMaxEntryCount = maxEntryCount;
    }

    public ArrayList<String> getThreadStackEntries(long startTime, long endTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);
        ArrayList<String> result = new ArrayList<String>();
        synchronized (mThreadStackEntries) {
            for (Long entryTime : mThreadStackEntries.keySet()) {
                if (startTime < entryTime && entryTime < endTime) {
                    result.add(dateFormat.format(entryTime) + Block.SEPARATOR + Block.SEPARATOR + mThreadStackEntries.get(entryTime));
                }
            }
        }
        return result;
    }

    @Override
    protected void doSample() {
        // Log.d("BlockCanary", "sample thread stack: [" + mThreadStackEntries.size() + ", " + mMaxEntryCount + "]");
        StringBuilder stringBuilder = new StringBuilder();

        // Fetch thread stack info
        for (StackTraceElement stackTraceElement : mThread.getStackTrace()) {
            stringBuilder.append(stackTraceElement.toString())
                    .append(Block.SEPARATOR);
        }

        // Eliminate obsolete entry
        synchronized (mThreadStackEntries) {
            if (mThreadStackEntries.size() == mMaxEntryCount && mMaxEntryCount > 0) {
                mThreadStackEntries.remove(mThreadStackEntries.keySet().iterator().next());
            }
            mThreadStackEntries.put(System.currentTimeMillis(), stringBuilder.toString());
        }
        stringBuilder.delete(0, stringBuilder.length());
    }
}
