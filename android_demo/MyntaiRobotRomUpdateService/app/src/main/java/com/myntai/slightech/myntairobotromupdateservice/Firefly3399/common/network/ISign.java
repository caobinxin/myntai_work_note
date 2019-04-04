package com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.network;

import java.util.Map;

public interface ISign {
    String getSignType();

    String sign(Map<String, Object> requestParams);

}
