package com.myntai.slightech.myntairobotromupdateservice.download;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.myntai.slightech.myntairobotromupdateservice.MyApplication;
import com.myntai.slightech.myntairobotromupdateservice.common.Common;
import com.myntai.slightech.myntairobotromupdateservice.common.GoUpRomLog;
import com.myntai.slightech.myntairobotromupdateservice.upgrade.RomGoUP;

public class DownloadRom {

    private String TAG = "DownloadRom";

    private long mTaskId;

    private DownloadManager mDownloadManager;
    private IDownloadStatus mIDownloadStatus;

    private BroadcastReceiver downloadSuccessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkDownloadStatus();//检查下载状态
        }
    };

    public void setListen(IDownloadStatus iDownloadStatus){
        this.mIDownloadStatus = iDownloadStatus;
    }

    public void download(String downloadFileUri, String fname) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadFileUri));
        request.setDestinationInExternalPublicDir(Common.DownLoadDir, fname + ".zip");
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setVisibleInDownloadsUi(true);

        mDownloadManager = (DownloadManager) MyApplication.getContext().getSystemService(MyApplication.getContext().DOWNLOAD_SERVICE);

        mTaskId = mDownloadManager.enqueue(request);
        MyApplication.getContext().registerReceiver(downloadSuccessReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public DownloadRom() {

    }

    //检查下载状态
    private void checkDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(mTaskId);//筛选下载任务，传入任务ID，可变参数
        Cursor c = mDownloadManager.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    GoUpRomLog.log("DownloadRom", "checkDownloadStatus", ">>>下载暂停");
                case DownloadManager.STATUS_PENDING:
                    GoUpRomLog.log("DownloadRom", "checkDownloadStatus", ">>>下载延迟");
                case DownloadManager.STATUS_RUNNING:
                    GoUpRomLog.log("DownloadRom", "checkDownloadStatus", ">>>正在下载");
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    GoUpRomLog.log("DownloadRom", "checkDownloadStatus", ">>>下载完成");
                    if(null != mIDownloadStatus){
                        mIDownloadStatus.onDownLoadSuccess("");
                    }else {
                        GoUpRomLog.log("DownloadRom","checkDownloadStatus","mIDownloadStatus = null 无法将成功上报");
                    }
                    break;
                case DownloadManager.STATUS_FAILED:
                    GoUpRomLog.log("DownloadRom", "checkDownloadStatus", ">>>下载失败");
                    if(null != mIDownloadStatus){
                        mIDownloadStatus.onDownLoadFail("");
                    }else {
                        GoUpRomLog.log("DownloadRom","checkDownloadStatus","mIDownloadStatus = null 无法将失败上报");
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
