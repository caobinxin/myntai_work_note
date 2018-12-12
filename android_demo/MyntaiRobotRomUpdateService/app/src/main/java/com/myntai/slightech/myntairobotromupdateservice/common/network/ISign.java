package com.myntai.slightech.myntairobotromupdateservice.common.network;

import java.util.Map;

public interface ISign {
    String getSignType();

    String sign(Map<String, Object> requestParams);

}
