package com.myntai.slightech.myntairobotromupdateservice.tx2.Cloud;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.myntai.slightech.myntairobotromupdateservice.Common;
import com.myntai.slightech.myntairobotromupdateservice.tx2.Tx2OtaLogTree;

import java.io.File;
import java.net.FileNameMap;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class PostData {

    private long systemVersion ;
    private String service = "tx2";
    private String method = "check_update";
    private Map<String, Object> mapData = new HashMap<>();
    Map<String, Object> mapBasic = new HashMap<>();
    ISign mSignStrategy = new Md5Sign();


    public Map<String, Object> getMapBasic(){
        return mapBasic;
    }

    public void setMapBasic(){
        mapBasic.put("app_id", Tx2MarvinCloudConfig.getInstance().getAppId());
        mapBasic.put("timestamp", System.currentTimeMillis() / 1000);
    }

    public long getSystemVersion() {
        systemVersion = Common.getTx2VersionCode();
        return systemVersion;
    }

    public Map<String, Object> getMapData(){
        return mapData;
    }
    public void setMapData(int type, long versionCode){
        //mapData.put("upgrade_type", type);
        mapData.put("version_code", versionCode);
    }



    public RequestBody createRequestBody() throws Exception {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        //add file data into form.
        buildFileData(builder, mapData);

        //add form data into form.
        buildFormData(builder, mapData, service, method);

        return builder.build();
    }

    protected void buildFileData(MultipartBody.Builder builder, Map<String, Object> dataMap) throws Exception {
        for (Map.Entry<String, Object> dataEntry : dataMap.entrySet()) {
            String dataKey = dataEntry.getKey();
            Object dataValue = dataEntry.getValue();
            //If data invalid, drop it.
            if (!isFileValid(dataValue)) {
                continue;
            }
            File dataFile = (File) dataValue;
            RequestBody fileBody = RequestBody.create(MediaType.parse(getMimeType(dataFile.getName())), dataFile);
            builder.addFormDataPart(dataKey, dataFile.getName(), fileBody);
        }
    }

    protected void buildFormData(MultipartBody.Builder builder,
                                 Map<String, Object> dataMap,
                                 String service,
                                 String method) throws JSONException {
        //Process data first
        //Add basic data from Tx2MarvinCloudConfig.
        dataMap.putAll(Tx2MarvinCloudConfig.getInstance().getBasicData());
        //Add into JSON
        JSONObject jsonData = new JSONObject();
        for (Map.Entry<String, Object> dataEntry : dataMap.entrySet()) {
            String dataKey = dataEntry.getKey();
            Object dataValue = dataEntry.getValue();

            //If data invalid, drop it.
            if (!isDataValid(dataValue)) {
                continue;
            }

            //Normal form data, add into data json.
            jsonData.put(dataKey, dataValue);
        }


        //Get basic parameter and add into form.
        Map<String, Object> requestParams = new HashMap<>();
        if (getMapBasic() != null) {
            requestParams.putAll(getMapBasic());
        }

        requestParams.put("service", service);
        requestParams.put("method", method);
        requestParams.put("data", jsonData.toString());
        if (mSignStrategy != null) {
            requestParams.put("sign", mSignStrategy.sign(requestParams));
            requestParams.put("sign_type", mSignStrategy.getSignType());
        }

        for (Map.Entry<String, Object> requestParam : requestParams.entrySet()) {
            builder.addFormDataPart(requestParam.getKey(), String.valueOf(requestParam.getValue()));
        }
        Tx2OtaLogTree.log( "buildFormData: " + requestParams.toString());
    }

    protected boolean isDataValid(Object dataValue) {
        return !(dataValue == null || dataValue instanceof File);
    }

    protected boolean isFileValid(Object dataValue) {
        return dataValue instanceof File;
    }

    /**
     * 获取文件MimeType
     *
     * @param filename 文件名
     */
    private String getMimeType(String filename) {
        FileNameMap filenameMap = URLConnection.getFileNameMap();
        String contentType = filenameMap.getContentTypeFor(filename);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return contentType;
    }

    public static String getFormatMacAddress() {
        String mac = getWifiMacAddress();

        StringBuilder builder = new StringBuilder();
        for (int i = 0, len = mac.length(); i < len; i++) {
            builder.append(mac.charAt(i));
            if (i % 2 == 1 && i > 0 && i < len - 1) {
                builder.append(":");
            }
        }

        return builder.toString();
    }

    public static String getWifiMacAddress() {
        loopPrintInterface();
        String mac = "";
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                String interfaceName = networkInterface.getName();
                if (!networkInterface.isLoopback() && interfaceName != null && interfaceName.contains("wlan")) {
                    byte[] address = networkInterface.getHardwareAddress();
                    mac = HexUtil.toHexString(address);
                }
            }
        } catch (SocketException ignored) {
        }
        return mac;
    }

    private static void loopPrintInterface() {
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                Tx2OtaLogTree.log("" + networkInterface.getName());
            }
        } catch (SocketException ignored) {
        }
    }

    public static String getVersionName(Context context) {
        String version;
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo("com.slightech.sdeno", 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            version = "";
        }
        return version;
    }

    public static int getVersion(Context context) {
        int code = -1;
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo("com.slightech.sdeno", 0);
            code = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return code;
    }

}
