package com.myntai.slightech.myntairobotromupdateservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.myntai.slightech.myntairobotromupdateservice.common.GoUpRomLog;

public class BootUP extends BroadcastReceiver {
    private final static String OTA_BOOT_URL = "android.intent.action.bootOta";
    @Override
    public void onReceive(Context context, Intent intent) {
        GoUpRomLog.log("接受到开机广播");
        Intent bootupIntent = new Intent(OTA_BOOT_URL);
        MyApplication.getContext().sendBroadcast(bootupIntent);
    }
}
