package com.myntai.slightech.myntairobotromupdateservice.tx2.Cloud.Download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.myntai.slightech.myntairobotromupdateservice.Common;
import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.SystemDialogUtil;
import com.myntai.slightech.myntairobotromupdateservice.dialog.DialogService;
import com.myntai.slightech.myntairobotromupdateservice.MessageHandler;
import com.myntai.slightech.myntairobotromupdateservice.MyApplication;
import com.myntai.slightech.myntairobotromupdateservice.tx2.Tx2OtaLogTree;
import com.myntai.slightech.myntairobotromupdateservice.tx2.transmission.Tx2Version;

import java.io.DataOutputStream;

public class Tx2OtaExternalTriggerReceiver extends BroadcastReceiver implements Handler.Callback, ICheckServer {

    Handler mMainHandler;
    Tx2CheckServer tx2CheckServer;
    Tx2AppUpdate tx2AppUpdate;

    Tx2Version tx2Version = new Tx2Version() {
        @Override
        public void onDisplayToast(String s) {
            final String ss = s;
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyApplication.getContext(), ss, Toast.LENGTH_LONG).show();
                }
            });

        }

        @Override
        public void onVersionUpdate(long tx2Version) {
            requestServer(tx2Version);
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {

        Tx2OtaLogTree.log("Tx2OtaExternalTriggerReceiver", "onReceive", "触发tx2全局外部的入口");
        if (null == mMainHandler) {
            mMainHandler = new MessageHandler(this);
            DownloadTx2.getInstance().setMainHander(mMainHandler);
        }

        tx2Version.requestTx2Version();
    }

    private void requestServer(long tx2Version) {
        int ret;
        if (null == tx2CheckServer) {
            tx2CheckServer = new Tx2CheckServer(this);
        }

        ret = versionChecking(tx2Version);
        if (ret != 0) {
            Tx2OtaLogTree.log("版本检查失败 退出之后的操作");
            return;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    @Override
    public void onUpgrade(Object object) {
        Tx2OtaLogTree.log("onUpgrade...");
        try {
            shellExec("rm -rf /data/media/0/tx2Update/* \n") ;
        } catch (Exception e) {
            Tx2OtaLogTree.log("执行rm -rf /sdcard/tx2Update/*　出错");
            e.printStackTrace();
        }
        tx2AppUpdate = (Tx2AppUpdate) object;
        ManageDownLoad.getInstance().setTx2AppUpdate(tx2AppUpdate);
        tipsUserUpgradeTx2(tx2AppUpdate);//提示用户升级tx2
    }

    @Override
    public void onNoUpgrade(Object object) {
        //todo 当前为最新版本，不需要升级
    }

    private int versionChecking(long tx2Version) {
        if (null == tx2CheckServer) {
            Tx2OtaLogTree.log(" tx2CheckServer == null");
            return -1;
        }

        tx2CheckServer.requestServerCheckROMVersion(tx2Version);
        return 0;
    }

    private int tipsUserUpgradeTx2(Tx2AppUpdate mAppupdate) {

        String tx2OtaNewVersion = "Tx2 服务器同步异常";
        if (null != mAppupdate) {
            tx2OtaNewVersion = mAppupdate.getVersionName();
        }

        DownloadTx2.getInstance().setTx2AppUpdate(mAppupdate);

        if (SystemDialogUtil.requestAlertWindowPermission()) {
            Intent intent = new Intent(MyApplication.getContext(), DialogService.class);
            intent.putExtra("CODE", Common.DIALOG_CODE_TX2_UPGRADE);//弹窗，tx2是否升级
            intent.putExtra(Common.DIALOG_TAG_TX2_NEW_VERSION, tx2OtaNewVersion);
            MyApplication.getContext().startService(intent);
        } else {
            Tx2OtaLogTree.log("OtaExternalTriggerReceiver", "startTheWindow", "没有dialog的权限");
        }

        return 0;
    }

    private void shellExec(String command)throws Exception {
        Process process2 = null;
        DataOutputStream dataOutputStream2 = null;
        try {
            process2 = Runtime.getRuntime().exec("su");
            dataOutputStream2 = new DataOutputStream(process2.getOutputStream());
            dataOutputStream2.writeBytes(command);
            dataOutputStream2.writeBytes("exit\n");
            dataOutputStream2.flush();
            process2.waitFor();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (dataOutputStream2 != null) {
                    dataOutputStream2.close();
                }
                process2.destroy();
            } catch (Exception e) {
                throw e;
            }
        }
    }
}
