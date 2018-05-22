
package com.xstv.desktop.app.widget;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.xstv.base.LetvLog;
import com.xstv.desktop.app.R;
import com.xstv.desktop.app.adapter.AppWorkspaceAdapter;
import com.xstv.desktop.app.adapter.BaseSpaceAdapter;
import com.xstv.desktop.app.bean.ContentBean;
import com.xstv.desktop.app.bean.FolderInfo;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.db.ItemInfoDBHelper;
import com.xstv.desktop.app.interfaces.IAppFragment;
import com.xstv.desktop.app.model.AppDataModel;
import com.xstv.desktop.app.model.DataModelList;
import com.xstv.desktop.app.util.IconFilterUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by wuh on 15-11-10.
 */

public class AppWorkspace extends BaseWorkspace<List<ItemInfo>> implements AppFolderWorkspace.onDataChangeListener {
    private static final String TAG = AppWorkspace.class.getSimpleName();

    public static BaseWorkspace.State mState = BaseWorkspace.State.STATE_NORMAL;

    private final int MSG_ADD_TO_FOLDER_BEGIN = 100;
    private final int MSG_ADD_TO_FOLDER_END = MSG_ADD_TO_FOLDER_BEGIN + 1;
    private final int MSG_FIND_FOCUSVIEW = MSG_ADD_TO_FOLDER_BEGIN + 2;
    private final int MSG_SYNC_DB = MSG_ADD_TO_FOLDER_BEGIN + 3;
    private final int MSG_FIND_UPDATE_FOCUSVIEW = MSG_ADD_TO_FOLDER_BEGIN + 4;

    protected AppWorkspaceAdapter mAdapter;
    private AppGridLayoutManager mLayoutManager;
    private AppRecyclerView mRecyclerView;
    private View mFocusedView;

    private View mTopView;
    private TextView mTopTitleView;

    // 语音
    private List<FolderInfo> mVoiceFolderList = new ArrayList<FolderInfo>();

    // 下载
    private List<ItemInfo> mPosterList = new CopyOnWriteArrayList<ItemInfo>();
    private List<ItemInfo> mCibnList = new CopyOnWriteArrayList<ItemInfo>();

    private boolean isScrolling;

    private boolean isAddingToFolder = false;

