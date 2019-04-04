package com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.myntai.slightech.myntairobotromupdateservice.MyApplication;

public class SystemDialogUtil {
    public static boolean requestAlertWindowPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(MyApplication.getContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + "com.myntai.slightech.myntairobotromupdateservice"));
                MyApplication.getContext().startActivity(intent);
            } else {
                //绘ui代码, 这里说明6.0系统已经有权限了
                return true;
            }
        } else {
            //绘ui代码,这里android6.0以下的系统直接绘出即可
            return true;
        }
        return false;
    }
}
