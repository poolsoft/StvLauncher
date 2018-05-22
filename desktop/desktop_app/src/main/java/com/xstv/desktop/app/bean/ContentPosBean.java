
package com.xstv.desktop.app.bean;

import java.util.List;
import java.util.Map;

public class ContentPosBean {
    public int errno;
    public Map<String, List<Item>> data;

    public class Item {
        public String type;
        public Promotion promotion;

        public class Promotion {
            public String reqid;
            public String promoid;
            public Creative creative;

            public class Creative {
                public String id;
                public Material material;
                public Jump jump;

                public class Material {
                    public String packagename;
                    public String activity;
                    public String picAD;
                    public String firsttitle;
                    public String secondtitle;
                    public String logoAD;
                }

                public class Jump {
                    public String jumpAD;
                }
            }
        }
    }

}
