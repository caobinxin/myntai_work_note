package com.myntai.slightech.myntairobotromupdateservice.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.myntai.slightech.myntairobotromupdateservice.common.Common;
import com.myntai.slightech.myntairobotromupdateservice.download.DownloadPkg;
import com.myntai.slightech.myntairobotromupdateservice.upgrade.RomGoUP;
import com.myntai.slightech.myntairobotromupdateservice.common.GoUpRomLog;
import com.myntai.slightech.myntairobotromupdateservice.common.ShellCommand;

public class OkDeleteDialogService extends Service {
    boolean LOGON = true;
    static final String TAG = "OkDeleteDialogService";
    private final static String PARSE_FILE = "/cache/recovery/last_install";
    private static int TimeDelay = 6000;

    private Handler mHandler = new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int code = intent.getIntExtra("CODE", 0);
        selectDialog(code, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    void selectDialog(int code, Intent intent) {
        switch (code) {
            case 0:
                break;
            case 1:
                deleteDialog(intent);
                break;
            case 2:
                startGoUpDialog(intent);
                break;
            case Common.DIALOG_CODE_DOWNLOAD:
                startAllOrDiffPkgDownloadDialog(intent);
                break;
            default:
                break;
        }
    }

    private void deleteDialog(Intent intent) {
        final String UpdateFile = intent.getStringExtra("DeleteUpdateFile");
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        unconditionalDeleteUpdatePackage(UpdateFile);
        builder.setTitle("RobotOS ");
        builder.setMessage("升级成功! (" + UpdateFile + ")");

        final Dialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }, TimeDelay);
    }

    private void unconditionalDeleteUpdatePackage(String UpdateFile) {
        try {
            ShellCommand.parsingLastInstallFileAndExecCommand(PARSE_FILE, 0, 2);//写删除成功的标识
            ShellCommand.parsingLastInstallFileAndExecCommand(UpdateFile, 0, 3);//删除升级文件
            GoUpRomLog.log("delete package success.");
        } catch (Exception e) {
            GoUpRomLog.log(getClass().getSimpleName(), "unconditionalDeleteUpdatePackage()", "" + e);
        }
    }

    private void startGoUpDialog(Intent intent) {
        final String UpdateFile = intent.getStringExtra("WillUpdatePackage");
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setTitle("RobotOS ");
        builder.setMessage("升级过程请保证机器人电量充足!!!  当前升级包:" + UpdateFile + "  点击OK确认升级继续,点击NO下次升级!");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GoUpRomLog.log("ok go up .");
                new RomGoUP().goUp(UpdateFile);

            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GoUpRomLog.log("no go up.");
                //暂时什么都不做
            }
        });
        final Dialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }, TimeDelay);
    }

    private void startAllOrDiffPkgDownloadDialog(Intent intent) {
        final String otaNewVersion = intent.getStringExtra(Common.DIALOG_TAG_NEW_VERSION);
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setTitle("RobotOS ");
        builder.setMessage("检测到最新的版本(" + otaNewVersion + "),当前版本(" + Common.getFireflyVersionCode() + ")，请选择升级方式:");
        builder.setPositiveButton("全包", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GoUpRomLog.log("用户-已点击全包下载");
                DownloadPkg.getInstance().downloadType(Common.DOWNLOADPKG_TYPE_ALL);

            }
        });
        builder.setNegativeButton("差分包", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GoUpRomLog.log("用户-已点击差分包下载");
                //暂时什么都不做
                DownloadPkg.getInstance().downloadType(Common.DOWNLOADPKG_TYPE_DIFF);
            }
        });
        final Dialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();


        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }, TimeDelay);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);

    }
}
