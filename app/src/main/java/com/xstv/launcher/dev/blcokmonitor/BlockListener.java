
package com.xstv.launcher.dev.blcokmonitor;

interface BlockListener {
    void onBlockEvent(long realStartTime, long realTimeEnd, long threadTimeStart,
                      long threadTimeEnd);
}
