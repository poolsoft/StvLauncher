package com.xstv.desktop.emodule.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import com.xstv.desktop.emodule.R;

public class TextViewQuick extends View {
    protected TextPaint mTextPaint = null;
    protected String mText;
    protected int mGravity = Gravity.CENTER;
    protected boolean mLongText = false;
    protected int mTextHeight = 0;
    protected int mTextBaseLine = 0;


    private static final int SANS = 1;
    private static final int SERIF = 2;
    private static final int MONOSPACE = 3;
    private ColorStateList mTextColor;
    private int mCurTextColor;

    public TextViewQuick(Context context) {
        this(context, null);
    }

    public TextViewQuick(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextViewQuick(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.density = getResources().getDisplayMetrics().density;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextViewQuick, defStyle,
                0);
        float textSize = a.getDimensionPixelSize(R.styleable.TextViewQuick_android_textSize, 30);
        mTextPaint.setTextSize(textSize);

        int styleIndex = a.getInt(R.styleable.TextViewQuick_android_textStyle, 0);
        String fontFamily = a.getString(R.styleable.TextViewQuick_android_fontFamily);
        int typefaceIndex = a.getInt(R.styleable.TextViewQuick_android_typeface, 0);
        setTypefaceFromAttrs(fontFamily, typefaceIndex, styleIndex);

        ColorStateList textColor = a.getColorStateList(R.styleable.TextViewQuick_android_textColor);
        setTextColor(textColor != null ? textColor : ColorStateList.valueOf(0xFF000000));
        String text = a.getString(R.styleable.TextViewQuick_android_text);
        setText(text);
        a.recycle();
    }

    public void setTextColor(ColorStateList colors) {
        if (colors == null) {
            throw new NullPointerException();
        }

        mTextColor = colors;
        updateTextColors();
    }

    private void updateTextColors() {
        boolean inval = false;
        int color = mTextColor.getColorForState(getDrawableState(), 0);
        if (color != mCurTextColor) {
            mCurTextColor = color;
            mTextPaint.setColor(mCurTextColor);
            inval = true;
        }
        if (inval) {
            invalidate();
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mTextColor != null && mTextColor.isStateful()) {
            updateTextColors();
        }

    }

    public void setTypeface(Typeface tf) {
        if (mTextPaint.getTypeface() != tf) {
            mTextPaint.setTypeface(tf);
        }
    }

    public void setTypeface(Typeface tf, int style) {
        if (style > 0) {
            if (tf == null) {
                tf = Typeface.defaultFromStyle(style);
            } else {
                tf = Typeface.create(tf, style);
            }

            setTypeface(tf);
            // now compute what (if any) algorithmic styling is needed
            int typefaceStyle = tf != null ? tf.getStyle() : 0;
            int need = style & ~typefaceStyle;
            mTextPaint.setFakeBoldText((need & Typeface.BOLD) != 0);
            mTextPaint.setTextSkewX((need & Typeface.ITALIC) != 0 ? -0.25f : 0);
        } else {
            mTextPaint.setFakeBoldText(false);
            mTextPaint.setTextSkewX(0);
            setTypeface(tf);
        }
    }

    private void setTypefaceFromAttrs(String familyName, int typefaceIndex, int styleIndex) {
        Typeface tf = null;
        if (familyName != null) {
            tf = Typeface.create(familyName, styleIndex);
            if (tf != null) {
                setTypeface(tf);
                return;
            }
        }
        switch (typefaceIndex) {
            case SANS:
                tf = Typeface.SANS_SERIF;
                break;

            case SERIF:
                tf = Typeface.SERIF;
                break;

            case MONOSPACE:
                tf = Typeface.MONOSPACE;
                break;
        }

        setTypeface(tf, styleIndex);
    }

    private void initTextPaint(){
        //mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        //mTextPaint.density = getResources().getDisplayMetrics().density;
        //mTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.row_header_title_text_size));
        //mTextPaint.setTypeface(Typeface.SERIF);
        //mTextPaint.setColor(getResources().getColor(R.color.white));
    }

    public void setText(String text){
        mText = text;
        if(!TextUtils.isEmpty(mText)) {
            mTextPaint.getTextBounds(mText, 0, mText.length(), rectS);
            //Paint.FontMetrics fontMetrics = sTextPaint.getFontMetrics();
            mTextBaseLine = -rectS.top;//(int)(fontMetrics.leading+fontMetrics.ascent);
        }
        mTextHeight = rectS.height() + getPaddingTop()+getPaddingBottom();
        requestLayout();
        invalidate();
    }

    public void setText(CharSequence aSeq)
    {
        if(aSeq!=null) {
            setText(aSeq.toString());
        }else{
            mText = null;
        }
    }

    public String getText()
    {
       return mText;
    }

    protected Rect rectS = new Rect();
    protected Rect clipRectS = new Rect();
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(!TextUtils.isEmpty(mText)) {
            canvas.save();
            clipRectS.set(0,0,getWidth(),getHeight());
            canvas.clipRect(clipRectS);

            canvas.drawText(mText, getPaddingStart(), mTextBaseLine + getPaddingTop(), mTextPaint);
            canvas.restore();
        }
    }

    public void setGravity(int gravity) {
        mGravity = gravity;
    }

    @Override
    public void setFocusable(boolean focusable) {
        super.setFocusable(focusable);
        invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (heightMode == View.MeasureSpec.EXACTLY) {
            // Parent has told us how big to be. So be it.
            height = heightSize;
        } else {
            int desired = getDesiredHeight();

            height = desired;

            if (heightMode == View.MeasureSpec.AT_MOST) {
                height = Math.min(desired, heightSize);
            }
        }
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                height);
    }

    private int getDesiredHeight() {
        int desired = mTextHeight;

        // Check against our minimum height
        desired = Math.max(desired, getSuggestedMinimumHeight());
        return desired;
    }
}
