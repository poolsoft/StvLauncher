package com.xstv.launcher.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.FocusFinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.xstv.launcher.R;
import com.xstv.launcher.provider.db.ScreenInfo;
import com.xstv.launcher.ui.activity.Launcher;
import com.xstv.library.base.ActivityActionHandler;
import com.xstv.library.base.BaseFragment;
import com.xstv.library.base.LetvLog;

import java.util.ArrayList;

public class TabStripImpl extends HorizontalScrollView implements ITabStrip {

    private static final String TAG = TabStripImpl.class.getSimpleName();
    private static final int LAYOUT_STATE_NORMAL = 0;
    private static final int LAYOUT_STATE_IMPORTANCE = 1;
    private static final int DEFAULT_FADING_EDGE_LENGTH = 100;


    private static final boolean mHasOverlappingRendering = false;
    // @formatter:off
    private static final int[] ATTRS = new int[]{android.R.attr.textSize,
            android.R.attr.textColor};

    // @formatter:on
    /**
     * LayoutParams for tabItems
     */
    private LinearLayout.LayoutParams defaultTabLayoutParams;


    /**
     * developer should listen TabItemNew's focus event from this listener
     * should <b color=red> NOT <b/> use original OnFocusChangeListener
     */
    private OnTabItemFocusChangeListener mOnTabItemFocusChangeListener;
    private TabSpace.OnTabChangedListener mOnTabChangeListener;
    private LinearLayout tabsContainer;
    private OnHierarchyChangeListener mOnHierarchyChangeListener = new OnHierarchyChangeListener() {
        @Override
        public void onChildViewAdded(View parent, View child) {
            if (child instanceof TabItemNew) {
                ((TabItemNew) child).addViewTreeObserverListener();
            }
        }

        @Override
        public void onChildViewRemoved(View parent, View child) {
            //this is not enough ..it too late.this time ,the attach info has already been removed;
//            if(child instanceof TabItemNew){
//                ((TabItemNew) child).removeViewTreeObserverListener();
//            }
        }
    };

    private TabPagerBindStrategy bindStrategy;

    private ArrayList<ScreenInfo> tabTitles;
    private Launcher mLauncher;
    private int mImportantPosition = -1;
    /**
     * current selected position
     */
    private int selectedPosition = -1;
    private String mLastSelectTag;
    private int tabPadding = 26;
    private int tabHeight = 50;
    private int tabTextSize = 12;
    private ColorStateList tabTextColor = ColorStateList.valueOf(0x8fffffff);
    private int selectedTabTextColor = 0x0fffffff;
    private Typeface tabTypeface = null;
    private int tabTypefaceStyle = Typeface.NORMAL;

    /**
     * the background of Live..
     */
    private View mImportanctBgView;

    /**
     * 上次滚动位置，用于在{@link #scrollToChild(int)}中判断滚动方向
     */
    private int lastScrollX = 0;

    private int tabBackgroundColor = getResources().getColor(android.R.color.transparent);

    private Paint rectPaint = new Paint();
//    private Paint dividerPaint = new Paint();

    private float currentPositionOffset = 0f;
    private int currentPosition;

    private RectF tempRect = new RectF();


    private final State mState = new State();
    private OnGlobalLayoutListener onGlobalLayoutListener;

    public TabStripImpl(Context context) {
        this(context, null);
    }

