package com.xstv.desktop.emodule.adapter;

import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;

import com.xstv.desktop.emodule.mode.Block;


public class BlockAdapter extends ArrayObjectAdapter {

    public Block mBlock;

    /**
     * Constructs an adapter.
     */
    public BlockAdapter(Block block) {
        super();
        mBlock = block;
    }

    /**
     * Constructs an adapter with the given {@link PresenterSelector}.
     */
    public BlockAdapter(Block block, PresenterSelector presenterSelector) {
        super(presenterSelector);
        mBlock = block;
    }

    /**
     * Constructs an adapter that uses the given {@link Presenter} for all items.
     */
    public BlockAdapter(Block block, Presenter presenter) {
        super(presenter);
        mBlock = block;
    }

    @Override
    public int size() {
        if (mBlock != null && mBlock.items != null) {
            return mBlock.items.size();
        }
        return 0;
    }

    @Override
    public Object get(int index) {
        if (mBlock != null && mBlock.items != null) {
            return mBlock.items.get(index);
        }
        return null;
    }

    public void updateBlock(Block block) {
        mBlock = block;
    }

    public void appendBlock(Block block) {
    }
}
