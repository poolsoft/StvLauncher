package com.xstv.desktop.emodule.view;

import android.app.Instrumentation;
import android.content.Context;
import android.support.v17.leanback.widget.VerticalGridView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.xstv.library.base.Logger;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FocusVerticalGridView extends VerticalGridView {
    private static ExecutorService mExecutor = Executors.newFixedThreadPool(2);
    Logger mLogger = Logger.getLogger("EModule", "FocusVerticalGridView");

    public FocusVerticalGridView(Context context) {
        this(context, null);
    }

    public FocusVerticalGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusVerticalGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private View mLastFocusedView;

    @Override
    public View focusSearch(View focused, int direction) {
        mLastFocusedView = focused;
        View v = super.focusSearch(focused, direction);
        mLogger.v("focusSearch nextFocusView=" + v);
        if (v == null) {
            if (direction == View.FOCUS_RIGHT) {
                sFocusRightToNullView = true;
                sendSimulatedKeyCode(KeyEvent.KEYCODE_DPAD_DOWN);
            } else if (direction == View.FOCUS_LEFT) {
                sFocusLeftToNullView = true;
                sendSimulatedKeyCode(KeyEvent.KEYCODE_DPAD_UP);
            }
        }
        return v;
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (hasFocus() || sNextFocus2FirstChild) {
            if (hasFocus()) {
                if (sFocusRightToNullView && direction == View.FOCUS_DOWN) {
                    int count = getChildCount();
                    for (int i = 0; i < count - 1; i++) {
                        if (getChildAt(i) == mLastFocusedView) {
                            views.add(getChildAt(i + 1));
                        }
                    }
                } else if (sFocusLeftToNullView && direction == View.FOCUS_UP) {
                    int count = getChildCount();
                    for (int i = 1; i < count; i++) {
                        if (getChildAt(i) == mLastFocusedView) {
                            views.add(getChildAt(i - 1));
                        }
                    }
                } else {
                    super.addFocusables(views, direction, focusableMode);
                }
            } else {
                super.addFocusables(views, direction, focusableMode);
            }
        } else {
            if ((direction == View.FOCUS_DOWN || direction == View.FOCUS_UP) && getChildCount() > 0) {
                addFocusablesFromChildren(views, direction, this);
            } else {
                super.addFocusables(views, direction, focusableMode);
            }
        }
    }

    public static boolean sFocusRightToNullView = false;
    public static boolean sFocusLeftToNullView = false;
    public static boolean sNextFocus2FirstChild = false;

    public static void resetFocusFlag() {
        if (FocusVerticalGridView.sFocusRightToNullView) {
            FocusVerticalGridView.sFocusRightToNullView = false;
        }

        if (FocusVerticalGridView.sFocusLeftToNullView) {
            FocusVerticalGridView.sFocusLeftToNullView = false;
        }

        if (FocusVerticalGridView.sNextFocus2FirstChild) {
            FocusVerticalGridView.sNextFocus2FirstChild = false;
        }
    }

    public static void sendSimulatedKeyCode(final int keyCode) {
        mExecutor.submit(
                new Runnable() {
                    public void run() {
                        try {
                            Instrumentation inst = new Instrumentation();
                            inst.sendKeyDownUpSync(keyCode);
                        } catch (Exception e) {
                        }
                    }
                });
    }

    public static void addFocusablesFromChildren(ArrayList<View> views, int direction, ViewGroup parent) {
        if (direction == View.FOCUS_DOWN) {
            for (int i = 0; i < parent.getChildCount() && i < 20; i++) {
                View child = parent.getChildAt(i);
                if (child.isFocusable()) {
                    views.add(child);

                    if (sFocusRightToNullView) {
                        break;
                    }
                }
            }
        } else if (direction == View.FOCUS_UP) {
            int count = 0;
            for (int i = parent.getChildCount() - 1; i >= 0 && count < 20; i--) {
                View child = parent.getChildAt(i);
                if (child.isFocusable()) {
                    views.add(child);
                    count++;

                    if (sFocusLeftToNullView) {
                        break;
                    }
                }
            }
        }
    }

    public ViewHolder getChildViewHolder(View child) {
        final ViewParent parent = child.getParent();
        if (parent != null && parent != this) {
        }
        return super.getChildViewHolder(child);
    }
}
