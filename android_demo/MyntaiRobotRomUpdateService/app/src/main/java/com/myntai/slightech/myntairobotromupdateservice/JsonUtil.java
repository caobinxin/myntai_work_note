package com.myntai.slightech.myntairobotromupdateservice;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class JsonUtil {

    private static final String TAG = "JsonUtil";

    public JsonUtil(){
    }

    public void parseJsonWithGson(String jsonData){
        Gson gson = new Gson();
        List<SystemRom> systemRomsList = gson.fromJson(jsonData, new TypeToken<List<SystemRom>>(){}.getType());
        for( SystemRom systemRom: systemRomsList){
            //TODO　根据　ＲＯＭ　版本信息，判断是否需要升级
            Log.i(TAG, "parseJsonWithGson: name = " + systemRom.getName());
            Log.i(TAG, "parseJsonWithGson: version = " + systemRom.getVersion());
            Log.i(TAG, "parseJsonWithGson: size = " + systemRom.getSize());
            Log.i(TAG, "parseJsonWithGson: romdownloadaddress = " + systemRom.getRomDownloadAddress());
        }
    }
}
