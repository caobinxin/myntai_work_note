package com.myntai.slightech.httpurlconnection;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class DownloadRom {

    private String TAG = "DownloadRom";

    private long mTaskId;

    private DownloadManager mDownloadManager;

    private BroadcastReceiver downloadSuccessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkDownloadStatus();//检查下载状态
        }
    };

    public void download(String downloadFileUri){
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadFileUri));

        request.setDestinationInExternalPublicDir("/androidRomUpdate", "update.zip");
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setVisibleInDownloadsUi(true);

        mDownloadManager = (DownloadManager)MyApplication.getContext().getSystemService(MyApplication.getContext().DOWNLOAD_SERVICE);

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
                    Log.i(TAG, "checkDownloadStatus: >>>下载暂停");
                case DownloadManager.STATUS_PENDING:
                    Log.i(TAG, "checkDownloadStatus: >>>下载延迟");
                case DownloadManager.STATUS_RUNNING:
                    Log.i(TAG, "checkDownloadStatus: >>>正在下载");
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    Log.i(TAG, "checkDownloadStatus: >>>下载完成");
                    //下载完成安装APK
                    //TODO 下载完成后　回调用户是否立即升级
                    break;
                case DownloadManager.STATUS_FAILED:
                    Log.i(TAG, "checkDownloadStatus: >>>下载失败");
                    break;
            }
        }
    }
}
