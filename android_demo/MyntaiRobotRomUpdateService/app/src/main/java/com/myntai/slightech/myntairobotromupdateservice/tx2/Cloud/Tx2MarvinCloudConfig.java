package com.myntai.slightech.myntairobotromupdateservice.tx2.Cloud;

import android.support.annotation.NonNull;
import com.myntai.slightech.myntairobotromupdateservice.tx2.Tx2OtaLogTree;

import java.util.HashMap;
import java.util.Map;

public class Tx2MarvinCloudConfig {
    private final String APP_ID = "1c477283e763871c136b8816a947fbcb";
    private final String SIGN_KEY = "3ec182aee858398b71ba43a2b0681654";
    private String URL = "";

    private Map<String, Object> mMapBasicData = new HashMap<>();

    public static Tx2MarvinCloudConfig getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private Tx2MarvinCloudConfig() {
    }

    public String getSignKey() {
        return SIGN_KEY;
    }

    public String getAppId() {
        return APP_ID;
    }

    public String getUrl() {
        Tx2OtaLogTree.log("服务器url: " + URL);
        return URL;
    }

    public void init(String buildType) {
        switch (buildType) {
            case "debug": //开发环境
                //内网（机器人在北京内网时选用）
                URL = "http://192.168.1.204:10001/tx2/api";
                //外网（机器人不在北京内网时选用）
                //URL = "http://61.149.7.239:10001/android/api";
                break;
            case "alpha": //测试环境
                URL = "https://marvin-api-test.slightech.com/tx2/api";
                break;
            case "release": //生产环境，即线上环境
                URL = "https://marvin-api.slightech.com/tx2/api";
                break;
            default:
                break;
        }
    }

    public Map<String, Object> getBasicData() {
        return mMapBasicData;
    }

    public void setRobotModel(@NonNull String robotModel) {
        mMapBasicData.put("robot_model", robotModel);
    }

    public void setMac(@NonNull String mac) {
        mMapBasicData.put("mac", mac);
    }

    public void setAppVersion(@NonNull String appVersion, int versionCode) {
        mMapBasicData.put("app_version", appVersion);
        mMapBasicData.put("app_version_code", versionCode);
    }

    /**
     * Set network type of robot.
     */
    public void setNetworkType(int networkType) {
        mMapBasicData.put("network_type", networkType);
    }

    public void setTx2Version(String tx2AppVersion, String tx2FirmwareVersion) {
        mMapBasicData.put("tx2_app_version", tx2AppVersion);
        mMapBasicData.put("tx2_firmware_version", tx2FirmwareVersion);
    }

    public void setTraceId(String traceId) {
        mMapBasicData.put("trace_id", traceId);
    }

    public String getTraceId() {
        return String.valueOf(mMapBasicData.get("trace_id"));
    }


    private static class InstanceHolder {
        private static final Tx2MarvinCloudConfig INSTANCE = new Tx2MarvinCloudConfig();
    }
}
