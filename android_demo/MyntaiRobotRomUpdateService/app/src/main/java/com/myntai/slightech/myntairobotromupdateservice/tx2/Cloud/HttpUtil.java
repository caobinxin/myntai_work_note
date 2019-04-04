package com.myntai.slightech.myntairobotromupdateservice.tx2.Cloud;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {



    public static void sendOkHttpRequest(String address, okhttp3.Callback callback, long tx2Version){
        PostData postData = new PostData();
        postData.setMapData(3,tx2Version);
        postData.setMapBasic();

        OkHttpClient client = new OkHttpClient();
        Request request = null;
        try {
            request = new Request.Builder().url(address).post(postData.createRequestBody())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.newCall(request).enqueue(callback);
    }
}