    // Use to update ui and animation sync
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            LetvLog.i(TAG, " handleMessage : " + msg);
            switch (msg.what) {
                case MSG_ADD_TO_FOLDER_BEGIN:
                    if (msg.obj instanceof CellView) {
                        CellView cellView = (CellView) msg.obj;
                        addShortcutToFolder(cellView);
                    }
                    break;
                case MSG_ADD_TO_FOLDER_END:
                    isAddingToFolder = false;
                    int fromPosition = msg.arg1;
                    int toPosition = msg.arg2;
                    LetvLog.d(TAG, " MSG_ADD_TO_FOLDER_END fromPosition = " + fromPosition + " toPosition = " + toPosition);
                    if (fromPosition != RecyclerView.NO_POSITION) {
                        ItemInfo addShortcut = mAdapter.getItemInfoByPosition(fromPosition);
                        if (msg.obj instanceof FolderInfo) {
                            FolderInfo folderInfo = (FolderInfo) msg.obj;
                            if (addShortcut != null) {
                                // if folder is null , set index of first children index and insert to db
                                if (folderInfo.getLength() == 0) {
                                    long id = ItemInfoDBHelper.getInstance().insert(folderInfo);
                                    folderInfo.setId(id);
                                    int itemCount = mAdapter.getItemCount();
                                    int count = itemCount;
                                    LetvLog.d(TAG, "handleMessage toPosition = " + toPosition + ",count = " + count);
                                    int beginMovePosition = toPosition + 1;
                                    if (beginMovePosition <= (count - 1)) {
                                        List<ItemInfo> itemInfoList = mAdapter.getDataSet();
                                        beginMovePosition = beginMovePosition - mAdapter.getHeaderSize();
                                        count = count - mAdapter.getHeaderSize();
                                        for (int i = beginMovePosition; i < count; i++) {
                                            ItemInfo info = itemInfoList.get(i);
                                            info.setIndex(info.getIndex() + 1);
                                            // LetvLog.d(TAG, "handleMessage info title = " + info.getTitle() + ",index = " + info.getIndex());
                                        }
                                        List<ItemInfo> updateList = itemInfoList.subList(beginMovePosition, count);
                                        if (updateList.size() > 0) {
                                            ItemInfoDBHelper.getInstance().updateInTx(updateList);
                                        }
                                    }
                                }
                                if (folderInfo.add(addShortcut)) {
                                    // Must after set id
                                    folderInfo.setItemContainer();
                                    // update children index
                                    ItemInfoDBHelper.getInstance().update(addShortcut);
                                    mAdapter.updateItem(folderInfo);

                                    // remove from workspace
                                    mAdapter.removeItem(fromPosition);
                                    List<ItemInfo> allAppList = DataModelList.getInstance().allAppList;
                                    if (allAppList != null) {
                                        int pos = fromPosition - mAdapter.getHeaderSize();
                                        if (pos > 0 && pos < allAppList.size()) {
                                            allAppList.remove(pos);
                                        }
                                    }
                                    // find focus
                                    Message msgFindFocus = mHandler.obtainMessage(MSG_FIND_FOCUSVIEW);
                                    msgFindFocus.arg1 = fromPosition;
                                    mHandler.sendMessageDelayed(msgFindFocus, 50);
                                }
                            }
                        }
                    }
                    break;
                case MSG_FIND_FOCUSVIEW:
                    final int focusPosition = msg.arg1;
                    View itemView = mLayoutManager.findViewByPosition(focusPosition);
                    if (itemView instanceof CellView) {
                        mFocusedView = itemView;
                    } else {
                        mFocusedView = findLastCompletelyVisibleView();
                    }

                    if (mFocusedView != null) {
                        mFocusedView.requestFocus();
                        if (mFocusedView instanceof BaseCellView) {
                            ItemInfo itemInfo = ((BaseCellView) mFocusedView).getItemInfo();
                            LetvLog.i(TAG, "handleMessage focusPosition = " + focusPosition + " itemInfo = " + itemInfo);
                        }
                    } else {
                        resetScroll();
                    }
                    break;
                case MSG_SYNC_DB:
                    break;
                case MSG_FIND_UPDATE_FOCUSVIEW:
                    Object obj = msg.obj;
                    if (obj instanceof String) {
                        String mark = (String) obj;
                        LetvLog.i(TAG, " MSG_FIND_UPDATE_FOCUSVIEW mState = " + mState + " mark =  " + mark);
                        mFocusedView = getFocusedView(mark);
                    }
                    if (getVisibility() != VISIBLE) {
                        return;
                    }
                    if (mFocusedView instanceof BaseCellView) {
                        BaseCellView baseCellView = (BaseCellView) mFocusedView;
                        ItemInfo itemInfo = baseCellView.getItemInfo();
                        LetvLog.i(TAG, " MSG_FIND_UPDATE_FOCUSVIEW itemInfo = " + itemInfo);
                    }
                    if (mFocusedView != null) {
                        mFocusedView.setFocusable(true);
                        mFocusedView.requestFocus();
                    } else {
                        resetScroll();
                    }
                    break;
            }
        }
    };

    public AppWorkspace(Context context) {
        this(context, null);
    }

    public AppWorkspace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppWorkspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void initView(Context context) {
        View.inflate(context, R.layout.app_workspace_layout, this);
        mRecyclerView = (AppRecyclerView) findViewById(R.id.app_workspace_recyclerView);

        mTopView = findViewById(R.id.app_workspace_top_layout);
        mTopTitleView = (TextView) findViewById(R.id.app_edit_title);

        mAdapter = new AppWorkspaceAdapter();
        mLayoutManager = new AppGridLayoutManager(getContext(), 5, OrientationHelper.VERTICAL, false);
        mLayoutManager.offsetScrolling(50);

        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0 || position == 1) {
                    return 5;
                } else {
                    return 1;
                }
            }
        });
        AppRecyclerViewDecoration decortation = new AppRecyclerViewDecoration();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(decortation);
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
                        refreshDownloadState(getVisibility());
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        // LetvLog.d(TAG, "onScrollStateChanged scrolling");
                        isScrolling = true;
                        break;
                }
            }
        });
    }

    @Override
    public BaseSpaceAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public RecyclerView.LayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    @Override
    public BaseRecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public boolean onFocus(int direction) {
        return mRecyclerView.onFocus(direction);
    }

    @Override
    public void setData(final List<ItemInfo> infoList, boolean isUpdate) {
        mAdapter.setAdapterData(infoList);
    }

    public void setData(List<ContentBean> contentBeanList, List<ItemInfo> infoList) {
        mAdapter.setHeaderData(contentBeanList);
        mLayoutManager.setHeaderSize(contentBeanList.size());
        mAdapter.setAdapterData(infoList);
    }


    public void updateHeader(List<ContentBean> list) {
        mAdapter.updateHeader(list);
    }

    @Override
    public void setAppFragment(IAppFragment fragmentRef) {
        super.setAppFragment(fragmentRef);
        mAdapter.setAppFragment(fragmentRef);
    }

    @Override
    public void onUserVisibleHint(final boolean isVisibleToUser) {
        super.onUserVisibleHint(isVisibleToUser);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        // LetvLog.d(TAG, " onVisibilityChanged visibility = " + visibility + " mFocusedView = " + mFocusedView
        // + " mState = " + mState);
        if (visibility != View.VISIBLE) {
            return;
        }

        /*if (mFocusedView != null && mState == State.STATE_NORMAL) {
            boolean is = mFocusedView.requestFocus();
            LetvLog.d(TAG, "onVisibilityChanged is = " + is);
        }*/
        refreshDownloadState(visibility);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void hide() {
        updateEditState(State.STATE_NORMAL, false);
        super.hide();
        mFocusedView = null;
        /*if (!Utilities.isNeedBlur()) {
            clearMemory();
            mLayoutManager = new AppGridLayoutManager(getContext(), 5, OrientationHelper.VERTICAL, false);
            mLayoutManager.offsetScrolling(50);
            mRecyclerView.setLayoutManager(mLayoutManager);
            RecyclerView.RecycledViewPool pool = mRecyclerView.getRecycledViewPool();
            pool.clear();
        }*/
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean consumed = super.dispatchKeyEvent(event);
        // boolean consumed = false;
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    if ((mState == BaseWorkspace.State.STATE_NEW_FOLDER ||
                            mState == BaseWorkspace.State.STATE_ADD) && !consumed) {
                        View focusedView = mRecyclerView.getFocusedChild();
                        if (focusedView instanceof BaseContent) {
                            LetvLog.i(TAG, "dispatchKeyEvent poster view is not add to folder.");
                        } else {
                            LetvLog.d(TAG, " dispatchKeyEvent mState = " + mState + " isAddingToFolder = "
                                    + isAddingToFolder + " consumed = " + consumed);

                            if (!isAddingToFolder) {
                                isAddingToFolder = true;
                                Message msgAdd = mHandler.obtainMessage(MSG_ADD_TO_FOLDER_BEGIN);
                                // focus maybe on top layout
                                if (focusedView != null) {
                                    msgAdd.obj = focusedView;
                                    mHandler.sendMessage(msgAdd);
                                } else {
                                    isAddingToFolder = false;
                                }
                            }
                        }
                        consumed = true;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    // mRecyclerView.invalidateChild();
                    if (mRecyclerView.getFocusedView() != null) {
                        mFocusedView = mRecyclerView.getFocusedView();
                    }
                    if (mState != State.STATE_NORMAL && !consumed) {
                        // In edit state ,can not respond key up\left\right
                        consumed = true;
                    }
                    break;
                case KeyEvent.KEYCODE_BACK:
                    if (fragmentRef != null && fragmentRef.get() != null) {
                        AppFolderWorkspace workspace = fragmentRef.get().getFolderWorkspace();
                        LetvLog.i(TAG, "dispatchKeyEvent folderWorkspace back event no respose," +
                                "so force hide folderWorkspace.");
                        if (workspace != null && workspace.getVisibility() == View.VISIBLE) {
                            workspace.hide();
                        }
                    }
                    if (mState != BaseWorkspace.State.STATE_NORMAL) {
                        if (mRecyclerView.getFocusedView() != null) {
                            mFocusedView = mRecyclerView.getFocusedView();
                        }
                        updateEditState(BaseWorkspace.State.STATE_NORMAL, false);
                    } else {
                        resetScrollAndBackToTab();
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
        } else if (action == KeyEvent.ACTION_UP) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    // mRecyclerView.invalidateChild();
                    break;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    if (mRecyclerView.getFocusedView() != null) {
                        mFocusedView = mRecyclerView.getFocusedView();
                    }
                    break;
            }
        }
        //LetvLog.d(TAG, " dispatchKeyEvent end consumed = " + consumed + " mFocusedView = " + mFocusedView);
        return consumed;
    }

    private void addShortcutToFolder(CellView itemView) {
        // 1.find item view
        final CellView fromView = itemView;
        final ItemInfo fromShortcut = fromView.getItemInfo();
        if (fromView == null || fromShortcut == null || fromShortcut.getType() == AppDataModel.ITEM_TYPE_FOLDER) {
            isAddingToFolder = false;
            return;
        }

        // 2.find folder view
        final FolderInfo folderInfo = mAdapter.getAddingStateFolder();
        if (folderInfo == null) {
            isAddingToFolder = false;
            Toast.makeText(getContext(), R.string.add_to_folder_fail, Toast.LENGTH_SHORT).show();
            LetvLog.w(TAG, " addShortcutToFolder folderInfo = " + folderInfo);
            return;
        }

        // check folder is full
        if (folderInfo.getLength() > (AppFolderCellView.FOLDER_COUNT - 1)) {
            isAddingToFolder = false;
            Toast.makeText(getContext(), R.string.folder_full, Toast.LENGTH_SHORT).show();
            return;
        }

        final int fromPosition = mAdapter.getPositionByBean(fromShortcut);
        final int folderPosition = mAdapter.getPositionByBean(folderInfo);
        View toView = mLayoutManager.findViewByPosition(folderPosition);
        LetvLog.d(TAG, "addShortcutToFolder folderInfo = " + folderInfo + " folderPosition = "
                + folderPosition + " fromPosition = " + fromPosition + " toView = " + toView);
        // 3.check folder view is exist
        if (toView instanceof AppFolderCellView) {

        } else {
            // 4. folder has removed from recycler view,so find a animation to view.
            int toPosition = 0;
            if (fromPosition < folderPosition) {
                toPosition = mLayoutManager.findLastVisibleItemPosition() - 2;
            } else {
                toPosition = mLayoutManager.findFirstVisibleItemPosition() + 2;
            }
            LetvLog.i(TAG, " addShortcutToFolder find toPosition = " + toPosition);
            toView = mLayoutManager.findViewByPosition(toPosition);
        }

        LetvLog.i(TAG, " addShortcutToFolder { fromView = " + fromView + " } { toView = " + toView + "}");

        Long duration = 250L;
        moveAnimation(fromView, toView, null, duration);

        Message endMsg = mHandler.obtainMessage(MSG_ADD_TO_FOLDER_END);
        endMsg.obj = folderInfo;
        LetvLog.i(TAG, " addShortcutToFolder endMsg.obj = " + endMsg.obj);
        // Maybe value is -1
        endMsg.arg1 = fromPosition;
        endMsg.arg2 = folderPosition;
        mHandler.sendMessageDelayed(endMsg, duration);
    }

    private void moveAnimation(View from, View to, Animator.AnimatorListener listener, long duration) {
        if (from == null || to == null) {
            return;
        }
        int fromX = from.getLeft() + from.getWidth() / 2;
        int fromY = from.getTop() + from.getHeight() / 2;
        int toX = to.getLeft() + to.getWidth() / 2;
        int toY = to.getTop() + to.getHeight() / 2;
        float translationX = toX - fromX;
        float translationY = toY - fromY;

        final ViewPropertyAnimator viewPropertyAnimator = from.animate().translationX(translationX)
                .translationY(translationY).alpha(0).scaleX(0).scaleY(0).setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator());
        viewPropertyAnimator.setListener(listener);
        viewPropertyAnimator.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void updateEditState(BaseWorkspace.State state, boolean force) {
        LetvLog.d(TAG, " updateEditState new state = " + state + " older state = " + mState + " force = " + force);
        if (mState == state && !force) {
            LetvLog.w(TAG, " updateEditState state not change return ");
            return;
        }

        if (mState == BaseWorkspace.State.STATE_MOVE) {
            updateIndex();
        }

        if (mState == BaseWorkspace.State.STATE_NEW_FOLDER || mState == BaseWorkspace.State.STATE_ADD) {
            // sync
            // Message msgSync = mHandler.obtainMessage(MSG_SYNC_DB);
            // mHandler.sendMessageDelayed(msgSync, 300);
        }

        mState = state;
        // update title
        updateEditTitle(state);

        int count = mLayoutManager.getChildCount();
        switch (mState) {
            case STATE_DELETE:
                // LetvLog.d(TAG, "updateEditState count = " + count);
                for (int i = 0; i < count; i++) {
                    View itemView = mLayoutManager.getChildAt(i);
                    if (itemView instanceof CellView) {
                        CellView cellView = (CellView) itemView;
                        cellView.setDeleteState(cellView.hasFocus());
                    } else if (itemView instanceof BaseContent) {
                        BaseContent content = ((BaseContent) itemView);
                        int childCount = content.getChildCount();
                        for (int i1 = 0; i1 < childCount; i1++) {
                            View childView = content.getChildAt(i1);
                            if (childView instanceof BaseCellView) {
                                ((BaseCellView) childView).setDeleteState(childView.hasFocus());
                            }
                        }
                    }
                }
                break;
            case STATE_NEW_FOLDER:
                for (int i = 0; i < count; i++) {
                    View itemView = mLayoutManager.getChildAt(i);
                    if (itemView instanceof CellView) {
                        CellView cellView = (CellView) itemView;
                        cellView.setNewFolderState(cellView.hasFocus());
                    } else if (itemView instanceof BaseContent) {
                        BaseContent content = ((BaseContent) itemView);
                        int childCount = content.getChildCount();
                        for (int i1 = 0; i1 < childCount; i1++) {
                            View childView = content.getChildAt(i1);
                            if (childView instanceof BaseCellView) {
                                ((BaseCellView) childView).setNewFolderState(childView.hasFocus());
                            }
                        }
                    }
                }
                break;
            case STATE_MOVE:
                for (int i = 0; i < count; i++) {
                    View itemView = mLayoutManager.getChildAt(i);
                    if (itemView instanceof CellView) {
                        CellView cellView = (CellView) itemView;
                        if (cellView.hasFocus()) {
                            cellView.setMoveState(true, true);
                        } else {
                            cellView.setMoveState(false, false);
                        }
                    } else if (itemView instanceof BaseContent) {
                        BaseContent content = ((BaseContent) itemView);
                        int childCount = content.getChildCount();
                        for (int i1 = 0; i1 < childCount; i1++) {
                            View childView = content.getChildAt(i1);
                            if (childView instanceof BaseCellView) {
                                BaseCellView baseCellView = (BaseCellView) childView;
                                if (baseCellView.hasFocus()) {
                                    baseCellView.setMoveState(true, true);
                                } else {
                                    baseCellView.setMoveState(false, false);
                                }
                            }
                        }
                    }
                }
                break;
            case STATE_FOLDER_OPENED:
                if (fragmentRef != null && fragmentRef.get() != null) {
                    fragmentRef.get().setKeyDragOut(false);
                }
                // Get focus view
                if (mRecyclerView.getFocusedView() != null) {
                    mFocusedView = mRecyclerView.getFocusedView();
                }
                setVisibility(View.GONE);
                break;
            case STATE_FOLDER_CLOSED:
                if (fragmentRef != null && fragmentRef.get() != null) {
                    fragmentRef.get().setKeyDragOut(true);
                    fragmentRef.get().setTouchDragOut(true);
                }
                if (getVisibility() != VISIBLE) {
                    setVisibility(VISIBLE);
                }
                mState = BaseWorkspace.State.STATE_NORMAL;
                for (int i = 0; i < count; i++) {
                    View itemView = mLayoutManager.getChildAt(i);
                    if (itemView instanceof CellView) {
                        CellView cellView = (CellView) itemView;
                        cellView.resetState();
                    } else if (itemView instanceof BaseContent) {
                        BaseContent content = ((BaseContent) itemView);
                        int childCount = content.getChildCount();
                        for (int i1 = 0; i1 < childCount; i1++) {
                            View childView = content.getChildAt(i1);
                            if (childView instanceof BaseCellView) {
                                BaseCellView baseCellView = (BaseCellView) childView;
                                baseCellView.resetState();
                            }
                        }
                    }
                }
                if (mFocusedView != null) {
                    mFocusedView.requestFocus();
                }
                break;
            case STATE_NORMAL:
                if (fragmentRef != null && fragmentRef.get() != null) {
                    fragmentRef.get().setKeyDragOut(true);
                    fragmentRef.get().setTouchDragOut(true);
                }
                if (getVisibility() != VISIBLE) {
                    setVisibility(VISIBLE);
                }
                for (int i = 0; i < count; i++) {
                    View itemView = mLayoutManager.getChildAt(i);
                    if (itemView instanceof CellView) {
                        CellView cellView = (CellView) itemView;
                        cellView.resetState();
                    } else if (itemView instanceof BaseContent) {
                        BaseContent content = ((BaseContent) itemView);
                        int childCount = content.getChildCount();
                        for (int i1 = 0; i1 < childCount; i1++) {
                            View childView = content.getChildAt(i1);
                            if (childView instanceof BaseCellView) {
                                BaseCellView baseCellView = (BaseCellView) childView;
                                baseCellView.resetState();
                            }
                        }
                    }
                }
                final int nullIndex = mAdapter.removeNUllFolder();
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (nullIndex != -1) {
                            View nullView = mLayoutManager.findViewByPosition(nullIndex);
                            if (nullView == null) {
                                if (mFocusedView != null) {
                                    Object tag = mFocusedView.getTag();
                                    if (tag instanceof String) {
                                        mFocusedView = getFocusedView((String) tag);
                                    }
                                }
                            } else {
                                mFocusedView = nullView;
                            }
                        }
                        if (mFocusedView == null) {
                            mFocusedView = findFirstCompletelyVisibleView();
                        }
                        if (mFocusedView != null) {
                            mFocusedView.requestFocus();
                        }
                    }
                }, 100);
                break;
            case STATE_ADD:
                if (getVisibility() != VISIBLE) {
                    setVisibility(VISIBLE);
                }
                for (int i = 0; i < count; i++) {
                    View itemView = mLayoutManager.getChildAt(i);
                    if (itemView instanceof CellView) {
                        CellView cellView = (CellView) itemView;
                        cellView.setAddState();
                    } else if (itemView instanceof BaseContent) {
                        BaseContent content = ((BaseContent) itemView);
                        int childCount = content.getChildCount();
                        for (int i1 = 0; i1 < childCount; i1++) {
                            View childView = content.getChildAt(i1);
                            if (childView instanceof BaseCellView) {
                                BaseCellView baseCellView = (BaseCellView) childView;
                                baseCellView.setAddState();
                            }
                        }
                    }
                }
                if (mFocusedView != null) {
                    mFocusedView.requestFocus();
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void updateEditTitle(State state) {
        View parent = (View) this.getParent();
        LetvLog.d(TAG, "updateEditTitle fragmentRef = " + fragmentRef + " state = " + state);
        switch (state) {
            case STATE_DELETE:
                if (fragmentRef != null && fragmentRef.get() != null) {
                    fragmentRef.get().hideStatusBar();
                    fragmentRef.get().hideTabView();
                }
                parent.setBackgroundResource(R.color.app_workspace_edit_state_color);
                mTopView.setVisibility(View.VISIBLE);
                mTopTitleView.setText(R.string.press_center_key_to_uninstall);
                break;
            case STATE_NEW_FOLDER:
                if (fragmentRef != null && fragmentRef.get() != null) {
                    fragmentRef.get().hideStatusBar();
                    fragmentRef.get().hideTabView();
                }
                parent.setBackgroundResource(R.color.app_workspace_edit_state_color);
                mTopView.setVisibility(View.VISIBLE);
                String stringResult = String.format(getResources().getString(R.string.press_center_key_to_add),
                        getResources().getString(R.string.default_new_folder_name));
                mTopTitleView.setText(stringResult);
                break;
            case STATE_MOVE:
                if (fragmentRef != null && fragmentRef.get() != null) {
                    fragmentRef.get().hideStatusBar();
                    fragmentRef.get().hideTabView();
                }
                parent.setBackgroundResource(R.color.app_workspace_edit_state_color);
                mTopView.setVisibility(View.VISIBLE);
                mTopTitleView.setText(R.string.press_center_key_to_confirm);
                break;
            case STATE_NORMAL:
                if (fragmentRef != null && fragmentRef.get() != null) {
                    fragmentRef.get().showStatusbar();
                    fragmentRef.get().showTabView();
                }
                if (IconFilterUtil.isUsedTheme()) {
                    parent.setBackgroundResource(R.drawable.app_fragment_background);
                } else {
                    parent.setBackground(null);
                }
                mTopView.setVisibility(View.GONE);
                break;
            case STATE_FOLDER_OPENED:
                if (fragmentRef != null && fragmentRef.get() != null) {
                    fragmentRef.get().hideStatusBar();
                    fragmentRef.get().hideTabView();
                }
                break;
            default:
                if (fragmentRef != null && fragmentRef.get() != null) {
                    fragmentRef.get().showStatusbar();
                    fragmentRef.get().showTabView();
                }
                if (IconFilterUtil.isUsedTheme()) {
                    parent.setBackgroundResource(R.drawable.app_fragment_background);
                } else {
                    parent.setBackground(null);
                }
                mTopView.setVisibility(View.GONE);
                break;
        }
    }

    private void updateIndex() {
        List<ItemInfo> itemInfos = mAdapter.getDataSet();
        Iterator<ItemInfo> infoIterator = itemInfos.iterator();
        int i = 0;
        while (infoIterator.hasNext()) {
            ItemInfo itemInfo = infoIterator.next();
            itemInfo.setIndex(i);
            if (itemInfo instanceof FolderInfo) {
                ((FolderInfo) itemInfo).setItemContainer();
            }
            i++;
        }
        ItemInfoDBHelper.getInstance().updateInTx(itemInfos);
    }

    /**
     * Notify item data has changed
     */
    @Override
    public void onUpdate(FolderInfo itemInfo) {
        LetvLog.i(TAG, " onUpdate itemInfo = " + itemInfo);
        // 文件夹中的应用移除,重置文件夹的下载进度
        /*DownloadStatusBean downloadStatusBean = itemInfo.getDownloadStatusBean();
        if (downloadStatusBean == null) {
            downloadStatusBean = new DownloadStatusBean();
            itemInfo.setDownloadStatusBean(downloadStatusBean);
        }
        downloadStatusBean.setSweepAngle(0);
        downloadStatusBean.setDownloadStatus(DownloadAppPresenter.STATE_RESET);*/
        mCibnList.remove(itemInfo);
        mAdapter.updateItem(itemInfo);
    }

    /**
     * Notify workspace has remove a icon from folder and add to workspace
     *
     * @param itemInfo
     */
    @Override
    public void onAddFromFolder(ItemInfo itemInfo) {
        LetvLog.i(TAG, " onAddFromFolder itemInfo = " + itemInfo);
        List<ItemInfo> allAppList = DataModelList.getInstance().allAppList;
        if (allAppList != null) {
            allAppList.add(itemInfo);
        }
        mAdapter.addItem(itemInfo);
    }

    /**
     * Notify app workspace delete folder
     *
     * @param folderInfo
     */
    @Override
    public void onDeleteFolder(FolderInfo folderInfo) {
        LetvLog.i(TAG, " onDeleteFolder  mState = " + mState + " mFocusedView = " + mFocusedView);
        int index = mAdapter.getDataSet().indexOf(folderInfo);
        if (index != -1) {
            index = index + mAdapter.getHeaderSize();
        }
        LetvLog.i(TAG, " onDeleteFolder remove index = " + index);
        mAdapter.removeItem(folderInfo);
        List<ItemInfo> allAppList = DataModelList.getInstance().allAppList;
        if (allAppList != null) {
            allAppList.remove(folderInfo);
        }
        final int focusIndex = index;// mAdapter.removeFolder(folderInfo);

        postDelayed(new Runnable() {
            @Override
            public void run() {
                // Should set top layout can focus
                mFocusedView = mLayoutManager.findViewByPosition(focusIndex);
                if (mFocusedView == null) {
                    mFocusedView = findLastCompletelyVisibleView();
                }
                LetvLog.i(TAG, " onDeleteFolder mFocusedView = " + mFocusedView);
                if (mFocusedView != null) {
                    mFocusedView.setFocusable(true);
                    mFocusedView.requestFocus();
                } else {
                    resetScroll();
                }
            }
        }, 100);
        mCibnList.remove(folderInfo);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCloseFolder() {
        //LetvLog.d(TAG, "onCloseFolder");
        updateEditState(BaseWorkspace.State.STATE_FOLDER_CLOSED, false);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAddApp() {
        updateEditState(BaseWorkspace.State.STATE_ADD, false);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void newFolder() {
        View focusedChild = mRecyclerView.getFocusedChild();
        int insertPosition = 0;
        if (focusedChild instanceof BaseContent) {
            LetvLog.i(TAG, "newFolder focus is in header.");
            insertPosition = mAdapter.getHeaderSize();
            mFocusedView = mRecyclerView.getFocusedView();
        } else {
            mFocusedView = focusedChild;
            if (mFocusedView != null) {
                insertPosition = mLayoutManager.getPosition(mFocusedView);
            }
        }
        updateEditState(State.STATE_NEW_FOLDER, false);

        FolderInfo folderInfo = new FolderInfo();
        folderInfo.isAdding = true;
        ItemInfo itemInfo = mAdapter.getItemInfoByPosition(insertPosition);
        int index = itemInfo.getIndex();
        LetvLog.d(TAG, "newFolder index = " + index);
        folderInfo.setIndex(index);
        folderInfo.setTitle(AppDataModel.getInstance().getNewFolderTitle());
        mAdapter.addItem(folderInfo, insertPosition);
        List<ItemInfo> allAppList = DataModelList.getInstance().allAppList;
        if (allAppList != null) {
            allAppList.add((insertPosition - mAdapter.getHeaderSize()), folderInfo);
        }
        final int finalInsertPosition = insertPosition;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                View view = mLayoutManager.findViewByPosition(finalInsertPosition);
                if (view != null) {
                    view.requestFocus();
                    mFocusedView = view;
                }
            }
        }, 50);
    }

    @Override
    public void onAppAdded(ArrayList<ItemInfo> adds) {
        LetvLog.i(TAG, " onAppAdded adds = " + adds + " state = " + mState);
        if (adds != null && adds.size() > 0) {
            LetvLog.d(TAG, "onAppAdded size = " + adds.size());
            for (ItemInfo add : adds) {
                mAdapter.addItem(add);
            }
        }
    }

    @Override
    public void onAppRemoved(ArrayList<ItemInfo> removeList, ArrayList<ItemInfo> removeContainFolderList) {
        if (removeContainFolderList == null || removeContainFolderList.size() == 0) {
            return;
        }
        // Must call before init
        String tag = findFocusedViewMark();
        LetvLog.d(TAG, "onAppRemoved tag = " + tag);
        for (ItemInfo remove : removeContainFolderList) {
            mAdapter.removeItem(remove);
            mCibnList.remove(remove);
        }
        Message msg = mHandler.obtainMessage(MSG_FIND_UPDATE_FOCUSVIEW);
        msg.obj = tag;
        mHandler.sendMessageDelayed(msg, 100);
    }

    @Override
    public void onAppUpdated(ArrayList<ItemInfo> updateList, ArrayList<ItemInfo> updateContainFolderList) {
        LetvLog.i(TAG, " onAppUpdated updateContainFolderList = " + updateContainFolderList);
        if (updateContainFolderList == null || updateContainFolderList.size() == 0) {
            return;
        }
        // Must call before init
        String focusedMark = findFocusedViewMark();

        for (ItemInfo updateItemInfo : updateContainFolderList) {
            mAdapter.updateItem(updateItemInfo);
        }
        Message msg = mHandler.obtainMessage(MSG_FIND_UPDATE_FOCUSVIEW);
        msg.obj = focusedMark;
        mHandler.sendMessageDelayed(msg, 100);
    }

    @Override
    public void onSuperscriptChange(ItemInfo update, FolderInfo inFolderItemInfo) {
        LetvLog.i(TAG, " onSuperscriptChange update = " + update + " inFolderItemInfo = " + inFolderItemInfo);
        if (update == null) {
            return;
        }
        // Must call before init
        String tag = findFocusedViewMark();
        if (inFolderItemInfo != null) {
            mAdapter.updateItem(inFolderItemInfo);
        } else {
            mAdapter.updateItem(update);
        }
        Message msg = mHandler.obtainMessage(MSG_FIND_UPDATE_FOCUSVIEW);
        msg.obj = tag;
        mHandler.sendMessageDelayed(msg, 100);
    }

    @Override
    public void onStateChange(List<ItemInfo> posterList, ItemInfo itemInfo, FolderInfo folderInfo) {
        LetvLog.d(TAG, " onStateChange update = " + itemInfo + " folderInfo = " + folderInfo);
        if (posterList != null && posterList.size() > 0) {
            mPosterList.addAll(posterList);
        }

        ItemInfo info = itemInfo;
        if (folderInfo != null) {
            info = folderInfo;
        }
        mCibnList.add(info);

        if (getVisibility() != View.VISIBLE || !isVisibleToUser) {
            return;
        }
        updateState();
    }

    private void updateState() {
        if (mLayoutManager == null || (mCibnList.size() == 0 && mPosterList.size() == 0)) {
            return;
        }

        int childCount = mLayoutManager.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View itemView = mLayoutManager.getChildAt(i);
            if (itemView instanceof BaseContent) {
                BaseContent baseContent = (BaseContent) itemView;
                int count = baseContent.getChildCount();
                for (int i1 = 0; i1 < count; i1++) {
                    View view = baseContent.getChildAt(i1);
                    if (view instanceof PosterCellView) {
                        final PosterCellView posterCellView = (PosterCellView) view;
                        final ItemInfo itemInfo = posterCellView.getItemInfo();
                        if (itemInfo != null && mPosterList.contains(itemInfo)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    posterCellView.invalidate();
                                    /*DownloadStatusBean downloadStatusBean = itemInfo.getDownloadStatusBean();
                                    if (downloadStatusBean == null) {
                                        mPosterList.remove(itemInfo);
                                    } else {
                                        if (downloadStatusBean.getDownloadStatus() == DownloadAppPresenter.STATE_INSTALLED ||
                                                downloadStatusBean.getDownloadStatus() == DownloadAppPresenter.STATE_RESET ||
                                                downloadStatusBean.getDownloadStatus() == null) {
                                            // 说明已经安装完成，在队列中移除
                                            mPosterList.remove(itemInfo);
                                        }
                                    }*/
                                }
                            });
                        }
                    }
                }
            } else if (itemView instanceof BaseCellView) {
                final BaseCellView cellView = (BaseCellView) itemView;
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

    private void runOnUiThread(Runnable r) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            r.run();
        } else {
            mHandler.post(r);
        }
    }

    private void refreshDownloadState(int visibility) {
        if (visibility != View.VISIBLE) {
            return;
        }
        updateState();
    }

    private View findFirstCompletelyVisibleView() {
        int pos = mLayoutManager.findFirstCompletelyVisibleItemPosition();
        View view = mLayoutManager.findViewByPosition(pos);
        if (view instanceof BaseContent) {
            view = ((BaseContent) view).getChildAt(0);
        }
        return view;
    }

    private View findLastCompletelyVisibleView() {
        int pos = mLayoutManager.findLastCompletelyVisibleItemPosition();
        View view = mLayoutManager.findViewByPosition(pos);
        if (view instanceof BaseContent) {
            view = ((BaseContent) view).getChildAt(0);
        }
        return view;
    }

    private String findFocusedViewMark() {
        String mark = null;
        if (mFocusedView instanceof PosterCellView) {
            mark = (String) mFocusedView.getTag();
        } else if (mFocusedView instanceof CellView) {
            ItemInfo itemInfo = ((CellView) mFocusedView).getItemInfo();
            List<ItemInfo> dataSet = mAdapter.getDataSet();
            int index = dataSet.indexOf(itemInfo);
            if (index != -1) {
                mark = (index + mAdapter.getHeaderSize()) + "";
            }
        }
        LetvLog.d(TAG, "findFocusedViewMark mark = " + mark);
        return mark;
    }

    private View getFocusedView(String tag) {
        View focusedView = null;
        if (tag != null) {
            if (tag.contains(",")) {
                //说明是顶部海报位
                String[] split = tag.split(",");
                if (split.length == 2) {
                    String posStr = split[0];
                    try {
                        int pos = Integer.parseInt(posStr);
                        View view = mLayoutManager.findViewByPosition(pos);
                        if (view instanceof BaseContent) {
                            BaseContent baseContent = (BaseContent) view;
                            int childCount = baseContent.getChildCount();
                            for (int i = 0; i < childCount; i++) {
                                View childView = baseContent.getChildAt(i);
                                if (tag.equals(childView.getTag())) {
                                    focusedView = childView;
                                    break;
                                }
                            }
                        }
                    } catch (NumberFormatException ex) {
                    }
                }
            } else {
                try {
                    int pos = Integer.parseInt(tag);
                    focusedView = mLayoutManager.findViewByPosition(pos);
                } catch (NumberFormatException ex) {

                }
            }
        }

        if (focusedView == null) {
            focusedView = findLastCompletelyVisibleView();
        }

        ItemInfo itemInfo = null;
        if (focusedView instanceof BaseCellView) {
            BaseCellView baseCellView = (BaseCellView) focusedView;
            itemInfo = baseCellView.getItemInfo();
        }
        LetvLog.i(TAG, "getFocusedView tag = " + tag + " itemInfo = " + itemInfo);
        return focusedView;
    }

    /**
     * Call when home key event.
     *
     * @return if true , key event is consumed and not back to home.
     */
    @Override
    public boolean backToHome() {
        boolean consumed = false;
        if (mState != State.STATE_NORMAL) {
            updateEditState(State.STATE_NORMAL, false);
            // only exit edit state
            consumed = true;
        } else {
            mFocusedView = null;
            resetScroll();
            consumed = false;
        }
        return consumed;
    }

    public void resetScrollAndBackToTab() {
        mRecyclerView.resetSmoothScroll();
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.resetScroll();
            }
        }, 200);
        if (fragmentRef != null && fragmentRef.get() != null) {
            fragmentRef.get().backToTab();
        }
    }

    public int getPageStatus() {
        return mLayoutManager.getPageStatus();
    }

    public void openFolder(FolderInfo folderInfo) {
        updateEditState(BaseWorkspace.State.STATE_FOLDER_OPENED, false);
        if (folderInfo.getLength() != 0) {
            if (fragmentRef != null && fragmentRef.get() != null) {
                AppFolderWorkspace folderWorkspace = fragmentRef.get().getFolderWorkspace();
                folderWorkspace.show(folderInfo);
            }
        } else {
            mAdapter.removeItem(folderInfo);
        }
    }

    /**
     * 移除所有添加的文件夹语音.
     */
    public void removeAllFolderVoice() {
        int childCount = mLayoutManager.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = mLayoutManager.getChildAt(i);
            if (view instanceof AppFolderCellView) {
                AppFolderCellView folderCellView = (AppFolderCellView) view;
                ItemInfo itemInfo = folderCellView.getItemInfo();
                LetvLog.d(TAG, "removeAllFolderVoice itemInfo = " + itemInfo);
                if (itemInfo instanceof FolderInfo) {
                    mVoiceFolderList.remove(itemInfo);
                }
            }
        }
    }

    @Override
    public void onRelease() {
        super.onRelease();
        mAdapter.setAppFragment(null);
        mAdapter.setOnFolderVoiceListener(null);
        mRecyclerView.setAdapter(null);
        mRecyclerView.setOnScrollListener(null);
        mAdapter = null;
        mLayoutManager = null;
        mRecyclerView = null;
        mFocusedView = null;
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mVoiceFolderList.clear();
        mCibnList.clear();
        mPosterList.clear();
    }
}
