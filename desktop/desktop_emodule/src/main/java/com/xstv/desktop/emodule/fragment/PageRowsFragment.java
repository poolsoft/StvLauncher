package com.xstv.desktop.emodule.fragment;

import android.animation.TimeAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.widget.ItemBridgeAdapter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.VerticalGridView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.xstv.desktop.emodule.R;
import com.xstv.desktop.emodule.adapter.BlockAdapter;
import com.xstv.desktop.emodule.mode.Block;
import com.xstv.desktop.emodule.mode.DisplayItem;
import com.xstv.desktop.emodule.presenter.BaseRowPresenter;
import com.xstv.desktop.emodule.presenter.RegularBlockPresenter;
import com.xstv.desktop.emodule.presenter.ItemBasePresenter;
import com.xstv.desktop.emodule.view.GridLayoutRecycleViewPool;
import com.xstv.library.base.Logger;

public class PageRowsFragment extends BaseRowFragment {

    public interface GridViewAvailableObserver {
        void onViewAvailable(VerticalGridView gridView);
    }

    /**
     * Internal helper class that manages row select animation and apply a default
     * dim to each row.
     */
    final class RowViewHolderExtra implements TimeAnimator.TimeListener {
        final BaseRowPresenter mRowPresenter;
        final Presenter.ViewHolder mRowViewHolder;

        final TimeAnimator mSelectAnimator = new TimeAnimator();

        int mSelectAnimatorDurationInUse;
        Interpolator mSelectAnimatorInterpolatorInUse;
        float mSelectLevelAnimStart;
        float mSelectLevelAnimDelta;

        RowViewHolderExtra(ItemBridgeAdapter.ViewHolder ibvh) {
            mRowPresenter = (BaseRowPresenter) ibvh.getPresenter();
            mRowViewHolder = ibvh.getViewHolder();
            mSelectAnimator.setTimeListener(this);
        }

        @Override
        public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
            if (mSelectAnimator.isRunning()) {
                updateSelect(totalTime, deltaTime);
            }
        }

        void updateSelect(long totalTime, long deltaTime) {
            float fraction;
            if (totalTime >= mSelectAnimatorDurationInUse) {
                fraction = 1;
                mSelectAnimator.end();
            } else {
                fraction = (float) (totalTime / (double) mSelectAnimatorDurationInUse);
            }
            if (mSelectAnimatorInterpolatorInUse != null) {
                fraction = mSelectAnimatorInterpolatorInUse.getInterpolation(fraction);
            }
            float level = mSelectLevelAnimStart + fraction * mSelectLevelAnimDelta;
            mRowPresenter.setSelectLevel(mRowViewHolder, level);
        }

