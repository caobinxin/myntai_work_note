package com.myntai.slightech.myntairobotromupdateservice.common;

import android.os.Build;

public class Common {
    public static final int MODE_AUTOMATIC = 1;//当前升级　不需要人参与
    public static final int MODE_MANUAL = 2;

    public static final int MODE_TYPE_ALL_PKG = 1;//
    public static final int MODE_TYPE_DIFF_PKG = 2;//
    public static final int MODE_TYPE_ALL_PKG_AUTO = 3;//

    public static final int DIALOG_CODE_DOWNLOAD = 3;
    public static final String DIALOG_TAG_NEW_VERSION = "dialogTagNewVersion";

    public static final int DOWNLOADPKG_TYPE_ALL = 1;
    public static final int DOWNLOADPKG_TYPE_DIFF = 2;
    public static final int DOWNLOADPKG_TYPE_ALL_AUTO = 3;

    public static final int UPGRADE_CODE_DIFF_PKG_MANUAL = 1;//手动　升级全包
    public static final int UPGRADE_CODE_ALL_PKG_MANUAL = 3;
    public static final int UPGRADE_CODE_ALL_PKG_AUTO = 4;//手动　升级全包

    public static final String DownLoadDir = "/androidRomUpdate";

    public static long getFireflyVersionCode() {
        String[] content = Build.DISPLAY.split("\\.");
        if (content.length > 0) {
            return Long.valueOf(content[0]);
        } else {
            return 1;
        }
    }
}
