package com.myntai.slightech.myntairobotromupdateservice.common.network;

public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
