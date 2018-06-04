package com.xstv.desktop.emodule.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v17.leanback.widget.ItemBridgeAdapter;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnChildViewHolderSelectedListener;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.VerticalGridView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xstv.library.base.Logger;

public class BaseRowFragment extends AbstractBaseFragment {

    Logger mLogger = Logger.getLogger("Module","BaseRowFragment");
    protected ObjectAdapter mAdapter;
    protected VerticalGridView mVerticalGridView;
    private PresenterSelector mPresenterSelector;
    private ItemBridgeAdapter mBridgeAdapter;
    private int mSelectedPosition = -1;
    private boolean mPendingTransitionPrepare;

    public int getLayoutResourceId() {
        return 0;
    }

    private final OnChildViewHolderSelectedListener mRowSelectedListener =
            new OnChildViewHolderSelectedListener() {
                @Override
                public void onChildViewHolderSelected(RecyclerView parent,
                                                      RecyclerView.ViewHolder view, int position, int subposition) {
                    onRowSelected(parent, view, position, subposition);
                }
            };

    protected void onRowSelected(RecyclerView parent, RecyclerView.ViewHolder view,
                                 int position, int subposition) {
    }

    public interface OnViewCreateListener {
        void onViewCreated(View view);

        void onViewDestroyed();
    }

    OnViewCreateListener mOnViewCreateListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResourceId(), container, false);
        mVerticalGridView = findGridViewFromRoot(view);
        if (mPendingTransitionPrepare) {
            mPendingTransitionPrepare = false;
            onTransitionPrepare();
        }
        return view;
    }

    public VerticalGridView findGridViewFromRoot(View view) {
        return (VerticalGridView) view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (mBridgeAdapter != null) {
            mVerticalGridView.setAdapter(mBridgeAdapter);
            if (mSelectedPosition != -1) {
                mVerticalGridView.setSelectedPosition(mSelectedPosition);
            }
        }
        mVerticalGridView.setOnChildViewHolderSelectedListener(mRowSelectedListener);
        if (mOnViewCreateListener != null) {
            mOnViewCreateListener.onViewCreated(view);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mVerticalGridView.setAdapter(null);
        mVerticalGridView = null;
        if (mOnViewCreateListener != null) {
            mOnViewCreateListener.onViewDestroyed();
        }

        if (mBridgeAdapter != null) {
            // detach observer from ObjectAdapter
            mBridgeAdapter.clear();
            mBridgeAdapter = null;
        }
    }

    /**
     * Set the presenter selector used to create and bind views.
     */
    public final void setPresenterSelector(PresenterSelector presenterSelector) {
        mPresenterSelector = presenterSelector;
        updateAdapter();
    }

    /**
     * Get the presenter selector used to create and bind views.
     */
    public final PresenterSelector getPresenterSelector() {
        return mPresenterSelector;
    }

    /**
     * Sets the adapter for the fragment.
     */
    public void setAdapter(ObjectAdapter rowsAdapter) {
        mAdapter = rowsAdapter;
        updateAdapter();
    }

    /**
     * Returns the list of rows.
     */
    public final ObjectAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * Returns the bridge adapter.
     */
    final public ItemBridgeAdapter getBridgeAdapter() {
        return mBridgeAdapter;
    }

    /**
     * Sets the selected row position with smooth animation.
     */
    public void setSelectedPosition(int position) {
        setSelectedPosition(position, true);
    }

    /**
     * Sets the selected row position.
     */
    public void setSelectedPosition(int position, boolean smooth) {
        mSelectedPosition = position;
        if (mVerticalGridView != null && mVerticalGridView.getAdapter() != null) {
            if (smooth) {
                mVerticalGridView.setSelectedPositionSmooth(position);
            } else {
                mVerticalGridView.setSelectedPosition(position);
            }
        }
    }

    public final VerticalGridView getVerticalGridView() {
        return mVerticalGridView;
    }

    protected void updateAdapter() {
        if (mAdapter == null) {
            if (mBridgeAdapter != null) {
                // detach observer from ObjectAdapter
                mBridgeAdapter.clear();
                mBridgeAdapter = null;
                if (mVerticalGridView != null) {
                    mVerticalGridView.setAdapter(mBridgeAdapter);
                }
            }
            return;
        }

        if (mBridgeAdapter != null) {
            // detach observer from ObjectAdapter
            mBridgeAdapter.clear();
            mBridgeAdapter = null;
        }

        if (mAdapter != null) {
            // If presenter selector is null, adapter ps will be used
            mBridgeAdapter = new ItemBridgeAdapter(mAdapter, mPresenterSelector);
        }

        if (mVerticalGridView != null) {
            mVerticalGridView.setAdapter(mBridgeAdapter);
            if (mBridgeAdapter != null && mSelectedPosition != -1) {
                mVerticalGridView.setSelectedPosition(mSelectedPosition);
            }
        }

        mLogger.v("updateAdapter mBridgeAdapter=" + mBridgeAdapter + " mAdapter=" + mAdapter);
    }

    Object getItem(Row row, int position) {
        if (row instanceof ListRow) {
            return ((ListRow) row).getAdapter().get(position);
        } else {
            return null;
        }
    }

    protected boolean onTransitionPrepare() {
        if (mVerticalGridView != null) {
            mVerticalGridView.setAnimateChildLayout(false);
            mVerticalGridView.setScrollEnabled(false);
            return true;
        }
        mPendingTransitionPrepare = true;
        return false;
    }

    protected void onTransitionStart() {
        if (mVerticalGridView != null) {
            mVerticalGridView.setPruneChild(false);
            mVerticalGridView.setLayoutFrozen(true);
            mVerticalGridView.setFocusSearchDisabled(true);
        }
    }

    protected void onTransitionEnd() {
        // be careful that fragment might be destroyed before header transition ends.
        if (mVerticalGridView != null) {
            mVerticalGridView.setLayoutFrozen(false);
            //mVerticalGridView.setAnimateChildLayout(true);
            mVerticalGridView.setPruneChild(true);
            mVerticalGridView.setFocusSearchDisabled(false);
            mVerticalGridView.setScrollEnabled(true);
        }
    }

    protected void setItemAlignment() {
        if (mVerticalGridView != null) {
            // align the top edge of staggered_item
            mVerticalGridView.setItemAlignmentOffset(0);
            mVerticalGridView.setItemAlignmentOffsetPercent(
                    VerticalGridView.ITEM_ALIGN_OFFSET_PERCENT_DISABLED);
        }
    }

    protected void setWindowAlignmentFromTop(int alignedTop) {
        if (mVerticalGridView != null) {
            // align to a fixed position from top
            mVerticalGridView.setWindowAlignmentOffset(alignedTop);
            mVerticalGridView.setWindowAlignmentOffsetPercent(
                    VerticalGridView.WINDOW_ALIGN_OFFSET_PERCENT_DISABLED);
            mVerticalGridView.setWindowAlignment(VerticalGridView.WINDOW_ALIGN_NO_EDGE);
        }
    }

    public void setOnViewCreateListener(OnViewCreateListener listener) {
        mOnViewCreateListener = listener;
    }

    protected boolean isActive() {
        Activity a = getActivity();
        if (a == null || a.isFinishing() || a.isDestroyed()) {
            return false;
        }
        return isAdded() && !isDetached() && !isRemoving();
    }
}
