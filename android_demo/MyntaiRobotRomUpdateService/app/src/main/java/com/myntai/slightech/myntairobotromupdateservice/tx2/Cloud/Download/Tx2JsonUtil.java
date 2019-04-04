package com.myntai.slightech.myntairobotromupdateservice.tx2.Cloud.Download;

import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myntai.slightech.myntairobotromupdateservice.tx2.Cloud.Response;

import java.lang.reflect.Type;
import java.util.List;

public class Tx2JsonUtil {

    private static final String TAG = "JsonUtil";

    public Tx2JsonUtil() {
    }

    public void parseJsonWithGson(String jsonData) {
        Gson gson = new Gson();
        List<SystemRom> systemRomsList = gson.fromJson(jsonData, new TypeToken<List<SystemRom>>() {
        }.getType());
        for (SystemRom systemRom : systemRomsList) {
            //TODO　根据　ＲＯＭ　版本信息，判断是否需要升级
            Log.i(TAG, "parseJsonWithGson: name = " + systemRom.getName());
            Log.i(TAG, "parseJsonWithGson: version = " + systemRom.getVersion());
            Log.i(TAG, "parseJsonWithGson: size = " + systemRom.getSize());
            Log.i(TAG, "parseJsonWithGson: romdownloadaddress = " + systemRom.getRomDownloadAddress());
        }
    }

    public Tx2AppUpdate parseJsonWithGsonRespond(String jsonData, Class analysisClass) {
        ParameterizedTypeImpl responseType = new ParameterizedTypeImpl(new Type[]{analysisClass}, null, Response.class);
        Response<Tx2AppUpdate> responseAppUpdate = JSON.parseObject(jsonData, responseType);
        return responseAppUpdate.getData();
    }
}

