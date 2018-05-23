package com.stv.plugin.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stv.plugin.demo.DemoApplication;
import com.xstv.base.Logger;
import com.xstv.desktop.R;


public class HeaderVideoContainer extends RelativeLayout {

    private Logger mLogger = Logger.getLogger(DemoApplication.PLUGINTAG, "HeaderVideoContainer");

    private SurfaceView mSurfaceView;
    private ProgressBar mProgressBar;
    private TextView mTipTextView;

    private boolean mSurfaceCreated;
    private boolean mHasRequestReleased;

    public HeaderVideoContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mTipTextView = (TextView) findViewById(R.id.text);
        init();
    }

    private void init() {
        mSurfaceView.getHolder().setKeepScreenOn(true);
        mSurfaceView.getHolder().addCallback(new SurfaceCallback());
    }

    public void requestVideoPlay() {
        mLogger.d("openVideo");
        openVideo();

        postDelayed(new Runnable() {
            @Override
            public void run() {
                openVideo();
            }
        }, 100);
    }

    public void requestVideoRelease() {
        mLogger.d("releaseVideo");
        releasePlayer();
    }

    private void openVideo() {
        if (!mSurfaceCreated || isPlaying()) {
            return;
        }
        mHasRequestReleased = false;
        mLogger.d("================= instantiate =============");

    }

    private void stopPlayer() {
    }

    private void releasePlayer() {
        mHasRequestReleased = true;
    }

    public boolean isPlaying() {
        return false;
    }

    private void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
        mTipTextView.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        mProgressBar.setVisibility(View.GONE);
        mTipTextView.setVisibility(View.GONE);
        mTipTextView.setText("0%");
    }

    class SurfaceCallback implements SurfaceHolder.Callback {
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        public void surfaceCreated(SurfaceHolder holder) {
            mLogger.d("surfaceCreated");
            mSurfaceCreated = true;
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            mLogger.d("surfaceDestroyed");
            mSurfaceCreated = false;
            releasePlayer();
        }
    }
}
