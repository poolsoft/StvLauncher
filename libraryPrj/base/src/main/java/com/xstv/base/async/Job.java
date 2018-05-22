package com.xstv.base.async;

/**
 * A abstract class implement from Runnable, that can be run in thread pool.
 */
public abstract class Job implements Runnable, Comparable {

    private static int counter = 0;
    private final int id = ++counter;

    private String tag;
    private JobType type;


    public Job(String tag) {
        this(tag, JobType.JOB_DEFAULT);
    }

    public Job(String tag, JobType type) {
        this.tag = tag;
        this.type = type;
    }

    public String getTag() {
        return tag;
    }

    public int getId() {
        return id;
    }

    public JobType getType() {
        return type;
    }

    @Override
    public final int compareTo(Object another) {
        if (!(another instanceof Job)) {
            throw new RuntimeException("Error type, cannot compare");
        }
        Job right = (Job) another;
        if (this.tag.equals(right.tag)) {
            return this.id - right.id;
        } else {
            if (right.tag.equals(ThreadPool.TAG_SDK_FRAMEWORK)) {
                return 1;
            } else if (this.tag.equals(ThreadPool.TAG_SDK_FRAMEWORK)) {
                return -1;
            }
        }
        return this.id - right.id;
    }

    @Override
    public String toString() {
        return tag + "-" + id + " : " + super.toString();
    }
}
