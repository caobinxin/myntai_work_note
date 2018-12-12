package com.myntai.slightech.myntairobotromupdateservice.manage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.myntai.slightech.myntairobotromupdateservice.MyApplication;
import com.myntai.slightech.myntairobotromupdateservice.dialog.OkDeleteDialogService;
import com.myntai.slightech.myntairobotromupdateservice.R;
import com.myntai.slightech.myntairobotromupdateservice.common.GoUpRomLog;
import com.myntai.slightech.myntairobotromupdateservice.common.ShellCommand;
import com.myntai.slightech.myntairobotromupdateservice.common.SystemDialogUtil;

import java.util.Timer;
import java.util.TimerTask;

public class BootReceiver extends BroadcastReceiver {
    private final static String TAG = "OTABootReceiver";
    private final static String OTA_BOOT_URL = "android.intent.action.bootOta";
    private final static String PARSE_FILE = "/cache/recovery/last_install";// 1:升级成功　本地升级文件
    private final static String DELETE_SUCCESS = "deleteOtaPackageSuccess";

    private String upgradeStatus = null;
    private String deleteStatus = null;
    private String OtaPackageFilePath = null;

    private String tempOtaLogFile = MyApplication.getContext().getResources().getText(R.string.SAVE_DIR_PATH).toString() +
            MyApplication.getContext().getResources().getText(R.string.TEMP_OTA_LOG);

    @Override
    public void onReceive(Context context, Intent intent) {
        GoUpRomLog.log("OTA boot receive.");
        if (OTA_BOOT_URL.equals(intent.getAction())) {
            pollStartBootReceiver(60);
            try {
                upgradeStatus = ShellCommand.parsingLastInstallFileAndExecCommand(PARSE_FILE, 2, 0);
                OtaPackageFilePath = ShellCommand.parsingLastInstallFileAndExecCommand(PARSE_FILE, 1, 0);
                deleteStatus = ShellCommand.parsingLastInstallFileAndExecCommand(PARSE_FILE, 0, 1);
            } catch (Exception e) {
                Log.e(TAG, "onReceive: ", e);
            }


            if (null == OtaPackageFilePath) {
                GoUpRomLog.log("last_install is null ,check PackageGoUp?");
                checkContinueGoUpRomAndHandle();
            }
            if (upgradeStatus == null || deleteStatus == null) return;

            if (upgradeStatus.equals("1") && deleteStatus.equals(DELETE_SUCCESS)) {
                // 此时状态：升级成功　并且　删除成功
                GoUpRomLog.log("check packageGoup?");
                checkContinueGoUpRomAndHandle();
            } else if (upgradeStatus.equals("1") && !deleteStatus.equals(DELETE_SUCCESS)) {
                //此时弹窗提示用户　升级成功 但 该升级包未删除
               // "onReceive: 此时弹窗提示用户　升级成功　是否删除升级包");
                checkAndDeleteTempOtaLogFirstRow(); //删除temp_ota_log 文件中的第一行
                prepareDialogEnv();
            } else if(upgradeStatus.equals("0") && !deleteStatus.equals(DELETE_SUCCESS)){
                GoUpRomLog.log("last up go fail.");
                String dir = MyApplication.getContext().getResources().getText(R.string.SAVE_DIR_PATH).toString();
                Toast.makeText(MyApplication.getContext(), "升级失败，请全量包升级.",Toast.LENGTH_LONG).show();
                try {
                    ShellCommand.parsingLastInstallFileAndExecCommand(PARSE_FILE, 0, 2);//写删除成功的标识
                    ShellCommand.shellExec("rm " +  dir + "* \n");
                }catch (Exception e){
                    GoUpRomLog.log(getClass().getSimpleName(), "onReceive()", "rm " + dir + "*");
                }
            }else if(upgradeStatus.equals("0") && deleteStatus.equals(DELETE_SUCCESS)){
                GoUpRomLog.log("check packageGoup?");
                checkContinueGoUpRomAndHandle();
            }
        }
    }

    private void prepareDialogEnv() {
        boolean isStartDeleteDialog = SystemDialogUtil.requestAlertWindowPermission();
        startDeleteSystemDialog(isStartDeleteDialog);
    }

    private void startDeleteSystemDialog(boolean start) {
        if (start) {
            Intent intent = new Intent(MyApplication.getContext(), OkDeleteDialogService.class);
            intent.putExtra("CODE", 1);
            intent.putExtra("DeleteUpdateFile", OtaPackageFilePath);
            MyApplication.getContext().startService(intent);
        }
    }

    private void checkContinueGoUpRomAndHandle() {
        try {
            String isContinueUpGo = ShellCommand.parsingLastInstallFileAndExecCommand(tempOtaLogFile, 1, 0);
            if (null == isContinueUpGo) {
                //此时不需要继续升级
            } else {
                GoUpRomLog.log("Upgrade is needed at this time.");
                //发送循环升级广播
                sendPollGoUpBroadcast();
            }
        } catch (Exception e) {
            GoUpRomLog.log(getClass().getSimpleName(),"checkContinueGoUpRomAndHandle()","" + e);
        }
    }

    private void sendPollGoUpBroadcast() {
        Intent intent = new Intent(MyApplication.getContext().getResources().getText(R.string.OTA_MANAGEEMENT_URL).toString());
        intent.putExtra("CODE", 2);
        MyApplication.getContext().sendBroadcast(intent);
    }

    private void checkAndDeleteTempOtaLogFirstRow() {
        try {
            String lastInstallFileInUpdateFile = ShellCommand.parsingLastInstallFileAndExecCommand(PARSE_FILE, 1, 0);
            String tempOtaLogInUpdateFile = ShellCommand.parsingLastInstallFileAndExecCommand(tempOtaLogFile, 1, 0);
            if (lastInstallFileInUpdateFile.equals(tempOtaLogInUpdateFile)) {
                //最后一次升级的正是该包，此时将删除 temp_ota_log文件，为下次升级做准备
                ShellCommand.shellExec("sed -i \"1d\" " + tempOtaLogFile + "\n");
                GoUpRomLog.log("temp_ota_log first line delete success.");
            } else {
                //不删除 循环升级时，再次升级
            }

        } catch (Exception e) {
            GoUpRomLog.log(getClass().getSimpleName(),"checkAndDeleteTempOtaLogFirstRow()","" + e);
        }
    }

    private void pollStartBootReceiver(int second) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(OTA_BOOT_URL);
                MyApplication.getContext().sendBroadcast(intent);
            }
        }, second * 1000);
    }
}
