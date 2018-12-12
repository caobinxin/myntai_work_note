package com.myntai.slightech.myntairobotromupdateservice.download;

import com.myntai.slightech.myntairobotromupdateservice.common.GoUpRomLog;
import com.myntai.slightech.myntairobotromupdateservice.common.network.HttpCallbackListener;
import com.myntai.slightech.myntairobotromupdateservice.common.network.HttpUtil;
import com.myntai.slightech.myntairobotromupdateservice.common.network.ICheckServer;
import com.myntai.slightech.myntairobotromupdateservice.common.network.MarvinCloudConfig;

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
                GoUpRomLog.log("onFailure: " + e);
                if(null != mICheckServer){
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                GoUpRomLog.log("服务器返回数据：　" + responseData);
                AppUpdate appUpdate = new JsonUtil().parseJsonWithGsonRespond(responseData, AppUpdate.class);
                GoUpRomLog.log("appupdate: " + appUpdate);
//                if(null == appUpdate){
//                    GoUpRomLog.log("appupdate: " + appUpdate);
//                    GoUpRomLog.log("请求服务器出错，ota 升级过程立即退出!!!");
//                    return;
//                }
                if(null != mICheckServer){
                    if(appUpdate.isHasNewVersion()){
                        GoUpRomLog.log("appUpdate.isHasNewVersion()　有新版本需要更新");
                        mICheckServer.onUpgrade(appUpdate);
                    }else {
                        GoUpRomLog.log("appUpdate.isHasNewVersion()　当前版本为最新，不需要更新");
                        mICheckServer.onNoUpgrade(appUpdate);
                    }

                }
            }
        });
    }
}
