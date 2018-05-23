package com.stv.plugin.demo.widget;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.stv.plugin.demo.DemoApplication;
import com.stv.plugin.demo.data.common.PosterHolder;
import com.stv.plugin.demo.widget.adapter.ViewAdapter;
import com.xstv.base.BaseFragment;
import com.xstv.base.FragmentActionHandler;
import com.xstv.base.Logger;
import com.xstv.desktop.R;

public class RootLayoutContainer extends RelativeLayout {

    private Logger mLogger = Logger.getLogger(DemoApplication.PLUGINTAG, "RootLayoutContainer");

    RelativeLayout mLeftPanelView;
    HeaderVideoContainer mVideoContainer;
    RecyclerView mRecyclerView;
    ImageView mProgressView;
    TextView mRefreshTimeLessTv;
    ViewAdapter mAdapter;
    RecyclerViewLayoutManager mLayoutManager;
    FocusFinder mFocusFinder;
    BaseFragment mFragment;

    FocusProcessTextView mStopPlay;

    public RootLayoutContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFocusFinder = FocusFinder.getInstance();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLeftPanelView = (RelativeLayout) findViewById(R.id.left_panel);
        mVideoContainer = (HeaderVideoContainer) findViewById(R.id.header);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mProgressView = (ImageView) findViewById(R.id.progressview);
        mRefreshTimeLessTv = (TextView) findViewById(R.id.timeless);

        /**
         * Temp debug
         */
        mStopPlay = (FocusProcessTextView) findViewById(R.id.menu6);
        mStopPlay.setClickable(true);
        mStopPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoContainer.isPlaying()) {
                    mVideoContainer.requestVideoRelease();
                } else {
                    mVideoContainer.requestVideoPlay();
                }
            }
        });
        //============================================================


        /**
         * init recycler view
         */
        mLayoutManager = new RecyclerViewLayoutManager(getContext(), 3);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ViewAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(mItemClickListener);

        ((AnimationDrawable) mProgressView.getBackground()).start();
    }

    ViewAdapter.OnItemClickListener mItemClickListener = new ViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            Toast.makeText(getContext(), "click " + position, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event) || handleFocus(event);
    }

    private boolean handleFocus(KeyEvent event) {
        boolean handled = false;
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            View nextFocus = null;
            View currentFocus = findFocus();
            if (currentFocus == this) {
                currentFocus = null;
            }
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    nextFocus = mFocusFinder.findNextFocus(this, currentFocus, FOCUS_LEFT);
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    nextFocus = mFocusFinder.findNextFocus(this, currentFocus, FOCUS_UP);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    nextFocus = mFocusFinder.findNextFocus(this, currentFocus, FOCUS_RIGHT);
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    nextFocus = mFocusFinder.findNextFocus(this, currentFocus, FOCUS_DOWN);
                    break;
                case KeyEvent.KEYCODE_BACK:
                case KeyEvent.KEYCODE_ESCAPE:
                    /**
                     * TODO
                     * 交互设定，在内容区按Back键，需要内容回滚到顶部，
                     * 并通知框架让焦点回到对应的Tab上。
                     */
                    mRecyclerView.scrollToPosition(0);
                    if (mFragment != null) {
                        mFragment.mFragmentHandler.onFragmentAction(mFragment,
                                FragmentActionHandler.FRAGMENT_ACTION_BACK_KEY, null);
                        handled = true;
                    }
                    break;
            }

            if (!handled) {
                if (nextFocus != null) {
                    handled = nextFocus.requestFocus();
                } else {
                    /**
                     * 若查找不到下一个焦点，并且RecyclerView 正在滚动，消耗掉所有按键
                     */
                    handled = mRecyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE;
                }
            }
        }
        return handled;
    }

    public void bindFragment(BaseFragment fragment) {
        mFragment = fragment;
    }

    public boolean onFocusRequested(int direction) {
        boolean handled = false;
        switch (direction) {
            /**
             * 焦点从左侧切进来
             */
            case BaseFragment.FOCUS_LEFT_IN:
                handled = mLeftPanelView.getChildAt(1).requestFocus();
                break;
            /**
             * 焦点从顶部Tab切下来
             */
            case BaseFragment.FOCUS_TOP_IN:
                if (mRecyclerView.getChildCount() > 0) {
                    handled = mRecyclerView.getChildAt(0).requestFocus();
                } else {
                    handled = mLeftPanelView.getChildAt(1).requestFocus();
                }
                break;
            /**
             * 焦点从右侧切进来
             */
            case BaseFragment.FOCUS_RIGHT_IN:
                if (mRecyclerView.getChildCount() > 0) {
                    handled = mRecyclerView.getChildAt(2).requestFocus();
                } else {
                    handled = mLeftPanelView.getChildAt(1).requestFocus();
                }
                break;
        }
        return handled;
    }

    public void onLayoutSeletedPre(boolean gainSelect) {
        if (!gainSelect) {
            mVideoContainer.requestVideoRelease();
        }
    }

    public void onLayoutShowChanged(boolean gainShow) {
        if (gainShow) {
            mVideoContainer.requestVideoPlay();
        } else {
            mRefreshTimeLessTv.setVisibility(View.GONE);
        }
    }

    public void bindData(PosterHolder posterHolder) {
        mLogger.d(">>> building UI <<<");


        mAdapter.bindData(posterHolder.data);

        mLeftPanelView.setVisibility(View.VISIBLE);
        mVideoContainer.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);

        mProgressView.setBackground(null);
        mProgressView.setVisibility(View.GONE);
    }

    public void updateRefreshTimeLess(int total, int time) {
        if (mRefreshTimeLessTv != null) {
            mRefreshTimeLessTv.setText(total + "->" + time);
            mRefreshTimeLessTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mLogger.d("onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mLogger.d("onDetachedFromWindow");
    }

    public void clearAllChildBitmap() {
    }
}
