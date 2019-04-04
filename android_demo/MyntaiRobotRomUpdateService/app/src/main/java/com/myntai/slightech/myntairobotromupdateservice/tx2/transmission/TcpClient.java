package com.myntai.slightech.myntairobotromupdateservice.tx2.transmission;

import com.myntai.slightech.myntairobotromupdateservice.Common;
import com.myntai.slightech.myntairobotromupdateservice.tx2.Tx2OtaLogTree;

import java.net.InetAddress;
import java.net.Socket;

public abstract class TcpClient implements Runnable {

    private int port;
    private String hostIP;
    private boolean connect = false;
    private SocketTransceiverUpload transceiver;

    /**
     * 建立连接
     * <p>
     * 连接的建立将在新线程中进行
     * <p>
     * 连接建立成功，回调{@code onConnect()}
     * <p>
     * 连接建立失败，回调{@code onConnectFailed()}
     *
     * @param hostIP 服务器主机IP
     * @param port   端口
     */
    public void connect(String hostIP, int port) {
        this.hostIP = hostIP;
        this.port = port;
        new Thread(this).start();
    }

    @Override
    public void run() {
        Thread.currentThread().setName("TcpClientThread");
        Tx2OtaLogTree.log("tcpclient run in: " + Thread.currentThread().getName());
        try {
            Socket socket = new Socket(hostIP, port);
            transceiver = new SocketTransceiverUpload(socket) {
                @Override
                public void onReady() {
                    onConnect(transceiver);
                }

                @Override
                public void onReceive(InetAddress addr, String s) {
                    TcpClient.this.onReceive(this, s);
                }

                @Override
                public void onDisconnect(InetAddress addr) {
                    connect = false;
                    TcpClient.this.onDisconnect(this);
                }
            };
            transceiver.start();
            connect = true;
        } catch (Exception e) {
            e.printStackTrace();
            this.onConnectFailed();
        }
    }

    /**
     * 断开连接
     * <p>
     * 连接断开，回调{@code onDisconnect()}
     */
    public void disconnect() {
        if (transceiver != null) {
            transceiver.stop();
            transceiver = null;
        }
    }

    /**
     * 判断是否连接
     *
     * @return 当前处于连接状态，则返回true
     */
    public boolean isConnected() {
        return connect;
    }

    /**
     * 获取Socket收发器
     *
     * @return 未连接则返回null
     */
    public SocketTransceiverUpload getTransceiver() {
        return isConnected() ? transceiver : null;
    }

    /**
     * 连接建立
     *
     * @param transceiver SocketTransceiver对象
     */
    public void onConnect(SocketTransceiverUpload transceiver) {

    }

    /**
     * 连接建立失败
     */
    public void onConnectFailed() {
    }

    /**
     * 接收到数据
     * <p>
     * 注意：此回调是在新线程中执行的
     *
     * @param transceiver SocketTransceiver对象
     * @param s           字符串
     */
    public void onReceive(SocketTransceiverUpload transceiver, final String s) {

        this.onDisplay(transceiver, s);

        if(s.equals(CMD.SEV_INQUIRY_UPLOAD_FILE_SIZE)){
            Tx2OtaLogTree.log("服务器询问 上传文件大小");
            transceiver.uploadFileSize(CMD.UPLOAD_PATH, CMD.UPLOAD_FILE_NAME) ;
        } else if (s.equals(CMD.SEV_READYED_RECEIVE_FILE)) {
            Tx2OtaLogTree.log("客户端开启上传");
            transceiver.uploadFile(CMD.UPLOAD_PATH, CMD.UPLOAD_FILE_NAME) ;
        } else if(s.equals(CMD.SEV_UPLOAD_SUCCESS)) {
            Tx2OtaLogTree.log("服务器已经接收完毕 服务器回传确认");
            transceiver.reqSerDecompressionExecSh() ;
        } else if(s.equals(CMD.SEV_ANDROID_REBOOT)) {
            Tx2OtaLogTree.log("服务器升级成功，android reboot");
            try {
                Tx2OtaLogTree.log("mkdir -p /sdcard/sdeno/");

                Common.shellExec("mkdir -p /sdcard/sdeno/ \n");

                Tx2OtaLogTree.log("echo \"1\" > /sdcard/sdeno/tx2_ota_update_status.txt\n");
                Common.shellExec("echo \"1\" > /sdcard/sdeno/tx2_ota_update_status.txt\n");

                Common.shellExec("reboot\n");
                Tx2OtaLogTree.log("reboot\n");

            }catch (Exception e){
                Tx2OtaLogTree.e("TcpClient", "onReceive", "3399重启异常", e);
            }

        }else if(s.equals(CMD.END_OF_COMMUNICATION)){
            Tx2OtaLogTree.log("服务端处理完毕 释放本次socket通信");
            disconnect();
            FileUtilTool.cleanFile(CMD.UPLOAD_PATH);
            Tx2OtaLogTree.log("tx2 缓存文件清理成功");
        }

    }

    /**
     * 连接断开
     * <p>
     * 注意：此回调是在新线程中执行的
     *
     * @param transceiver SocketTransceiver对象
     */
    public void onDisconnect(SocketTransceiverUpload transceiver) {
        Tx2OtaLogTree.log("断开本次socket回调 socket status = " + isConnected());
        //todo 清理下载的目录
    }

    public abstract void onDisplay(SocketTransceiverUpload transceiver, String s);
}

