
package com.xstv.desktop.app.bean;

/**
 * Created by zhangguanhua on 17-6-8.
 */

public class JumpParamBean {
    public ParamValue paramValue;
    public String paramType;
    public JumpDetect jumpDetect;

    public class ParamValue {
        public String params;
    }

    public class JumpDetect{
        public String jumpVersion;
        public String jumpName;
        public String jumpPackage;
    }

}
