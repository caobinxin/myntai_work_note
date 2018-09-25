package com.myntai.slightech.myntairobotromupdateservice;

public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
