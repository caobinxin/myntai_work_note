package com.myntai.slightech.myntairobotromupdateservice.tx2.Cloud.Download;
import android.os.Handler;
import android.widget.Toast;

import com.myntai.slightech.myntairobotromupdateservice.Common;
import com.myntai.slightech.myntairobotromupdateservice.MyApplication;
import com.myntai.slightech.myntairobotromupdateservice.tx2.Tx2OtaLogTree;
import com.myntai.slightech.myntairobotromupdateservice.tx2.transmission.SocketTransceiverUpload;
import com.myntai.slightech.myntairobotromupdateservice.tx2.transmission.TcpClient;


public class DownloadTx2 {

    private Tx2AppUpdate tx2AppUpdate ;
    static private DownloadTx2 instance ;
    private Handler mainHander ;

    private TcpClient tcpClientUpload = new TcpClient() {
        @Override
        public void onConnect(SocketTransceiverUpload transceiver) {
            try {
                Tx2OtaLogTree.log("服务器连接成功");
                String data = Common.TX2_CMD_UPLOAD;
                Tx2OtaLogTree.log(data);
                tcpClientUpload.getTransceiver().send(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConnectFailed() {

        }

        @Override
        public void onDisplay(SocketTransceiverUpload transceiver, String s) {
            if(null == mainHander){
                return;
            }
            final String ss = s ;

            mainHander.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyApplication.getContext(), ss, Toast.LENGTH_LONG).show();
                }
            });

        }
    } ;

    static public DownloadTx2 getInstance() {
        if( null == instance){
            instance = new DownloadTx2();
        }
        return instance;
    }

    private DownloadTx2(){

    }

    public void setMainHander(Handler mainHander) {
        this.mainHander = mainHander;
    }

    public void confirmDownload(){
        if( null == tx2AppUpdate){
            Tx2OtaLogTree.log("DownloadTx2", "confirmDownload", "null == tx2AppUpdate" + " 不进行下载");
            return;
        }
        startDownLoadZIP();
    }

    public void temporarilyDownload(){

    }

    public void setTx2AppUpdate(Tx2AppUpdate tx2AppUpdate) {
        this.tx2AppUpdate = tx2AppUpdate;
    }

    private void startDownLoadZIP(){
        Tx2OtaLogTree.log("开始下载");
        DownLoadZip.getInstance().setiTx2DownLoadStatus(new downloadStatus());
        DownLoadZip.getInstance().download(tx2AppUpdate.getFullFirmwareRrl(), "test");
    }

    class downloadStatus implements ITx2DownLoadStatus{
        @Override
        public void onSuccess() {
            Tx2OtaLogTree.log("下载成功 回传成功");
            Toast.makeText(MyApplication.getContext(), "tx2 升级包下载成功，开始透传", Toast.LENGTH_SHORT).show();
            startTransmission();
        }

        @Override
        public void onFail() {
            Tx2OtaLogTree.log("下载失败 回传成功");
            Toast.makeText(MyApplication.getContext(), "tx2 升级包下载失败!!! 请重新下载升级。", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTransmission(){
        Tx2OtaLogTree.log("开始通过 tcp 传输给 tx2");
        connect(Common.TX2_SERVICE_IP, Common.TX2_SERVICE_PORT);
    }

    private void connect(String IP, int serverPort) {
        if (tcpClientUpload.isConnected()) {
            // 断开连接
            tcpClientUpload.disconnect();
        } else {
            try {
                String hostIP = IP;
                int port = serverPort;
                tcpClientUpload.connect(hostIP, port);
            } catch (NumberFormatException e) {
                Toast.makeText(MyApplication.getContext(), "端口错误", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}
