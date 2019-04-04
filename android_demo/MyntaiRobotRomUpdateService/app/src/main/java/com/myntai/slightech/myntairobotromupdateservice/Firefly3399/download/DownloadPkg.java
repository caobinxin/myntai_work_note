package com.myntai.slightech.myntairobotromupdateservice.Firefly3399.download;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.myntai.slightech.myntairobotromupdateservice.MyApplication;
import com.myntai.slightech.myntairobotromupdateservice.R;
import com.myntai.slightech.myntairobotromupdateservice.Common;
import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.AndroidOtaLogTree;
import com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common.ShellCommand;

import java.util.List;

public class DownloadPkg implements IDownloadStatus {
    private AppUpdate mAppupdate;
    private IDownloadStatus mIDownloadStatus;

    @Override
    public void onDownLoadAllAUTOSuccess(Object object) {

    }

    @Override
    public void onDownLoadAllAUTOFail(Object object) {

    }

    @Override
    public void onDownLoadAllSuccess(Object object) {

    }

    @Override
    public void onDownLoadAllFail(Object object) {

    }

    @Override
    public void onDownLoadDiffSuccess(Object object) {

    }

    @Override
    public void onDownLoadDiffFail(Object object) {

    }

    private DownloadRom downloadRom;
    private List<String> diffUrlList;
    private int MODE_TYPE;
    private HandlerDiff msubTheadHandler;
    private final static int WHAT_HANDLE_DIFF_DOWNLOAD = 1;
    private int countDiffPkg = 0;
    private String allPkgName;

    @Override
    public void onDownLoadSuccess(Object object) {
        if (null != mIDownloadStatus) {

            if(MODE_TYPE == Common.MODE_TYPE_ALL_PKG){
                mIDownloadStatus.onDownLoadAllSuccess(allPkgName+".zip");
                mIDownloadStatus.onDownLoadSuccess("");
            }else if(MODE_TYPE == Common.MODE_TYPE_DIFF_PKG){
                if(diffUrlList.isEmpty()){
                    mIDownloadStatus.onDownLoadDiffSuccess(countDiffPkg);
                    mIDownloadStatus.onDownLoadSuccess("");
                }else{
                    Message message = Message.obtain();
                    message.what = WHAT_HANDLE_DIFF_DOWNLOAD;
                    msubTheadHandler.sendMessage(message);
                }
            }else if(MODE_TYPE == Common.MODE_TYPE_ALL_PKG_AUTO){
                mIDownloadStatus.onDownLoadAllAUTOSuccess(allPkgName+".zip");
                mIDownloadStatus.onDownLoadSuccess("");
            }
        } else {
            AndroidOtaLogTree.log("DownloadPkg", "onDownLoadSuccess", "mIDownloadStatus = null 无法将成功上报");
        }
    }

    @Override
    public void onDownLoadFail(Object object) {

    }


    public static DownloadPkg getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public void set3399AppUpdate(AppUpdate appUpdate) {
        this.mAppupdate = appUpdate;
    }

    public void set3399Listen(IDownloadStatus iDownloadStatus) {
        this.mIDownloadStatus = iDownloadStatus;
    }

    public void downloadType(int type) {
        //todo 下载前先将当前目录下的文件全清理了
        clearAndroidRomDir();
        switch (type) {
            case Common.DOWNLOADPKG_TYPE_ALL:
                MODE_TYPE = Common.MODE_TYPE_ALL_PKG;
                startDownload3399AllPkg();
                //下载全包
                break;
            case Common.DOWNLOADPKG_TYPE_DIFF:
                MODE_TYPE = Common.MODE_TYPE_DIFF_PKG;
                startDownload3399DiffPkg();
                //下载差分包
                break;
            case Common.DOWNLOADPKG_TYPE_ALL_AUTO:
                MODE_TYPE = Common.MODE_TYPE_ALL_PKG_AUTO;
                startDownload3399AllPkg();
            default:
                break;
        }
    }

    private DownloadPkg() {
        downloadRom = new DownloadRom();
        downloadRom.setListen(this);
        new Thread(){
            @Override
            public void run() {
                super.run();
                Looper.prepare();
                msubTheadHandler = new HandlerDiff(Looper.myLooper());
                Looper.loop();
            }
        }.start();

    }

    private static class InstanceHolder {
        private static final DownloadPkg INSTANCE = new DownloadPkg();
    }

    private void startDownload3399AllPkg() {
        if (null != mAppupdate) {
            AndroidOtaLogTree.log("全包下载地址：" + mAppupdate.getFullFirmwareRrl());
            allPkgName = mAppupdate.getVersionName();
            downloadRom.download(mAppupdate.getFullFirmwareRrl(), allPkgName);
        } else {
            AndroidOtaLogTree.log("DownloadPkg", "startDownload3399AllPkg", "mAppupdate = null 无法开始下载");
        }
    }

    private void startDownload3399DiffPkg() {
        if (null != mAppupdate) {
            diffUrlList = mAppupdate.getFireflyFirmware();
            Message message = Message.obtain();
            message.what = WHAT_HANDLE_DIFF_DOWNLOAD;
            msubTheadHandler.sendMessage(message);
        } else {
            AndroidOtaLogTree.log("DownloadPkg", "startDownload3399DiffPkg", "mAppupdate = null 无法开始下载");
        }
    }

    private void downLoadDiff() {
        if (null != diffUrlList) {
            if (!diffUrlList.isEmpty()) {
                String diffUrl = diffUrlList.get(0);
                ++countDiffPkg;
                AndroidOtaLogTree.log("当前差分包下载地址：　" + diffUrl);
                downloadRom.download(diffUrl, "" + countDiffPkg);
                diffUrlList.remove(0);
            }

        }
    }

    private class HandlerDiff extends Handler{
        public HandlerDiff(Looper looper){
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_HANDLE_DIFF_DOWNLOAD:
                    downLoadDiff();
                    break;
                default:
                    break;
            }
            return;
        }
    }

    private void clearAndroidRomDir(){
        String dir = MyApplication.getContext().getResources().getText(R.string.SAVE_DIR_PATH).toString();
        try {
            ShellCommand.shellExec("rm " +  dir + "* \n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
