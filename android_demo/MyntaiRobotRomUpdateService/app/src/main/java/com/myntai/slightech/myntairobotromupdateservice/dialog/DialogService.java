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

import com.myntai.slightech.myntairobotromupdateservice.Common;
import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.AndroidOtaLogTree;
import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.download.DownloadPkg;
import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.upgrade.RomGoUP;
import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.ShellCommand;
import com.myntai.slightech.myntairobotromupdateservice.tx2.Cloud.Download.DownloadTx2;
import com.myntai.slightech.myntairobotromupdateservice.tx2.Tx2OtaLogTree;

public class DialogService extends Service {
    boolean LOGON = true;
    static final String TAG = "DialogService";
    private final static String PARSE_FILE = "/cache/recovery/last_install";
    private static int TimeDelay = 3000;

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
            case Common.DIALOG_CODE_TX2_UPGRADE:
                isCurrentUpgradeTx2(intent) ;
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
            AndroidOtaLogTree.log("delete package success.");
        } catch (Exception e) {
            AndroidOtaLogTree.log(getClass().getSimpleName(), "unconditionalDeleteUpdatePackage()", "" + e);
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
                AndroidOtaLogTree.log("ok go up .");
                new RomGoUP().goUp(UpdateFile);

            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AndroidOtaLogTree.log("no go up.");
                //暂时什么都不做
            }
        });
        final Dialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                AndroidOtaLogTree.log("ok 自动点击继续升级");
                new RomGoUP().goUp(UpdateFile);
            }
        });

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
                AndroidOtaLogTree.log("用户-已点击全包下载");
                DownloadPkg.getInstance().downloadType(Common.DOWNLOADPKG_TYPE_ALL);

            }
        });
        builder.setNegativeButton("差分包", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AndroidOtaLogTree.log("用户-已点击差分包下载");
                //暂时什么都不做
                DownloadPkg.getInstance().downloadType(Common.DOWNLOADPKG_TYPE_DIFF);
            }
        });
        final Dialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                AndroidOtaLogTree.log("自动点击全包下载");
                DownloadPkg.getInstance().downloadType(Common.DOWNLOADPKG_TYPE_ALL);
            }
        });


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

    private void isCurrentUpgradeTx2(Intent intent){

        final String otaNewVersion = intent.getStringExtra(Common.DIALOG_TAG_TX2_NEW_VERSION);
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setTitle("TX2-OS ");
        builder.setMessage("检测到最新的版本(" + otaNewVersion + ")，是否立即升级:");
        builder.setPositiveButton("立即升级tx2", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Tx2OtaLogTree.log("用户-已点击立即升级tx2");
                DownloadTx2.getInstance().confirmDownload();

            }
        });
        builder.setNegativeButton("暂不升级tx2", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Tx2OtaLogTree.log("用户-暂不升级tx2");
                //暂时什么都不做
                DownloadTx2.getInstance().temporarilyDownload();
            }
        });
        final Dialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Tx2OtaLogTree.log("自动点击升级tx2");
                DownloadTx2.getInstance().confirmDownload();
            }
        });

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
}
