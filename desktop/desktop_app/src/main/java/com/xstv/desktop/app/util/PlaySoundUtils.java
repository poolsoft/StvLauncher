
package com.xstv.desktop.app.util;

import android.content.Context;

public class PlaySoundUtils {
    private String TAG = PlaySoundUtils.class.getSimpleName();
    private static PlaySoundUtils mInstance;
    private boolean isSupprotSdk056 = false;

    private PlaySoundUtils() {
        isSupprotSdk056 = Utilities.verifySupportSdk(Utilities.support_sdk_version_100);
    }

    public static PlaySoundUtils getInstance() {
        if (mInstance == null) {
            mInstance = new PlaySoundUtils();
        }
        return mInstance;
    }

    public void play(Context context, int SoundType) {

    }
}
