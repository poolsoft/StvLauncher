package com.xstv.desktop.emodule.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.PaintDrawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.xstv.desktop.emodule.R;
import com.xstv.desktop.emodule.mode.DisplayItem;


public class HintTextView extends View {
    public HintTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HintTextView(Context context) {
        this(context, null, 0);
    }

    public HintTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (leftPadding == -1) {
            leftPadding = getResources().getDimensionPixelSize(R.dimen.item_hint_padding_horizontal);
            rightPadding = getResources().getDimensionPixelSize(R.dimen.item_hint_padding_horizontal);
            bottomPadding = getResources().getDimensionPixelOffset(R.dimen.item_hint_padding_bottom);
            textSize = getResources().getDimensionPixelSize(R.dimen.item_hint_text_size);
            textColor = getResources().getColor(R.color.text_blue);
            textbgColor = getResources().getColor(R.color.hint_text_bg_color);
            height = getResources().getDimensionPixelSize(R.dimen.item_hint_height);

            if (paint == null) {
                paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
                paint.density = getResources().getDisplayMetrics().density;
                paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.item_title_text_size));
                paint.setColor(getResources().getColor(R.color.hint_text_color));
                paint.bgColor = getResources().getColor(R.color.hint_text_bg_color);
            }

            if (rectDrawable == null) {
                rectDrawable = new PaintDrawable(getResources().getColor(R.color.hint_text_bg_color));
                rectDrawable.setCornerRadius(5);
            }
            text_height = (int) (paint.getFontMetrics().descent - paint.getFontMetrics().top);
            height_start = height - bottomPadding - text_height * 1 / 5;
        }
    }

    private static int leftPadding = -1;
    private static int rightPadding = -1;
    private static int bottomPadding = -1;
    private static int textSize = -1;
    private static int textColor = -1;
    private static int textbgColor = -1;
    private static int height = -1;
    private static int text_height = -1;
    private static int height_start = -1;
    private static TextPaint paint;
    private static PaintDrawable rectDrawable;

    private DisplayItem item;

    //private int         layout_width;
    public void bindDate(DisplayItem content) {
        item = content;
        //layout_width = width;
        paint.setTextSize(textSize);
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        if (item != null) {
            if (item.hint != null) {
                /*if (!TextUtils.isEmpty(staggered_item.hint.left())) {
                    float text_width = paint.measureText(staggered_item.hint.right());
                    //canvas.drawRect(leftPadding, 0,text_width,text_height,paintbg);
                    canvas.drawText(staggered_item.hint.left(), leftPadding, height_start, paint);
                }

                if (!TextUtils.isEmpty(staggered_item.hint.mid())) {
                    float text_width = paint.measureText(staggered_item.hint.mid());
                    int startPos = (int) ((getWidth() - text_width) / 2);
                    if (text_width > getWidth()) {
                        startPos = 0;
                    }
                    //canvas.drawRect(startPos, 0,text_width,text_height,paintbg);
                    canvas.drawText(staggered_item.hint.mid(), startPos, height_start, paint);
                }

                if (!TextUtils.isEmpty(staggered_item.hint.right())) {
                    float text_width = paint.measureText(staggered_item.hint.right());
                    int startPos = (int) (getWidth() - text_width - rightPadding - getPaddingRight());
                    if (text_width > getWidth() || startPos < 0) {
                        startPos = 0;
                    }

                    rectDrawable.setBounds(startPos - leftPadding, 0 + getPaddingTop(), (int) (startPos + text_width + rightPadding), height - getPaddingBottom());
                    rectDrawable.draw(canvas);
                    canvas.drawText(staggered_item.hint.right(), startPos, height_start, paint);
                }*/
            }
        }
    }
}
