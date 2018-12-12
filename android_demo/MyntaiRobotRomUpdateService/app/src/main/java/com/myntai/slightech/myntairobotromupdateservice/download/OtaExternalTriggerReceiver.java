package com.myntai.slightech.myntairobotromupdateservice.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.myntai.slightech.myntairobotromupdateservice.MainActivity;
import com.myntai.slightech.myntairobotromupdateservice.MyApplication;
import com.myntai.slightech.myntairobotromupdateservice.R;
import com.myntai.slightech.myntairobotromupdateservice.common.Common;
import com.myntai.slightech.myntairobotromupdateservice.common.GoUpRomLog;
import com.myntai.slightech.myntairobotromupdateservice.common.MessageHandler;
import com.myntai.slightech.myntairobotromupdateservice.common.SystemDialogUtil;
import com.myntai.slightech.myntairobotromupdateservice.common.network.ICheckServer;
import com.myntai.slightech.myntairobotromupdateservice.dialog.OkDeleteDialogService;

import java.security.Permission;

public class OtaExternalTriggerReceiver extends BroadcastReceiver implements ICheckServer, Handler.Callback, IDownloadStatus {
    private final static String OTA_EXTERNAL_TRIGGER_BROADCAST_URL = MyApplication.getContext().getResources().getText(R.string.OTA_EXTERNAL_TRIGGER_BROADCAST_URL).toString();
    private final static int WHAT_HANDLE_POPUP_DOWNLOAD = 1;
    private final static int WHAT_HANDLE_AUTO_DOWNLOAD = 2;
    private final static int WHAT_HANDLE_NEWEST_VERSION = 3;
    private CheckServer checkServer;
    private AppUpdate mAppupdate;
    private MessageHandler mMainHandler;
    private int mMode;

    @Override
    public void onDownLoadAllAUTOSuccess(Object object) {
        GoUpRomLog.log("自动升级　全量包　已经下载完毕 通知升级模块直接全量包进行升级");
        sendUpgradeBroadcast(Common.MODE_AUTOMATIC,0,(String) object,0);
    }

    @Override
    public void onDownLoadAllAUTOFail(Object object) {

    }

