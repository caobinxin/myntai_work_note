package com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.network;

import android.util.Log;

import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.download.PostData;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    final static String TAG = "HttpUtil";
    public static void sendHttpRequest(final String address,final HttpCallbackListener listener){

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setDoInput(true);
                    //connection.setDoOutput(true); //如果是post方式，这个开关需要打开
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    if(listener != null){
                        //TODO 正常的ＨＴＴＰ请求完成
                        Log.i(TAG, "run: 正常的ＨＴＴＰ请求完成");
                        listener.onFinish(response.toString());
                    }
                }catch (Exception e){
                    if(listener != null){
                        //TODO ＨＴＴＰ请求失败
                        listener.onError(e);
                    }

                }finally {
                    if(connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

   public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
       PostData postData = new PostData();
       postData.setMapData(3,postData.getSystemVersion());
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
