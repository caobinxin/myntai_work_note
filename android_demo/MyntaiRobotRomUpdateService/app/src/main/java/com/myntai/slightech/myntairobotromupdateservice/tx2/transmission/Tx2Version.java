package com.myntai.slightech.myntairobotromupdateservice.tx2.transmission;
import android.widget.Toast;

import com.myntai.slightech.myntairobotromupdateservice.Common;
import com.myntai.slightech.myntairobotromupdateservice.MyApplication;
import com.myntai.slightech.myntairobotromupdateservice.tx2.Tx2OtaLogTree;

public abstract class Tx2Version {

    private TcpClientTx2Version tcpClientTx2Version = new TcpClientTx2Version();
    /***
     * 代表状态跟新
     */
    abstract public void onVersionUpdate(long tx2Version);
    abstract public void onDisplayToast(String s) ;

    public void requestTx2Version(){
        connect(Common.TX2_SERVICE_IP, Common.TX2_SERVICE_PORT);
    }

    private class TcpClientTx2Version extends TcpClient{
        @Override
        public void onDisplay(SocketTransceiverUpload transceiver, String s) {
            onDisplayToast(s);
        }

        @Override
        public void onReceive(SocketTransceiverUpload transceiver, String s) {
            this.onDisplay(transceiver, s);
            if(s.equals(CMD.END_OF_COMMUNICATION)){
                Tx2OtaLogTree.log("服务端处理完毕 释放本次socket通信");
                disconnect();
            }else {
                Tx2OtaLogTree.log("TcpClientTx2Version", "onReceive", "" + s);
                long version = Long.parseLong(s);
                Tx2OtaLogTree.log("Long.parseLong(s) = " + version);
                onVersionUpdate(version) ;
            }
        }

        @Override
        public void onConnect(SocketTransceiverUpload transceiver) {
            Tx2OtaLogTree.log("TcpClientTx2Version", "onConnect", "客户端已经连接成功");
            sendVersionClient() ;

        }

        @Override
        public void onConnectFailed() {
            Tx2OtaLogTree.log("TcpClientTx2Version", "onConnectFailed", "客户端连接失败");
        }

        @Override
        public void onDisconnect(SocketTransceiverUpload transceiver) {
            Tx2OtaLogTree.log("TcpClientTx2Version", "onDisconnect", "断开连接");
        }

        private void sendVersionClient(){
            try {
                String data = Common.TX2_CMD_VERSION;
                Tx2OtaLogTree.log(data);
                tcpClientTx2Version.getTransceiver().send(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void connect(String IP, int serverPort) {
        if (tcpClientTx2Version.isConnected()) {
            // 断开连接
            tcpClientTx2Version.disconnect();
        } else {
            try {
                String hostIP = IP;
                int port = serverPort;
                tcpClientTx2Version.connect(hostIP, port);
            } catch (NumberFormatException e) {
                Toast.makeText(MyApplication.getContext(), "端口错误", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}
