
package com.xstv.launcher.ui.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.xstv.launcher.R;

import java.lang.ref.WeakReference;
import java.util.Calendar;

public class TimeView extends TextView {
    static String TAG = "TimeView";

    static int mSecond = 0;
    private SpannableStringBuilder mTimeText;
    private int timeSize;
    private int aMpMSize;
    private TimeHandler mHandler;
    private AbsoluteSizeSpan mTimeSpan;
    private AbsoluteSizeSpan mAmPmSpan;
    public final static String m12 = "hh:mm";
    public final static String m24 = "kk:mm";
    private Context mContext;

    public TimeView(Context context) {
        this(context, null);
    }

    public TimeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TimeView);
            timeSize = ta.getDimensionPixelSize(R.styleable.TimeView_timeSize, 24);
            aMpMSize = ta.getDimensionPixelSize(R.styleable.TimeView_aMpMSize, 12);
            ta.recycle();
        }
        mTimeText = new SpannableStringBuilder();
        mContext = context;
        mHandler = new TimeHandler(this);
    }

    private BroadcastReceiver mTimeBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHandler.sendEmptyMessage(0);
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHandler.sendEmptyMessage(0);
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getContext().registerReceiver(mTimeBroadcast, intentFilter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(null);
        getContext().unregisterReceiver(mTimeBroadcast);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            mHandler.sendEmptyMessage(0);
        }
    }

    void setTime(Calendar calendar) {
        mTimeText.delete(0, mTimeText.length());

        if (DateFormat.is24HourFormat(mContext)) {
            mTimeText.append(DateFormat.format(m24, calendar));
            if(mTimeSpan == null)
                mTimeSpan = new AbsoluteSizeSpan(timeSize);

            mTimeText.setSpan(mTimeSpan, 0, mTimeText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            mTimeText.append(DateFormat.format(m12, calendar));
            CharSequence nn = DateFormat.format(m24, calendar);
            if ("12:00".compareTo(nn.toString()) > 0) {
                mTimeText.append("AM");
            } else {
                mTimeText.append("PM");
            }
            if (mTimeSpan == null || mAmPmSpan == null) {
                mTimeSpan = new AbsoluteSizeSpan(timeSize);
                mAmPmSpan = new AbsoluteSizeSpan(aMpMSize);
            }

            mTimeText.setSpan(mTimeSpan, 0, mTimeText.length() - 2, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            mTimeText.setSpan(mAmPmSpan, mTimeText.length() - 2, mTimeText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        setText(mTimeText);

        mTimeText.removeSpan(mTimeSpan);
        mTimeText.removeSpan(mAmPmSpan);
    }

    static class TimeHandler extends Handler {
        WeakReference<TimeView> weakReference;

        TimeHandler(TimeView timeTextView) {
            weakReference = new WeakReference<TimeView>(timeTextView);
        }

        @Override
        public void handleMessage(Message msg) {
            TimeView timeTextView = weakReference.get();
            if (timeTextView != null) {
                Calendar calendar = Calendar.getInstance();
                timeTextView.setTime(calendar);
                mSecond = calendar.get(Calendar.SECOND);
                removeMessages(0);
                sendEmptyMessageDelayed(0, (60 - mSecond) * 1000);
            }
        }
    }
}
