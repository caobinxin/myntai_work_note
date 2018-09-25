package com.myntai.slightech.myntairobotromupdateservice;

import android.util.Log;
import java.io.DataOutputStream;
import java.io.IOException;


public class RomGoUP {

    private final String mRECOVERY_LASTFLG = "/cache/recovery/last_flag\n";
    private final String mRECOVERY_COMMAND = "/cache/recovery/command\n";
    private String TAG = "RomGoUP";

    RomGoUP() {

    }

    public void goUp(String updateFilePath) {
        String updating = "echo updating$path=" + updateFilePath + " > " + mRECOVERY_LASTFLG;
        String updatePackage = "echo --update_package=" + updateFilePath + " > " + mRECOVERY_COMMAND;
        stratGoup(updating, updatePackage);
    }

    private void stratGoup(String lastFlg, String command){
        try {
            Log.i(TAG, "stratGoup: shell in");
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataOutputStream.writeBytes(lastFlg);
            dataOutputStream.writeBytes(command);
            dataOutputStream.writeBytes("reboot recovery\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
