package com.myntai.slightech.myntairobotromupdateservice.common.network;

public interface ICheckServer {
    void onUpgrade(Object object);
    void onNoUpgrade(Object object);
}
