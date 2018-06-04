package com.xstv.desktop.emodule.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xstv.desktop.emodule.R;
import com.xstv.desktop.emodule.util.Utils;

public class DiBaseView extends RelativeLayout {
    private FocusAss mAss = new FocusAss(this);
    private boolean mRectTop = false;

    public DiBaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DiBaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setFocusAss(int resId, boolean top) {
        mAss.setFocusHLT(resId);
        setTag(R.id.focus_rect, true);
        mRectTop = top;
    }

    public void update(int resId, boolean top) {
        setTag(R.id.focus_res, resId);
        setTag(R.id.focus_rect, true);
        mAss.setFocusHLT(resId);
        mRectTop = top;
    }

    public void update(int resId) {
        setTag(R.id.focus_res, resId);
        setTag(R.id.focus_rect, true);
        mAss.setFocusHLT(resId);
    }

    public void update() {
        setTag(R.id.focus_rect, true);
        mAss.update();
    }

    @Override
    protected void dispatchDraw(Canvas aCanvas) {
        if (mRectTop) {
            super.dispatchDraw(aCanvas);
            boolean noRect = getTag(R.id.focus_rect) == Boolean.TRUE;
            if (noRect && !Utils.longPressScrolling()) {
                mAss.drawFocusEffect(aCanvas);
            }
        } else {
            boolean noRect = getTag(R.id.focus_rect) == Boolean.TRUE;
            if (noRect && !Utils.longPressScrolling()) {
                mAss.drawFocusEffect(aCanvas);
            }
            super.dispatchDraw(aCanvas);
        }
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {

        return super.requestFocus(direction, previouslyFocusedRect);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        onFocusChange(this, gainFocus);
    }

    public static final void onFocusChange(View aView, boolean aGainFocus) {
        if (aView.findViewById(R.id.di_focus_effect) == null) {
           /* FocusEffectManager mgr = FocusEffectManager.getMgr(aView.getContext());
            if (aGainFocus) {
                if (mgr != null) {
                    mgr.viewGotFocus(aView);
                }
            } else {
                if (mgr != null) {
                    mgr.viewLostFocus(aView);
                }
            }*/
        }

        FocusVerticalGridView.resetFocusFlag();
        final TextView tv = (TextView) aView.findViewById(R.id.di_title);
        ItemTextView text = (ItemTextView) aView.findViewById(R.id.di_text);
        if (tv != null && text != null) {
            CharSequence obj = (CharSequence) tv.getTag(R.id.text_view_title);
            if (!TextUtils.isEmpty(obj)) {
                if (text.isTextOutOfBound()) {
                    if (aGainFocus) {
                        tv.setVisibility(View.VISIBLE);
                        text.setVisibility(View.GONE);
                        if (obj != null) {
                            tv.setText(obj);
                        } else {
                            tv.setText("");
                        }
                    } else {
                        tv.setVisibility(View.GONE);
                        text.setVisibility(View.VISIBLE);

                    }
                    //Log.d("textviewfocus", "tv.isSelected() = " + tv.isSelected() + " , tv.getText=" + tv.getText());
                    if (tv.getVisibility() == VISIBLE) {
                        if (aGainFocus) {
                            if (tv.isSelected()) {
                                tv.setSelected(false);
                            }
                            tv.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    tv.setSelected(true);
                                }
                            }, 100);
                        } else {
                            tv.setSelected(false);
                        }
                    }
                }
            } else {
                tv.setVisibility(View.GONE);
                text.setVisibility(View.GONE);
            }
        } else {
            if (text != null) {
                text.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void dispatchSetSelected(boolean selected) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            if (getChildAt(i).getVisibility() == VISIBLE) {
                getChildAt(i).setSelected(selected);
            }
        }
    }

    @Override
    public String toString() {
        return "DiBaseView{" +
                "title=" + ((TextView) this.findViewById(R.id.di_title)).getText() +
                '}';
    }
}
