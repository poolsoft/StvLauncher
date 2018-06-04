package com.xstv.desktop.emodule.mode;

public class DisplayItem {

    public static class UI {
        public String ui_type;
        public int columns;
        public int rows;
        public int columnSpan;
        public int rowSpan;
        public float ratio;//比例，等比的gridView时,宽高的比例
        public int width;
        public int height;
    }

    public static class UIType {
        public static final String T1 = "横滚";
        public static final String T2 = "竖向gridview";
        public static final String T3 = "写死布局";

        public static final String T4 = "海报";
        public static final String T5 = "简单item";
    }

    public String id;
    public String title;
    public String desc;
    public String src;
    public String hint;
    public int default_res_id;
    public int default_focus_res_id;
    public UI ui;

    @Override
    public String toString() {
        return "DisplayItem{" +
                "title='" + title + '\'' +
                ", ui='" + (ui != null ? ui.ui_type : "null") + '\'' +
                '}';
    }
}
