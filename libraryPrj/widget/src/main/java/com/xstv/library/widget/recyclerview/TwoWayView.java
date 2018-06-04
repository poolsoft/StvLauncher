/*
 * Copyright (C) 2014 Lucas Rocha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xstv.library.widget.recyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.xstv.library.widget.R;

import java.lang.reflect.Constructor;

public class TwoWayView extends RecyclerView {
    private static final Class<?>[] sConstructorSignature = new Class[]{
            Context.class, AttributeSet.class};

    final Object[] sConstructorArgs = new Object[2];

    public TwoWayView(Context context) {
        this(context, null);
    }

    public TwoWayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwoWayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setChildrenDrawingOrderEnabled(true);
        /*final TypedArray a =
                context.obtainStyledAttributes(attrs, R.styleable.twowayview_TwoWayView, defStyle, 0);
        final String name = a.getString(R.styleable.twowayview_TwoWayView_twowayview_layoutManager);
        if (!TextUtils.isEmpty(name)) {
            loadLayoutManagerFromName(context, attrs, name);
        }
        a.recycle();*/

        //set default manager.
        setLayoutManager(new DefaultLayoutManager(context));
    }

    public static class DefaultLayoutManager extends StaggeredGridLayoutManager {

        public DefaultLayoutManager(Context context) {
            super(context);
        }

        public DefaultLayoutManager(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public DefaultLayoutManager(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public DefaultLayoutManager(Orientation orientation, int numColumns, int numRows) {
            super(orientation, numColumns, numRows);
        }
    }

    private void loadLayoutManagerFromName(Context context, AttributeSet attrs, String name) {
        try {
            name = "com.xstv.library.widget.recyclerview." + name;
            Class<? extends TwoWayLayoutManager> clazz =
                    context.getClassLoader().loadClass(name).asSubclass(TwoWayLayoutManager.class);
            Constructor<? extends TwoWayLayoutManager> constructor = clazz.getConstructor(sConstructorSignature);
            sConstructorArgs[0] = context;
            sConstructorArgs[1] = attrs;
            setLayoutManager(constructor.newInstance(sConstructorArgs));
        } catch (Exception e) {
            throw new IllegalStateException("Could not load TwoWayLayoutManager from " +
                    "class: " + name, e);
        }
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        if (!(layout instanceof TwoWayLayoutManager)) {
            throw new IllegalArgumentException("TwoWayView can only use TwoWayLayoutManager " +
                    "subclasses as its layout manager");
        }

        super.setLayoutManager(layout);
    }

    public TwoWayLayoutManager.Orientation getOrientation() {
        TwoWayLayoutManager layout = (TwoWayLayoutManager) getLayoutManager();
        return layout.getOrientation();
    }

    public void setOrientation(TwoWayLayoutManager.Orientation orientation) {
        TwoWayLayoutManager layout = (TwoWayLayoutManager) getLayoutManager();
        layout.setOrientation(orientation);
    }

    public int getFirstVisiblePosition() {
        TwoWayLayoutManager layout = (TwoWayLayoutManager) getLayoutManager();
        return layout.getFirstVisiblePosition();
    }

    public int getLastVisiblePosition() {
        TwoWayLayoutManager layout = (TwoWayLayoutManager) getLayoutManager();
        return layout.getLastVisiblePosition();
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        Log.d("yyu", "getChildDrawingOrder " + i);
        View view = getFocusedChild();
        if (view == null) {
            return i;
        }
        int focusIndex = indexOfChild(view);
        if (i < focusIndex) {
            return i;
        } else if (i < childCount - 1) {
            return focusIndex + childCount - 1 - i;
        } else {
            return focusIndex;
        }

    }
}
