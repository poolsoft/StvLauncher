
package com.xstv.launcher.ui.activity;

import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.ArrayMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xstv.launcher.R;
import com.xstv.launcher.logic.controller.IUICallback;
import com.xstv.launcher.logic.manager.DataModel;
import com.xstv.launcher.provider.db.ScreenInfo;
import com.xstv.launcher.ui.presenter.LauncherAdapterPresenter;
import com.xstv.launcher.ui.widget.SmartLoadingBar;
import com.xstv.launcher.ui.widget.SwitchIndicator;
import com.xstv.launcher.ui.widget.TabSpace;
import com.xstv.launcher.ui.widget.ViewPagerSpace;
import com.xstv.library.base.ActivityActionHandler;
import com.xstv.library.base.BaseFragment;
import com.xstv.library.base.FragmentActionHandler;
import com.xstv.library.base.LetvLog;

import java.util.List;


public class Launcher extends FragmentActivity implements FragmentActionHandler, IUICallback {

    public static final String TAG = "LauncherActivity";
    public static Launcher INSTANCE;


    private DataModel mDataModel;

    private TextView mDebugDisplayMemTv;
    private TextView mDebugDisplayPluginTv;
    private View mTitleView;
    private TabSpace mTabSpace;
    private ViewPagerSpace mViewPagerSpace;
    private LauncherAdapterPresenter mAdapterPresenter;
    private ActivityActionHandler mActivityHandler;
    private LocalBroadcastManager mLocalBroadcastManager;

    private boolean mBlockKeyEvent = true;
    private Animation mTabHideAnimation;
    private Animation mTabShowAnimation;

    private final int MESSAGE_CHECK_HANDDETECT = 1;
    private final int MESSAGE_HIDE_TABTITLE = MESSAGE_CHECK_HANDDETECT + 1;
    private final int MESSAGE_REFRESH_MEMORY_STATUS_DISPLAY = MESSAGE_HIDE_TABTITLE + 1;
    private final int MESSAGE_CHECK_IS_TOP_ACTIVITY = MESSAGE_REFRESH_MEMORY_STATUS_DISPLAY + 1;
    private final int MESSAGE_REFRESH_PLUGIN_INFO_DISPLAY = MESSAGE_CHECK_IS_TOP_ACTIVITY + 1;
    private final int MESSAGE_CHECK_GUIDE_VIEW_DISPLAY = MESSAGE_REFRESH_PLUGIN_INFO_DISPLAY + 1;
    private final int MEESAGE_AUDIO_HIDE_EDITING_LOAING = MESSAGE_CHECK_GUIDE_VIEW_DISPLAY + 1;
    private final int MEESAGE_SET_STORAGE = MEESAGE_AUDIO_HIDE_EDITING_LOAING + 1;
    private final int MESSAGE_FPS_CHECK_TIMEOUT = MEESAGE_SET_STORAGE + 1;
    private final int MESSAGE_DELAY_HIDE_CIBN_VERIFY_LOADING = MESSAGE_FPS_CHECK_TIMEOUT + 1;//TODO 没有地方发出这个消息，什么时候去掉的?
    private final int MESSAGE_GUIDE_VIEW_DISPLAY_TIMEOUT = MESSAGE_DELAY_HIDE_CIBN_VERIFY_LOADING + 1;

    private ViewGroup mRootView;
    private String mLastSelectedScreen;
    private boolean mBootSignalFirst = false;
    private boolean mScreenLessOptionShow;
    private boolean mHasFirstPluginShown;
    private boolean mInEditing;
    private SmartLoadingBar mSmartLoadingBar;
    private SwitchIndicator mSwitchIndicator;
    private boolean mHasOnCreateNecessaryDone;
    private boolean mSignNotNeedRootVisible;

    private ArrayMap<String, Integer> mShowRedDotMap = new ArrayMap<String, Integer>(3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /** 处理Fragment中包含surfaceview时，会闪黑屏的问题 */
        getWindow().setFormat(PixelFormat.TRANSLUCENT);//主题中设置
        super.onCreate(savedInstanceState);
        LetvLog.i(TAG, "onCreate");

        INSTANCE = this;

        /** set layout to decorView */
        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        decorView.removeAllViews();
        LayoutInflater.from(this).inflate(R.layout.activity_launcher, decorView);
        getWindow().setBackgroundDrawable(null);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        mDataModel = DataModel.getInstance();

        /** set up loading view. */
        deploySmartLoadingBar();

        /** init views */
        setupViews();

        mScreenLessOptionShow = false;

        doOnCreateNecessary();
    }

