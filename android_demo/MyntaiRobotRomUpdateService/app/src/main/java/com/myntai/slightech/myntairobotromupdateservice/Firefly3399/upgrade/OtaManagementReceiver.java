package com.myntai.slightech.myntairobotromupdateservice.Firefly3399.upgrade;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.AndroidOtaLogTree;
import com.myntai.slightech.myntairobotromupdateservice.MyApplication;
import com.myntai.slightech.myntairobotromupdateservice.Common;
import com.myntai.slightech.myntairobotromupdateservice.dialog.DialogService;
import com.myntai.slightech.myntairobotromupdateservice.R;
import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.ShellCommand;
import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.SystemDialogUtil;

public class OtaManagementReceiver extends BroadcastReceiver {
    final static String OTA_MANAGEEMENT_URL = MyApplication.getContext().getResources().getText(R.string.OTA_MANAGEEMENT_URL).toString();
    private static final String TAG = "OtaManagementReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (OTA_MANAGEEMENT_URL.equals(intent.getAction())) {
            AndroidOtaLogTree.log("ota management receiver.");
            int code = intent.getIntExtra("CODE", 0);
            allocatingTask(code, intent);
        }
    }

    private void allocatingTask(int code, Intent intent) {
        switch (code) {
            case 0://默认什么都不做
                break;
            case Common.UPGRADE_CODE_DIFF_PKG_MANUAL://手动差分包升级
                prepareGoUpEnv(intent);
                break;
            case 2://循环升级固件
                pollGoUpRom();
                break;
            case Common.UPGRADE_CODE_ALL_PKG_MANUAL://全量包升级
                allPackageUpGoRom(intent);
                break;
            case Common.UPGRADE_CODE_ALL_PKG_AUTO://全自动升级全包，中间没有人为的参与
                startAutoAllPackageUpdate(intent);
                break;
            default:
                break;

        }
    }

    static final String SAVE_DIR_PATH = MyApplication.getContext().getResources().getText(R.string.SAVE_DIR_PATH).toString();
    static final String TEMP_OTA_LOG = MyApplication.getContext().getResources().getText(R.string.TEMP_OTA_LOG).toString();

    private void prepareGoUpEnv(Intent intent) {
        int otaPackagesCount = intent.getIntExtra("OTAPACKAGESCOUNT", 0);
        AndroidOtaLogTree.log(" Differential package count:" + otaPackagesCount);
        String command;
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
                    AndroidOtaLogTree.log(getClass().getSimpleName(), "prepareGoUpEnv()", "" + e);
                }
            }
        }
    }

    private void pollGoUpRom() {
        String preparedGoupOTAfile = null;
        try {
            preparedGoupOTAfile = ShellCommand.parsingLastInstallFileAndExecCommand(SAVE_DIR_PATH + TEMP_OTA_LOG,
                    1, 0);
            AndroidOtaLogTree.log("update file name: " + preparedGoupOTAfile);
            if (null != preparedGoupOTAfile) {
                //这里启动升级，升级成功再将第一行删除
                //弹窗提示用户，需要升级 同意后启动升级
                startUpGoDialog(preparedGoupOTAfile);
            }
        } catch (Exception e) {
            AndroidOtaLogTree.log(getClass().getSimpleName(), "pollGoUpRom()", "" + e);
        }
    }

    private void startUpGoSystemDialog(boolean start, String WillUpdatePackage) {
        if (start) {
            Intent intent = new Intent(MyApplication.getContext(), DialogService.class);
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
            AndroidOtaLogTree.log("allPackageUpGoRomName = null.");
        } else {
            String command = "echo " + SAVE_DIR_PATH + allPackageUpGoRomName + " > " + SAVE_DIR_PATH + TEMP_OTA_LOG + "\n";
            AndroidOtaLogTree.log(command);
            try {
                ShellCommand.shellExec(command);
            } catch (Exception e) {
                AndroidOtaLogTree.log(getClass().getSimpleName(), "allPackageUpGoRom()", "" + e);
            }
        }
    }

    private void startAutoAllPackageUpdate(Intent intent) {
        String detailedAddress;
        String allPackageUpGoRomName = intent.getStringExtra("ALLPACKAGENAME");
        if (null == allPackageUpGoRomName) {
            AndroidOtaLogTree.log("allPackageUpGoRomName = null.");
        } else {
            detailedAddress = MyApplication.getContext().getResources().getText(R.string.SAVE_DIR_PATH).toString() + allPackageUpGoRomName;
            AndroidOtaLogTree.log("最终升级的目录:　" + detailedAddress);
            new RomGoUP().goUp(detailedAddress);
        }
    }
}