        void animateSelect(boolean select, boolean immediate) {
            mSelectAnimator.end();
            final float end = select ? 1 : 0;
            if (immediate) {
                mRowPresenter.setSelectLevel(mRowViewHolder, end);
            } else if (mRowPresenter.getSelectLevel(mRowViewHolder) != end) {
                mSelectAnimatorDurationInUse = mSelectAnimatorDuration;
                mSelectAnimatorInterpolatorInUse = mSelectAnimatorInterpolator;
                mSelectLevelAnimStart = mRowPresenter.getSelectLevel(mRowViewHolder);
                mSelectLevelAnimDelta = end - mSelectLevelAnimStart;
                mSelectAnimator.start();
            }
        }
    }

    Logger mLogger = Logger.getLogger("EModule", "PageRowsFragment");
    public static final boolean DEBUG = false;

    private ItemBridgeAdapter.ViewHolder mSelectedViewHolder;
    private int mSubPosition;
    private boolean mExpand = false;
    private boolean mViewsCreated;
    private float mRowScaleFactor;
    private int mAlignedTop;
    private boolean mRowScaleEnabled;
    private boolean mAfterEntranceTransition = true;
    private int mPaddingTop = -1;

    private BaseRowPresenter.OnItemViewSelectedListener mOnItemViewSelectedListener;
    private BaseRowPresenter.OnItemViewClickedListener mOnItemViewClickedListener;

    private GridViewAvailableObserver mGridViewAvailableObserver;
    // Select animation and interpolator are not intended to be
    // exposed at this moment. They might be synced with vertical scroll
    // animation later.
    int mSelectAnimatorDuration;
    Interpolator mSelectAnimatorInterpolator = new DecelerateInterpolator(2);

    private RecyclerView.RecycledViewPool mRecycledViewPool = null;
    private GridLayoutRecycleViewPool mRecycledViewHolderPoolAnother = null;

    private ItemBridgeAdapter.AdapterListener mExternalAdapterListener;

    //static LoaderCallback loaderCallback;
    RecyclerView.OnScrollListener mOnScrollListener;

    public void setRecycledViewPool(RecyclerView.RecycledViewPool recycledViewPool) {
        mRecycledViewPool = recycledViewPool;
    }

    public void setGridLayoutRecycleViewPool(GridLayoutRecycleViewPool recycledViewPool) {
        mRecycledViewHolderPoolAnother = recycledViewPool;
    }

    @Override
    public VerticalGridView findGridViewFromRoot(View view) {
        return (VerticalGridView) view.findViewById(R.id.container_list);
    }

    /**
     * Sets an staggered_item clicked listener on the fragment.
     * OnItemViewClickedListener will override {@link View.OnClickListener} that
     * staggered_item presenter sets during {@link Presenter#onCreateViewHolder(ViewGroup)}.
     * So in general,  developer should choose one of the listeners but not both.
     */
    public void setOnItemViewClickedListener(BaseRowPresenter.OnItemViewClickedListener listener) {
        mOnItemViewClickedListener = listener;
        if (mViewsCreated) {
            throw new IllegalStateException(
                    "Item clicked listener must be set before views are created");
        }
    }

    public void setGridViewAvailableObserver(GridViewAvailableObserver aGridViewAvailableObserver) {
        mGridViewAvailableObserver = aGridViewAvailableObserver;
    }

    /**
     * Returns the staggered_item clicked listener.
     */
    public BaseRowPresenter.OnItemViewClickedListener getOnItemViewClickedListener() {
        return mOnItemViewClickedListener;
    }

    /**
     * Set the visibility of titles/hovercard of browse rows.
     */
    public void setExpand(boolean expand) {
        mExpand = expand;
        VerticalGridView listView = getVerticalGridView();
        if (listView != null) {
            updateRowScaling();
            final int count = listView.getChildCount();
            if (DEBUG) {
                mLogger.v("setExpand " + expand + " count " + count);
            }
            for (int i = 0; i < count; i++) {
                View view = listView.getChildAt(i);
                ItemBridgeAdapter.ViewHolder vh = (ItemBridgeAdapter.ViewHolder) listView.getChildViewHolder(view);
                setRowViewExpanded(vh, mExpand);
            }
        }
    }

    /**
     * Sets an staggered_item selection listener.
     */
    public void setOnItemViewSelectedListener(BaseRowPresenter.OnItemViewSelectedListener listener) {
        mOnItemViewSelectedListener = listener;
        VerticalGridView listView = getVerticalGridView();
        if (listView != null) {
            final int count = listView.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = listView.getChildAt(i);
                ItemBridgeAdapter.ViewHolder ibvh = (ItemBridgeAdapter.ViewHolder)
                        listView.getChildViewHolder(view);
                BaseRowPresenter rowPresenter = (BaseRowPresenter) ibvh.getPresenter();
                BaseRowPresenter.ViewHolder vh = rowPresenter.getRowViewHolder(ibvh.getViewHolder());
                vh.setOnItemViewSelectedListener(mOnItemViewSelectedListener);
            }
        }
    }

    /**
     * Returns an staggered_item selection listener.
     */
    public BaseRowPresenter.OnItemViewSelectedListener getOnItemViewSelectedListener() {
        return mOnItemViewSelectedListener;
    }

    /**
     * Enables scaling of rows.
     *
     * @param enable true to enable row scaling
     */
    public void enableRowScaling(boolean enable) {
        mRowScaleEnabled = enable;
    }

    @Override
    public void onRowSelected(RecyclerView parent, RecyclerView.ViewHolder viewHolder,
                              int position, int subposition) {
        if (mSelectedViewHolder != viewHolder || mSubPosition != subposition) {
            if (DEBUG) {
                mLogger.v("new row selected position " + position + " subposition "
                        + subposition + " view " + viewHolder.itemView);
            }
            mSubPosition = subposition;
            if (mSelectedViewHolder != null) {
                setRowViewSelected(mSelectedViewHolder, false, false);
            }
            mSelectedViewHolder = (ItemBridgeAdapter.ViewHolder) viewHolder;
            if (mSelectedViewHolder != null) {
                setRowViewSelected(mSelectedViewHolder, true, false);
            }
            mLogger.v("onRowSelected " + position + " title=" + ((Block) mSelectedViewHolder.getItem()).title);
        }
    }

    @Override
    public void setAdapter(ObjectAdapter rowsAdapter) {
        if (DEBUG) {
            mLogger.v("rowsAdapter " + rowsAdapter);
        }
        if (getAdapter() == rowsAdapter) {
            return;
        }
        super.setAdapter(rowsAdapter);
    }

    @Override
    public final int getLayoutResourceId() {
        return R.layout.page_rows_fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectAnimatorDuration = 1;
        //getResources().getInteger(android.support.v17.leanback.R.integer.lb_browse_rows_anim_duration);
        mRowScaleFactor = 1;
        //getResources().getFraction(android.support.v17.leanback.R.fraction.lb_browse_rows_scale, 1, 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (DEBUG) {
            mLogger.v("onViewCreated");
        }
        super.onViewCreated(view, savedInstanceState);
        // Align the top edge of child with id row_content.
        // Need set this for directly using RowsFragment.
        // getVerticalGridView().setItemAlignmentViewId(android.support.v17.leanback.R.id.row_content);
        getVerticalGridView().setSaveChildrenPolicy(VerticalGridView.SAVE_ON_SCREEN_CHILD);
        // getVerticalGridView().setVerticalMargin((int)getResources().getDimension(R.dimen.grid_block_margin));
        // getVerticalGridView().getLayoutManager().setFocusOutAllowed(true,false);
        setExpand(true);

        if (mOnScrollListener != null) {
            getVerticalGridView().setOnScrollListener(mOnScrollListener);
        }
        if (mGridViewAvailableObserver != null) {
            mGridViewAvailableObserver.onViewAvailable(getVerticalGridView());
        }

        if (mPaddingTop >= 0) {
            setTopPadding(mPaddingTop);
        }
    }

    @Override
    public void onDestroyView() {
        mViewsCreated = false;
        super.onDestroyView();
    }

    @Override
    protected void setItemAlignment() {
        super.setItemAlignment();
        if (getVerticalGridView() != null) {
            getVerticalGridView().setItemAlignmentOffsetWithPadding(true);
        }
    }

    void setExternalAdapterListener(ItemBridgeAdapter.AdapterListener listener) {
        mExternalAdapterListener = listener;
    }

    /**
     * Returns the view that will change scale.
     */
    View getScaleView() {
        return getVerticalGridView();
    }

    private static void setRowViewExpanded(ItemBridgeAdapter.ViewHolder vh, boolean expanded) {
        ((BaseRowPresenter) vh.getPresenter()).setRowViewExpanded(vh.getViewHolder(), expanded);
    }

    private static void setRowViewSelected(ItemBridgeAdapter.ViewHolder vh, boolean selected,
                                           boolean immediate) {
        RowViewHolderExtra extra = (RowViewHolderExtra) vh.getExtraObject();
        extra.animateSelect(selected, immediate);
        ((BaseRowPresenter) vh.getPresenter()).setRowViewSelected(vh.getViewHolder(), selected);
    }

    private final ItemBridgeAdapter.AdapterListener mBridgeAdapterListener =
            new ItemBridgeAdapter.AdapterListener() {
                @Override
                public void onAddPresenter(Presenter presenter, int type) {
                    if (mExternalAdapterListener != null) {
                        mExternalAdapterListener.onAddPresenter(presenter, type);
                    }
                }

                @Override
                public void onCreate(ItemBridgeAdapter.ViewHolder vh) {
                    VerticalGridView listView = getVerticalGridView();
                    if (listView != null) {
                        // set clip children false for slide animation
                        listView.setClipChildren(false);
                    }
                    setupSharedViewPool(vh);
                    mViewsCreated = true;
                    vh.setExtraObject(new RowViewHolderExtra(vh));
                    // selected state is initialized to false, then driven by grid view onChildSelected
                    // events.  When there is rebind, grid view fires onChildSelected event properly.
                    // So we don't need do anything special later in onBind or onAttachedToWindow.
                    setRowViewSelected(vh, false, true);
                    if (mExternalAdapterListener != null) {
                        mExternalAdapterListener.onCreate(vh);
                    }
                }

                @Override
                public void onAttachedToWindow(ItemBridgeAdapter.ViewHolder vh) {
                    if (DEBUG) {
                        mLogger.v("onAttachToWindow");
                    }
                    // All views share the same mExpand value.  When we attach a view to grid view,
                    // we should make sure it pick up the latest mExpand value we set early on other
                    // attached views.  For no-structure-change update,  the view is rebound to new data,
                    // but again it should use the unchanged mExpand value,  so we don't need do any
                    // thing in onBind.
                    //setRowViewExpanded(vh, mExpand);
                    BaseRowPresenter rowPresenter = (BaseRowPresenter) vh.getPresenter();
                    BaseRowPresenter.ViewHolder rowVh = rowPresenter.getRowViewHolder(vh.getViewHolder());
                    rowVh.setOnItemViewSelectedListener(mOnItemViewSelectedListener);
                    rowVh.setOnItemViewClickedListener(mOnItemViewClickedListener);
                    rowPresenter.setEntranceTransitionState(rowVh, mAfterEntranceTransition);
                    if (mExternalAdapterListener != null) {
                        mExternalAdapterListener.onAttachedToWindow(vh);
                    }
                }

                @Override
                public void onDetachedFromWindow(ItemBridgeAdapter.ViewHolder vh) {
                    if (mSelectedViewHolder == vh) {
                        setRowViewSelected(mSelectedViewHolder, false, true);
                        mSelectedViewHolder = null;
                    }
                    if (mExternalAdapterListener != null) {
                        mExternalAdapterListener.onDetachedFromWindow(vh);
                    }
                }

                @Override
                public void onBind(ItemBridgeAdapter.ViewHolder vh) {
                    if (mExternalAdapterListener != null) {
                        mExternalAdapterListener.onBind(vh);
                    }
                }

                @Override
                public void onUnbind(ItemBridgeAdapter.ViewHolder vh) {
                    setRowViewSelected(vh, false, true);
                    if (mExternalAdapterListener != null) {
                        mExternalAdapterListener.onUnbind(vh);
                    }
                }
            };

    private void setupSharedViewPool(ItemBridgeAdapter.ViewHolder bridgeVh) {
        if (bridgeVh.getPresenter() instanceof BaseRowPresenter) {
            BaseRowPresenter rowPresenter = (BaseRowPresenter) bridgeVh.getPresenter();
            BaseRowPresenter.ViewHolder rowVh = rowPresenter.getRowViewHolder(bridgeVh.getViewHolder());

            if (rowVh instanceof RegularBlockPresenter.ViewHolder) {
                RecyclerView view = ((RegularBlockPresenter.ViewHolder) rowVh).getGridView();
                if (view != null) {
                    // Recycled view pool is shared between all list rows
                    if (mRecycledViewPool == null) {
                        mRecycledViewPool = view.getRecycledViewPool();
                    } else {
                        view.setRecycledViewPool(mRecycledViewPool);
                    }
                }

            }
        }
    }

    public void pauseViewAdapterDelayed(Handler handler) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; mVerticalGridView != null && i < mVerticalGridView.getChildCount(); i++) {
                    ViewGroup child = (ViewGroup) mVerticalGridView.getChildAt(i);
                    Presenter.ViewHolder holder = (Presenter.ViewHolder) child.getTag(ItemBasePresenter.VIEW_HOLD_ID);
                    if (child.getTag(ItemBasePresenter.VIEW_HOLD_ID) != null) {
                        BaseRowPresenter.ViewHolder rowVh;
                        if (holder instanceof BaseRowPresenter.ContainerViewHolder) {
                            rowVh = ((BaseRowPresenter.ContainerViewHolder) holder).mRowViewHolder;
                        } else {
                            rowVh = (BaseRowPresenter.ViewHolder) holder;
                        }

                        if (rowVh instanceof RegularBlockPresenter.ViewHolder && rowVh.mItem != null) {
                            Presenter presenter = mAdapter.getPresenterSelector().getPresenter(rowVh.mItem);
                            if (presenter instanceof RegularBlockPresenter) {
                                ((RegularBlockPresenter) presenter).onUnbindViewHolder1(rowVh);
                            } /*else if (presenter instanceof BlockGridPresenter) {
                                ((BlockGridPresenter) presenter).onUnbindViewHolder1(rowVh);
                            }*/
                        }
                    }
                }
            }
        }, 100);
    }

    public void resumeViewAdapter() {
        for (int i = 0; mVerticalGridView != null && i < mVerticalGridView.getChildCount(); i++) {
            ViewGroup child = (ViewGroup) mVerticalGridView.getChildAt(i);
            Presenter.ViewHolder holder = (Presenter.ViewHolder) child.getTag(ItemBasePresenter.VIEW_HOLD_ID);
            if (child.getTag(ItemBasePresenter.VIEW_HOLD_ID) != null) {
                BaseRowPresenter.ViewHolder rowVh;
                if (holder instanceof BaseRowPresenter.ContainerViewHolder) {
                    rowVh = ((BaseRowPresenter.ContainerViewHolder) holder).mRowViewHolder;
                } else {
                    rowVh = (BaseRowPresenter.ViewHolder) holder;
                }

                if (rowVh instanceof RegularBlockPresenter.ViewHolder && rowVh.mItem != null) {
                    Presenter presenter = mAdapter.getPresenterSelector().getPresenter(rowVh.mItem);
                    if (presenter instanceof RegularBlockPresenter) {
                        ((RegularBlockPresenter) presenter).onBindViewHolder1(rowVh);
                    } /*else if (presenter instanceof BlockGridPresenter) {
                        ((BlockGridPresenter) presenter).onBindViewHolder1(rowVh);
                    }*/
                } /*else if(rowVh instanceof  ShortVideoInnerBlockPresenter.ViewHolder && rowVh.mItem != null){
                    Presenter presenter = mAdapter.getPresenterSelector().getPresenter(rowVh.mItem);
                    ((ShortVideoInnerBlockPresenter) presenter).onBindRowViewHolderWhenResume(rowVh, rowVh.mItem);
                }*/
            }
        }

    }


    @Override
    protected void updateAdapter() {
        super.updateAdapter();
        mSelectedViewHolder = null;
        mViewsCreated = false;

        ItemBridgeAdapter adapter = getBridgeAdapter();
        if (adapter != null) {
            adapter.setAdapterListener(mBridgeAdapterListener);
        }
    }

    @Override
    protected boolean onTransitionPrepare() {
        boolean prepared = super.onTransitionPrepare();
        if (prepared) {
            freezeRows(true);
        }
        return prepared;
    }

    class ExpandPreLayout implements ViewTreeObserver.OnPreDrawListener {

        final View mVerticalView;
        final Runnable mCallback;
        int mState;

        final static int STATE_INIT = 0;
        final static int STATE_FIRST_DRAW = 1;
        final static int STATE_SECOND_DRAW = 2;

        ExpandPreLayout(Runnable callback) {
            mVerticalView = getVerticalGridView();
            mCallback = callback;
        }

        void execute() {
            mVerticalView.getViewTreeObserver().addOnPreDrawListener(this);
            setExpand(false);
            mState = STATE_INIT;
        }

        @Override
        public boolean onPreDraw() {
            if (getView() == null || getActivity() == null) {
                mVerticalView.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
            if (mState == STATE_INIT) {
                setExpand(true);
                mState = STATE_FIRST_DRAW;
            } else if (mState == STATE_FIRST_DRAW) {
                mCallback.run();
                mVerticalView.getViewTreeObserver().removeOnPreDrawListener(this);
                mState = STATE_SECOND_DRAW;
            }
            return false;
        }
    }

    void onExpandTransitionStart(boolean expand, final Runnable callback) {
        onTransitionPrepare();
        onTransitionStart();
        if (expand) {
            callback.run();
            return;
        }
        // Run a "pre" layout when we go non-expand, in order to get the initial
        // positions of added rows.
        new ExpandPreLayout(callback).execute();
    }

    private boolean needsScale() {
        return mRowScaleEnabled && !mExpand;
    }

    private void updateRowScaling() {
        final float scaleFactor = needsScale() ? mRowScaleFactor : 1f;
        getScaleView().setScaleY(scaleFactor);
        getScaleView().setScaleX(scaleFactor);
        updateWindowAlignOffset();
    }

    private void updateWindowAlignOffset() {
        int alignOffset = mAlignedTop;
        if (needsScale()) {
            alignOffset = (int) (alignOffset / mRowScaleFactor + 0.5f);
        }
        getVerticalGridView().setWindowAlignmentOffset(alignOffset);
    }

    @Override
    protected void setWindowAlignmentFromTop(int alignedTop) {
        mAlignedTop = alignedTop;
        final VerticalGridView gridView = getVerticalGridView();
        if (gridView != null) {
            updateWindowAlignOffset();
            // align to a fixed position from top
            gridView.setWindowAlignmentOffsetPercent(
                    VerticalGridView.WINDOW_ALIGN_OFFSET_PERCENT_DISABLED);
            gridView.setWindowAlignment(VerticalGridView.WINDOW_ALIGN_NO_EDGE);
        }
    }

    @Override
    protected void onTransitionEnd() {
        super.onTransitionEnd();
        freezeRows(false);
    }

    private void freezeRows(boolean freeze) {
        VerticalGridView verticalView = getVerticalGridView();
        if (verticalView != null) {
            final int count = verticalView.getChildCount();
            for (int i = 0; i < count; i++) {
                ItemBridgeAdapter.ViewHolder ibvh = (ItemBridgeAdapter.ViewHolder)
                        verticalView.getChildViewHolder(verticalView.getChildAt(i));
                BaseRowPresenter rowPresenter = (BaseRowPresenter) ibvh.getPresenter();
                BaseRowPresenter.ViewHolder vh = rowPresenter.getRowViewHolder(ibvh.getViewHolder());
                rowPresenter.freeze(vh, freeze);
            }
        }
    }

    /**
     * For rows that willing to participate entrance transition,  this function
     * hide views if afterTransition is true,  show views if afterTransition is false.
     */
    void setEntranceTransitionState(boolean afterTransition) {
        mAfterEntranceTransition = afterTransition;
        VerticalGridView verticalView = getVerticalGridView();
        if (verticalView != null) {
            final int count = verticalView.getChildCount();
            for (int i = 0; i < count; i++) {
                ItemBridgeAdapter.ViewHolder ibvh = (ItemBridgeAdapter.ViewHolder)
                        verticalView.getChildViewHolder(verticalView.getChildAt(i));
                BaseRowPresenter rowPresenter = (BaseRowPresenter) ibvh.getPresenter();
                BaseRowPresenter.ViewHolder vh = rowPresenter.getRowViewHolder(ibvh.getViewHolder());
                rowPresenter.setEntranceTransitionState(vh, mAfterEntranceTransition);
            }
        }
    }

    public void appendData(Block<DisplayItem> data) {
        if (data != null) {
            BlockAdapter adapter = (BlockAdapter) getAdapter();
            adapter.appendBlock(data);
        }
    }

    public void setOnScrollListener(RecyclerView.OnScrollListener listener) {
        mOnScrollListener = listener;
    }

    public void scrollToRowFirst(int index) {
        VerticalGridView verticalView = getVerticalGridView();
        if (verticalView != null && verticalView.getChildAt(index) != null) {
            ItemBridgeAdapter.ViewHolder ibvh = (ItemBridgeAdapter.ViewHolder)
                    verticalView.getChildViewHolder(verticalView.getChildAt(index));
            BaseRowPresenter rowPresenter = (BaseRowPresenter) ibvh.getPresenter();
            BaseRowPresenter.ViewHolder vh = rowPresenter.getRowViewHolder(ibvh.getViewHolder());
            rowPresenter.setSelectPosition(vh, 0);
            //vh.view.requestFocus();
        }
    }

    public void setTopPadding(int paddingTop) {
        if (paddingTop != mPaddingTop) {
            mPaddingTop = paddingTop;
        }
        final VerticalGridView gridView = getVerticalGridView();
        if (gridView != null) {
            if (gridView.getPaddingTop() != mPaddingTop) {
                gridView.setPadding(gridView.getPaddingLeft(), mPaddingTop, gridView.getPaddingRight(), gridView.getPaddingBottom());
            }
        }
    }
}
