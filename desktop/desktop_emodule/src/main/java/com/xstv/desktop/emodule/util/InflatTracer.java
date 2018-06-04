package com.xstv.desktop.emodule.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.support.v4.os.TraceCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class InflatTracer {
    private static final String TRACE_INFLATE_TAG = "tvhome staggered_item inflate";
    public static View inflate(Context context, int layoutId, ViewGroup parent){
        TraceCompat.beginSection(TRACE_INFLATE_TAG);
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        final Resources res = context.getResources();
        final XmlResourceParser parser = res.getLayout(layoutId);
        try {
            return inf.inflate(parser, parent, false);
        } finally {
            TraceCompat.endSection();
            parser.close();
        }
    }
}
