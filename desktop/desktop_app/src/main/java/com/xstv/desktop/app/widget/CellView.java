
package com.xstv.desktop.app.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xstv.base.LetvLog;
import com.xstv.desktop.app.R;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.util.IconFilterUtil;
import com.xstv.desktop.app.util.Utilities;

public abstract class CellView<T extends ItemInfo> extends BaseCellView<T> {

    private static final String TAG = CellView.class.getSimpleName();

    private final int arrowPadding = -35;

    // 显示数字类型的消息，如果消息数大于99，则显示“99+”，否则显示实际的未读消息数
    private static final int APP_BADGE_MSG_TYPE_DIGIT = 0;
    // 显示文字类型的消息，如系统升级，显示“new”
    private static final int APP_BADGE_MSG_TYPE_TEXT = 1;

    /**
     * Use in move state
     */
    protected boolean canMove = false;
    /**
     * Use for notify superscript
     */
    private TextView mSuperscriptView = null;

    /**
     * Icon of edit state
     */
    private ImageView mEditIcon;
    private ImageView mMaskView;
    private ImageView leftArrow, rightArrow, upArrow, downArrow;
    private Drawable mLeftShadeBorder, mTopShadeBorder, mRightShadeBorder, mBottomShadeBorder;
    private CircleProgress1 mCircleProgress;
    protected int mWidth;
    protected int mHight;

    public CellView(Context context) {
        this(context, null);
    }

    public CellView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CellView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void initData(Context context, AttributeSet attrs, int defStyle) {
        super.initData(context, attrs, defStyle);
        Resources r = context.getResources();
        mWidth = r.getDimensionPixelSize(R.dimen.poster_workspace_item_width1);
        mHight = r.getDimensionPixelSize(R.dimen.poster_workspace_item_height2);

        mLeftShadeBorder = getResources().getDrawable(R.drawable.left);
        Rect leftRect = new Rect();
        leftRect.left = -5;
        leftRect.top = 0;
        leftRect.right = 0;
        leftRect.bottom = mHight;
        mLeftShadeBorder.setBounds(leftRect);

        mTopShadeBorder = getResources().getDrawable(R.drawable.top);
        Rect topRect = new Rect();
        topRect.left = 0;
        topRect.top = -5;
        topRect.right = mWidth;
        topRect.bottom = 0;
        mTopShadeBorder.setBounds(topRect);

        mRightShadeBorder = getResources().getDrawable(R.drawable.right);
        Rect rightRect = new Rect();
        rightRect.left = mWidth;
        rightRect.top = 0;
        rightRect.right = mWidth + 5;
        rightRect.bottom = mHight;
        mRightShadeBorder.setBounds(rightRect);

        mBottomShadeBorder = getResources().getDrawable(R.drawable.bottom);
        Rect bottomRect = new Rect();
        bottomRect.left = 0;
        bottomRect.top = mHight;
        bottomRect.right = mWidth;
        bottomRect.bottom = mHight + 2;
        mBottomShadeBorder.setBounds(bottomRect);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int myWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(mWidth, View.MeasureSpec.EXACTLY);
        int myHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(mHight, View.MeasureSpec.EXACTLY);
        super.onMeasure(myWidthMeasureSpec, myHeightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!IconFilterUtil.isUsedTheme()) {
            mLeftShadeBorder.draw(canvas);
            mTopShadeBorder.draw(canvas);
            mRightShadeBorder.draw(canvas);
            mBottomShadeBorder.draw(canvas);
        }
        if (isFocused()) {
            if (mState == BaseWorkspace.State.STATE_DELETE) {
                setDeleteState(true);
            } else if (mState == BaseWorkspace.State.STATE_MOVE) {
                setMoveState(true, canMove);
            }
        } else {
            if ((mState == BaseWorkspace.State.STATE_DELETE)) {
                resetDeleteState();
            } else if (mState == BaseWorkspace.State.STATE_MOVE) {
                setMoveState(false, false);
            }
        }
    }

    @Override
    protected Bitmap getLoadingBitmap(float sweepAngle) {
        if (mCircleProgress == null) {
            mCircleProgress = new CircleProgress1(getContext());
        }
        mCircleProgress.setMarginTop(20);
        Bitmap bitmap = mCircleProgress.createProgress(sweepAngle, getWidth(), getHeight(),
                getLoadingTextSize(), getLoadingTextPoint(), getLoadingText());
        return bitmap;
    }

    protected abstract String getLoadingText();

