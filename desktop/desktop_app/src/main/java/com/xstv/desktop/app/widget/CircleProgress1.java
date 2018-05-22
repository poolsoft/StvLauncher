
package com.xstv.desktop.app.widget;

import android.content.Context;
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

public class CircleProgress1 {
    private static final String TAG = CircleProgress1.class.getSimpleName();

    /**
     * 圆环的宽度
     */
    private int mCircleStrokeWidth;
    /**
     * 圆环的半径
     */
    private int mRadius;
    /**
     * 扇形的起始角度
     */
    private int mStartAngle = -90;
    /**
     * @see AppCellView icon位置不是居中 所以加个marginTop
     */
    private int mMarginTop;
    /**
     * @see AppCellView#getHeight()
     */
    private int mPadding;

    /**
     * 进度最大值
     */
    private long max;
    /**
     * 当前进度
     */
    private long progress;

    private Paint paint;
    private Drawable bgDrawable;

    public CircleProgress1(Context context) {
        init(context);
    }

    private void init(Context context) {
        mRadius = context.getResources().getDimensionPixelSize(R.dimen.circle_progress_view_radius);
        mCircleStrokeWidth = context.getResources().getDimensionPixelSize(R.dimen.circle_progress_view_strokeWidth);
        bgDrawable = context.getResources().getDrawable(R.drawable.progress_bar_view_bg);
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    public void setStartAngle(int startAngle) {
        this.mStartAngle = startAngle;
    }

    public void setMarginTop(int marginTop) {
        this.mMarginTop = marginTop;
    }

    public Bitmap createProgress(float sweepAngle, int w, int h, float textSize, Point loadingTextPoint, String loadingText) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mCircleStrokeWidth);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        int width = w;
        int height = h - mPadding;
        int x = 12;
        int y = h - x;
        Bitmap loadingBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas loadingCanvas = new Canvas(loadingBitmap);
        loadingCanvas.drawBitmap(createBgBitmap(width, height), 0, 0, null);
        loadingCanvas.drawCircle(width / 2, height / 2 - mMarginTop, mRadius, paint);
        int left = width / 2 - mRadius;
        int top = height / 2 - mRadius - mMarginTop;
        int right = width / 2 + mRadius;
        int bottom = height / 2 + mRadius - mMarginTop;
        paint.setStyle(Paint.Style.FILL);
        RectF oval = new RectF(left, top, right, bottom);
        loadingCanvas.drawArc(oval, mStartAngle, sweepAngle, true, paint);
        if (loadingTextPoint != null) {
            x = loadingTextPoint.x;
            y = loadingTextPoint.y;
        }
        paint.setXfermode(null);
        if (textSize > 0 && !TextUtils.isEmpty(loadingText)) {
            paint.setTextSize(textSize);
            paint.setColor(Color.WHITE);
            x = (int) ((x - paint.measureText(loadingText)) / 2);
            loadingCanvas.drawText(loadingText, x, y, paint);
        }
        return loadingBitmap;
    }

    private Bitmap createBgBitmap(int width, int height) {
        Bitmap bgBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bgBitmap);
        bgDrawable.setBounds(0, 0, width, height);
        bgDrawable.draw(canvas);
        return bgBitmap;
    }
}