    public TabStripImpl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabStripImpl(Context context, AttributeSet attrs,
                        int defStyle) {
        super(context, attrs, defStyle);
        setClipChildren(false);
        setClipToPadding(false);
        setFillViewport(true);
        setWillNotDraw(false);// ensure onDraw() will be called
        setChildrenDrawingOrderEnabled(mHasOverlappingRendering);
        setSmoothScrollingEnabled(false);// always scroll without animation
        //set left and right edge to indicate that this line have more items
        setHorizontalFadingEdgeEnabled(true);
        setFadingEdgeLength(DEFAULT_FADING_EDGE_LENGTH);

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                removeOnLayoutChangeListener(this);
                LetvLog.d(TAG, "on layout changed");
                //the first time this view get's its layout  ...
                invokeGradient();
            }
        });

        tabTitles = new ArrayList<ScreenInfo>();

        DisplayMetrics dm = getResources().getDisplayMetrics();

        tabPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, tabPadding, dm);
        tabHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, tabHeight, dm);
        tabTextSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, tabTextSize, dm);

        // get system attrs (android:textSize and android:textColor)

        TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);

        tabTextSize = a.getDimensionPixelSize(0, tabTextSize);
        tabTextColor = a.getColorStateList(1);
        a.recycle();

        // get custom attrs
        a = context.obtainStyledAttributes(attrs,
                R.styleable.TabStripImpl);

        tabPadding = a.getDimensionPixelSize(
                R.styleable.TabStripImpl_pstsTabPaddingLeftRight,
                tabPadding);
        tabHeight = a.getDimensionPixelSize(
                R.styleable.TabStripImpl_pstsTabHeight,
                tabHeight);
        tabBackgroundColor = a.getColor(
                R.styleable.TabStripImpl_pstsTabBackground,
                tabBackgroundColor);
        a.recycle();

        defaultTabLayoutParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        onGlobalLayoutListener = new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver()
                        .removeGlobalOnLayoutListener(onGlobalLayoutListener);
                scrollToChild(selectedPosition);
                LetvLog.d(TAG, "selected Position onGlobalLayout :" + selectedPosition);
                if ((getRootView().findFocus() == null || getRootView().findFocus() == tabsContainer) && getTabItem(selectedPosition) != null) {
                    LetvLog.d(TAG, "tab request focus");
                    getTabItem(selectedPosition).requestFocus();
                }
            }
        };

        rectPaint.setColor(tabBackgroundColor);

        //init container that holds the tabItems
        tabsContainer = new LinearLayout(context);
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabsContainer.setGravity(Gravity.CENTER_VERTICAL);
        tabsContainer.setFocusable(true);
        tabsContainer.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        tabsContainer.setClipChildren(false);
        tabsContainer.setClipToPadding(false);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, tabHeight);
        lp.gravity = Gravity.BOTTOM;
        tabsContainer.setLayoutParams(lp);
        tabsContainer.setOnHierarchyChangeListener(mOnHierarchyChangeListener);
        addView(tabsContainer);
        //end init
    }

