package com.xstv.desktop.emodule.view;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;


public class ItemTextView extends TextViewQuick {
    int mTextWidth = 0;

    public ItemTextView(Context context) {
        this(context, null);
    }

    public ItemTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ItemTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public void setText(String text) {
        mText = text;
        if (!TextUtils.isEmpty(text)) {
            mTextPaint.getTextBounds(mText, 0, mText.length(), rectS);
            mTextWidth = rectS.width();
            mTextHeight = rectS.height();
            mTextBaseLine = -rectS.top;
            if (mTextWidth < getWidth()) {
                mLongText = false;
            } else {
                mLongText = true;
            }
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        if (!TextUtils.isEmpty(mText)) {
            canvas.save();
            clipRectS.set(0, 0, getWidth(), getHeight());
            canvas.clipRect(clipRectS);
            if (mTextWidth < getWidth()) {
                mLongText = false;
            } else {
                mLongText = true;
            }
            if (mGravity == Gravity.CENTER) {
                if (!mLongText) {
                    canvas.drawText(mText, (getWidth() - getPaddingLeft() - getPaddingRight() - mTextWidth) / 2, mTextBaseLine + getPaddingTop(), mTextPaint);
                } else {
                    canvas.drawText(mText, 5, mTextBaseLine + getPaddingTop(), mTextPaint);
                }
            } else {
                canvas.drawText(mText, 5, mTextBaseLine + getPaddingTop(), mTextPaint);
            }
            canvas.restore();
        }
    }

    public boolean isTextOutOfBound() {
        return mLongText;
    }
}
