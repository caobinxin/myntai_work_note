package com.myntai.slightech.myntairobotromupdateservice;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        new DownloadRom().download("http://192.168.1.195:1122/update.zip");
//
//        HttpUtil.sendHttpRequest("http://192.168.1.195:1122/get_data.json", new HttpCallbackListener() {
//            @Override
//            public void onFinish(String response){
//                //TODO 解析数据
//                new JsonUtil().parseJsonWithGson(response);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Log.i(TAG, "onError: e = " + e);
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.finish();
    }
}
