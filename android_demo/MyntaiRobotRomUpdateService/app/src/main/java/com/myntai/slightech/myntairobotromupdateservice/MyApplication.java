package com.myntai.slightech.myntairobotromupdateservice;
import android.app.Application;
import android.content.Context;
import com.myntai.slightech.myntairobotromupdateservice.common.SystemDialogUtil;
import com.myntai.slightech.myntairobotromupdateservice.common.network.MarvinCloudConfig;

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        MarvinCloudConfig marvinCloudConfig = MarvinCloudConfig.getInstance();
        marvinCloudConfig.init("release");
        SystemDialogUtil.requestAlertWindowPermission();
    }

    public static Context getContext() {
        return context;
    }
}
