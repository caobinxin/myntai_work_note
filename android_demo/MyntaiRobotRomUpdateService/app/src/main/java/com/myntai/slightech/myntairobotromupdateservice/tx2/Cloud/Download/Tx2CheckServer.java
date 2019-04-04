package com.myntai.slightech.myntairobotromupdateservice.tx2.Cloud.Download;

import com.myntai.slightech.myntairobotromupdateservice.tx2.Cloud.HttpUtil;
import com.myntai.slightech.myntairobotromupdateservice.tx2.Cloud.Tx2MarvinCloudConfig;
import com.myntai.slightech.myntairobotromupdateservice.tx2.Tx2OtaLogTree;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class Tx2CheckServer {
    private ICheckServer mICheckServer;

    public Tx2CheckServer(ICheckServer iCheckServer) {
        this.mICheckServer = iCheckServer;
    }

    public void requestServerCheckROMVersion(long tx2Version) {
        HttpUtil.sendOkHttpRequest(Tx2MarvinCloudConfig.getInstance().getUrl(), new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Tx2OtaLogTree.log("onFailure: " + e);
                if (null != mICheckServer) {
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                Tx2OtaLogTree.log("服务器返回数据：　" + responseData);
                Tx2AppUpdate appUpdate = new Tx2JsonUtil().parseJsonWithGsonRespond(responseData, Tx2AppUpdate.class);
                Tx2OtaLogTree.log("appupdate: " + appUpdate);

                if (null != mICheckServer) {
                    if (appUpdate.isHasNewVersion()) {
                        Tx2OtaLogTree.log("appUpdate.isHasNewVersion()　tx2 有新版本需要更新");
                        mICheckServer.onUpgrade(appUpdate);
                    } else {
                        Tx2OtaLogTree.log("appUpdate.isHasNewVersion()　tx2 当前版本为最新，不需要更新");
                        mICheckServer.onNoUpgrade(appUpdate);
                    }

                }
            }
        }, tx2Version);
    }
}
