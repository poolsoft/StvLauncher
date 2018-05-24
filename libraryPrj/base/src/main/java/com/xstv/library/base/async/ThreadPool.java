package com.xstv.library.base.async;


import android.os.Process;

import com.xstv.library.base.DesktopHelper;
import com.xstv.library.base.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Common thread pool for launcher framework and plugins.
 */
public class ThreadPool {

    static final String TAG_SDK_FRAMEWORK = "SDK_ThreadPool";

    private static final int POOL_SIZE_CORE = 5;
    private static final int POOL_SIZE_MAX = 10;

    private static final int KEEP_ALIVE_TIME = 60;

    private PriorityBlockingQueue mBlockingQueue;
    private ExecutorService mExecutorService;

    private List<Job> mJobList;

    private static ThreadPool sInstance;

    private ThreadPool() {
        mJobList = new LinkedList<Job>();
        mBlockingQueue = new PriorityBlockingQueue();
        mExecutorService = new ThreadPoolExecutor(POOL_SIZE_CORE,
                POOL_SIZE_MAX, KEEP_ALIVE_TIME, TimeUnit.SECONDS, mBlockingQueue,
                new FramePriorityThreadFactory(Process.THREAD_PRIORITY_BACKGROUND,
                        TAG_SDK_FRAMEWORK));
        DesktopHelper.getInstance().addDesktopSwitchObserver(mDesktopObserver);
    }

    /**
     * Execute a job
     *
     * @param job code to run with sub thread.
     */
    public void submit(Job job) {
        if (job == null) {
            throw new RuntimeException("Job cannot be null");
        }
        if (job.getTag() == null) {
            throw new RuntimeException("Job cannot be null");
        }
        String tag = job.getTag();
        String currentDesktop = "";//DesktopHelper.getInstance().getCurrentDesktop();
        //Logger.getLogger(TAG_SDK_FRAMEWORK, "submit").d(tag + ", " + currentDesktop);

        if (job.getType() == JobType.DATA_REPORT) {
            mExecutorService.execute(job);
            Logger.getLogger(TAG_SDK_FRAMEWORK, "submit").d("data report job add to execute");
        } else if (job.getType() == JobType.JOB_FRAME_WORK) {         //framework job, execute it.
            mExecutorService.execute(job);
            Logger.getLogger(TAG_SDK_FRAMEWORK, "submit").d("framework job add to execute");
        } else if (job.getType() == JobType.JOB_DATA_INIT) {    //data init job, execute it.
            mExecutorService.execute(job);
            Logger.getLogger(TAG_SDK_FRAMEWORK, "submit").d("data init job add to execute");
        } else if (job.getTag().equals(currentDesktop)) {       //current desktop job, execute it.
            mExecutorService.execute(job);
            Logger.getLogger(TAG_SDK_FRAMEWORK, "submit").d("current desktop job add to execute");
        } else {                                               //waiting for desktop switch to.
            mJobList.add(job);
            Logger.getLogger(TAG_SDK_FRAMEWORK, "submit").d("job add to waiting list");
        }

    }

    /**
     * Get instance of the ThreadPool
     *
     * @return
     */
    public static ThreadPool getInstance() {
        if (sInstance == null) {
            synchronized (ThreadPool.class) {
                if (sInstance == null) {
                    sInstance = new ThreadPool();
                }
            }
        }
        return sInstance;
    }

    private DesktopHelper.DesktopSwitchObserver mDesktopObserver = new DesktopHelper.DesktopSwitchObserver() {
        @Override
        public void onSwitchStart(String currentTag, String toTag) {
            //Do noting
        }

        @Override
        public void onSwitchEnd(String tag) {
            if (mJobList.size() > 0) {
                for (int i = mJobList.size() - 1; i >= 0; i--) {
                    Job j = mJobList.get(i);
                    if (j.getTag().equals(tag)) {
                        j = mJobList.remove(i);
                        Logger.getLogger(TAG_SDK_FRAMEWORK, "onSwitchEnd").d(j.getTag());
                        mExecutorService.execute(j);
                    }
                }
            }
        }
    };

    class FramePriorityThreadFactory implements ThreadFactory {
        private final int threadPriority;
        private int count = -1;
        private String name;

        public FramePriorityThreadFactory(int threadPriority, String name) {
            this.threadPriority = threadPriority;
            this.name = name;
        }

        public Thread newThread(final Runnable runnable) {
            Runnable wrapperRunnable = new Runnable() {
                public void run() {
                    try {
                        Process.setThreadPriority(threadPriority);
                    } catch (Throwable throwable) {
                    }
                    runnable.run();
                }
            };
            count++;
            Thread t = new Thread(wrapperRunnable);
            t.setName(name + TAG_SDK_FRAMEWORK + "-" + count);
            return t;
        }
    }

}
