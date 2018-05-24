
package com.xstv.launcher.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xstv.library.base.BaseFragment;
import com.xstv.library.base.LetvLog;
import com.xstv.launcher.R;

public class EmptyFragment extends BaseFragment {

    private static final String TAG = EmptyFragment.class.getSimpleName();
    private TextView mTv;

    public EmptyFragment() {
        setInstanceCacheEnabled(false);
    }

    @Override
    public View onInflaterContent(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LetvLog.d(TAG, "[" + tag + "] onInflaterContent ");
        View view = inflater.inflate(R.layout.fragment_empty, null);
        mTv = view.findViewById(R.id.tv);
        mTv.setText("EmptyFragment");
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LetvLog.d(TAG, "[" + tag + "] onViewCreated ");
    }

    @Override
    public void onFragmentSeletedPre(boolean gainSelect) {
        LetvLog.d(TAG, "[" + tag + "] onFragmentSeletedPre " + gainSelect);
        if (gainSelect) {
            /** begin to enter current page with animation. */
            if (getActivity() != null && mTv != null) {
                mTv.setText(tag);
            }

        } else {
            /** begin to leave current page with animation. */
            // TODO should be cancel some task.
        }
    }

    @Override
    public void onFragmentShowChanged(boolean gainShow) {
        LetvLog.d(TAG, "[" + tag + "] onFragmentShowChanged " + gainShow);
        if (gainShow) {
            /** enter current page after animation. */
        } else {
            /** leave current page after animation. */
        }
    }

    @Override
    public boolean onFocusRequested(int requestDirection) {
        LetvLog.d(TAG, "[" + tag + "] onFocusRequested " + requestDirection);
        return getView() != null && getView().findViewById(R.id.btn_action).requestFocus();
    }

    @Override
    public boolean onHomeKeyEventHandled() {
        return false;
    }

    @Override
    public void setHoverListener(View.OnHoverListener listener) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LetvLog.d(TAG, "[" + tag + "] onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LetvLog.d(TAG, "[" + tag + "] onDestroy");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LetvLog.d(TAG, "[" + tag + "] onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LetvLog.d(TAG, "[" + tag + "] onDetach");
    }

    @Override
    public void onPause() {
        super.onPause();
        LetvLog.d(TAG, "[" + tag + "] onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        LetvLog.d(TAG, "[" + tag + "] onStop");
    }

    @Override
    public void onResume() {
        super.onResume();
        LetvLog.d(TAG, "[" + tag + "] onResume");
    }

    @Override
    public void onStart() {
        super.onStart();
        LetvLog.d(TAG, "[" + tag + "] onStart");
    }
}