    @Override
    public boolean actionClickOrEnterKey() {
        LetvLog.d(TAG, " actionClickOrEnterKey mState = " + mState);
        boolean consumed = false;
        switch (mState) {
            case STATE_NORMAL:
                openCellView();
                if (Utilities.isSupportCTR()) {
                    if (getParent() instanceof RecyclerView) {
                        RecyclerView recyclerView = (RecyclerView) getParent();
                        int pos = recyclerView.getChildPosition(this);
                        String tag = (String) getTag();
                    }
                }
                consumed = true;
                break;
            case STATE_DELETE:
                removeCellView();
                consumed = true;
                break;
            case STATE_ADD:
            case STATE_NEW_FOLDER:
                /**
                 * add logic in workspace
                 *
                 * @see AppWorkspace#addShortcutToFolder()
                 */
                consumed = false;
                break;
            case STATE_MOVE:
                canMove = !canMove;
                setMoveState(isFocused(), canMove);
                consumed = true;
                break;
        }
        return consumed;
    }

    /**
     * 正常模式下,响应click事件
     */
    public abstract void openCellView();

    /**
     * 删除模式下,响应删除应用
     */
    public abstract void removeCellView();

    public void setMoveState(boolean isFocus, boolean canMove) {
        super.setMoveState(isFocus, canMove);
        //LetvLog.d(TAG, "setMoveState isFocus = " + isFocus + " canMove = " + canMove);
        this.canMove = canMove;
        updateArrow(canMove);
        if (isFocus && !canMove) {
            setMaskView();
            setEditIcon(R.drawable.ic_home_app_move);
        } else {
            removeMaskView();
            setEditIcon(-1);
        }
    }

    public void updateArrow(boolean visible) {
        if (visible) {
            if(getLayerType() == LAYER_TYPE_HARDWARE){
                setLayerType(LAYER_TYPE_NONE, null);
            }
            View focusDown = focusSearch(this, View.FOCUS_DOWN);
            if (focusDown == null) {
                //LetvLog.d(TAG, " updateArrow FOCUS_DOWN null ");
                removeArrowView(downArrow);
            } else {
                downArrow = setArrowView(downArrow, View.FOCUS_DOWN);
            }

            View focusUp = focusSearch(this, View.FOCUS_UP);
            if (focusUp == null) {
                //LetvLog.d(TAG, " updateArrow FOCUS_UP null ");
                removeArrowView(upArrow);
            } else {
                if (focusUp instanceof PosterCellView) {
                    removeArrowView(upArrow);
                } else {
                    upArrow = setArrowView(upArrow, View.FOCUS_UP);
                }
            }

            View focusLeft = focusSearch(this, View.FOCUS_LEFT);
            if (focusLeft == null) {
                //LetvLog.d(TAG, " updateArrow FOCUS_LEFT null ");
                removeArrowView(leftArrow);
            } else {
                leftArrow = setArrowView(leftArrow, View.FOCUS_LEFT);
            }

            View focusRight = focusSearch(this, View.FOCUS_RIGHT);
            if (focusRight == null) {
                //LetvLog.d(TAG, " updateArrow FOCUS_RIGHT null ");
                removeArrowView(rightArrow);
            } else {
                rightArrow = setArrowView(rightArrow, View.FOCUS_RIGHT);
            }
        } else {
            if(getLayerType() == LAYER_TYPE_NONE){
                setLayerType(LAYER_TYPE_HARDWARE, null);
            }
            removeArrowView(leftArrow);
            removeArrowView(upArrow);
            removeArrowView(rightArrow);
            removeArrowView(downArrow);
        }
    }

    private void removeArrowView(View view) {
        if (view != null && indexOfChild(view) != -1) {
            removeView(view);
        }
    }

    private ImageView setArrowView(ImageView view, int direct) {
        int left = 0;
        int up = 0;
        int right = 0;
        int down = 0;
        int rule1;
        int rule2;
        int resId;
        switch (direct) {
            case View.FOCUS_LEFT:
                left = arrowPadding;
                rule1 = RelativeLayout.ALIGN_PARENT_LEFT;
                rule2 = RelativeLayout.CENTER_VERTICAL;
                resId = R.drawable.ic_list_arrow_left;
                break;
            case View.FOCUS_UP:
                up = arrowPadding;
                rule1 = RelativeLayout.ALIGN_PARENT_TOP;
                rule2 = RelativeLayout.CENTER_HORIZONTAL;
                resId = R.drawable.ic_list_arrow_up;
                break;
            case View.FOCUS_RIGHT:
                right = arrowPadding;
                rule1 = RelativeLayout.ALIGN_PARENT_RIGHT;
                rule2 = RelativeLayout.CENTER_VERTICAL;
                resId = R.drawable.ic_list_arrow_right;
                break;
            case View.FOCUS_DOWN:
                down = arrowPadding;
                rule1 = RelativeLayout.ALIGN_PARENT_BOTTOM;
                rule2 = RelativeLayout.CENTER_HORIZONTAL;
                resId = R.drawable.ic_list_arrow_down;
                break;
            default:
                rule1 = -1;
                rule2 = -1;
                resId = -1;
                break;
        }
        if (view == null) {
            view = new ImageView(mContext);
            if (resId != -1) {
                view.setImageResource(resId);
            }
            view.setFocusable(false);
            view.setFocusableInTouchMode(false);
            view.setPadding(left, up, right, down);
            RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (rule1 != -1 && rule2 != -1) {
                relativeParams.addRule(rule1);
                relativeParams.addRule(rule2);
            }
            addView(view, relativeParams);
        }
        if (indexOfChild(view) == -1) {
            addView(view, view.getLayoutParams());
        }

        return view;
    }

