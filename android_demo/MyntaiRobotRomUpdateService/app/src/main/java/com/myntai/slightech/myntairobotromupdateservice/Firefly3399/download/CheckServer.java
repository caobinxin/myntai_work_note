package com.myntai.slightech.myntairobotromupdateservice.Firefly3399.download;

import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.AndroidOtaLogTree;
import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.network.HttpUtil;
import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.network.ICheckServer;
import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.network.MarvinCloudConfig;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class CheckServer {
    private ICheckServer mICheckServer;

    public CheckServer(ICheckServer iCheckServer) {
        this.mICheckServer = iCheckServer;
    }

    public void requestServerCheckROMVersion() {
        HttpUtil.sendOkHttpRequest(MarvinCloudConfig.getInstance().getUrl(), new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                AndroidOtaLogTree.log("onFailure: " + e);
                if(null != mICheckServer){
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                AndroidOtaLogTree.log("服务器返回数据：　" + responseData);
                AppUpdate appUpdate = new JsonUtil().parseJsonWithGsonRespond(responseData, AppUpdate.class);
                AndroidOtaLogTree.log("appupdate: " + appUpdate);
//                if(null == appUpdate){
//                    AndroidOtaLogTree.log("appupdate: " + appUpdate);
//                    AndroidOtaLogTree.log("请求服务器出错，ota 升级过程立即退出!!!");
//                    return;
//                }
                if(null != mICheckServer){
                    if(appUpdate.isHasNewVersion()){
                        AndroidOtaLogTree.log("appUpdate.isHasNewVersion()　有新版本需要更新");
                        mICheckServer.onUpgrade(appUpdate);
                    }else {
                        AndroidOtaLogTree.log("appUpdate.isHasNewVersion()　当前版本为最新，不需要更新");
                        mICheckServer.onNoUpgrade(appUpdate);
                    }

                }
            }
        });
    }
}