    @Override
    public void onDownLoadSuccess(Object object) {
        Toast.makeText(MyApplication.getContext(), "下载成功", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDownLoadFail(Object object) {

    }

    @Override
    public void onDownLoadAllSuccess(Object object) {
        String allPkgName = (String) object;
        GoUpRomLog.log("全包下载成功　需要升级:　" + allPkgName);
        sendUpgradeBroadcast(0,Common.UPGRADE_CODE_ALL_PKG_MANUAL,allPkgName,0);
    }

    @Override
    public void onDownLoadAllFail(Object object) {

    }

    @Override
    public void onDownLoadDiffSuccess(Object object) {
        int count = (int) object;
        GoUpRomLog.log("差分包下载成功　需要升级:　" + count);
        sendUpgradeBroadcast(0,Common.UPGRADE_CODE_DIFF_PKG_MANUAL,"",count);
    }

    @Override
    public void onDownLoadDiffFail(Object object) {

    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case WHAT_HANDLE_POPUP_DOWNLOAD://弹窗统一从这走
                startTheWindow();
                break;
            case WHAT_HANDLE_AUTO_DOWNLOAD://自动升级触发
                autoDownloadGoup();
                break;
            case WHAT_HANDLE_NEWEST_VERSION:
                Toast.makeText(MyApplication.getContext(), "当前就是最新版本，不需要更新.", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onUpgrade(Object object) {
        mAppupdate = (AppUpdate) object;
        DownloadPkg.getInstance().setAppUpdate(mAppupdate);
        DownloadPkg.getInstance().setListen(this);

        if(mMode == Common.MODE_AUTOMATIC){
            Message message = Message.obtain();
            message.what = WHAT_HANDLE_AUTO_DOWNLOAD;
            mMainHandler.sendMessage(message);
            //自动升级
        }else if(mMode == Common.MODE_MANUAL){
            //需要人为参与
            Message message = Message.obtain();
            message.what = WHAT_HANDLE_POPUP_DOWNLOAD;
            mMainHandler.sendMessage(message);
        }else {
            GoUpRomLog.log("ota　外界触发方式不对，是全自动还是手动?请指定，否则不触发升级");
        }

    }

    @Override
    public void onNoUpgrade(Object object) {
        mAppupdate = null;//如果不需要版本更新，就将这个设置空
        GoUpRomLog.log("当前就是最新版本，不需要更新.");
        Message message = Message.obtain();
        message.what = WHAT_HANDLE_NEWEST_VERSION;
        mMainHandler.sendMessage(message);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        GoUpRomLog.log("OtaExternalTriggerReceiver", "onReceive", "触发全局外部的入口");
        mMainHandler = new MessageHandler(this);
        if (OTA_EXTERNAL_TRIGGER_BROADCAST_URL.equals(intent.getAction())) {
            if (null == checkServer) {
                checkServer = new CheckServer(this);
            }
            mMode = intent.getIntExtra("MODE", 0);//自动还是手动
            GoUpRomLog.log("Mode :" + mMode);
            checkServerAndUpgrades();
            verifyStoragePermissions();
        }
    }


    private void checkServerAndUpgrades() {
        if (null != checkServer) {
            checkServer.requestServerCheckROMVersion();
        } else {
            GoUpRomLog.log("checkserver == null");
        }
    }

    private void startTheWindow() {
        String otaNewVersion = "服务器同步异常";
        if (null != mAppupdate) {
            otaNewVersion = mAppupdate.getVersionName();
        }
        if (SystemDialogUtil.requestAlertWindowPermission()) {
            Intent intent = new Intent(MyApplication.getContext(), OkDeleteDialogService.class);
            intent.putExtra("CODE", Common.DIALOG_CODE_DOWNLOAD);
            intent.putExtra(Common.DIALOG_TAG_NEW_VERSION, otaNewVersion);
            MyApplication.getContext().startService(intent);
        } else {
            GoUpRomLog.log("OtaExternalTriggerReceiver", "startTheWindow", "没有dialog的权限");
        }

    }

    /***
     *
     * @param modeType 手动　自动(支持全连)
     * @param pkgType 差分包　还是　全包升级
     * @param allName　全包升级的时候　需要包的名字
     * @param diffCount　差分包需要知道　升级的个数
     */
    private void sendUpgradeBroadcast(int modeType, int pkgType, String allName, int diffCount) {
        Intent intent = new Intent();
        String targetAction = MyApplication.getContext().getResources().getText(R.string.OTA_MANAGEEMENT_URL).toString();
        intent.setAction(targetAction);
        switch (modeType) {
            case Common.MODE_AUTOMATIC:
                GoUpRomLog.log("启动全自动升级");
                intent.putExtra("CODE", Common.UPGRADE_CODE_ALL_PKG_AUTO);
                intent.putExtra("ALLPACKAGENAME", allName);
                break;
            default:
                GoUpRomLog.log("当前mode默认为手动升级");
                break;
        }

        switch (pkgType) {
            case Common.UPGRADE_CODE_ALL_PKG_MANUAL:
                GoUpRomLog.log("启动手动全包升级");
                intent.putExtra("CODE", Common.UPGRADE_CODE_ALL_PKG_MANUAL);
                intent.putExtra("ALLPACKAGENAME", allName);
                break;
            case Common.UPGRADE_CODE_DIFF_PKG_MANUAL:
                GoUpRomLog.log("启动手动差分包升级");
                intent.putExtra("CODE", Common.UPGRADE_CODE_DIFF_PKG_MANUAL);
                intent.putExtra("OTAPACKAGESCOUNT", diffCount);
                break;
            default:
                GoUpRomLog.log("当前pkgType无效");
                break;
        }
        MyApplication.getContext().sendBroadcast(intent);
    }

    private void autoDownloadGoup(){
        if(null != mAppupdate){
            Toast.makeText(MyApplication.getContext(), "检测到新版本(" + mAppupdate.getVersionName()+"),将自动完成升级！", Toast.LENGTH_LONG).show();
            DownloadPkg.getInstance().downloadType(Common.DOWNLOADPKG_TYPE_ALL_AUTO);
        }else {
            GoUpRomLog.log("OtaExternalTriggerReceiver", "autoDownloadGoup", "null == mAppupdate 无法完成自动升级");
        }
    }

    static int PermissionCount = 0;
    private void verifyStoragePermissions(){
        if(0 == PermissionCount){
            Intent noteList = new Intent(MyApplication.getContext(),MainActivity.class);
            noteList.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MyApplication.getContext().startActivity(noteList);
            PermissionCount++;
        }
    }
}
