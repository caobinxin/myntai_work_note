package com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.network;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class Md5Sign implements ISign {
    @Override
    public String sign(Map<String, Object> requestParams) {
        requestParams.remove("sign_type");
        requestParams.remove("sign");

        //Sort data
        Object dataJsonStr = requestParams.get("data");
        if (dataJsonStr != null) {
            JSONObject jsonData = JSON.parseObject((String) dataJsonStr);
            Map<String, Object> mapData = jsonData.toJavaObject(Map.class);
            Map<String, Object> sortedMapData = new TreeMap<>(new MapKeyComparator());
            sortedMapData.putAll(mapData);
            requestParams.put("data", JSON.toJSONString(sortedMapData));
        }

        //Sort request parameters
        Map<String, Object> sortedRequestParams = new TreeMap<>(new MapKeyComparator());
        sortedRequestParams.putAll(requestParams);

        //convert into string
        StringBuilder encodedMessage = new StringBuilder();
        for (Map.Entry<String, Object> requestParam : sortedRequestParams.entrySet()) {
            encodedMessage.append(requestParam.getKey());
            encodedMessage.append("=");
            encodedMessage.append(requestParam.getValue());
            encodedMessage.append("&");
        }
        encodedMessage.append("key=");
        encodedMessage.append(MarvinCloudConfig.getInstance().getSignKey());

        //MD5
        return MD5.getMD5(encodedMessage.toString());
    }

    @Override
    public String getSignType() {
        return "MD5";
    }


    class MapKeyComparator implements Comparator<String> {
        @Override
        public int compare(String str1, String str2) {
            return str1.compareTo(str2);
        }

    }
}
