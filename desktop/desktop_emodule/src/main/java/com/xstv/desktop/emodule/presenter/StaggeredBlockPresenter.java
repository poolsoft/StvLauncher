package com.xstv.desktop.emodule.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v17.leanback.system.Settings;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.FocusHighlight;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.OnChildSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.ShadowOverlayHelper;
import android.support.v17.leanback.widget.VerticalGridView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xstv.desktop.emodule.R;
import com.xstv.desktop.emodule.adapter.ItemBridgeAdapter;
import com.xstv.desktop.emodule.mode.Block;
import com.xstv.desktop.emodule.mode.DisplayItem;
import com.xstv.library.base.Logger;
import com.xstv.library.widget.recyclerview.DividerItemDecoration;
import com.xstv.library.widget.recyclerview.StaggeredGridLayoutManager;
import com.xstv.library.widget.recyclerview.TwoWayLayoutManager;
import com.xstv.library.widget.recyclerview.TwoWayView;

import java.util.HashMap;

/**
 * 规则的不同大小的GridView布局，使用TwoWayView库实现
 */
public class StaggeredBlockPresenter extends BaseRowPresenter {
    private static final int DEFAULT_RECYCLED_POOL_SIZE = 10;
    private static final int DEFAULT_RECYCLED_POOL_HALF_SIZE = 10;
    protected CommonPresenterSelector mPresenterSelector = null;

    public static class ViewHolder extends BaseRowPresenter.ViewHolder {
        public final StaggeredBlockPresenter mBlockBasePresenter;
        public final TwoWayView twoWayView;
        public ItemBridgeAdapter mItemBridgeAdapter;
        public ArrayObjectAdapter mAdapter;
        public int mItemWidth;
        public int mItemHeight;

        public ViewHolder(View rootView, TwoWayView gridView, StaggeredBlockPresenter p) {
            super(rootView);
            twoWayView = gridView;
            mBlockBasePresenter = p;
        }

        public final RecyclerView getGridView() {
            return twoWayView;
        }

        public final ItemBridgeAdapter getBridgeAdapter() {
            return mItemBridgeAdapter;
        }
    }

    class BlockPresenterItemBridgeAdapter extends ItemBridgeAdapter {
        StaggeredBlockPresenter.ViewHolder mBlockViewHolder;

        BlockPresenterItemBridgeAdapter(StaggeredBlockPresenter.ViewHolder rowViewHolder) {
            mBlockViewHolder = rowViewHolder;
        }

        @Override
        protected void onCreate(ViewHolder viewHolder) {
        }

