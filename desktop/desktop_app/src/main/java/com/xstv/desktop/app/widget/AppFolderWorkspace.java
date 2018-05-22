
package com.xstv.desktop.app.widget;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xstv.base.LetvLog;
import com.xstv.desktop.app.R;
import com.xstv.desktop.app.adapter.AppFolderWorkspaceAdapter;
import com.xstv.desktop.app.adapter.BaseSpaceAdapter;
import com.xstv.desktop.app.bean.FolderInfo;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.db.ItemInfoDBHelper;
import com.xstv.desktop.app.interfaces.IAppFragment;
import com.xstv.desktop.app.model.AppDataModel;
import com.xstv.desktop.app.util.IMEUtil;
import com.xstv.desktop.app.util.LauncherState;
import com.xstv.desktop.app.util.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by wuh on 15-12-8.
 */
public class AppFolderWorkspace extends BaseWorkspace<FolderInfo> {
    private static final String TAG = AppFolderWorkspace.class.getSimpleName();

    private final int MSG_MOVE_OUT_BEGIN = 1000;
    private final int MSG_MOVE_OUT_END = MSG_MOVE_OUT_BEGIN + 1;
    private final int MSG_CHANGE_POSITION = MSG_MOVE_OUT_BEGIN + 2;
    private final int MSG_FIND_FOCUS = MSG_MOVE_OUT_BEGIN + 3;

    private EditText mFolderTitle;
    private AppRecyclerView mRecyclerView;
    private FolderInfo mFolderInfo;
    private GridLayoutManager mLayoutManager;
    public AppFolderWorkspaceAdapter mFolderAdapter;
    public static BaseWorkspace.State mState = BaseWorkspace.State.STATE_NORMAL;
    private onDataChangeListener mOnDataChangeListener;
    private View mLine;
    private View mTopIcon;
    private View mTopMoveLayout;
    private TextView mFolderTitleTv;
    private View mFolderTitleLayout;
    private ImageView mAnimationView;
    private AppCellView mFocusedView;
    // Use for keyEvent
    private static final long KEY_INTERVAL = 300;
    private long mLastKeyDownTime = -1;
    private String mTitle;

    // 下载
    private List<ItemInfo> mCibnList = new CopyOnWriteArrayList<ItemInfo>();

    private boolean isScrolling;