    /**
     * It must be initialized only once when launcher onCreate.
     */
    private void doOnCreateNecessary() {
        LetvLog.i(TAG, "doOnCreateNecessary, mHasOnCreateNecessaryDone=" + mHasOnCreateNecessaryDone);
        mHasOnCreateNecessaryDone = true;
        mHandler.sendEmptyMessageDelayed(MESSAGE_GUIDE_VIEW_DISPLAY_TIMEOUT, 60000);
        mHandler.sendEmptyMessageDelayed(MESSAGE_FPS_CHECK_TIMEOUT, 60000);

        //FIXME
        mSmartLoadingBar.calculateRealVisibleTime();

        // request data from DataModel
        mDataModel.setCallback(this);
    }

    private void deploySmartLoadingBar() {
        mSmartLoadingBar = new SmartLoadingBar((ViewGroup) getWindow().getDecorView(), new SmartLoadingBar.OnHideListener() {

            @Override
            public void onHide(boolean isCibnCertificationLoading) {
                LetvLog.i(TAG, " SmartLoadingBar onHide ");
                if (!mSignNotNeedRootVisible && mRootView != null) {
                    mRootView.setVisibility(View.VISIBLE);
                }
                mSignNotNeedRootVisible = false;
            }
        });

        mSmartLoadingBar.setMinRealVisibleTime(3000);
        mSmartLoadingBar.setMaxRealVisibleTime(8000);
        //FIXME-xubin mSmartLoadingBar.showDefault();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void setupViews() {
        mRootView = (ViewGroup) findViewById(R.id.activity_root);
        mTabSpace = (TabSpace) findViewById(R.id.tabspace);
        mViewPagerSpace = (ViewPagerSpace) findViewById(R.id.metro_space);
        mTitleView = (View) findViewById(R.id.status_bar);
        mDebugDisplayMemTv = (TextView) findViewById(R.id.mem_stats_display);
        mDebugDisplayPluginTv = (TextView) findViewById(R.id.plugin_info_display);
        mSwitchIndicator = new SwitchIndicator(mRootView, mViewPagerSpace);

        //FIXME-xubin mTitleView.setLauncher(this);
        mTabSpace.setLauncher(this);
        mViewPagerSpace.setLauncher(this);
        mViewPagerSpace.setLauncherRootView((FrameLayout) mRootView);
        mAdapterPresenter = new LauncherAdapterPresenter(this, mTabSpace, mViewPagerSpace);
        //FIXME-xubin mTabSpace.setOnTabStatusListenter(mTitleView);
        //FIXME-xubin mAdapterPresenter.addScreenSwitchedListener(mTitleView);
        mAdapterPresenter.addScreenSwitchedListener(mViewPagerSpace);
        mAdapterPresenter.addScreenSwitchedListener(mSwitchIndicator);
    }

    public LauncherAdapterPresenter getAdapterPresenter() {
        return mAdapterPresenter;
    }

    public ViewPagerSpace getViewPagerSpace() {
        return mViewPagerSpace;
    }

    public TabSpace getTabSpace() {
        return mTabSpace;
    }