//    private OnShowImportanceListener mImportanceListener;

    /**
     * set bind strategy to this TabStrip.
     * then refresh the hole UI
     *
     * @param strategy
     */
    @Override
    public void setBindStrategy(TabPagerBindStrategy strategy) {
        if (checkNotNull(strategy)) {
            return;
        }
        this.bindStrategy = strategy;
        if (strategy.getCount() <= 0) {
            //nothing to show on this tab
            return;
        }
        notifyStrategyChanged();
    }

    /**
     * sync tabTitles with bindStrategy
     */
    private void updateTabTitles() {
        tabTitles.clear();
        for (int i = 0; i < bindStrategy.getCount(); i++) {
            if (bindStrategy.getPageTitle(i) instanceof ScreenInfo) {
                tabTitles.add(i, (ScreenInfo) bindStrategy.getPageTitle(i));
            }
        }
    }

    private boolean checkNotNull(Object o) {
        return o == null;
    }

    /**
     * notify that the pager's data set has been changed
     * TabStrip should update its UI;
     * //TODO optimize the reorder logic now this method cost to much
     */
    @Override
    public void notifyStrategyChanged() {
        LetvLog.d(TAG, "notifyStrategyChanged");

        if (checkNotNull(bindStrategy)) {
            throw new IllegalStateException(
                    "bindStrategy has not been set.");
        }
        updateTabTitles();
        tabsContainer.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        if (tabsContainer.hasFocus()) {
            tabsContainer.requestFocus();
        }
        removeAllTabs();
        int pagerCount = bindStrategy.getCount();
        // judge if add unfocus items
        if (false) {

        } else {
            for (int i = 0; i < pagerCount; i++) {
                addTextTab(i, tabTitles.get(i).getName(), tabTitles.get(i).getPackageName(), true, tabTitles.get(i).getIsNew());
                if (TextUtils.equals(mLastSelectTag, tabTitles.get(i).getPackageName())) {
                    selectedPosition = i;
                }
            }
        }


        mImportantPosition = -1;
        for (int i = 0; i < pagerCount; i++) {
            //LIVE position
            /*if (LauncherFragmentTagUtil.TAG_LIVE.equals(tabTitles.get(i).getPackageName())) {
                mImportantPosition = parsePager2TabPosition(i);
                break;
            }*/
        }
        //if current the tabs is less than tabCount set the last to be focused
        if (selectedPosition > getTabCount() && getTabCount() > 0) {
            mLastSelectTag = (String) getTabItem(getTabCount() - 1).getTag();
            selectedPosition = getTabCount() - 1;
        }
        tabsContainer.setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        updateTabStyles();
        getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
        getViewTreeObserver().addOnGlobalLayoutListener(
                onGlobalLayoutListener);
    }

    private void removeAllTabs() {
        View child;
        for (int i = 0; i < tabsContainer.getChildCount(); i++) {
            child = tabsContainer.getChildAt(i);
            if (child instanceof TabItemNew) {
                ((TabItemNew) child).removeViewTreeObserverListener();
            }
        }
        tabsContainer.removeAllViews();
    }

    @Override
    public void setTabText(String newText, int position) {
        if (!checkIndex(position)) {
            //index out of boundary
            return;
        }
        // not modify the data Source
        getTabItem(position).setText(newText);
        scrollToChild(selectedPosition);
    }

    @Override
    public void onScrollChanged(int position, float positionOffset, int positionOffsetPixels) {
        currentPosition = parsePager2TabPosition(position);
        currentPositionOffset = positionOffset;
        if (null == tabsContainer || tabsContainer.getChildCount() <= 0) {
            return;
        }
        scrollToChild(currentPosition, (int) (positionOffset * tabsContainer.getChildAt(currentPosition).getWidth()));
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getTabCount() == 0) {
            return;
        }

        final int height = getHeight();

        // draw indicator line

        rectPaint.setAntiAlias(true);

        // default: line below current tab
        TabItemNew currentTab = (TabItemNew) tabsContainer.getChildAt(currentPosition);
        if (currentTab == null) {
            return;
        }
        float lineLeft = currentTab.getLeft();
        float lineRight = currentTab.getRight();

        // if there is an offset, start interpolating left and right coordinates between current and next tab
        if (currentPositionOffset > 0f && currentPosition < getTabCount() - 1) {
            TabItemNew nextTab = (TabItemNew) tabsContainer.getChildAt(currentPosition + 1);
            final float nextTabLeft = nextTab.getLeft();
            final float nextTabRight = nextTab.getRight();
            lineLeft = (currentPositionOffset * nextTabLeft + (1f - currentPositionOffset) * lineLeft);
            lineRight = (currentPositionOffset * nextTabRight + (1f - currentPositionOffset) * lineRight);
            //TODO 粗细的渐变不好处理
//            nextTab.getTabTextView()
//                    .setTextColor(Utilities.blendARGB(tabTextColor.getDefaultColor(), tabTextColor.getColorForState(View.SELECTED_STATE_SET, 0), currentPositionOffset));
//            currentTab.getTabTextView()
//                    .setTextColor(Utilities.blendARGB(tabTextColor.getColorForState(View.SELECTED_STATE_SET, 0), tabTextColor.getDefaultColor(), currentPositionOffset));
        }

        tempRect.left = lineLeft;
        tempRect.top = height - tabsContainer.getHeight() + (tabsContainer.getHeight() - currentTab.getHeight()) / 2;
        tempRect.right = lineRight;
        tempRect.bottom = height - (tabsContainer.getHeight() - currentTab.getHeight()) / 2;
        canvas.drawRoundRect(tempRect, currentTab.getHeight() / 2, currentTab.getHeight() / 2, rectPaint);
    }

    /**
     * add a {@link TabItemNew} with {@link AlphaGradientTextView} in it to this TabStrip
     *
     * @param position if position <0 will add item to the end of this strip
     * @param title
     * @param tag
     * @param isActive
     * @param isNew
     */
    public void addTextTab(final int position, String title, String tag, boolean isActive, boolean isNew) {
        TabItemNew tabItem = new TabItemNew(getContext());
        tabItem.setTag(tag);
        AlphaGradientTextView tabText = tabItem.getTabTextView();
        tabText.setText(title);
        tabText.setGravity(Gravity.CENTER);
        tabText.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
        addTab(position, tabItem, isActive, isNew);

        //TODO add Advertise
//        SmartAdBean adBean = DataModel.getInstance().getTabAdvData(tag);
//        if (null != adBean) {
//            tabItem.addAdvertisingView(adBean.img_url, adBean.begin_time, adBean.end_time, tag);
//        }
    }

    /**
     * when focus change within this scroll view, this method will be called twice in a line :
     * onChildFocusChanged(false) -> onChildFocusChanged(true)<br/>
     * <p/>
     * when the focus went <b>outside</b> this view group : onChildFocusChanged(false)<br/>
     * <p/>
     * when the focus went <b>inside</b> this view group : onChildFocusChanged(true)<br/>
     *
     * @param hasFocus
     */
    private void onChildFocusChanged(boolean hasFocus) {
        setTabBackgroundVisible(hasFocus);
    }

    private void setTabBackgroundVisible(boolean visible) {
        rectPaint.setColor(visible ? tabBackgroundColor : getResources().getColor(android.R.color.transparent));
        invalidate();
    }

    /**
     * add tab to this TabStrip and bind it with ViewPager
     *
     * @param position if position <0 will add item to the end of this strip
     * @param tab      item to be add to this component :must be a child of {@link TabItemNew}
     */
    @Override
    public <T extends TabItemNew> void addTab(final int position, T tab, boolean isActive, boolean isNew) {
        tab.setFocusable(true);
        tab.setFocusableInTouchMode(true);
        tab.setTextColor(tabTextColor);
        tab.setOnClickListener(mItemOnClickListener);
        tab.setOnKeyListener(mItemOnKeyListener);

        if (isActive) {
            tab.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    onChildFocusChanged(hasFocus);
                    if (v instanceof TabItemNew) {
                        TabItemNew item = ((TabItemNew) v);
                        if (hasFocus) {
                            item.getTabTextView().getPaint().setFakeBoldText(true);
                            int numTabs = getTabCount();
                            //
                            for (int i = 0; i < numTabs; i++) {
                                if (getTabItem(i) == v) {
                                    mLastSelectTag = (String) v.getTag();
                                    if (selectedPosition != i) {
                                        selectedPosition = i;
                                        if (mOnTabChangeListener != null) {
                                            Object o = v.getTag(R.id.switch_immediately);
                                            final boolean immediately = o != null ? (Boolean) o : false;
                                            v.setTag(R.id.switch_immediately, false);
                                            mOnTabChangeListener.onTabChanged((String) v.getTag(), immediately);
                                        }
                                    }
                                }
                            }
                            bindStrategy.setPagerCurrentItem(position);

                            updateTabStyles();
                        } else {
                            //Fix by xubin@le.com
                            //避免该tag得不到重置
                            v.setTag(R.id.switch_immediately, false);
                        }
                        if (mOnTabItemFocusChangeListener != null) {
                            mOnTabItemFocusChangeListener.onFocusChange(v, hasFocus, position);
                        }
                    }
                }
            });
        } else {
            tab.setFocusable(false);
            tab.setFocusableInTouchMode(false);
        }

        if (isNew) {
            tab.getTabTextView().showColorPoint();
        }

        tabsContainer.addView(tab, position, defaultTabLayoutParams);
    }

    /**
     * return the tabItem at specific position as a {@link View}
     *
     * @param position TabPosition
     * @return TabItemNew at the position or null if not found;
     */
    @Override
    public <T extends TabItemNew> T getTabItem(int position) {
        if (!checkIndex(position)) {
            return null;
        }
        //noinspection unchecked
        return (T) tabsContainer.getChildAt(position);
    }

    /**
     * @return number of currently showing items
     */
    @Override
    public int getTabCount() {
        return tabsContainer == null ? 0 : tabsContainer.getChildCount();
    }

    /**
     * @return true if the position is legal
     */
    private boolean checkIndex(int position) {
        return position >= 0 && position < getTabCount();
    }

    /**
     * traverse and update all TabItemNew's style based on current selected position
     */
    private void updateTabStyles() {
//        LetvLog.d(TAG, "updateTabStyles");

        for (int i = 0; i < getTabCount(); i++) {

            View tab = getTabItem(i);

//            tab.setBackgroundResource(tabBackgroundColor);
//            tab.setPadding(tabPadding, 0, tabPadding, 0);

            AlphaGradientTextView tabTextView = ((TabItemNew) tab).getTabTextView();
            tabTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize);
            tabTextView.setTypeface(tabTypeface, tabTypefaceStyle);
            tab.setSelected(false);

            //current viewpager's selected position. should be marked with selectedTabTextColor NOT focus color
            if (i == selectedPosition) {
                tab.setSelected(true);
                if (isInTouchMode()) {
                    tab.requestFocus();
                }
            }
        }
    }

    /**
     * parse the position(witch is currently scrolling to) to direction{@link #FOCUS_RIGHT} or {@link #FOCUS_LEFT}
     *
     * @param position the position we scrolling towards
     * @return direction relative to current focused item
     */
