
package com.xstv.desktop.app.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.xstv.desktop.app.R;


public class CircleProgress {
    private static final String TAG = CircleProgress.class.getSimpleName();

    private int mBigRadius;
    private int mSmallRadius;
    /**
     * 扇形的起始角度
     */
    private int mStartAngle = -90;

    private int mCorner = 9;
    private Paint paint;
    private Drawable bgDrawable;
    private int mProgressBigCircleColor;
    private int mProgressBg;
    private Bitmap loadingBitmap = null;

    public CircleProgress(Context context) {
        init(context);
    }

    private void init(Context context) {
        Resources res = context.getResources();
        mCorner = res.getDimensionPixelSize(R.dimen.app_corners);
        mBigRadius = res.getDimensionPixelSize(R.dimen.circle_progress_poster_big_radius);
        mSmallRadius = res.getDimensionPixelSize(R.dimen.circle_progress_poster_small_radius);
        bgDrawable = res.getDrawable(R.drawable.progress_bar_view_bg);
        mProgressBigCircleColor = res.getColor(R.color.progress_big_circle_bg);
        mProgressBg = res.getColor(R.color.progress_bg);
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    public Bitmap createProgress(float sweepAngle, int w, int h, float textSize, Point loadingTextPoint,
                                 RectF logoRectF, String title, String loadingTitle) {
        float cx = w * 1.0f / 2;
        float cy = h * 0.4f;
        int x = 12;
        int y = h - x;
        loadingBitmap = createBgBitmap(w, h, logoRectF);
        Canvas canvas = new Canvas(loadingBitmap);
        paint.setXfermode(null);
        paint.setColor(mProgressBigCircleColor);
        float left = cx - mSmallRadius;
        float top = cy - mSmallRadius;
        float right = cx + mSmallRadius;
        float bottom = cy + mSmallRadius;
        RectF oval = new RectF(left, top, right, bottom);
        canvas.drawArc(oval, mStartAngle, sweepAngle, true, paint);
        if (loadingTextPoint != null) {
            x = loadingTextPoint.x;
            y = loadingTextPoint.y;
        }
        paint.setTextSize(textSize);
        paint.setColor(Color.WHITE);
        if (TextUtils.isEmpty(title)) {
            title = "";
        }
        if (TextUtils.isEmpty(loadingTitle)) {
            loadingTitle = "";
        }
        String progressBar = getProgressTitle(w,paint,title,loadingTitle);
        canvas.drawText(progressBar, x, y, paint);
        return loadingBitmap;
    }

    private Bitmap createBgBitmap(int width, int height, RectF logoRectF) {
        Bitmap bgBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bgBitmap);
        bgDrawable.setBounds(0, 0, width, height);
        bgDrawable.draw(canvas);
        paint.setColor(mProgressBigCircleColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6);
        float cx = width * 1.0f / 2;
        float cy = height * 0.4f;
        canvas.drawCircle(cx, cy, mBigRadius - 3.2f, paint);
        paint.setStyle(Paint.Style.FILL);
        if (logoRectF != null) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawRoundRect(logoRectF, mCorner, mCorner, paint);
        }
        return bgBitmap;
    }

    private String getProgressTitle(int w, Paint paint, String title, String loadingTitle) {
        String str = title + loadingTitle;
        float titleWidth = paint.measureText(title);
        float loadingTitleWidth = paint.measureText(loadingTitle);
        if ((titleWidth + loadingTitleWidth) <= w) {
            return str;
        }
        float remainWidth = w - loadingTitleWidth;
        int len = title.length();
        if (len > 0) {
            for (int i = 1; i <= len; i++) {
                String subTitle = title.substring(0, i);
                subTitle = subTitle + "...";
                float subWidth = paint.measureText(subTitle);
                if (subWidth < remainWidth) {
                    str = subTitle + loadingTitle;
                }else{
                    break;
                }
            }
        }
        return str;
    }
}