    private final Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (isFinishing()) {
                return;
            }
            switch (msg.what) {
                case MESSAGE_REFRESH_MEMORY_STATUS_DISPLAY:
                    if (mDebugDisplayMemTv.getVisibility() == View.VISIBLE) {
                        mDebugDisplayMemTv.setText("null");
                        sendEmptyMessageDelayed(MESSAGE_REFRESH_MEMORY_STATUS_DISPLAY, 2000);
                    }
                    break;
                case MESSAGE_REFRESH_PLUGIN_INFO_DISPLAY:
                    if (mDebugDisplayPluginTv.getVisibility() == View.VISIBLE) {
                        mDebugDisplayPluginTv.setText("null");
                        sendEmptyMessageDelayed(MESSAGE_REFRESH_PLUGIN_INFO_DISPLAY, 20000);
                    }
                    break;
                case MESSAGE_CHECK_GUIDE_VIEW_DISPLAY:
                    mSmartLoadingBar.hideByPreset();
                    break;
                case MEESAGE_AUDIO_HIDE_EDITING_LOAING:
                    mSmartLoadingBar.hideForcibly();
                    break;
                case MESSAGE_GUIDE_VIEW_DISPLAY_TIMEOUT:
                    if (mSmartLoadingBar != null) {
                        mSmartLoadingBar.hideForcibly();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private boolean isUIReady() {
        return mAdapterPresenter != null && mAdapterPresenter.hasBindScreen();
    }


    @Override
    protected void onDestroy() {
        // mDataModel.stopLoader();// <-- Avoid frequent start loader
        super.onDestroy();// <-- will destroy all fragments and loaders.
        LetvLog.i(TAG, "onDestroy");

        mHandler.removeMessages(MESSAGE_CHECK_HANDDETECT);
        mHandler.removeMessages(MESSAGE_HIDE_TABTITLE);
        mHandler.removeMessages(MESSAGE_REFRESH_MEMORY_STATUS_DISPLAY);
        mHandler.removeMessages(MESSAGE_CHECK_GUIDE_VIEW_DISPLAY);
        mHandler.removeMessages(MEESAGE_AUDIO_HIDE_EDITING_LOAING);
        mHandler.removeMessages(MESSAGE_GUIDE_VIEW_DISPLAY_TIMEOUT);
        mAdapterPresenter.destory();

        mRootView.setBackgroundDrawable(null);
        mRootView.removeAllViews();

        /** release memory when change language */
        mTitleView = null;
        mTabSpace = null;
        mViewPagerSpace = null;
        mDebugDisplayMemTv = null;
        mDebugDisplayPluginTv = null;
        mRootView = null;
        mSmartLoadingBar = null;
        mSwitchIndicator = null;

        // mDataModel = null;
        mAdapterPresenter = null;
        mActivityHandler = null;
        mTabHideAnimation = null;
        mTabShowAnimation = null;
    }

    @Override
    public void onBackPressed() {
        // disable activity onDestroy when handle back key.
        // super.onBackPressed();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mScreenLessOptionShow) {
            return super.dispatchKeyEvent(event);
        }
        /** handle before all view */
        if (mBlockKeyEvent) {
            LetvLog.i(TAG, "--- block keyevent ---");
            return true;
        }

        /**
         * In case lose focus view when onStart() from Signal
         * Bug to see {@link IRIS-10603}
         */
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mRootView != null && mRootView.getFocusedChild() == null) {
                LetvLog.w(TAG, "Lose focus view when dispatchKeyEvent, last tab=" + mTabSpace.getSelection());
                mTabSpace.clearFocus();
                mTabSpace.setCurrentTab(mTabSpace.getSelection());
            }
        }

        /** handle by all view */
        boolean handled = super.dispatchKeyEvent(event);

