package com.myntai.slightech.myntairobotromupdateservice.tx2.Cloud;

import java.util.Map;

public interface ISign {
    String getSignType();

    String sign(Map<String, Object> requestParams);

}

