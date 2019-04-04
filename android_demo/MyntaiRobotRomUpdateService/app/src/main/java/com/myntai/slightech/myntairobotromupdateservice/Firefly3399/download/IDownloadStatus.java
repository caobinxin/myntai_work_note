package com.myntai.slightech.myntairobotromupdateservice.Firefly3399.download;

public interface IDownloadStatus {
    void onDownLoadSuccess(Object object);
    void onDownLoadFail(Object object);
    void onDownLoadAllSuccess(Object object);
    void onDownLoadAllFail(Object object);
    void onDownLoadDiffSuccess(Object object);
    void onDownLoadDiffFail(Object object);
    void onDownLoadAllAUTOSuccess(Object object);
    void onDownLoadAllAUTOFail(Object object);
}
