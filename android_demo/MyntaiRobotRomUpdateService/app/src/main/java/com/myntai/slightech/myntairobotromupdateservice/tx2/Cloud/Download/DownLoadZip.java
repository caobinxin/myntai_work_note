package com.myntai.slightech.myntairobotromupdateservice.tx2.Cloud.Download;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;

import com.myntai.slightech.myntairobotromupdateservice.Common;
import com.myntai.slightech.myntairobotromupdateservice.MyApplication;
import com.myntai.slightech.myntairobotromupdateservice.tx2.Tx2OtaLogTree;

public class DownLoadZip {

    private long mTaskId;
    private ITx2DownLoadStatus iTx2DownLoadStatus;
    private static DownLoadZip instance ;
    private DownloadManager mDownloadManager;
    private BroadcastReceiver downloadSuccessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkDownloadStatus();//检查下载状态
        }
    };


    public void download(String downloadFileUri, String fname) {
        if(null == downloadFileUri){
            Tx2OtaLogTree.log("tx2 zip 下载地址为空");
            return;
        }
        Tx2OtaLogTree.log("tx2 zip包下载地址：" + downloadFileUri);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadFileUri));
        request.setDestinationInExternalPublicDir(Common.DownLoadTX2Dir, fname + ".zip");
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setVisibleInDownloadsUi(true);

        mDownloadManager = (DownloadManager) MyApplication.getContext().getSystemService(MyApplication.getContext().DOWNLOAD_SERVICE);

        mTaskId = mDownloadManager.enqueue(request);
        MyApplication.getContext().registerReceiver(downloadSuccessReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private DownLoadZip() {

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
                    Tx2OtaLogTree.log("DownloadRom", "checkDownloadStatus", ">>>下载暂停");
                case DownloadManager.STATUS_PENDING:
                    Tx2OtaLogTree.log("DownloadRom", "checkDownloadStatus", ">>>下载延迟");
                case DownloadManager.STATUS_RUNNING:
                    Tx2OtaLogTree.log("DownloadRom", "checkDownloadStatus", ">>>正在下载");
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    Tx2OtaLogTree.log("DownloadRom", "checkDownloadStatus", ">>>下载完成");
                    if(null != iTx2DownLoadStatus){
                        iTx2DownLoadStatus.onSuccess();
                    }else {
                        Tx2OtaLogTree.log("DownloadRom","checkDownloadStatus","mIDownloadStatus = null 无法将成功上报");
                    }
                    break;
                case DownloadManager.STATUS_FAILED:
                    Tx2OtaLogTree.log("DownloadRom", "checkDownloadStatus", ">>>下载失败");
                    if(null != iTx2DownLoadStatus){
                        iTx2DownLoadStatus.onFail();
                    }else {
                        Tx2OtaLogTree.log("DownloadRom","checkDownloadStatus","mIDownloadStatus = null 无法将失败上报");
                    }
                    break;
                default:
                    break;
            }
        }
    }


    public void setiTx2DownLoadStatus(ITx2DownLoadStatus iTx2DownLoadStatus) {
        this.iTx2DownLoadStatus = iTx2DownLoadStatus;
    }

    static public DownLoadZip getInstance() {
        if( null == instance){
            instance = new DownLoadZip();
        }
        return instance;
    }
}
