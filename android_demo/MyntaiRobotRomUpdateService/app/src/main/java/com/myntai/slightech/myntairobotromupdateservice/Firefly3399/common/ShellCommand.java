package com.myntai.slightech.myntairobotromupdateservice.Firefly3399.common;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class ShellCommand {
    static private Process process = null;
    static private DataOutputStream dataOutputStream = null;
    static private DataInputStream dataInputStream = null;
    static private final String DELETE_SUCCESS = "deleteOtaPackageSuccess";

    public static String parsingLastInstallFileAndExecCommand(String parsingFile, int rowNum, int code) throws Exception {
        String command = null;
        StringBuffer stringBuffer = new StringBuffer();

        switch (code) {
            case 0:
                command = "sed -n " + rowNum + "p " + parsingFile + "\n"; //解析指定的行
                break;
            case 1:
                command = "tail -n 1 " + parsingFile + "\n";//解析最后一行 目的是检测是否成功删除了ota 升级包
                break;
            case 2:
                command = "echo " + DELETE_SUCCESS + " >> " + parsingFile + "\n";//删除操作执行后　添加删除标识
                break;
            case 3:
                command = "rm -f " + parsingFile + " \n";
                break;
            default:
                command = "\n";
        }

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
            throw e;
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
                throw e;
            }
        }

        if (stringBuffer.length() != 0) {
            return stringBuffer.toString();
        }
        return null;
    }

    static private Process process2 = null;
    static private DataOutputStream dataOutputStream2 = null;
    public static void shellExec(String command) throws Exception{
        try {
            process2 = Runtime.getRuntime().exec("su");
            dataOutputStream2 = new DataOutputStream(process2.getOutputStream());
            dataOutputStream2.writeBytes(command);
            dataOutputStream2.writeBytes("exit\n");
            dataOutputStream2.flush();
            process2.waitFor();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (dataOutputStream2 != null) {
                    dataOutputStream2.close();
                }
                process2.destroy();
            } catch (Exception e) {
                throw e;
            }
        }
    }
}
