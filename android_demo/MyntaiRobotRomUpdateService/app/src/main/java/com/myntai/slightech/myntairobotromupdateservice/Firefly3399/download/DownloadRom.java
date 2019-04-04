package com.myntai.slightech.myntairobotromupdateservice.Firefly3399.download;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;

import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.AndroidOtaLogTree;
import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.ShellCommand;
import com.myntai.slightech.myntairobotromupdateservice.MyApplication;
import com.myntai.slightech.myntairobotromupdateservice.Common;
import com.myntai.slightech.myntairobotromupdateservice.R;

public class DownloadRom {

    private long mTaskId;

    private DownloadManager mDownloadManager;
    private IDownloadStatus mIDownloadStatus;
    private static int FailDownloadCountMax = 10;
    private static int mFailDownloadCount = 0;
    private static String mCurrentDownLoadUrl = null;
    private static String mCurrentFirmwareName = null;

    private BroadcastReceiver downloadSuccessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkDownloadStatus();//检查下载状态
        }
    };

    public void setListen(IDownloadStatus iDownloadStatus) {
        this.mIDownloadStatus = iDownloadStatus;
    }

    public void download(String downloadFileUri, String fname) {
        mCurrentDownLoadUrl = downloadFileUri;
        mCurrentFirmwareName = fname;

        deleteTmpDownloadFile();

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

    /***
     * 检查下载状态
     */
    private void checkDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        /*筛选下载任务，传入任务ID，可变参数*/
        query.setFilterById(mTaskId);
        Cursor c = mDownloadManager.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    AndroidOtaLogTree.log("DownloadRom", "checkDownloadStatus", ">>>下载暂停");
                    break;
                case DownloadManager.STATUS_PENDING:
                    AndroidOtaLogTree.log("DownloadRom", "checkDownloadStatus", ">>>下载延迟");
                    break;
                case DownloadManager.STATUS_RUNNING:
                    AndroidOtaLogTree.log("DownloadRom", "checkDownloadStatus", ">>>正在下载");
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    retryCurrentDownLoadTaskReset();
                    AndroidOtaLogTree.log("DownloadRom", "checkDownloadStatus", ">>>下载完成");
                    if (null != mIDownloadStatus) {
                        mIDownloadStatus.onDownLoadSuccess("");
                    } else {
                        AndroidOtaLogTree.log("DownloadRom", "checkDownloadStatus", "mIDownloadStatus = null 无法将成功上报");
                    }
                    break;
                case DownloadManager.STATUS_FAILED:
                    AndroidOtaLogTree.log("DownloadRom", "checkDownloadStatus", ">>>下载失败");

                    retryCurrentDownLoadTask();
                    if (null != mIDownloadStatus) {
                        mIDownloadStatus.onDownLoadFail("");
                    } else {
                        AndroidOtaLogTree.log("DownloadRom", "checkDownloadStatus", "mIDownloadStatus = null 无法将失败上报");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void retryCurrentDownLoadTask() {
        if (null == mCurrentDownLoadUrl || null == mCurrentFirmwareName) {
            AndroidOtaLogTree.log("retryCurrentDownLoadTask　参数传入有问题，不执行后续操作");
            return;
        }

        if (mFailDownloadCount > FailDownloadCountMax || mFailDownloadCount < 0) {
            AndroidOtaLogTree.log("mFailDownloadCount >= 10 || mFailDownloadCount < 0　退出失败重试机制");
            mFailDownloadCount = 0;
            return;
        }

        AndroidOtaLogTree.log("当前是第" + mFailDownloadCount + "次，下载失败重试");
        AndroidOtaLogTree.log("重试url:" + mCurrentDownLoadUrl + " name:" + mCurrentFirmwareName);
        mFailDownloadCount++;

        download(mCurrentDownLoadUrl, mCurrentFirmwareName);

    }

    private void retryCurrentDownLoadTaskReset() {
        mCurrentDownLoadUrl = null;
        mCurrentFirmwareName = null;
        mFailDownloadCount = 0;

    }

    private void deleteTmpDownloadFile(){
        String dir = MyApplication.getContext().getResources().getText(R.string.SAVE_DIR_PATH).toString();
        try {
            ShellCommand.shellExec("rm " + dir + "* \n");
        } catch (Exception e) {
            AndroidOtaLogTree.log(getClass().getSimpleName(), "retryCurrentDownLoadTask()", "rm " + dir + "*");
        }
    }
}