        @Override
        public void onBind(final ViewHolder viewHolder) {
            // Only when having an OnItemClickListener, we will attach the OnClickListener.
            if (mBlockViewHolder.getOnItemViewClickedListener() != null) {
                viewHolder.getViewHolder().view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewHolder ibh = (ViewHolder)
                                mBlockViewHolder.twoWayView.getChildViewHolder(viewHolder.itemView);
                        if (mBlockViewHolder.getOnItemViewClickedListener() != null) {
                            mBlockViewHolder.getOnItemViewClickedListener().onItemClicked(viewHolder.getViewHolder(),
                                    ibh.getItem(), mBlockViewHolder, mItem);
                        }
                    }
                });
            }
        }

        @Override
        public void onUnbind(ViewHolder viewHolder) {
            if (mBlockViewHolder.getOnItemViewClickedListener() != null) {
                viewHolder.getViewHolder().view.setOnClickListener(null);
            }
        }

        @Override
        public void onAttachedToWindow(ViewHolder viewHolder) {
            mBlockViewHolder.syncActivatedStatus(viewHolder.itemView);
        }

        @Override
        public void onAddPresenter(Presenter presenter, int type) {
        }

    }

    Logger mLogger = Logger.getLogger("EModule", "StaggeredBlockPresenter");
    private int mRowHeight;
    private int mExpandedRowHeight;
    private PresenterSelector mHoverCardPresenterSelector;
    private int mFocusZoomFactor;
    private boolean mUseFocusDimmer;
    private boolean mShadowEnabled = true;
    private int mBrowseRowsFadingEdgeLength = -1;
    private boolean mRoundedCornersEnabled = true;
    private boolean mKeepChildForeground = true;
    private HashMap<Presenter, Integer> mRecycledPoolSize = new HashMap<Presenter, Integer>();

    protected ViewHolder mHolder;
    protected int mParentWidth;

    /**
     * Constructs a ListRowPresenter with defaults.
     * Uses {@link FocusHighlight#ZOOM_FACTOR_MEDIUM} for focus zooming and
     * disabled dimming on focus.
     */
    public StaggeredBlockPresenter() {
        this(FocusHighlight.ZOOM_FACTOR_MEDIUM);
    }

    /**
     * Constructs a ListRowPresenter with the given parameters.
     *
     * @param focusZoomFactor Controls the zoom factor used when an staggered_item view is focused. One of
     *                        {@link FocusHighlight#ZOOM_FACTOR_NONE},
     *                        {@link FocusHighlight#ZOOM_FACTOR_SMALL},
     *                        {@link FocusHighlight#ZOOM_FACTOR_XSMALL},
     *                        {@link FocusHighlight#ZOOM_FACTOR_MEDIUM},
     *                        {@link FocusHighlight#ZOOM_FACTOR_LARGE}
     *                        Dimming on focus defaults to disabled.
     */
    public StaggeredBlockPresenter(int focusZoomFactor) {
        this(focusZoomFactor, false);
    }

    /**
     * Constructs a ListRowPresenter with the given parameters.
     *
     * @param focusZoomFactor Controls the zoom factor used when an staggered_item view is focused. One of
     *                        {@link FocusHighlight#ZOOM_FACTOR_NONE},
     *                        {@link FocusHighlight#ZOOM_FACTOR_SMALL},
     *                        {@link FocusHighlight#ZOOM_FACTOR_XSMALL},
     *                        {@link FocusHighlight#ZOOM_FACTOR_MEDIUM},
     *                        {@link FocusHighlight#ZOOM_FACTOR_LARGE}
     * @param useFocusDimmer  determines if the FocusHighlighter will use the dimmer
     */
    public StaggeredBlockPresenter(int focusZoomFactor, boolean useFocusDimmer) {
        mFocusZoomFactor = focusZoomFactor;
        mUseFocusDimmer = useFocusDimmer;
    }

    /**
     * Sets the row height for rows created by this Presenter. Rows
     * created before calling this method will not be updated.
     *
     * @param rowHeight Row height in pixels, or WRAP_CONTENT, or 0
     * to use the default height.
     */
    //public void setRowHeight(int rowHeight) {
    //    mRowHeight = rowHeight;
    //}

    /**
     * Returns the row height for list rows created by this Presenter.
     */
    //public int getRowHeight() {
    //    return mRowHeight;
    //}


    /**
     * Sets the expanded row height for rows created by this Presenter.
     * If not set, expanded rows have the same height as unexpanded
     * rows.
     *
     * @param rowHeight The row height in to use when the row is expanded,
     *                  in pixels, or WRAP_CONTENT, or 0 to use the default.
     */
    public void setExpandedRowHeight(int rowHeight) {
        mExpandedRowHeight = rowHeight;
    }

    /**
     * Returns the expanded row height for rows created by this Presenter.
     */
    public int getExpandedRowHeight() {
        return mExpandedRowHeight != 0 ? mExpandedRowHeight : mRowHeight;
    }

    /**
     * Returns the zoom factor used for focus highlighting.
     */
    public final int getFocusZoomFactor() {
        return mFocusZoomFactor;
    }

    /**
     * Returns the zoom factor used for focus highlighting.
     *
     * @deprecated use {@link #getFocusZoomFactor} instead.
     */
    @Deprecated
    public final int getZoomFactor() {
        return mFocusZoomFactor;
    }

    /**
     * Returns true if the focus dimmer is used for focus highlighting; false otherwise.
     */
    public final boolean isFocusDimmerUsed() {
        return mUseFocusDimmer;
    }

    @Override
    protected void initializeRowViewHolder(BaseRowPresenter.ViewHolder holder) {
        super.initializeRowViewHolder(holder);
        final ViewHolder blockViewHolder = (ViewHolder) holder;
        blockViewHolder.mItemBridgeAdapter = new BlockPresenterItemBridgeAdapter(blockViewHolder);
        if (mPresenterSelector != null) {
            blockViewHolder.mItemBridgeAdapter.setPresenterMapper(mPresenterSelector.getPresentersArray());
        }
        setEventListener(blockViewHolder.twoWayView, blockViewHolder);
    }

    private void setEventListener(RecyclerView view, ViewHolder viewHolder) {
        final ViewHolder blockViewHolder = viewHolder;
        if (view instanceof HorizontalGridView) {
            HorizontalGridView horizontalGridView = (HorizontalGridView) view;
            horizontalGridView.setOnChildSelectedListener(
                    new OnChildSelectedListener() {
                        @Override
                        public void onChildSelected(ViewGroup parent, View view, int position, long id) {
                            selectChildView(blockViewHolder, view, true);
                        }
                    });
            horizontalGridView.setOnUnhandledKeyListener(
                    new HorizontalGridView.OnUnhandledKeyListener() {
                        @Override
                        public boolean onUnhandledKey(KeyEvent event) {
                            if (blockViewHolder.getOnKeyListener() != null &&
                                    blockViewHolder.getOnKeyListener().onKey(
                                            blockViewHolder.view, event.getKeyCode(), event)) {
                                return true;
                            }
                            return false;
                        }
                    });
        } else if (view instanceof VerticalGridView) {
            VerticalGridView verticalGridView = (VerticalGridView) view;
            verticalGridView.setOnUnhandledKeyListener(
                    new HorizontalGridView.OnUnhandledKeyListener() {
                        @Override
                        public boolean onUnhandledKey(KeyEvent event) {
                            if (blockViewHolder.getOnKeyListener() != null &&
                                    blockViewHolder.getOnKeyListener().onKey(
                                            blockViewHolder.view, event.getKeyCode(), event)) {
                                return true;
                            }
                            return false;
                        }
                    });
        }
    }

    /**
     * Sets the recycled pool size for the given presenter.
     */
    public void setRecycledPoolSize(Presenter presenter, int size) {
        mRecycledPoolSize.put(presenter, size);
    }

    /**
     * Returns the recycled pool size for the given presenter.
     */
    public int getRecycledPoolSize(Presenter presenter) {
        return mRecycledPoolSize.containsKey(presenter) ? mRecycledPoolSize.get(presenter) :
                DEFAULT_RECYCLED_POOL_SIZE;
    }

    /**
     * Sets the {@link PresenterSelector} used for showing a select object in a hover card.
     */
    public final void setHoverCardPresenterSelector(PresenterSelector selector) {
        mHoverCardPresenterSelector = selector;
    }

    /**
     * Returns the {@link PresenterSelector} used for showing a select object in a hover card.
     */
    public final PresenterSelector getHoverCardPresenterSelector() {
        return mHoverCardPresenterSelector;
    }

    /*
     * Perform operations when a child of horizontal grid view is selected.
     */
    private void selectChildView(ViewHolder rowViewHolder, View view, boolean fireEvent) {
        if (view != null) {
            ItemBridgeAdapter.ViewHolder ibh = (ItemBridgeAdapter.ViewHolder)
                    rowViewHolder.twoWayView.getChildViewHolder(view);
            if (/*fireEvent && */rowViewHolder.getOnItemViewSelectedListener() != null) {
                rowViewHolder.getOnItemViewSelectedListener().onItemSelected(
                        ibh.getViewHolder(), ibh.getItem(), rowViewHolder, rowViewHolder.getRow());
            }
        } else {
            if (/*fireEvent &&*/ rowViewHolder.getOnItemViewSelectedListener() != null) {
                rowViewHolder.getOnItemViewSelectedListener().onItemSelected(
                        null, null, rowViewHolder, rowViewHolder.getRow());
            }
        }
    }

    @Override
    protected ViewHolder createRowViewHolder(ViewGroup parent) {
        mParentWidth = parent.getWidth();
        return createBlockViewHolder(parent);
    }

    protected ViewHolder createBlockViewHolder(ViewGroup parent) {
        TwoWayView view = (TwoWayView) LayoutInflater.from(parent.getContext()).inflate(R.layout.block_staggered, null, false);
        StaggeredBlockPresenter.ViewHolder holder = new StaggeredBlockPresenter.ViewHolder(view, view, this);
        return holder;
    }

    @Override
    protected void dispatchItemSelectedListener(BaseRowPresenter.ViewHolder holder, boolean selected) {
        ViewHolder vh = (ViewHolder) holder;
        ItemBridgeAdapter.ViewHolder itemViewHolder = null;
        itemViewHolder = null;//vh.twoWayView.findViewHolderForAdapterPosition(vh.twoWayView.getLayoutManager().get);

        if (itemViewHolder == null) {
            super.dispatchItemSelectedListener(holder, selected);
            return;
        }

        if (selected) {
            if (holder.getOnItemViewSelectedListener() != null) {
                holder.getOnItemViewSelectedListener().onItemSelected(
                        itemViewHolder.getViewHolder(), itemViewHolder.getItem(), vh, vh.getRow());
            }
        }
    }

    @Override
    protected void onRowViewSelected(BaseRowPresenter.ViewHolder holder, boolean selected) {
        super.onRowViewSelected(holder, selected);
        ViewHolder vh = (ViewHolder) holder;
        //setVerticalPadding(vh);
        updateFooterViewSwitcher(vh);
    }

    /**
     * Show or hide hover card when row selection or expanded state is changed.
     */
    private void updateFooterViewSwitcher(ViewHolder vh) {
        if (vh.isExpanded() && vh.isExpanded()) {
            if (mHoverCardPresenterSelector != null) {
                //vh.mHoverCardViewSwitcher.init((ViewGroup) vh.view, mHoverCardPresenterSelector);
            }
            ItemBridgeAdapter.ViewHolder ibh = null;
            ibh = null;//vh.twoWayView.findViewHolderForAdapterPosition(vh.twoWayView.getSelectedPosition)
            selectChildView(vh, ibh == null ? null : ibh.itemView, false);
        } else {
            if (mHoverCardPresenterSelector != null) {
                //vh.mHoverCardViewSwitcher.unselect();
            }
        }
    }

    @Override
    protected void onRowViewExpanded(BaseRowPresenter.ViewHolder holder, boolean expanded) {
        super.onRowViewExpanded(holder, expanded);
        ViewHolder vh = (ViewHolder) holder;
        updateFooterViewSwitcher(vh);
    }

    @Override
    protected void onBindRowViewHolder(BaseRowPresenter.ViewHolder holder, Object item) {
        //mLogger.v("onBindRowViewHolder " + staggered_item);
        ViewHolder vh = (ViewHolder) holder;
        holder.mItem = item;
        mHolder = vh;
        if (item instanceof Block) {
            boolean needTitle = !TextUtils.isEmpty(((Block) item).title);
            if (needTitle) {
                super.onBindRowViewHolder(vh, new Row(new HeaderItem(0, ((Block) item).title)));
            }
            onBindBlockData(vh, item);
        } else {
            super.onBindRowViewHolder(holder, item);
            ListRow rowItem = (ListRow) item;
            vh.mItemBridgeAdapter.setAdapter(rowItem.getAdapter());
            vh.twoWayView.setAdapter(vh.mItemBridgeAdapter);
        }
    }

    protected void onBindBlockData(ViewHolder vh, final Object item) {
        mLogger.d("onBindBlockData " + item + " presenter=" + this);
        vh.mContainerViewHolder.view.setVisibility(View.VISIBLE);
        vh.mContainerViewHolder.view.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        vh.mContainerViewHolder.view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

        final Block block = (Block) item;
        synchronized (block) {
            if (mPresenterSelector != null && block.items != null && block.items.size() > 0) {
                boolean needTitle = !TextUtils.isEmpty(((Block) item).title);
                // 如果该Block没有标题，需要设置一定的paddingTop，当上边沿item获取焦点放大时，才不会被遮挡
                // 存在标题View时，Item放大后会覆盖它
                if (!needTitle) {
                    int paddingTop = (int) vh.twoWayView.getResources().getDimension(R.dimen.grid_item_margin_small);
                    vh.twoWayView.setPadding(0, paddingTop, 0, 0);
                }

                //TODO 当前只支持横向
                int parentPadding = (int) vh.view.getResources().getDimension(R.dimen.grid_block_hor_padding);
//                int itemMargin = vh.view.getResources().getDimension(R.dimen.staggered_item_margin)
                int rows = block.ui.rows;
                int columns = block.ui.columns;
                int widthOneSpan = (mParentWidth - 2 * parentPadding) / columns;
                mLogger.d("xubin " + widthOneSpan + " mParentWidth=" + mParentWidth);

                RecyclerView.LayoutManager layoutManager = vh.twoWayView.getLayoutManager();
                if (layoutManager == null || layoutManager instanceof TwoWayView.DefaultLayoutManager) {
                    StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(vh.twoWayView.getContext());
                    manager.setNumRows(rows);
                    manager.setNumColumns(columns);
                    manager.setOrientation(TwoWayLayoutManager.Orientation.HORIZONTAL);
                    vh.twoWayView.setLayoutManager(manager);
                }

                vh.mAdapter = new ArrayObjectAdapter(mPresenterSelector);
                for (int i = 0; i < block.items.size(); ++i) {
                    DisplayItem displayItem = (DisplayItem) block.items.get(i);
                    displayItem.ui.width = displayItem.ui.columnSpan * widthOneSpan;
                    vh.mAdapter.add(displayItem);
                }

                ViewGroup.LayoutParams lp = vh.twoWayView.getLayoutParams();
                lp.height = block.ui.height;
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                vh.twoWayView.setLayoutParams(lp);
                vh.twoWayView.setHasFixedSize(true);

                vh.mItemBridgeAdapter.setAdapter(vh.mAdapter);
                vh.twoWayView.setAdapter(vh.mItemBridgeAdapter);
                vh.mItemBridgeAdapter.setItem(item);
            }
        }
    }

    @Override
    protected void onUnbindRowViewHolder(BaseRowPresenter.ViewHolder holder) {
        super.onUnbindRowViewHolder(holder);
        ViewHolder vh = (ViewHolder) holder;
        if (vh.twoWayView != null) {
            vh.twoWayView.setAdapter(null);
        }
        vh.mItemBridgeAdapter.clear();
        super.onUnbindRowViewHolder(holder);
        mHolder = null;
    }

    /**
     * ListRowPresenter overrides the default select effect of {@link BaseRowPresenter}
     * and return false.
     */
    @Override
    public final boolean isUsingDefaultSelectEffect() {
        return false;
    }

    /**
     * Returns true so that default select effect is applied to each individual
     * child of {@link HorizontalGridView}.  Subclass may return false to disable
     * the default implementation.
     *
     * @see #onSelectLevelChanged(BaseRowPresenter.ViewHolder)
     */
    public boolean isUsingDefaultListSelectEffect() {
        return true;
    }

    /**
     * Returns true if SDK >= 18, where default shadow
     * is applied to each individual child of {@link HorizontalGridView}.
     * Subclass may return false to disable.
     */
    public boolean isUsingDefaultShadow() {
        return ShadowOverlayHelper.supportsShadow();
    }

    /**
     * Returns true if SDK >= L, where Z shadow is enabled so that Z order is enabled
     * on each child of horizontal list.   If subclass returns false in isUsingDefaultShadow()
     * and does not use Z-shadow on SDK >= L, it should override isUsingZOrder() return false.
     */
    @SuppressLint("RestrictedApi")
    public boolean isUsingZOrder(Context context) {
        return !Settings.getInstance(context).preferStaticShadows();
    }

    /**
     * Enables or disables child shadow.
     * This is not only for enable/disable default shadow implementation but also subclass must
     * respect this flag.
     */
    public final void setShadowEnabled(boolean enabled) {
        mShadowEnabled = enabled;
    }

    /**
     * Returns true if child shadow is enabled.
     * This is not only for enable/disable default shadow implementation but also subclass must
     * respect this flag.
     */
    public final boolean getShadowEnabled() {
        return mShadowEnabled;
    }

    /**
     * Enables or disabled rounded corners on children of this row.
     * Supported on Android SDK >= L.
     */
    public final void enableChildRoundedCorners(boolean enable) {
        mRoundedCornersEnabled = enable;
    }

    /**
     * Returns true if rounded corners are enabled for children of this row.
     */
    public final boolean areChildRoundedCornersEnabled() {
        return mRoundedCornersEnabled;
    }

    final boolean needsDefaultShadow() {
        return isUsingDefaultShadow() && getShadowEnabled();
    }

    /**
     * When ListRowPresenter applies overlay color on the child,  it may change child's foreground
     * Drawable.  If application uses child's foreground for other purposes such as ripple effect,
     * it needs tell ListRowPresenter to keep the child's foreground.  The default value is true.
     *
     * @param keep true if keep foreground of child of this row, false ListRowPresenter might change
     *             the foreground of the child.
     */
    public final void setKeepChildForeground(boolean keep) {
        mKeepChildForeground = keep;
    }

    /**
     * Returns true if keeps foreground of child of this row, false otherwise.  When
     * ListRowPresenter applies overlay color on the child,  it may change child's foreground
     * Drawable.  If application uses child's foreground for other purposes such as ripple effect,
     * it needs tell ListRowPresenter to keep the child's foreground.  The default value is true.
     *
     * @return true if keeps foreground of child of this row, false otherwise.
     */
    public final boolean isKeepChildForeground() {
        return mKeepChildForeground;
    }

    /**
     * Create ShadowOverlayHelper Options.  Subclass may override.
     * e.g.
     * <code>
     * return new ShadowOverlayHelper.Options().roundedCornerRadius(10);
     * </code>
     *
     * @return The options to be used for shadow, overlay and rouded corner.
     */
    protected ShadowOverlayHelper.Options createShadowOverlayOptions() {
        return ShadowOverlayHelper.Options.DEFAULT;
    }

    /**
     * Applies select level to header and draw a default color dim over each child
     * of {@link HorizontalGridView}.
     * <p>
     * Subclass may override this method.  A subclass
     * needs to call super.onSelectLevelChanged() for applying header select level
     * and optionally applying a default select level to each child view of
     * {@link HorizontalGridView} if {@link #isUsingDefaultListSelectEffect()}
     * is true.  Subclass may override {@link #isUsingDefaultListSelectEffect()} to return
     * false and deal with the individual staggered_item select level by itself.
     * </p>
     */
    @Override
    protected void onSelectLevelChanged(BaseRowPresenter.ViewHolder holder) {
        super.onSelectLevelChanged(holder);
    }

    @Override
    public void freeze(BaseRowPresenter.ViewHolder holder, boolean freeze) {
        ViewHolder vh = (ViewHolder) holder;
        //vh.twoWayView.setScrollEnabled(!freeze);
    }

    @Override
    public void setEntranceTransitionState(BaseRowPresenter.ViewHolder holder,
                                           boolean afterEntrance) {
        super.setEntranceTransitionState(holder, afterEntrance);

        //((ViewHolder) holder).twoWayView.setChildrenVisibility(afterEntrance ? View.VISIBLE : View.INVISIBLE);
    }

    public void setPresenterSelector(CommonPresenterSelector selector) {
        mPresenterSelector = selector;
    }

    @Override
    public void setSelectPosition(BaseRowPresenter.ViewHolder viewHolder, int position) {
        ViewHolder vh = (ViewHolder) viewHolder;
        vh.twoWayView.scrollToPosition(position);
    }
}
