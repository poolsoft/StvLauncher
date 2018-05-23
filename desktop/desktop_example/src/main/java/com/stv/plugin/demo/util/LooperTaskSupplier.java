
package com.stv.plugin.demo.util;

import android.os.Handler;

import com.stv.plugin.demo.DemoApplication;

public class LooperTaskSupplier {

    private HandlerThreadWrapper mJsonIOThread = new HandlerThreadWrapper("Json-IO");

    /**
     * Get handler of Json-IO thread
     */
    public Handler getLocalIOHandler() {
        return mJsonIOThread.getHandler();
    }

    public void quit() {
        mJsonIOThread.quit();
    }

    private static class HandlerThreadWrapper {
        private Handler handler = null;
        private android.os.HandlerThread handlerThread;

        HandlerThreadWrapper(String name) {
            handlerThread = new android.os.HandlerThread(makeTag(name));
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        }

        String makeTag(String tag) {
            return "PluginThread_" + DemoApplication.PLUGINTAG + "_[" + tag + "]";
        }

        Handler getHandler() {
            return handler;
        }

        void quit() {
            if (handlerThread != null) {
                handlerThread.quit();
                handlerThread.interrupt();
                handlerThread = null;
            }
            if (handler != null) {
                handler.getLooper().quit();
                handler = null;
            }
        }
    }
}
