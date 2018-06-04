package com.xstv.desktop.emodule.fragment;

import android.os.Bundle;
import android.support.v17.leanback.widget.Presenter;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.xstv.desktop.emodule.R;
import com.xstv.desktop.emodule.adapter.BlockAdapter;
import com.xstv.desktop.emodule.mode.Block;
import com.xstv.desktop.emodule.mode.DisplayItem;
import com.xstv.desktop.emodule.presenter.BaseRowPresenter;
import com.xstv.desktop.emodule.presenter.CommonPresenterSelector;
import com.xstv.desktop.emodule.util.Utils;
import com.xstv.library.base.Logger;

import java.util.ArrayList;
import java.util.Random;

public class ModuleFragment extends PageRowsFragment {

    Logger mLogger = Logger.getLogger("EModule", "ModuleFragment");
    boolean hasBindData = false;
    String index = "1";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        index = getArguments().getString("index");
    }

    @Override
    public void onFragmentOffsetChanged(int offset) {
        mLogger.d("offset=" + offset);
        super.onFragmentOffsetChanged(offset);
    }

    @Override
    public boolean onFocusRequested(int requestDirection) {
        if (getVerticalGridView() != null) {
            View search = getVerticalGridView().focusSearch(View.FOCUS_DOWN);
            mLogger.d("xubin, focus down, search view= " + search);
            return getVerticalGridView().requestFocus();
        }
        return super.onFocusRequested(requestDirection);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLogger.d("onViewCreated");
        getVerticalGridView().addOnScrollListener(new RecyclerView.OnScrollListener() {

            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                Utils.setScrollState(newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Utils.setScrolling(false);
                } else {
                    Utils.setScrolling(true);
                }
            }

            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            }
        });

        if (isInitEnabled() && !hasBindData) {
            hasBindData = true;
            if ("1".equals(index)) {
                test();
            } else {
                test2();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hasBindData = false;
    }

    private void test() {
        final Block root = new Block();
        root.items = new ArrayList();
        for (int i = 0; i < 4; i++) {
            Block block = new Block();
            if (i == 0) {
                block.title = "";
                block.ui = new Block.UI();
                block.ui.ui_type = Block.UIType.T3;
                block.ui.rows = 4;
                block.ui.columns = 7;
                block.ui.height = 600;
                block.items = new ArrayList();
                for (int i1 = 0; i1 < 10; i1++) {
                    DisplayItem item = new DisplayItem();
                    item.title = "staggered_item" + i1;
                    item.default_res_id = R.drawable.ic_launcher;
                    item.src = srcs[new Random().nextInt(srcs.length - 1)];
                    item.ui = new Block.UI();
                    item.ui.ui_type = Block.UIType.T5;
                    if (i1 == 0) {
                        item.ui.rowSpan = 4;
                        item.ui.columnSpan = 2;
                    } else if (i1 == 1) {
                        item.ui.rowSpan = 2;
                        item.ui.columnSpan = 2;
                    } else {
                        item.ui.rowSpan = 2;
                        item.ui.columnSpan = 1;
                    }
                    block.items.add(item);
                }
            } else if (i == 1) {
                block.title = "横向列表";
                block.ui = new Block.UI();
                block.ui.ui_type = Block.UIType.T1;
                block.ui.columns = 5;
                block.ui.ratio = 1.75f;
                block.items = new ArrayList();
                for (int i1 = 0; i1 < 15; i1++) {
                    DisplayItem item = new DisplayItem();
                    item.title = "staggered_item" + i1;
                    item.default_res_id = R.drawable.ic_launcher;
                    item.src = srcs[new Random().nextInt(srcs.length - 1)];
                    item.ui = new Block.UI();
                    item.ui.ui_type = Block.UIType.T4;
                    block.items.add(item);
                }
            } else if (i == 2) {
                block.title = "横向列表";
                block.ui = new Block.UI();
                block.ui.ui_type = Block.UIType.T1;
                block.ui.columns = 5;
                block.ui.ratio = 1.75f;
                block.items = new ArrayList();
                for (int i1 = 0; i1 < 15; i1++) {
                    DisplayItem item = new DisplayItem();
                    item.title = "staggered_item" + i1;
                    item.default_res_id = R.drawable.ic_launcher;
                    item.src = srcs[new Random().nextInt(srcs.length - 1)];
                    item.ui = new Block.UI();
                    item.ui.ui_type = Block.UIType.T4;
                    block.items.add(item);
                }
            } else if (i == 3) {
                block.title = "GridView";
                block.ui = new Block.UI();
                block.ui.ui_type = Block.UIType.T2;
                block.ui.columns = 5;
                block.ui.ratio = 1.3f;
                block.items = new ArrayList();
                for (int i1 = 0; i1 < 15; i1++) {
                    DisplayItem item = new DisplayItem();
                    item.title = "staggered_item" + i1;
                    item.default_res_id = R.drawable.ic_launcher;
                    item.src = srcs[new Random().nextInt(srcs.length - 1)];
                    item.ui = new Block.UI();
                    item.ui.ui_type = Block.UIType.T4;
                    block.items.add(item);
                }
            } else {

            }
            root.items.add(block);
        }

        //mLogger.d("test block " + root);
        BlockAdapter blockAdapter = new BlockAdapter(root, new CommonPresenterSelector());
        setAdapter(blockAdapter);
        setSelectedPosition(0, false);

        setOnItemViewClickedListener(new BaseRowPresenter.OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, BaseRowPresenter.ViewHolder rowViewHolder, Object row) {

                Toast.makeText(getActivity(), "row=" + ((DisplayItem) row).title + ", staggered_item=" + ((DisplayItem) item).title, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void test2() {
        final Block root = new Block();
        root.items = new ArrayList();
        for (int i = 0; i < 4; i++) {
            Block block = new Block();
            if (i == 0) {
                block.ui = new Block.UI();
                block.ui.ui_type = Block.UIType.T1;
                block.ui.columns = 5;
                block.ui.ratio = 1.75f;
                block.items = new ArrayList();
                for (int i1 = 0; i1 < 15; i1++) {
                    DisplayItem item = new DisplayItem();
                    item.title = "staggered_item" + i1;
                    item.default_res_id = R.drawable.ic_launcher;
                    item.src = srcs[new Random().nextInt(srcs.length - 1)];
                    item.ui = new Block.UI();
                    item.ui.ui_type = Block.UIType.T4;
                    block.items.add(item);
                }
            } else if (i == 1) {
                block.title = "等比布局";
                block.title = "";
                block.ui = new Block.UI();
                block.ui.ui_type = Block.UIType.T3;
                block.ui.rows = 4;
                block.ui.columns = 8;
                block.ui.height = 600;
                block.items = new ArrayList();
                for (int i1 = 0; i1 < 4; i1++) {
                    DisplayItem item = new DisplayItem();
                    item.title = "staggered_item" + i1;
                    item.default_res_id = R.drawable.ic_launcher;
                    item.src = srcs[new Random().nextInt(srcs.length - 1)];
                    item.ui = new Block.UI();
                    item.ui.ui_type = Block.UIType.T5;
                    if (i1 == 0) {
                        item.ui.rowSpan = 4;
                        item.ui.columnSpan = 2;
                    } else if (i1 == 1) {
                        item.ui.rowSpan = 4;
                        item.ui.columnSpan = 2;
                    } else {
                        item.ui.rowSpan = 2;
                        item.ui.columnSpan = 4;
                    }
                    block.items.add(item);
                }
            } else if (i == 2) {
                block.title = "横向列表";
                block.ui = new Block.UI();
                block.ui.ui_type = Block.UIType.T1;
                block.ui.columns = 5;
                block.ui.ratio = 1.75f;
                block.items = new ArrayList();
                for (int i1 = 0; i1 < 15; i1++) {
                    DisplayItem item = new DisplayItem();
                    item.title = "staggered_item" + i1;
                    item.default_res_id = R.drawable.ic_launcher;
                    item.src = srcs[new Random().nextInt(srcs.length - 1)];
                    item.ui = new Block.UI();
                    item.ui.ui_type = Block.UIType.T4;
                    block.items.add(item);
                }
            } else if (i == 3) {
                block.title = "GridView";
                block.ui = new Block.UI();
                block.ui.ui_type = Block.UIType.T2;
                block.ui.columns = 5;
                block.ui.ratio = 1.3f;
                block.items = new ArrayList();
                for (int i1 = 0; i1 < 15; i1++) {
                    DisplayItem item = new DisplayItem();
                    item.title = "staggered_item" + i1;
                    item.default_res_id = R.drawable.ic_launcher;
                    item.src = srcs[new Random().nextInt(srcs.length - 1)];
                    item.ui = new Block.UI();
                    item.ui.ui_type = Block.UIType.T4;
                    block.items.add(item);
                }
            }
            root.items.add(block);
        }

        //mLogger.d("test block " + root);
        BlockAdapter blockAdapter = new BlockAdapter(root, new CommonPresenterSelector());
        setAdapter(blockAdapter);
        setSelectedPosition(0, false);

        setOnItemViewClickedListener(new BaseRowPresenter.OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, BaseRowPresenter.ViewHolder rowViewHolder, Object row) {

                Toast.makeText(getActivity(), "row=" + ((DisplayItem) row).title + ", staggered_item=" + ((DisplayItem) item).title, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //20张
    String[] srcs = new String[]{
            "https://wx2.sinaimg.cn/mw690/75d41189gy1frtg9d06y5j21am1jknpd.jpg",
            "https://wx2.sinaimg.cn/mw690/75d41189gy1frtg9n2ufzj215o1jkb2a.jpg",
            "https://wx4.sinaimg.cn/mw690/75d41189gy1frtg9i7v9tj215o1jkqv5.jpg",
            "https://wx2.sinaimg.cn/mw690/75d41189gy1frtg9byr55j21jk15o7wi.jpg",
            "https://wx4.sinaimg.cn/mw690/75d41189gy1frtg9os6ixj215o1jk4qp.jpg",
            "https://wx2.sinaimg.cn/mw690/75d41189gy1frtg9elrbaj215o1jk4qp.jpg",
            "https://wx3.sinaimg.cn/mw690/75d41189gy1frtg9gj5t5j215o1jke82.jpg",
            "https://wx2.sinaimg.cn/mw690/75d41189gy1frtg9lef0vj215o1jk7wi.jpg",
            "https://wx3.sinaimg.cn/mw690/75d41189gy1frtg9jo94qj21jk15o4qp.jpg",
            "https://wx4.sinaimg.cn/mw690/467a4bd1gy1fru9f4t4xjj21kw11x1l1.jpg",
            "https://wx4.sinaimg.cn/mw690/4d59dfa9ly1frd2myv89sj20u0190n55.jpg",
            "https://wx4.sinaimg.cn/mw690/7dc8d9e8gy1fruaqgzmwaj21kw11x7wj.jpg",
            "https://wx3.sinaimg.cn/mw690/da4bb44agy1fruap6m5gmj212f0qoad7.jpg",
            "https://wx2.sinaimg.cn/mw690/819cc17dly1fruan5iv12j20j60aogm6.jpg",
            "https://wx4.sinaimg.cn/mw690/6b804b51gy1fruail19eij20d308c3ze.jpg",
            "https://wx4.sinaimg.cn/mw690/006r9bLcly1fru9hxwe4lj30m80m8n7n.jpg",
            "https://wx1.sinaimg.cn/mw690/69077c94gy1fru9mx60tlj20u011itay.jpg",
            "https://wx2.sinaimg.cn/mw690/ae9db69aly1fru947bn54j20u013yafj.jpg",
            "https://wx3.sinaimg.cn/mw690/986a8fd1gy1fruamf2wk7j21kw0miqgi.jpg",
            "https://wx1.sinaimg.cn/mw690/9ff831e3gy1frua5ysz9mj20rs18gn13.jpg"
    };
}
