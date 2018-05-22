
package com.xstv.desktop.app.bean;

/**
 * Created by zhangguanhua on 18-1-16.
 * 应用下载状态信息的实体类
 */

public class DownloadStatusBean {

    private String downloadStatus;
    private long currentBytes;
    private long totalBytes;
    private float sweepAngle;
    private String loadingTitle;
    private long downloadId = -1;

    public String getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(String downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public long getCurrentBytes() {
        return currentBytes;
    }

    public void setCurrentBytes(long currentBytes) {
        this.currentBytes = currentBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public String getLoadingTitle() {
        return loadingTitle;
    }

    public void setLoadingTitle(String loadingTitle) {
        this.loadingTitle = loadingTitle;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }

    public float getSweepAngle() {
        return sweepAngle;
    }

    public void setSweepAngle(float sweepAngle) {
        this.sweepAngle = sweepAngle;
    }

    @Override
    public String toString() {
        return "[" + " downloadStatus = " + downloadStatus + " currentBytes = " + currentBytes +
                " totalBytes = " + totalBytes + " sweepAngle = " + sweepAngle + " loadingTitle = "
                + loadingTitle + " downloadId = " + downloadId + " ]";
    }
}
