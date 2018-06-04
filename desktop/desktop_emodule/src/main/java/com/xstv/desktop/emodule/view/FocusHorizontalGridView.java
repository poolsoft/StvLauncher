package com.xstv.desktop.emodule.view;

import android.content.Context;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import com.xstv.library.base.Logger;

import java.util.ArrayList;

public class FocusHorizontalGridView extends HorizontalGridView {

    Logger mLogger = Logger.getLogger("EModule", "FocusHorizontalGridView");

    public FocusHorizontalGridView(Context context) {
        this(context, null);
    }

    public FocusHorizontalGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusHorizontalGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public View focusSearch(View focused, int direction) {
        View v = super.focusSearch(focused, direction);
        mLogger.v("focusSearch nextFocusView=" + v);
        boolean isNotFound = v == null /*|| v == focused*/;

        if (isNotFound && direction == View.FOCUS_RIGHT) {
            FocusVerticalGridView.sFocusRightToNullView = true;
            FocusVerticalGridView.sendSimulatedKeyCode(KeyEvent.KEYCODE_DPAD_DOWN);
            return null;
        } /*else if (isNotFound && direction == View.FOCUS_LEFT) {
            FocusVerticalGridView.sFocusLeftToNullView = true;
            FocusVerticalGridView.sendSimulatedKeyCode(KeyEvent.KEYCODE_DPAD_UP);
            return null;
        }*/
        return v;
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (hasFocus() || FocusVerticalGridView.sFocusRightToNullView || FocusVerticalGridView.sNextFocus2FirstChild) {
            super.addFocusables(views, direction, focusableMode);
        } else {
            super.addFocusables(views, direction, focusableMode);
//            if ((direction == View.FOCUS_DOWN || direction == View.FOCUS_UP) && getChildCount() > 0) {
//                FocusVerticalGridView.addFocusablesFromChildren(views, direction, this);
//            } else {
//                super.addFocusables(views, direction, focusableMode);
//            }
        }
    }
}