//    private int parsePositionToDirection(int position) {
//        if (checkIndex(position)) {
//            return -1;
//        }
//        int newScrollX = getTabItem(position).getLeft();
//        LetvLog.d(TAG, "newScrollX : " + newScrollX + " lastScrollX : " + lastScrollX);
//        int direction = -1;
//        if (newScrollX > lastScrollX) {
//            direction = View.FOCUS_RIGHT;
//        } else if (newScrollX < lastScrollX) {
//            direction = View.FOCUS_LEFT;
//        } else {
//            LetvLog.d(TAG, "no need to scroll");
//        }
//
//        if (newScrollX != lastScrollX) {
//            lastScrollX = newScrollX;
//        }
//        return direction;
//    }
    public void scrollToChild(int position, int offset) {
        if (getTabCount() == 0) {
            return;
        }

        int newScrollX = tabsContainer.getChildAt(position).getLeft() + offset;

        if (position > 0 || offset > 0) {
            newScrollX -= getWidth() / 2;
        }

        if (newScrollX != lastScrollX) {
            lastScrollX = newScrollX;
            scrollTo(newScrollX, 0);
        }

    }

    /**
     * control the scroll.
     * <br/>
     * the logic is copied from {@link HorizontalScrollView#arrowScroll(int)}
     *
     * @param position tabPosition
     */
    @Override
    public void scrollToChild(int position) {
        //按照父类 arrowScroll的方法，滚动ScrollView
        if (getTabCount() == 0 || !checkIndex(position)) {
            return;
        }
        //Always trigger scroll. because the selectionPosition may be out of this view.
        mLastSelectTag = (String) getTabItem(position).getTag();
        selectedPosition = position;

        scrollToChild(position, 0);
        updateTabStyles();
        invokeGradient();
        bindStrategy.setPagerCurrentItem(selectedPosition);

        //TODO showImportance should be called by outer class..for now  put it here
//        if (selectedPosition >= 0 && selectedPosition == mImportantPosition) {
//            showImportance(mImportantPosition);
//        } else {
//            hideImportance();
//        }
//        View nextFocused = getTabItem(position);
//        final Rect mTempRect = new Rect();
////        final int maxJump = getMaxScrollAmount();
//        //in this mode the maxJump is the hole scrollView' length
//        //so that the pager's setCurrentItem(int item) can work well;
//        final int maxJump = getScrollX();
//        int direction = parsePositionToDirection(position);
//
//        if (nextFocused != null && isWithinDeltaOfScreen(nextFocused, maxJump)) {
//            nextFocused.getDrawingRect(mTempRect);
//            offsetDescendantRectToMyCoords(nextFocused, mTempRect);
//            int scrollDelta = computeScrollDeltaToGetChildRectOnScreen(mTempRect);
//            scrollBy(scrollDelta, 0);
//        } else {
//            // no new focus
//            int scrollDelta = maxJump;
//
//            if (direction == View.FOCUS_LEFT && getScrollX() < scrollDelta) {
//                scrollDelta = getScrollX();
//            } else if (direction == View.FOCUS_RIGHT && getChildCount() > 0) {
//
//                int daRight = getChildAt(0).getRight();
//
//                int screenRight = getScrollX() + getWidth();
//
//                if (daRight - screenRight < maxJump) {
//                    scrollDelta = daRight - screenRight;
//                }
//            }
//            if (scrollDelta == 0) {
//                return;
//            }
//            //noinspection ResourceType
//            scrollBy(direction == View.FOCUS_RIGHT ? scrollDelta : -scrollDelta, 0);
//        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    // help to show the hole tab of SIGNAL
                    View nextFocused = FocusFinder.getInstance().findNextFocus(this, findFocus(), FOCUS_LEFT);
                    if (nextFocused == null) {
                        arrowScroll(FOCUS_LEFT);
                        return false; //return false so that the launcher will handle this event and scroll to SignalDesktop
                    }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * @return whether the descendant of this scroll view is within delta
     * pixels of being on the screen.
     */
    private boolean isWithinDeltaOfScreen(View descendant, int delta) {
        Rect mTempRect = new Rect();
        descendant.getDrawingRect(mTempRect);
        offsetDescendantRectToMyCoords(descendant, mTempRect);

        return (mTempRect.right + delta) >= getScrollX()
                && (mTempRect.left - delta) <= (getScrollX() + getWidth());
    }

//    public void setImportanceListener(OnShowImportanceListener listener) {
//        this.mImportanceListener = listener;
//    }

    /**
     * 显示Live Tab的背景
     *
     * @param width
     * @param leftMargin
     */
//    private void showImportanctBg(int width, int leftMargin) {
//        if (mImportanctBgView == null) {
//            mImportanctBgView = new View(getContext());
//            mImportanctBgView.setBackgroundResource(R.drawable.bg_home_sys_videobar);
//            ((ViewGroup) getParent()).addView(mImportanctBgView, 0);
//        }
//        RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) mImportanctBgView.getLayoutParams();
//        int height = getResources().getDimensionPixelSize(R.dimen.activity_launcher_tab_strip_height);
//        if (params == null) {
//            params = new RelativeLayout.LayoutParams(width, height);
//        }
//        params.width = width;
//        params.height = height;
//        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        params.leftMargin = leftMargin;
//        mImportanctBgView.setLayoutParams(params);
//        mImportanctBgView.setVisibility(View.VISIBLE);
//    }

    /**
     * 隐藏 Live Tab的背景
     */
//    private void hideImportanctBg() {
//        if (mImportanctBgView != null) {
//            mImportanctBgView.setVisibility(View.GONE);
//        }
//    }

    /**
     * show a special effect at a specific position
     */
//    public void showImportance(int position) {
//        if (!checkIndex(position)) {
//            return;
//        }
//        mState.currentState = LAYOUT_STATE_IMPORTANCE;
//        mState.mCurrentImprotancePosition = position;
//
//        if (position >= 0 && position < getTabCount()) {
//            int leftPoint = position - 1;
//            int rightPoint = position + 1;
//            int numTabs = getTabCount();
//
//            int bgWidth = 0;
//            int bgLeftMargin = 0;
//            for (int i = 0; i < numTabs; i++) {
//                View target = getTabItem(i);
//                target.setAlpha(1.0f);//for those already out of boundary
//                if (leftPoint == i || rightPoint == i) {
//                    if (target instanceof TabItemNew) {
//                        ((TabItemNew) target).getTabTextView().setGraditentDirection(leftPoint == i
//                                ? AlphaGradientTextView.RIGHT2LEFT
//                                : AlphaGradientTextView.LEFT2RIGHT);
//                        ((TabItemNew) target).getTabTextView().showGradient();
//                    } else {
//                        target.setAlpha(0.4f);
//                    }
//                    bgWidth += target.getWidth();
//                    if (leftPoint == i) {
//                        int[] location = new int[2];
//                        target.getLocationOnScreen(location);
//                        bgLeftMargin = location[0];
//                    }
//                } else if (i != position) {
//                    target.setAlpha(0);
//                }
//            }
//
//            // background for importance position
//            bgWidth += getTabItem(position).getWidth();
//            int contentMarginLeft = LauncherState.getInstance().getDynamicGrid(getContext()).getDeviceProfile().tabspace_layout_padding_horizontal_leftedge;
//            showImportanctBg(bgWidth, bgLeftMargin - contentMarginLeft);
//            if (mImportanceListener != null) {
//                mImportanceListener.onImportanceChanged(true);
//            }
//        }
//    }

    /***
     * return to normal state everyItem return to initial state
     */
//    public void hideImportance() {
//        if (LAYOUT_STATE_NORMAL == mState.currentState) {
//            return;
//        }
//        mState.currentState = LAYOUT_STATE_NORMAL;
//        mState.mCurrentImprotancePosition = -1;
//        int numTabs = getTabCount();
//        for (int i = 0; i < numTabs; i++) {
//            View target = getTabItem(i);
//            target.setAlpha(1.0f);
//            if (target instanceof TabItemNew) {
//                ((TabItemNew) target).getTabTextView().hideGradient();
//            }
//        }
//        hideImportanctBg();
//        if (mImportanceListener != null) {
//            mImportanceListener.onImportanceChanged(false);
//        }
//        invokeGradient();
//    }

    /**
     *
     */
//    public void resetImportanctShow() {
//        if (selectedPosition >= 0 && selectedPosition == mImportantPosition) {
//            showImportance(mImportantPosition);
//        } else {
//            hideImportance();
//        }
//    }

    /**
     * @return
     */
    public String getLastSelectTag() {
        return mLastSelectTag;
    }

    public void setTextSize(int textSizePx) {
        this.tabTextSize = textSizePx;
        updateTabStyles();
    }

    public int getTextSize() {
        return tabTextSize;
    }

    public void setTextColor(int textColor) {
        this.tabTextColor = ColorStateList.valueOf(textColor);
        updateTabStyles();
    }

    public void setTextColorResource(int resId) {
        this.tabTextColor = ColorStateList.valueOf(getResources().getColor(resId));
        updateTabStyles();
    }

    public ColorStateList getTextColor() {
        return tabTextColor;
    }

    public void setSelectedTextColor(int textColor) {
        this.selectedTabTextColor = textColor;
        updateTabStyles();
    }

    public void setSelectedTextColorResource(int resId) {
        this.selectedTabTextColor = getResources().getColor(resId);
        updateTabStyles();
    }

    public int getSelectedTextColor() {
        return selectedTabTextColor;
    }

    public void setTypeface(Typeface typeface, int style) {
        this.tabTypeface = typeface;
        this.tabTypefaceStyle = style;
        updateTabStyles();
    }

    public void setTabBackground(int color) {
        this.tabBackgroundColor = color;
        updateTabStyles();
    }

    /**
     * set the tab's Item at index to be focused.
     *
     * @param index       TabIndex...<b color = "red">NOT</b> ViewPager Position
     * @param immediately whether the pager should perform scroll animation
     */
    public void setCurrentTab(int index, boolean immediately) {
        if (!checkIndex(index)) {
            return;
        }

        getTabItem(index).setTag(R.id.switch_immediately, immediately);
        getTabItem(index).requestFocus();
    }


    /**
     * @param fadingEdgeLength the length to show fading at both sides
     */
    public void setFadingEdgeLength(int fadingEdgeLength) {
        super.setFadingEdgeLength(fadingEdgeLength);
        invalidate();
    }

    /**
     * @return current selected position
     */
    public int getSelectedPosition() {
        return selectedPosition;
    }

    /**
     * @param position
     * @return
     */
    public static int parseTab2PagerPosition(int position) {
        return position;
    }

    /**
     * @param position
     * @return
     */
    public static int parsePager2TabPosition(int position) {
        return position;
    }

    public int getTabBackground() {
        return tabBackgroundColor;
    }

    public void setTabPaddingLeftRight(int paddingPx) {
        this.tabPadding = paddingPx;
        updateTabStyles();
    }

    public int getTabPaddingLeftRight() {
        return tabPadding;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        selectedPosition = savedState.selectedPosition;
        mLastSelectTag = savedState.lastSelectTag;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.selectedPosition = selectedPosition;
        savedState.lastSelectTag = mLastSelectTag;
        return savedState;
    }

    public void setOnTabChangeListener(TabSpace.OnTabChangedListener mOnTabChangeListener) {
        this.mOnTabChangeListener = mOnTabChangeListener;
    }

    /**
     * @return The live position (TabPosition)
     */
    public int getImportantPosition() {
        return mImportantPosition;
    }

    public void setLauncher(Launcher mLauncher) {
        this.mLauncher = mLauncher;
    }

    /**
     * mainly save the current selected position when this activity has been brought to backward
     */
    static class SavedState extends BaseSavedState {
        int selectedPosition;
        String lastSelectTag;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            selectedPosition = in.readInt();
            lastSelectTag = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(selectedPosition);
            dest.writeString(lastSelectTag);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    /**
     * control the view's alpha when scroll out of the window
     */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        // in some condition we need to maintain is state ...Like we are focusing in Live tab;
        switch (mState.currentState) {
            case LAYOUT_STATE_IMPORTANCE:
                break;
            default:
                //  the API has handled this ...keep this to..
//                //normal Mode
//                for (int i = 0; i < tabCount; i++) {
//                    AlphaGradientTextView v = (AlphaGradientTextView) tabsContainer.getChildAt(i);
//                    int left = v.getLeft();
//                    int right = v.getRight();
//                    if (left < l) {
//                        //items on the left
//                        v.setGraditentDirection(AlphaGradientTextView.RIGHT2LEFT);
//                        v.showGradient(50, l - left - v.getPaddingLeft());
//                    } else if (right > l + getWidth()) {
//                        //items on the right
//                        v.setGraditentDirection(AlphaGradientTextView.LEFT2RIGHT);
//                        v.showGradient(50, l + getWidth() - left - v.getPaddingRight());
////                        v.setAlpha(alpha);
//                    } else {
//                        //items can be seen
//                        v.hideGradient();
//                        v.setAlpha(1.0f);
//                    }
//                }
                break;
        }
    }

    /**
     * trigger tab item's Gradient
     * <p/>
     * trigger the onScrollChanged Method to make sure text color changed<br/>
     * cause setTextColor can't change the shader color fo text
     */
    private void invokeGradient() {
        onScrollChanged(getScrollX(), getScrollY(), getScrollX(), getScrollY());
    }

    @Override
    public boolean hasOverlappingRendering() {
        //TODO ....
        return false;
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (mHasOverlappingRendering) {
            // draw the selected tab last
            if (selectedPosition == -1) {
                return i;
            } else {
                if (i == childCount - 1) {
                    return selectedPosition;
                } else if (i >= selectedPosition) {
                    return i + 1;
                } else {
                    return i;
                }
            }
        } else {
            return super.getChildDrawingOrder(childCount, i);
        }
    }

    private OnKeyListener mItemOnKeyListener = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_MENU) {
                BaseFragment current = mLauncher.getAdapterPresenter().getCurrentFragment();
                if (current != null && current.getContainer() != null) {
                    LetvLog.d("TabStriplmpl", "Perform menu key event: " + current.tag);
                    KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU);
                    current.getContainer().dispatchKeyEvent(keyEvent);
                    return true;
                }
            }
            return false;
        }
    };

    private OnClickListener mItemOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getTag() == null) {
                return;
            }

            TabItemNew item = getTabItem(getSelectedPosition());
            if (item == null || item.getTag() == null) {
                return;
            }

            if (item.getTag().equals(v.getTag())) {
                BaseFragment current = mLauncher.getAdapterPresenter().getCurrentFragment();
                if (current != null) {
                    current.onActivityAction(ActivityActionHandler.ACTIVITY_ACTION_DESKTOP_TAB_ONCLICK, null);
                }
            }
        }
    };

    /**
     * store current TabStrip State..
     * <p/>
     * TODO
     */
    public static class State {
        private int currentState = LAYOUT_STATE_NORMAL;
        private int mCurrentImprotancePosition = -1;
    }

//    interface OnShowImportanceListener {
//        void onImportanceChanged(boolean hasShow);
//    }

    public void setOnTabItemFocusChangeListener(OnTabItemFocusChangeListener listener) {
        mOnTabItemFocusChangeListener = listener;
    }

    /**
     * when tabItem get focused this method will be called
     * <p>
     * if user want to do something when TabItemNew get focused. <b color="red">DO NOT<b/> set {@link OnFocusChangeListener} on TabItemNew
     * this will cause Chaos in this component;
     * <br>
     * set {@link #setOnTabItemFocusChangeListener(OnTabItemFocusChangeListener)} instead.
     */
    public interface OnTabItemFocusChangeListener {
        void onFocusChange(View v, boolean hasFocus, int position);
    }
}
