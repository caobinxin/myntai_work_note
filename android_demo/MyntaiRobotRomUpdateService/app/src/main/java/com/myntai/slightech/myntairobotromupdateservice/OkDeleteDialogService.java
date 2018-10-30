package com.myntai.slightech.myntairobotromupdateservice;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;

public class OkDeleteDialogService extends Service {
    boolean LOGON = true;
    static final String TAG = "OkDeleteDialogService";
    private final static String PARSE_FILE = "/cache/recovery/last_install";

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
        Dialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

    private void unconditionalDeleteUpdatePackage(String UpdateFile){
        try {
            ShellCommand.parsingLastInstallFileAndExecCommand(PARSE_FILE, 0, 2);//写删除成功的标识
            ShellCommand.parsingLastInstallFileAndExecCommand(UpdateFile, 0, 3);//删除升级文件
            GoUpRomLog.log("delete package success.");
        } catch (Exception e) {
            GoUpRomLog.log(getClass().getSimpleName(),"unconditionalDeleteUpdatePackage()","" + e);
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
        Dialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }
}