    // Use to update ui and animation sync
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MOVE_OUT_BEGIN:
                    if (msg.obj instanceof AppCellView) {
                        AppCellView view = (AppCellView) msg.obj;
                        actionMoveOutAnimation(view);
                    }
                    break;
                case MSG_MOVE_OUT_END:
                    if (msg.obj instanceof AppCellView) {
                        AppCellView view = (AppCellView) msg.obj;
                        moveOutFolder(view);
                    }
                    break;
                case MSG_CHANGE_POSITION:
                    int fromPosition = msg.arg1;
                    int toPosition = msg.arg2;
                    if (toPosition < 0) {
                        toPosition = 0;
                    }
                    if (toPosition >= mFolderInfo.getLength()) {
                        toPosition = mFolderInfo.getLength() - 1;
                    }
                    ItemInfo from = mFolderInfo.getContents().remove(fromPosition);
                    mFolderInfo.getContents().add(toPosition, from);
                    mFolderAdapter.moveItem(fromPosition, toPosition);
                    break;
                case MSG_FIND_FOCUS:
                    int removeIndex = msg.arg1;
                    if (removeIndex >= mFolderInfo.getLength()) {
                        removeIndex = mFolderInfo.getLength() - 1;
                    }
                    final int focusIndex = removeIndex;
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final View focus = mLayoutManager.findViewByPosition(focusIndex);
                            LetvLog.d(TAG, " MSG_FIND_FOCUS focusIndex = " + focusIndex + " focus = " + focus);
                            if (focus != null) {
                                focus.requestFocus();
                            }
                        }
                    }, 100);
                    break;
            }
        }
    };

    public AppFolderWorkspace(Context context) {
        this(context, null);
    }

    public AppFolderWorkspace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppFolderWorkspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void initView(Context context) {
        View.inflate(context, R.layout.app_folder_workspace, this);
        mRecyclerView = (AppRecyclerView) findViewById(R.id.app_folder_recyclerview);
        mFolderTitle = (EditText) findViewById(R.id.app_folder_workspace_title);
        mLine = findViewById(R.id.app_folder_line);
        mTopIcon = findViewById(R.id.folder_top_icon);
        mTopMoveLayout = findViewById(R.id.folder_top_container);
        mFolderTitleLayout = findViewById(R.id.folder_title_layout);
        mFolderTitleTv = (TextView) findViewById(R.id.folder_edit_title);
        mAnimationView = (ImageView) findViewById(R.id.animation_view);

        mRecyclerView.setHasFixedSize(true);
        AppRecyclerViewDecoration decortation = new AppRecyclerViewDecoration();
        mRecyclerView.addItemDecoration(decortation);
        mLayoutManager = new GridLayoutManager(getContext(), 4, OrientationHelper.VERTICAL, false);
        mFolderAdapter = new AppFolderWorkspaceAdapter();
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void initEvent() {
        super.initEvent();
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        isScrolling = false;
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        LetvLog.d(TAG, "onScrollStateChanged scrolling");
                        isScrolling = true;
                        break;
                }
            }
        });

        /*appMenu.setOnAppMenuListener(new OnAppMenuListener() {
            @Override
            public void moveApp() {
                AppFolderWorkspace.this.moveApp();
            }

            @Override
            public void addApp() {
                AppFolderWorkspace.this.addApp();
            }

            @Override
            public void newFolder() {
                // no need
            }

            @Override
            public void deleteApp() {
                AppFolderWorkspace.this.deleteApp();
            }

            @Override
            public void manageApp() {
                AppFolderWorkspace.this.manageApp();
            }

            @Override
            public void feedBack() {
                AppFolderWorkspace.this.feedBack();
            }
        });*/

        mFolderTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mFolderTitle.setSelection(mFolderTitle.getText().length());
                }
            }
        });

        mTopMoveLayout.setOnClickListener(new View.OnClickListener() {

            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                LetvLog.d(TAG, " onClick mFocusedView = " + mFocusedView);
                Message moveOutMsg = mHandler.obtainMessage(MSG_MOVE_OUT_END);
                moveOutMsg.obj = mFocusedView;
                mHandler.sendMessageDelayed(moveOutMsg, 100);
            }
        });
    }

    @Override
    public BaseSpaceAdapter getAdapter() {
        return mFolderAdapter;
    }

    @Override
    public RecyclerView.LayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    @Override
    public BaseRecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    public void setAppFragment(IAppFragment fragmentRef) {
        super.setAppFragment(fragmentRef);
        mFolderAdapter.setAppFragment(fragmentRef);
    }

    public void onUserVisibleHint(final boolean isVisibleToUser) {
        super.onUserVisibleHint(isVisibleToUser);
    }

    public void show(FolderInfo folderInfo) {
        LetvLog.i(TAG, " show " + folderInfo + " mFocusView = " + mFocusedView);
        super.show(folderInfo);

        if (fragmentRef != null && fragmentRef.get() != null) {
            fragmentRef.get().checkHandDetectEnter();
        }
    }

    @Override
    public void setData(FolderInfo list, boolean isUpdate) {
        mFolderInfo = list;
        mFolderAdapter.setAdapterData(mFolderInfo.getContents());
        mTitle = mFolderInfo.getTitle();
        mFolderTitle.setText(mTitle);
        mFolderTitle.setFocusable(false);
        mFolderTitle.setFocusableInTouchMode(false);

        if (mFolderInfo.getLength() > 0) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mRecyclerView.getChildAt(0) instanceof AppCellView) {
                        mFocusedView = (AppCellView) mRecyclerView.getChildAt(0);
                        mFocusedView.requestFocus();
                        mFolderTitle.setFocusable(true);
                        mFolderTitle.setFocusableInTouchMode(true);
                    }
                }
            }, 100);
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        // LetvLog.d(TAG, "onVisibilityChanged visibility = " + visibility);
        if (visibility == View.VISIBLE) {
            updateState();
        }
    }

    public void hide() {
        LetvLog.i(TAG, " hide state = " + mState);
        saveTitle();
        IMEUtil.hideIME(getContext(), mFolderTitle);
        updateEditState(BaseWorkspace.State.STATE_NORMAL);
        super.hide();
        if (fragmentRef != null && fragmentRef.get() != null) {
            fragmentRef.get().checkHandDetectEnter();
        }
        mFocusedView = null;
        LauncherState.getInstance().setAppInFolderFocusTag(null);

        if (!Utilities.isNeedBlur()) {
            clearMemory();
            mLayoutManager = new GridLayoutManager(getContext(), 4, OrientationHelper.VERTICAL, false);
            mRecyclerView.setLayoutManager(mLayoutManager);
            RecyclerView.RecycledViewPool pool = mRecyclerView.getRecycledViewPool();
            pool.clear();
        }

        if(mOnDataChangeListener != null){
            mOnDataChangeListener.onCloseFolder();
        }
    }

    private void hideTitle() {
        mLine.setVisibility(INVISIBLE);
        mFolderTitle.setVisibility(INVISIBLE);
    }

    private void showTitle() {
        mLine.setVisibility(VISIBLE);
        mFolderTitle.setVisibility(VISIBLE);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setClipToPadding(false);
        setClipChildren(false);
        setFocusable(false);
        setFocusableInTouchMode(false);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean consumed = super.dispatchKeyEvent(event);
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        LetvLog.i(TAG, " dispatchKeyEvent begin consumed = " + consumed + "keyCode = " + keyCode +
                " action = " + action + " mState = " + mState + " mFocusedView = " + mFocusedView);
        if (action == KeyEvent.ACTION_DOWN) {
            long current = SystemClock.elapsedRealtime();
            long interval = current - mLastKeyDownTime;
            //LetvLog.d(TAG, " dispatchKeyEvent interval = " + interval);
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    findFocusChildView();

                    if (mFolderTitle.isShown() && mFolderTitle.isFocused()) {
                        saveTitle();
                        setChildFocusable(false);
                        IMEUtil.showIME(getContext(), mFolderTitle);
                        mFolderTitle.setCursorVisible(false);
                        final Editable title = mFolderTitle.getText();
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // mFolderTitle.setSelection(title.length());
                                mFolderTitle.requestFocus();
                                setChildFocusable(true);
                                mFolderTitle.setCursorVisible(true);
                            }
                        }, 500);
                        consumed = true;
                    }

                    if (mState == BaseWorkspace.State.STATE_MOVE) {
                        if (mTopMoveLayout.isShown() && mTopMoveLayout.isFocused()) {
                            if (Math.abs(interval) > KEY_INTERVAL) {
                                mTopMoveLayout.callOnClick();
                                consumed = true;
                            }
                        }
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    //mRecyclerView.invalidateChild();
                    findFocusChildView();
                    if (!consumed) {
                        if (mFolderTitle.isShown() && !mFolderTitle.isFocused()) {
                            mFolderTitle.setFocusableInTouchMode(true);
                            mFolderTitle.requestFocus();
                        }
                        if (mState == BaseWorkspace.State.STATE_MOVE) {
                            if (mRecyclerView.getFocusedChild() instanceof AppCellView) {
                                findFocusChildView();
                                Message msgMoveOut = mHandler.obtainMessage(MSG_MOVE_OUT_BEGIN);
                                msgMoveOut.obj = mFocusedView;
                                mHandler.sendMessage(msgMoveOut);
                            }
                        }
                        consumed = true;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    //mRecyclerView.invalidateChild();
                    findFocusChildView();
                    // consumed = handleFocusView(keyCode);
                    if (!consumed) {
                        if (mFolderTitle.isShown() && saveTitle()) {
                            mFolderTitle.clearFocus();
                            if (mFocusedView != null) {
                                mFocusedView.requestFocus();
                            } else {
                                if (mRecyclerView.getChildAt(0) != null) {
                                    mRecyclerView.getChildAt(0).requestFocus();
                                }
                            }
                        }
                        if (mState == BaseWorkspace.State.STATE_MOVE) {
                            if (mFocusedView != null) {
                                mAnimationView.setVisibility(INVISIBLE);
                                mAnimationView.setImageDrawable(null);
                                mFocusedView.setAlpha(1.0f);
                                mFocusedView.requestFocus();
                                mFocusedView.setMoveState(true, true);
                            } else {
                                if (mRecyclerView.getChildAt(0) != null) {
                                    mRecyclerView.getChildAt(0).requestFocus();
                                }
                            }
                        }
                    }
                    consumed = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    //mRecyclerView.invalidateChild();
                    findFocusChildView();
                    // consumed = handleFocusView(keyCode);
                    if (!consumed) {
                        consumed = true;
                    }
                    break;
                case KeyEvent.KEYCODE_BACK:
                    if (mState != BaseWorkspace.State.STATE_NORMAL) {
                        updateEditState(BaseWorkspace.State.STATE_NORMAL);
                    } else {
                        boolean saved = saveTitle();
                        LetvLog.i(TAG, "dispatchKeyEvent saved = " + saved);
                        if (saved) {
                            hide();
                        }
                    }
                    consumed = true;
                    break;
                case KeyEvent.KEYCODE_MENU:
                    // Show menu only in normal state
                    if (mState == BaseWorkspace.State.STATE_NORMAL) {
                    }
                    consumed = true;
                    break;
            }
            mLastKeyDownTime = SystemClock.elapsedRealtime();
            /*LetvLog.i(TAG, " dispatchKeyEvent end consumed = " + consumed + " mState = " + mState +
                    " mFocusedView = " + mFocusedView + " getFocusedChild() = " + getFocusedChild());*/
        } else if (action == KeyEvent.ACTION_UP) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    //mRecyclerView.invalidateChild();
                    break;
            }
            consumed = true;
        }
        return consumed;
    }

    private boolean saveTitle() {
        LetvLog.i(TAG, " saveTitle mFolderTitle.isFocused() = " + mFolderTitle.isFocused());
        boolean saved = false;
        if (mFolderTitle != null && mFolderTitle.isFocused()) {
            // Check title change
            final Editable title = mFolderTitle.getText();
            String titleStr = title.toString();
            if (!TextUtils.isEmpty(titleStr)) {
                titleStr = removeBlankAndN(titleStr);
                if (titleStr.equals(mTitle)) {
                    return true;
                }
                String[] allFolderTitle = AppDataModel.getInstance().getAllFolderTitle();
                for (int i = 0; i < allFolderTitle.length; i++) {
                    if (titleStr.equals(allFolderTitle[i])) {
                        Toast.makeText(getContext(), R.string.folder_name_same, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                // if changed ,save to db
                mTitle = titleStr;
                mFolderInfo.setTitle(titleStr);
                ItemInfoDBHelper.getInstance().update(mFolderInfo);
                // and notify workspace this change
                if (mOnDataChangeListener != null) {
                    mOnDataChangeListener.onUpdate(mFolderInfo);
                }
                saved = true;
            } else {
                if (TextUtils.isEmpty(titleStr)) {
                    Toast.makeText(getContext(), R.string.folder_name_cant_null, Toast.LENGTH_SHORT).show();
                } else if (title.length() > 12) {
                    Toast.makeText(getContext(), R.string.folder_name_full, Toast.LENGTH_SHORT).show();
                }
                saved = false;
            }
        } else {
            saved = true;
        }
        return saved;
    }

    private void addApp() {
        hide();
        if (mFolderInfo != null) {
            mFolderInfo.isAdding = true;
        }
        if(mOnDataChangeListener != null){
            mOnDataChangeListener.onAddApp();
        }
    }

    private void deleteApp() {
        updateEditState(State.STATE_DELETE);
    }

    private void moveApp() {
        updateEditState(State.STATE_MOVE);
    }

    /**
     * Update edit state title text
     *
     * @param state
     */
    public void updateEditTitle(BaseWorkspace.State state) {
        switch (state) {
            case STATE_DELETE:
                mFolderTitleTv.setText(R.string.press_center_key_to_uninstall);
                mFolderTitleLayout.setVisibility(View.VISIBLE);
                if (mFolderTitle.isFocused()) {
                    if (mFocusedView != null) {
                        mFocusedView.requestFocus();
                    }
                }
                hideTitle();
                break;
            case STATE_MOVE:
                mFolderTitleTv.setText(R.string.press_center_key_to_confirm);
                mFolderTitleLayout.setVisibility(View.VISIBLE);
                mTopMoveLayout.setVisibility(VISIBLE);
                if (mFolderTitle.isFocused()) {
                    if (mFocusedView != null) {
                        mFocusedView.requestFocus();
                    }
                }
                hideTitle();
                break;
            case STATE_NORMAL:
                mFolderTitleLayout.setVisibility(View.GONE);
                mTopMoveLayout.setVisibility(GONE);
                mAnimationView.setVisibility(INVISIBLE);
                mAnimationView.setImageDrawable(null);
                if (mFocusedView != null && mFocusedView.isShown()) {
                    mFocusedView.setAlpha(1.0f);
                    mFocusedView.requestFocus();
                }
                showTitle();
                break;
            case STATE_FOLDER_OPENED:
                break;
        }
    }

    public void updateEditState(BaseWorkspace.State state) {
        LetvLog.i(TAG, " updateEditState new state = " + state + " mState = " + mState);

        if (mState == BaseWorkspace.State.STATE_MOVE) {
            saveChildrenIndex();
        }

        mState = state;
        // update title
        updateEditTitle(mState);

        int count = mLayoutManager.getChildCount();
        switch (mState) {
            case STATE_DELETE:
                // update cell view
                for (int i = 0; i < count; i++) {
                    final AppCellView cellView = (AppCellView) mLayoutManager.getChildAt(i);
                    if (cellView != null) {
                        cellView.setDeleteState(cellView.hasFocus());
                    }
                }
                break;
            case STATE_NEW_FOLDER:
                // update cell view
                // for (int i = 0; i < count; i++) {
                // final AppCellView cellView = (AppCellView) mLayoutManager.getChildAt(i);
                // if (cellView != null) {
                // cellView.setNewFolderState(false);
                // }
                // }
                // break;
            case STATE_MOVE:
                for (int i = 0; i < count; i++) {
                    final AppCellView cellView = (AppCellView) mLayoutManager.getChildAt(i);
                    if (cellView != null) {
                        if (cellView.hasFocus()) {
                            cellView.setMoveState(true, true);
                        } else {
                            cellView.setMoveState(false, false);
                        }
                    }
                }
                break;
            case STATE_NORMAL:
                // update cell view
                for (int i = 0; i < count; i++) {
                    AppCellView cellView = (AppCellView) mLayoutManager.getChildAt(i);
                    if (cellView != null) {
                        cellView.resetState();
                    }
                }
                break;
        }
    }

    /**
     * Call when exit state_move
     */
    private void saveChildrenIndex() {
        LetvLog.i(TAG, " saveChildrenIndex mState = " + mState + " mFolderInfo = " + mFolderInfo);
        if (mFolderInfo == null) {
            return;
        }
        List<ItemInfo> itemInfoList = mFolderAdapter.getDataSet();
        mFolderInfo.getContents().clear();
        mFolderInfo.getContents().addAll(itemInfoList);
        // set index
        mFolderInfo.setItemContainer();
        // save index to db
        ItemInfoDBHelper.getInstance().updateInTx(mFolderInfo.getContents());
        // notify app workspace folder has changes
        if (mOnDataChangeListener != null) {
            mOnDataChangeListener.onUpdate(mFolderInfo);
        }
        // update folder in db
        boolean updateDB = ItemInfoDBHelper.getInstance().update(mFolderInfo);
        LetvLog.i(TAG, " saveChildrenIndex updateDB " + updateDB);
    }

    /**
     * if has not children ,destroy this.
     *
     * @return
     */
    private boolean destroy() {
        LetvLog.i(TAG, " destroy mFolderInfo = " + mFolderInfo);
        if (mFolderInfo.getLength() == 0) {
            ItemInfoDBHelper.getInstance().delete(mFolderInfo.getId());
            // remove this folder
            if (mOnDataChangeListener != null) {
                mOnDataChangeListener.onDeleteFolder(mFolderInfo);
            }
            hide();
            mFolderInfo = null;
            mFocusedView = null;
            return true;
        }
        return false;
    }

    private void moveAnimation(View from, View to, Animator.AnimatorListener listener) {
        if (from == null || to == null || mFocusedView == null) {
            return;
        }

        int[] fromInWindow = new int[2];
        mFocusedView.getLocationInWindow(fromInWindow);
        float fromX = fromInWindow[0];
        float fromY = fromInWindow[1];
        LetvLog.d(TAG, " moveAnimation from x = " + fromX + " y = " + fromY);

        int[] toInWindow = new int[2];
        to.getLocationInWindow(toInWindow);
        float toX = toInWindow[0];
        float toY = toInWindow[1];
        LetvLog.d(TAG, " moveAnimation to x = " + toX + " y = " + toY);

        float translationX = toX - fromX - 100;
        float translationY = toY - fromY - 60;
        float scaleX = (to.getWidth() * 100f) / (from.getWidth() * 100f);
        float scaleY = (to.getHeight() * 100f) / (from.getHeight() * 100f);

        LetvLog.d(TAG, " moveAnimation translationX = " + translationX + " translationY = " + translationY);

        final ViewPropertyAnimator viewPropertyAnimator = from.animate()
                .translationX(translationX)
                .translationY(translationY)
                .scaleX(scaleX)
                .scaleY(scaleY)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator());
        viewPropertyAnimator.setListener(listener);
        viewPropertyAnimator.start();
    }

    public void actionMoveOutAnimation(AppCellView target) {
        LetvLog.d(TAG, " actionMoveOutAnimation target = " + target + " mState = " + mState);
        if (target == null) {
            return;
        }
        final AppCellView focusd = target;
        if (focusd.canMove) {

            focusd.setMoveState(false, true);

            int[] locationInWindow = new int[2];
            focusd.getLocationInWindow(locationInWindow);
            LetvLog.d(TAG, " actionMoveOutAnimation from x = " + locationInWindow[0] + " y = " + locationInWindow[1]);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(312, 190);
            params.leftMargin = locationInWindow[0];
            params.topMargin = locationInWindow[1];
            mAnimationView.setTranslationX(0);
            mAnimationView.setTranslationY(0);
            mAnimationView.setScaleX(1.0f);
            mAnimationView.setScaleY(1.0f);
            mAnimationView.setLayoutParams(params);

            moveAnimation(mAnimationView, mTopIcon, new Animator.AnimatorListener() {
                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    focusd.setDrawingCacheEnabled(true);
                    focusd.buildDrawingCache();
                    Bitmap dragBitmap = focusd.getDrawingCache();
                    if (!dragBitmap.isRecycled()) {
                        mAnimationView.setImageBitmap(dragBitmap);
                    }
                    mAnimationView.setVisibility(VISIBLE);

                    focusd.setAlpha(0.6f);
                    focusd.setMoveState(false, false);
                    mTopMoveLayout.requestFocus();
                }
            });
        }
    }

    private void moveOutFolder(final AppCellView moveView) {
        if (moveView == null || mFolderInfo == null) {
            return;
        }
        ItemInfo itemInfo = moveView.getItemInfo();
        int removeIndex = mFolderInfo.getContents().indexOf(itemInfo);
        LetvLog.i(TAG, " moveOutFolder removeIndex = " + removeIndex);
        if (mFolderInfo.getContents().remove(itemInfo)) {
            // 1.remove from folder
            itemInfo.setContainer(0L);
            itemInfo.setContainerName("");
            itemInfo.setIndex(ItemInfoDBHelper.getInstance().getLastIndex() + 1);
            itemInfo.setInFolderIndex(0);
            mFolderAdapter.removeItem(itemInfo);
            // and update in db
            ItemInfoDBHelper.getInstance().update(itemInfo);
            // 2.notify app workspace add a item
            if (mOnDataChangeListener != null) {
                mOnDataChangeListener.onAddFromFolder(itemInfo);
                mCibnList.remove(itemInfo);
            }
            // 3.Check can destroy
            if (!destroy()) {
                // 4.notify app workspace folder cellview change.
                if (mOnDataChangeListener != null) {
                    mOnDataChangeListener.onUpdate(mFolderInfo);
                }

                if (removeIndex >= mFolderInfo.getLength()) {
                    removeIndex = mFolderInfo.getLength() - 1;
                }
                final int focusIndex = removeIndex;
                // 5.Request focus
                LetvLog.i(TAG, " moveOutFolder removeItem focusIndex = " + focusIndex);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final View focus = mLayoutManager.findViewByPosition(focusIndex);
                        LetvLog.i(TAG, " moveOutFolder focusIndex = " + focusIndex + " focus = " + focus);
                        if (focus != null) {
                            focus.requestFocus();
                        }
                    }
                }, 100);
            }
            // hide move animation
            mAnimationView.setVisibility(INVISIBLE);
            mAnimationView.setImageDrawable(null);
            mFocusedView = null;
        }
    }

    private void findFocusChildView() {
        if (mRecyclerView.getFocusedChild() instanceof AppCellView) {
            mFocusedView = (AppCellView) mRecyclerView.getFocusedChild();
        }
    }

    private void setChildFocusable(boolean focusable) {
        int count = mLayoutManager.getChildCount();
        for (int i = 0; i < count; i++) {
            AppCellView cellView = (AppCellView) mLayoutManager.getChildAt(i);
            cellView.setFocusable(focusable);
            cellView.setFocusableInTouchMode(focusable);
        }
    }

    @Override
    public void onAppAdded(ArrayList<ItemInfo> adds) {
        // no need
    }

    @Override
    public void onAppRemoved(ArrayList<ItemInfo> removeList, ArrayList<ItemInfo> removeContainFolderList) {
        LetvLog.i(TAG, " onRemoveShortcut removeContainFolderList = " + removeList + " mFolderInfo = " + mFolderInfo);
        if (removeList == null || removeList.size() == 0 || mFolderInfo == null || getVisibility() != VISIBLE) {
            return;
        }
        for (ItemInfo bean : removeList) {
            boolean hasRemoved = mFolderAdapter.getDataSet().contains(bean);
            if (hasRemoved) {
                mRecyclerView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
                int removeIndex = mFolderAdapter.removeItem(bean);
                mRecyclerView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                // if is null destroy this.
                if (!destroy()) {
                    if (removeIndex >= mFolderInfo.getLength()) {
                        removeIndex = mFolderInfo.getLength() - 1;
                    }
                    final int focusIndex = removeIndex;
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final View focus = mLayoutManager.findViewByPosition(focusIndex);
                            if (focus != null) {
                                boolean is = focus.requestFocus();
                                LetvLog.i(TAG, " onRemoveShortcut focusIndex = " + focusIndex + " focus = " + focus + " is = " + is);
                            }
                        }
                    }, 100);
                }
                mCibnList.remove(bean);
            }
        }
    }

    @Override
    public void onAppUpdated(ArrayList<ItemInfo> updateList, ArrayList<ItemInfo> updateContainFolderList) {
        if (updateList == null || updateList.size() == 0 || mFolderInfo == null || getVisibility() != VISIBLE) {
            return;
        }
        for (ItemInfo bean : updateList) {
            int updateIndex = mFolderInfo.getContents().indexOf(bean);
            if (updateIndex > -1 && updateIndex < mFolderInfo.getLength()) {
                mFolderInfo.getContents().set(updateIndex, bean);
                mFolderInfo.setItemContainer();
                LetvLog.i(TAG, " updateItem bean = " + bean);
                mFolderAdapter.updateItem(bean);
                // and notify workspace this change
                /*
                 * if (mOnDataChangeListener != null) { mOnDataChangeListener.onUpdate(mFolderInfo); }
                 */
                /*if (getVisibility() != VISIBLE) {
                    return;
                }*/
                if (mFocusedView != null) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mFocusedView != null) {
                                mFocusedView.requestFocus();
                            }
                        }
                    }, 100);
                } else {
                    Message msg = mHandler.obtainMessage(MSG_FIND_FOCUS);
                    msg.arg1 = updateIndex;
                    mHandler.sendMessage(msg);
                }
            }
        }
    }

    @Override
    public void onSuperscriptChange(ItemInfo update, FolderInfo inFolderInfo) {
        if (update == null || mFolderInfo == null || getVisibility() != VISIBLE) {
            return;
        }
        int updateIndex = mFolderInfo.getContents().indexOf(update);
        if (updateIndex > -1 && updateIndex < mFolderInfo.getLength()) {
            ItemInfo info = mFolderInfo.getChildrenByIndex(updateIndex);
            info.superscriptType = update.superscriptType;
            info.superscriptCount = update.superscriptCount;
            mFolderInfo.getContents().set(updateIndex, info);
            mFolderInfo.setItemContainer();
            LetvLog.i(TAG, " onUpdateTitle info = " + info);
            mFolderAdapter.updateItem(info);
            // and notify workspace this change
            /*
             * if (mOnDataChangeListener != null) { mOnDataChangeListener.onUpdate(mFolderInfo); }
             */
            if (mFocusedView != null) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mFocusedView != null) {
                            mFocusedView.requestFocus();
                        }
                    }
                }, 100);
            } else {
                Message msg = mHandler.obtainMessage(MSG_FIND_FOCUS);
                msg.arg1 = updateIndex;
                mHandler.sendMessage(msg);
            }
        }
    }

    @Override
    public void onStateChange(List<ItemInfo> posterList, ItemInfo itemInfo, FolderInfo folderInfo) {
        if (folderInfo == null || itemInfo == null) {
            LetvLog.i(TAG, "onStateChange not in folder, so dont update");
            return;
        }

        mCibnList.add(itemInfo);

        if (getVisibility() != View.VISIBLE || !isVisibleToUser) {
            return;
        }

        updateState();
    }

    private void updateState() {
        if (mLayoutManager == null || mCibnList.size() == 0) {
            return;
        }
        int childCount = mLayoutManager.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = mLayoutManager.getChildAt(i);
            if (view instanceof CellView) {
                final CellView cellView = (CellView) view;
                if (cellView != null) {
                    final ItemInfo info = cellView.getItemInfo();
                    if (info != null && mCibnList.contains(info)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cellView.invalidate();
                                /*DownloadStatusBean downloadStatusBean = info.getDownloadStatusBean();
                                if (downloadStatusBean == null) {
                                    Log.w(TAG, "updateState downloadStatusBean is null.");
                                    mCibnList.remove(info);
                                } else {
                                    if (downloadStatusBean.getDownloadStatus() == DownloadAppPresenter.STATE_INSTALLED ||
                                            downloadStatusBean.getDownloadStatus() == DownloadAppPresenter.STATE_RESET ||
                                            downloadStatusBean.getDownloadStatus() == null) {
                                        // 说明已经安装完成，在队列中移除
                                        LetvLog.i(TAG, "updateState remove itemInfo = " + info);
                                        mCibnList.remove(info);
                                    }
                                }*/
                            }
                        });
                    }
                }
            }
        }
    }

    private void runOnUiThread(Runnable r) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            r.run();
        } else {
            mHandler.post(r);
        }
    }

    /**
     * Folder change item info notify app workspace change.
     */
    interface onDataChangeListener {
        /**
         * Notify item data has changed
         *
         * @param itemInfo : which has changed
         */
        void onUpdate(FolderInfo itemInfo);

        /**
         * Notify workspace has remove a icon from folder and add to workspace
         *
         * @param itemInfo
         */
        void onAddFromFolder(ItemInfo itemInfo);

        /**
         * Notify app workspace delete folder
         *
         * @param folderInfo
         */
        void onDeleteFolder(FolderInfo folderInfo);

        void onCloseFolder();

        void onAddApp();

    }

    public void setOnDataChangeListener(onDataChangeListener mOnDataChangeListener) {
        this.mOnDataChangeListener = mOnDataChangeListener;
    }

    private String removeBlankAndN(String str) {
        if (!TextUtils.isEmpty(str)) {
            str = str.replace("\n", "").replace("\t", "").trim();
        }
        return str;
    }

    @Override
    public void onRelease() {
        super.onRelease();
        mFolderTitle = null;
        mRecyclerView = null;
        mLayoutManager = null;
        mFolderAdapter = null;
        mLine = null;
        mTopIcon = null;
        mTopMoveLayout = null;
        mFolderTitleTv = null;
        mFolderTitleLayout = null;
        mAnimationView = null;
        mFocusedView = null;
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mCibnList.clear();
    }
}
