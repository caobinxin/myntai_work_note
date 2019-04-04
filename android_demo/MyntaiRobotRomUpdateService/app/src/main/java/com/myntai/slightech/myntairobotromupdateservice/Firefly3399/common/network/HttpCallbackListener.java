package com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.network;

public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
