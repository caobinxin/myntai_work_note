package com.myntai.slightech.myntairobotromupdateservice.common;

import android.util.Log;

public class GoUpRomLog {
    static final String TAG = "SlightechGoUpRom";
    public static void log(String className, String methodName, String message) {
        String logMessage = "=== className: " + className + " === methodName: " + methodName + " === message: " + message;
        Log.i(TAG, "" + logMessage);
    }
    public static void log(String message){
        Log.i(TAG, "" + message);
    }
}
