package com.xstv.desktop.emodule.view;

import android.content.Context;
import android.support.v17.leanback.widget.Presenter;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import com.xstv.desktop.emodule.adapter.ItemBridgeAdapter;

import java.util.ArrayList;

public class FocusOnTopGridLayout extends GridLayout {
    private GridLayoutRecycleViewPool mRecyclerPool;
    protected int mItemMargin = 0;
    private boolean mFirstLayout = true;

    protected OnChildFocusListener mOnChildFocusListener;

    public FocusOnTopGridLayout(Context context) {
        this(context, null, 0);
    }

    public FocusOnTopGridLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusOnTopGridLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setChildrenDrawingOrderEnabled(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        //setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        View focus = getFocusedChild();
        if (null == focus) {
            return i;
        } else {
            int focusIdx = indexOfChild(focus);
            if (i < focusIdx) {
                return i;
            } else if (i < childCount - 1) {
                return focusIdx + childCount - 1 - i;
            } else {
                return focusIdx;
            }
        }
    }

    public interface OnChildFocusListener {
        /**
         * See {@link ViewGroup#requestChildFocus(
         *View, View)}.
         */
        public void onRequestChildFocus(View child, View focused);
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        if (mOnChildFocusListener != null) {
            mOnChildFocusListener.onRequestChildFocus(child, focused);
        }
        invalidate();
    }

    public void setOnChildFocusListener(OnChildFocusListener listener) {
        mOnChildFocusListener = listener;
    }

//    public void setRecyclerPool(GridLayoutRecycleViewPool pool) {
//        mRecyclerPool = pool;
//    }
//
//    public void addOneRecycledHolder(ItemBasePresenter.ItemViewHolder aRecycled, int type) {
//        if (mRecyclerPool != null) {
//            mRecyclerPool.addRecycledViewHolder(aRecycled, type);
//        }
//    }
//
//    public ItemBasePresenter.ItemViewHolder fetchOneRecycledHolder(int type) {
//        if (mRecyclerPool != null) {
//            return mRecyclerPool.fetchOneViewHolder(type);
//        } else {
//            return null;
//        }
//    }


    private View mLastFocusedView;

    @Override
    public View focusSearch(View focused, int direction) {
        mLastFocusedView = focused;
        View v = super.focusSearch(focused, direction);

        if (v == null) {
            if (direction == View.FOCUS_RIGHT) {
                FocusVerticalGridView.sFocusRightToNullView = true;
                FocusVerticalGridView.sendSimulatedKeyCode(KeyEvent.KEYCODE_DPAD_DOWN);
            } else if (direction == View.FOCUS_LEFT) {
                FocusVerticalGridView.sFocusLeftToNullView = true;
                FocusVerticalGridView.sendSimulatedKeyCode(KeyEvent.KEYCODE_DPAD_UP);
            }
        }
        return v;
    }


    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (hasFocus() || FocusVerticalGridView.sNextFocus2FirstChild) {
            if (hasFocus()) {
                if (FocusVerticalGridView.sFocusRightToNullView && direction == View.FOCUS_DOWN) {
                    int count = getChildCount();
                    for (int i = 0; i < count - 1; i++) {
                        if (getChildAt(i) == mLastFocusedView) {
                            views.add(getChildAt(i + 1));
                        }
                    }
                } else if (FocusVerticalGridView.sFocusLeftToNullView && direction == View.FOCUS_UP) {
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
                views.add(this);
            }
        } else {

            if ((direction == View.FOCUS_DOWN || direction == View.FOCUS_UP) && getChildCount() > 0) {
                FocusVerticalGridView.addFocusablesFromChildren(views, direction, this);
            } else {
                super.addFocusables(views, direction, focusableMode);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mFirstLayout && !hasFocus() && hasFocusable()) {
            ViewCompat.postOnAnimation(this, mAskFocusRunnable);
        }
        mFirstLayout = false;
    }

    private final Runnable mAskFocusRunnable = new Runnable() {
        @Override
        public void run() {
            if (hasFocus()) {
                return;
            }
            for (int i = 0, count = getChildCount(); i < count; i++) {
                View view = getChildAt(i);
                if (view != null && view.hasFocusable()) {
                    focusableViewAvailable(view);
                    break;
                }
            }
        }
    };

    RecyclerView.RecycledViewPool mRecycledViewPool;
    //private ArrayList<Presenter> mPresenters = new ArrayList<Presenter>();
    int mPresenterId = -1;
    //private ArrayList<Object> mItems;
    Presenter mPresenter;
    public ItemBridgeAdapter mItemBridgeAdapter = new ItemBridgeAdapter();

    public void setPresenterMapper(ArrayList<Presenter> presenters) {
        mItemBridgeAdapter.setPresenterMapper(presenters);
    }

    public void setAdapter(ItemBridgeAdapter adapter) {
        mItemBridgeAdapter = adapter;
        //mPresenters = mItemBridgeAdapter.getPresenterMapper();
    }

    public void setPresenter(Presenter presenter) {
        mPresenterId = mItemBridgeAdapter.getPresenterMapper().indexOf(presenter);
        mPresenter = presenter;
    }

    public void setRecyclerPool(RecyclerView.RecycledViewPool pool) {
        mRecycledViewPool = pool;
    }

    public void recycleAllViews() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.setMargins(0, 0, 0, 0);
            ItemBridgeAdapter.ViewHolder viewHolder = (ItemBridgeAdapter.ViewHolder) layoutParams.mViewHolder;
            mPresenter.onUnbindViewHolder(viewHolder.getViewHolder());
            //Log.d("killer","recycle ViewHolder "+viewHolder);
            if (mRecycledViewPool != null && viewHolder != null) {
                mRecycledViewPool.putRecycledView(viewHolder);
            }
        }
        removeAllViews();
    }


    public Presenter.ViewHolder getViewHolderForLayout() {
        //Log.d("killer","get ViewHolder ");
        if (mRecycledViewPool != null) {
            ItemBridgeAdapter.ViewHolder viewHolder = (ItemBridgeAdapter.ViewHolder) mRecycledViewPool.getRecycledView(mPresenterId);
            if (viewHolder != null) {
                LayoutParams lp = new LayoutParams();
                lp.mViewHolder = viewHolder;
                viewHolder.itemView.setLayoutParams(lp);
                return viewHolder.getViewHolder();
            } else {
                return createViewHolder();
            }
        } else {
            return createViewHolder();
        }
    }

    protected Presenter.ViewHolder createViewHolder() {
        ItemBridgeAdapter.ViewHolder holder = (ItemBridgeAdapter.ViewHolder) mItemBridgeAdapter.createViewHolder(this, mPresenterId);
        LayoutParams lp = (LayoutParams) holder.itemView.getLayoutParams();
        lp.mViewHolder = holder;
        return holder.getViewHolder();
    }


    public static class LayoutParams extends GridLayout.LayoutParams {
        private RecyclerView.ViewHolder mViewHolder;

        public LayoutParams() {
            super();
        }

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public LayoutParams(ViewGroup.LayoutParams lp) {
            super(lp);
        }

        public LayoutParams(Spec rowSpec, Spec columnSpec) {
            super(rowSpec, columnSpec);
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

}