    public void setMaskView() {
        if (mMaskView == null) {
            mMaskView = (ImageView) LayoutInflater.from(mContext).inflate(R.layout.app_mask_view, null);
        }
        if (indexOfChild(mMaskView) == -1) {
            addView(mMaskView);
        }
    }

    /**
     * Set edit state icon
     *
     * @param iconID
     */
    public void setEditIcon(int iconID) {
        if (iconID > 0) {
            setEditView(iconID);
        } else {
            removeEditView();
        }
    }

    private void setEditView(int iconID) {
        int topMargin = mContext.getResources().getDimensionPixelOffset(R.dimen.app_cell_view_icon_marginTop);
        int topAddMargin = mContext.getResources().getDimensionPixelOffset(R.dimen.app_cell_view_icon_add_marginTop);
        if (mEditIcon == null) {
            mEditIcon = (ImageView) LayoutInflater.from(mContext).inflate(R.layout.app_edit_view, null);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            if (iconID == R.drawable.ic_home_app_add) {
                // layoutParams.topMargin = 0;
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            } else {
                layoutParams.topMargin = topMargin;
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            }
            mEditIcon.setFocusable(false);
            mEditIcon.setFocusableInTouchMode(false);
            addView(mEditIcon, layoutParams);
        }
        if (indexOfChild(mEditIcon) == -1) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mEditIcon.getLayoutParams();
            if (iconID == R.drawable.ic_home_app_add) {
                params.topMargin = topAddMargin;
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            } else {
                params.topMargin = topMargin;
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            }
            mEditIcon.setFocusable(false);
            mEditIcon.setFocusableInTouchMode(false);
            addView(mEditIcon, params);
        }
        mEditIcon.setImageResource(iconID);
    }

    private void removeEditView() {
        if (mEditIcon != null && indexOfChild(mEditIcon) != -1) {
            removeView(mEditIcon);
        }
    }

    public void removeMaskView() {
        if (mMaskView != null && indexOfChild(mMaskView) != -1) {
            removeView(mMaskView);
        }
    }

    public void updateSuperscriptView(ItemInfo itemInfo) {
        if (itemInfo == null) {
            return;
        }
        LetvLog.i(TAG, " updateSuperscriptView msg count = " + itemInfo.superscriptCount
                + "type = " + itemInfo.superscriptType);

        int count = itemInfo.superscriptCount;
        if (count <= 0) {
            removeSuperscriptView();
            return;
        }

        Resources resource = mContext.getResources();
        if (mSuperscriptView == null) {
            mSuperscriptView = (TextView) LayoutInflater.from(mContext).inflate(R.layout.app_isnew_view, null);
            int superscriptHeight = resource.getDimensionPixelOffset(R.dimen.app_cell_view_superscript_height);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, superscriptHeight);
            int margin = resource.getDimensionPixelOffset(R.dimen.app_cell_view_superscript_margin);
            layoutParams.topMargin = margin;
            layoutParams.rightMargin = margin;
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            addView(mSuperscriptView, layoutParams);
        }
        if (indexOfChild(mSuperscriptView) == -1) {
            addView(mSuperscriptView, mSuperscriptView.getLayoutParams());
        }
        int smallTextSize = resource.getDimensionPixelSize(R.dimen.app_cell_view_superscript_system_textSize);
        int textSize = resource.getDimensionPixelSize(R.dimen.app_cell_view_superscript_msg_textSize);
        int padding = resource.getDimensionPixelOffset(R.dimen.app_cell_view_superscript_padding);
        int leftPadding = 0;
        int topPadding = 0;
        int rightPadding = 0;
        int bottomPadding = 0;
        String superscriptStr = "";
        int type = itemInfo.superscriptType;
        if (type == APP_BADGE_MSG_TYPE_TEXT) {
            superscriptStr = resource.getString(R.string.superscript_new);
            mSuperscriptView.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            leftPadding = padding;
            rightPadding = padding;
        } else if (type == APP_BADGE_MSG_TYPE_DIGIT) {
            mSuperscriptView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            if (count > 9) {
                leftPadding = padding;
                rightPadding = padding;
            }
            if (count < 100) {
                superscriptStr = count + "";
            } else {
                superscriptStr = "99+";
            }
        }
        mSuperscriptView.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
        mSuperscriptView.setText(superscriptStr);
    }

    private void removeSuperscriptView() {
        if (mSuperscriptView != null && indexOfChild(mSuperscriptView) != -1) {
            removeView(mSuperscriptView);
        }
    }

    public void recycle(boolean isCleanView){

    }

}
