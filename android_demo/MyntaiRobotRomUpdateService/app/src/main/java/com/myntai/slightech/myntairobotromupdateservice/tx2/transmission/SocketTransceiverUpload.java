package com.myntai.slightech.myntairobotromupdateservice.tx2.transmission;

import com.myntai.slightech.myntairobotromupdateservice.tx2.Tx2OtaLogTree;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Socket收发器 通过Socket发送数据，并使用新线程监听Socket接收到的数据
 *
 */
public abstract class SocketTransceiverUpload implements Runnable {

    protected Socket socket;
    protected InetAddress addr;
    protected DataOutputStream out;
    private boolean runFlag;
    InputStream is;

    /**
     * 实例化
     *
     * @param socket 已经建立连接的socket
     */
    public SocketTransceiverUpload(Socket socket) {
        this.socket = socket;
        this.addr = socket.getInetAddress();
    }

    /**
     * 获取连接到的Socket地址
     *
     * @return InetAddress对象
     */
    public InetAddress getInetAddress() {
        return addr;
    }

    /**
     * 开启Socket收发
     * <p>
     * 如果开启失败，会断开连接并回调{@code onDisconnect()}
     */
    public void start() {
        runFlag = true;
        new Thread(this).start();
    }

    /**
     * 断开连接(主动)
     * <p>
     * 连接断开后，会回调{@code onDisconnect()}
     */
    public void stop() {
        runFlag = false;
        try {
            socket.shutdownInput();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送字符串
     *
     * @param s 字符串
     * @return 发送成功返回true
     */
    public boolean send(String s) {
        Tx2OtaLogTree.log("SocketTransceiverUpload", "send", s);
        if (out != null) {
            final String ss = s;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Tx2OtaLogTree.log("准备发送的内容：" + ss + " 线程：" + Thread.currentThread().getName());
                        out.write(ss.getBytes());
                        out.flush();
                    } catch (IOException e) {
                        Tx2OtaLogTree.log(e.getMessage());
                        e.printStackTrace();
                    }

                }
            }).start();

            return true;
        }else {
            Tx2OtaLogTree.log("out == null");
        }
        return false;
    }

    /**
     * 监听Socket接收的数据(新线程中运行)
     */
    @Override
    public void run() {
        Thread.currentThread().setName("SocketTransceiverThread");
        Tx2OtaLogTree.log("SocketTransceiverThread run in: " + Thread.currentThread().getName());
        try {
            is = this.socket.getInputStream();
            out = new DataOutputStream(this.socket.getOutputStream());
            this.onReady();
        } catch (IOException e) {
            e.printStackTrace();
            runFlag = false;
        }
        while (runFlag) {
            try {
                String s = read(is);
                this.onReceive(addr, s);
            } catch (IOException e) {
                Tx2OtaLogTree.log("连接被断开(被动)" + e.getMessage());
                runFlag = false;
            }
        }
        // 断开连接
        try {
            is.close();
            out.close();
            socket.close();
            is = null;
            out = null;
            socket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.onDisconnect(addr);
    }

    /**
     * 接收到数据
     * <p>
     * 注意：此回调是在新线程中执行的
     *
     * @param addr 连接到的Socket地址
     * @param s    收到的字符串
     */
    public abstract void onReceive(InetAddress addr, String s);

    public abstract void onReady();
    /**
     * 连接断开
     * <p>
     * 注意：此回调是在新线程中执行的
     *
     * @param addr 连接到的Socket地址
     */
    public abstract void onDisconnect(InetAddress addr);

    public String read(InputStream in) throws IOException {
        String str = "";
        byte[] buf = new byte[1];
        int size = 0;
        StringBuffer sb = new StringBuffer();
        while ((size = in.read(buf, 0, buf.length)) != -1) {
            str = new String(buf);
            if (str.equals("\n")) {
                Tx2OtaLogTree.log("检测到 终止");
                break;
            }
            sb.append(str);
        }
        Tx2OtaLogTree.log(sb.toString());
        return sb.toString();
    }


    public void uploadFile(String fileDir, String fileName) {

        String readFileAllPath = fileDir + fileName;
        try {
            FileInputStream fis = new FileInputStream(readFileAllPath);
            FileUtilTool.inSteamToOutSteam(fis, out);
            Tx2OtaLogTree.log("客户端上传完毕");
            fis.close();

        } catch (FileNotFoundException e) {
            Tx2OtaLogTree.log(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Tx2OtaLogTree.log(e.getMessage());
            e.printStackTrace();
        }
    }

    public void uploadFileSize(String fileDir, String fileName){
        String readFileAllPath = fileDir + fileName;

        File f = new File(readFileAllPath);
        String size = "" + f.length() ;

        OutputStream os = out;
        try {
            os.write(size.getBytes());
            os.flush();
        } catch (IOException e) {
            Tx2OtaLogTree.log("SocketTransceiverUpload", "uploadFileSize", "" + e.getMessage());
            e.printStackTrace();
        }

    }

    /***
     * reqSerDecompressionExecSh
     * 请求服务端 解压并且 执行对应脚本
     * @return
     */
    public int reqSerDecompressionExecSh(){
        this.send(CMD.CLI_DECOMPRESSION_CMD);
        return  0 ;
    }
}
