package com.myntai.slightech.myntairobotromupdateservice.tx2.transmission;

import com.myntai.slightech.myntairobotromupdateservice.tx2.Tx2OtaLogTree;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class FileUtilTool {

    public void localCopy(String str, String dtr){
        int n = 0 ;
        final int BufSize = 1024 * 1024 ;
        try {
            FileInputStream fis = new FileInputStream(str) ;
            FileOutputStream fos = new FileOutputStream(dtr) ;
            byte[] bs = new byte[BufSize] ;

            while( ( n = fis.read(bs)) != -1){
                fos.write(bs, 0, n) ;
            }

            fis.close() ;
            fos.close() ;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }

    }

    public static void inSteamToOutSteam(FileInputStream fis, OutputStream fos){
        Tx2OtaLogTree.log("上传开始 thread:" + Thread.currentThread().getName());
        int n = 0 ;
        final int BufSize = 512 ;
        try {
            byte[] bs = new byte[BufSize] ;

            while( ( n = fis.read(bs)) != -1){
                fos.write(bs, 0, n) ;
                fos.flush() ;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void cleanFile(String dir){
        StringBuffer stringBuffer = new StringBuffer();
        Process process = null;
        DataOutputStream dataOutputStream = null;
        DataInputStream dataInputStream = null;

        String command = "rm -f " + dir + "*\n";
        Tx2OtaLogTree.log(command);

        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataInputStream = new DataInputStream(process.getInputStream());
            dataOutputStream.writeBytes(command);
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            InputStreamReader inputStreamReader = new InputStreamReader(
                    dataInputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(
                    inputStreamReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            process.waitFor();
        } catch (Exception e) {
            Tx2OtaLogTree.log(e.getMessage());
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
                process.destroy();
            } catch (Exception e) {
                Tx2OtaLogTree.log(e.getMessage());
            }
        }
    }
}
