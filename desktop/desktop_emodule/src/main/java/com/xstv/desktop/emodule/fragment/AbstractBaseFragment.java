package com.xstv.desktop.emodule.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xstv.library.base.BaseFragment;

public class AbstractBaseFragment extends BaseFragment {

    @Override
    public View onInflaterContent(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onFragmentSeletedPre(boolean gainSelect) {

    }

    @Override
    public void onFragmentShowChanged(boolean gainShow) {

    }

    @Override
    public boolean onFocusRequested(int requestDirection) {
        return false;
    }

    @Override
    public boolean onHomeKeyEventHandled() {
        return false;
    }

    @Override
    public void setHoverListener(View.OnHoverListener listener) {

    }
}
