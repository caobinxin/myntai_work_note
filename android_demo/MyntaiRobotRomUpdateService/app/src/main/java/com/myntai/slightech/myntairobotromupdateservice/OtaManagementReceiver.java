package com.myntai.slightech.myntairobotromupdateservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OtaManagementReceiver extends BroadcastReceiver {
    final static String OTA_MANAGEEMENT_URL = MyApplication.getContext().getResources().getText(R.string.OTA_MANAGEEMENT_URL).toString();
    private static final String TAG = "OtaManagementReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (OTA_MANAGEEMENT_URL.equals(intent.getAction())) {
            GoUpRomLog.log("ota management receiver.");
            int code = intent.getIntExtra("CODE", 0);
            allocatingTask(code, intent);
        }
    }

    private void allocatingTask(int code, Intent intent) {
        switch (code) {
            case 0://默认什么都不做
                break;
            case 1://拿到　要升级包的总数　和　对应升级包的存贮路径
                prepareGoUpEnv(intent);
                break;
            case 2://循环升级固件
                pollGoUpRom();
                break;
            case 3://全量包升级
                allPackageUpGoRom(intent);
                break;
            default:
                break;

        }
    }

    static final String SAVE_DIR_PATH = MyApplication.getContext().getResources().getText(R.string.SAVE_DIR_PATH).toString();
    static final String TEMP_OTA_LOG = MyApplication.getContext().getResources().getText(R.string.TEMP_OTA_LOG).toString();

    private void prepareGoUpEnv(Intent intent) {
        int otaPackagesCount = intent.getIntExtra("OTAPACKAGESCOUNT", 0);
        GoUpRomLog.log(" Differential package count:" + otaPackagesCount);
        String command = null;
        if (0 == otaPackagesCount) {
            //不需要升级
        } else {
            for (int i = 1; i <= otaPackagesCount; i++) {
                if (1 == i) {
                    command = "echo " + SAVE_DIR_PATH + i + ".zip > " + SAVE_DIR_PATH + TEMP_OTA_LOG + "\n";
                } else {
                    command = "echo " + SAVE_DIR_PATH + i + ".zip >> " + SAVE_DIR_PATH + TEMP_OTA_LOG + "\n";
                }

                try {
                    ShellCommand.shellExec(command);
                } catch (Exception e) {
                    GoUpRomLog.log(getClass().getSimpleName(),"prepareGoUpEnv()", "" + e);
                }
            }
        }
    }

    private void pollGoUpRom() {
        String preparedGoupOTAfile = null;
        try {
            preparedGoupOTAfile = ShellCommand.parsingLastInstallFileAndExecCommand(SAVE_DIR_PATH + TEMP_OTA_LOG,
                    1, 0);
            GoUpRomLog.log("update file name: " + preparedGoupOTAfile);
            if (null != preparedGoupOTAfile) {
                //这里启动升级，升级成功再将第一行删除
                //弹窗提示用户，需要升级 同意后启动升级
                startUpGoDialog(preparedGoupOTAfile);
            }
        } catch (Exception e) {
            GoUpRomLog.log(getClass().getSimpleName(), "pollGoUpRom()", "" + e);
        }
    }

    private void startUpGoSystemDialog(boolean start, String WillUpdatePackage) {
        if (start) {
            Intent intent = new Intent(MyApplication.getContext(), OkDeleteDialogService.class);
            intent.putExtra("CODE", 2);
            intent.putExtra("WillUpdatePackage", WillUpdatePackage);
            MyApplication.getContext().startService(intent);
        }
    }

    private void startUpGoDialog(String WillUpdatePackage) {
        boolean isStartDeleteDialog = SystemDialogUtil.requestAlertWindowPermission();
        startUpGoSystemDialog(isStartDeleteDialog, WillUpdatePackage);
    }

    private void allPackageUpGoRom(Intent intent) {
        String allPackageUpGoRomName = intent.getStringExtra("ALLPACKAGENAME");
        if (null == allPackageUpGoRomName) {
            GoUpRomLog.log("allPackageUpGoRomName = null.");
        } else {
            String command = "echo " + SAVE_DIR_PATH + allPackageUpGoRomName + " > " + SAVE_DIR_PATH + TEMP_OTA_LOG + "\n";
            GoUpRomLog.log(command);
            try {
                ShellCommand.shellExec(command);
            } catch (Exception e) {
                GoUpRomLog.log(getClass().getSimpleName(),"allPackageUpGoRom()", "" + e);
            }
        }
    }
}
