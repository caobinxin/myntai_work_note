package com.myntai.slightech.myntairobotromupdateservice;
import android.app.Application;
import android.content.Context;

import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.AndroidOtaLogTree;
import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.SystemDialogUtil;
import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.network.MarvinCloudConfig;
import com.myntai.slightech.myntairobotromupdateservice.tx2.Cloud.Tx2MarvinCloudConfig;
import com.myntai.slightech.myntairobotromupdateservice.tx2.Tx2OtaLogTree;

import timber.log.Timber;

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        MarvinCloudConfig marvinCloudConfig = MarvinCloudConfig.getInstance();
        marvinCloudConfig.init("alpha");

        Tx2MarvinCloudConfig tx2MarvinCloudConfig = Tx2MarvinCloudConfig.getInstance();
        tx2MarvinCloudConfig.init("alpha");

        SystemDialogUtil.requestAlertWindowPermission();

        Timber.plant(new Tx2OtaLogTree());
        Timber.plant(new AndroidOtaLogTree());
    }

    public static Context getContext() {
        return context;
    }
}