        /** handle after all view */
        if (!handled) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                handled = true;
            }
        }
        return handled;
    }

    /**
     * Notify ui start load.
     */
    @Override
    public void startLoad() {
        LetvLog.d(TAG, "====== startLoad ======");
        mBlockKeyEvent = true;
    }

    /**
     * Notify ui plugin is loading.
     *
     * @param loadingList
     */
    @Override
    public void onLoad(final List<ScreenInfo> loadingList) {
        LetvLog.i(TAG, "onLoadScreens=" + loadingList + "  mAdapterPresenter=" + mAdapterPresenter);
        if (mAdapterPresenter != null) {
            mAdapterPresenter.bindScreens(loadingList, mLastSelectedScreen);
        }
    }

    // May be drawing screen items now, this only tell you data load finished.
    @Override
    public void finishLoad() {
        LetvLog.i(TAG, "====== finishLoad ======");
        mBlockKeyEvent = false;
    }

    /**
     * Notify ui plugin is added.
     *
     * @param addList : list of plugin which to add
     */
    @Override
    public void add(List<ScreenInfo> addList) {

    }

    /**
     * Notify ui plugin is updated.
     *
     * @param updateList : list of plugin which to update
     */
    @Override
    public void update(List<ScreenInfo> updateList) {
        if (mAdapterPresenter != null) {
            mAdapterPresenter.dispatchUpgradeEvent(updateList);
        }
    }

    /**
     * Notify ui plugin is removed.
     *
     * @param pluginIDList : list of plugin which to removed
     */
    public void remove(List<String> pluginIDList) {

    }

    /**
     * Notify ui plugin lock state has changed
     *
     * @param changedList : list of plugin which lock state has changed
     */
    @Override
    public void changeLock(List<ScreenInfo> changedList) {
        if (mAdapterPresenter != null) {
            mAdapterPresenter.dispatchLockChangeEvent(changedList);
        }
    }

    /**
     * Notify ui plugin show red dot
     * 64S UIPE-3016 :开机后同一个桌面插件只展示一次红点
     *
     * @param pkName
     */
    @Override
    public void showRedDot(String pkName) {
        if (pkName != null) {
            Integer count = mShowRedDotMap.get(pkName);
            if (count == null) {
                mShowRedDotMap.put(pkName, Integer.valueOf(0));
                if (mAdapterPresenter != null) {
                    mAdapterPresenter.dispatchRedDotEvent(pkName);
                }
            }
        }
    }

    @Override
    public void getBootVideoDuration(int second) {
        LetvLog.d(TAG, "onBootVideoDuration " + second + "  mSmartLoadingBar.isShow=" + mSmartLoadingBar.isShown());
        mHandler.removeMessages(MESSAGE_GUIDE_VIEW_DISPLAY_TIMEOUT);
        mHandler.sendEmptyMessageDelayed(MESSAGE_GUIDE_VIEW_DISPLAY_TIMEOUT, second * 1000);
    }

    public void hideTab(long dalyTime) {
        if (mTabHideAnimation == null) {
            mTabHideAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.tab_alpha_out);
        }
        mTabHideAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTabSpace.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mTabHideAnimation.setStartOffset(dalyTime);
        mTabSpace.startAnimation(mTabHideAnimation);

    }

    public void showTab(long dalyTime) {
        if (mTabShowAnimation == null) {
            mTabShowAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.tab_alpha_in);
        }
        mTabShowAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTabSpace.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mTabShowAnimation.setStartOffset(dalyTime);
        mTabSpace.startAnimation(mTabShowAnimation);
    }

    private void startAnimationBg(Drawable drawable) {
        if (drawable != null) {
            if (drawable instanceof AnimationDrawable) {
                AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                animationDrawable.start();
            }
        }
    }

    @Override
    public Object onFragmentAction(BaseFragment who, int what, Object arg) {
        switch (what) {
            case FragmentActionHandler.FRAGMENT_ACTION_HIDE_TAB:
                //TODO:add log for demeter-74740
                LetvLog.d("hide tab");
                if (mAdapterPresenter.getCurrentFragment() != who) {
                    LetvLog.d("current fragment not match");
                    break;
                }
                who.setContentDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                if (arg != null && arg instanceof Long) {
                    LetvLog.d("hide tab with anim");
                    hideTab((Long) arg);
                } else {
                    LetvLog.d("hide tab no anim");
                    mTabSpace.hideTab();
                }
                break;
            case FragmentActionHandler.FRAGMENT_ACTION_HIDE_ON_ANIM_TAB:
                mHandler.sendEmptyMessage(MESSAGE_HIDE_TABTITLE);
                break;
            case FragmentActionHandler.FRAGMENT_ACTION_SHOW_TAB:
                if (arg != null && arg instanceof Long) {
                    showTab((Long) arg);
                } else {
                    mTabSpace.showTab();
                }
                break;
            case FragmentActionHandler.FRAGMENT_ACTION_SWITCH_DESKTOP:
                if (arg == null) {
                    mAdapterPresenter.rightMove();
                    break;
                }
                int direct = -1;
                if (arg instanceof Integer) {
                    direct = (Integer) arg;
                }
                switch (direct) {
                    case 1:
                        mAdapterPresenter.leftMove();
                        break;
                    case 2:
                        mAdapterPresenter.rightMove();
                        break;
                    default:
                        break;
                }

                break;
            case FragmentActionHandler.FRAGMENT_ACTION_BACK_KEY:
                try {
                    mTabSpace.setCurrentTab(mTabSpace.getCurrentTab());
                } catch (Exception e) {
                    LetvLog.d(TAG, "FRAGMENT_ACTION_BACK_KEY e=" + e);
                }
                break;
            case FragmentActionHandler.FRAGMENT_ACTION_CHECK_HAND_DETECT_ENTER:
                break;
            case FragmentActionHandler.FRAGMENT_ACTION_HIDE_STATUSBAR:
                if (mAdapterPresenter.getCurrentFragment() != who) {
                    break;
                }
                if (mTitleView != null) {
                    //FIXME-xubin mTitleView.hide();
                    mTitleView.setVisibility(View.GONE);
                }
                break;
            case FragmentActionHandler.FRAGMENT_ACTION_SHOW_STATUSBAR:
//                int index = mAdapterPresenter.getCurrentTab();
//                if (index == 0) {
//                    break;
//                }
                if (mAdapterPresenter.getCurrentFragment() != who) {
                    break;
                }
                if (mTitleView != null) {
                    mTitleView.setVisibility(View.VISIBLE);
                }
                break;
            case FragmentActionHandler.FRAGMENT_ACTION_FIRST_SHOWN_COMPLETED:
                LetvLog.i(TAG, ">>> has first plugin [" + who.tag + "] shown <<<");
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || mSmartLoadingBar == null) {
                            return;
                        }
                        mSmartLoadingBar.hideByPreset();
                    }
                };
                mHandler.postDelayed(r, 1000);
                break;
            default:
                break;
        }
        return null;
    }

    public boolean isStopped() {
        return false;
    }
}
