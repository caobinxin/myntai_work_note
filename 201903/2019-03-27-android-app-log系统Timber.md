# android-app-log系统Timber

## 1. 引用

在build.gradle(app)中添加

```groovy
implementation 'com.jakewharton.timber:timber:4.7.0'
```

## 2. 构建一个log树

```java
package com.myntai.slightech.testtimber;

import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import timber.log.Timber;

public class Tx2OtaLogTree extends Timber.Tree {
    private static final String TAG = "Tx2OtaLogTree";

    private static final String DIR_TX2_DEBUG = "tx2_ota.log";

    public static final String[] TAG_FILTERS = {"tx2-ota"};

    private ExecutorService mThreadExecutor = Executors.newFixedThreadPool(2);

    @Override
    protected void log(int priority, @Nullable String tag, String message, @Nullable Throwable t) {

        if (TextUtils.isEmpty(tag)) {
            return;
        }
        if (Arrays.binarySearch(TAG_FILTERS, tag) == -1) {
            return;
        }
        saveRosDebugLog(tag, message);
    }

    public void saveRosDebugLog(String tag1, String logMessage1) {
        final String tag = tag1;
        final String logMessage = logMessage1;
        mThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "run: saveRosDebugLog ...");
                StringBuilder stringBuilder = new StringBuilder();
                String time = getDateFormat().format(new Date(System.currentTimeMillis()));
                stringBuilder.append(time);
                stringBuilder.append(" : ");

                stringBuilder.append(TextUtils.isEmpty(tag) ? TAG : tag);

                stringBuilder.append(" : ");
                //追加本次打印参数
                stringBuilder.append(logMessage);

                stringBuilder.append("\n");

                Log.i(TAG, "run: " + stringBuilder.toString());

                //写入文件
                writeToFile(getTargetFile(), stringBuilder.toString());
            }
        });
    }


    private SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    private SimpleDateFormat getDayFormat() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    public File getTargetFile() {
        File dir = getTargetDir();
        String filePath = dir.getPath() + "/" + DIR_TX2_DEBUG;
        File myFile = new File(filePath);
        if (!myFile.exists()) {
            try {
                myFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return myFile;
    }

    private File getTargetDir() {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        //每次写入的时候都初始化文件的地址和日期，因为机器人开机会初始化
        String date = getDayFormat().format(new Date(System.currentTimeMillis()));
        // SDCARD/sdeno/log/日期/ros-debug.log
        String dirPath = root + "/sdeno/log/" + date;

        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * 写入日志内容到文件
     *
     * @param saveFile 文件对象
     * @param log      日志内容
     */
    public void writeToFile(File saveFile, String log) {
        try {
            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }

            FileUtil.checkFileExistOrCreate(saveFile);

            FileOutputStream outputStream = new FileOutputStream(saveFile, true);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            writer.write(log);
            writer.flush();
            writer.close();
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "writeToFile: " + e);
        }
    }

    public static String getStackTrace(Throwable throwable) {
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            pw.flush();
            sw.flush();
        } finally {
            try {
                if (sw != null) {
                    sw.close();
                }

                if (pw != null) {
                    pw.close();
                }
            } catch (IOException e) {
            }
        }
        return sw.toString();
    }

    public static String getCause(Throwable ex) {
        return getStackTrace(ex);
    }

    private static void logDebug(String message, Object... objects) {
        for (String tag : TAG_FILTERS) {
            Timber.tag(tag).d(message, objects);
        }
    }

    private static String assembleLogMessage(@Nullable String classTag, @Nullable String methodName, String... logs) {
        // classTag#methodName#logContent
        StringBuilder logBuilder = new StringBuilder();

        logBuilder.append(classTag).append("#");

        logBuilder.append(methodName).append("#");

        if (logs != null && logs.length > 0) {
            for (String log : logs) {
                logBuilder.append("#").append(log);
            }
        }
        return logBuilder.toString();
    }

    public static void i(@Nullable String classTag, @Nullable String methodName, String... logs) {
        String message = assembleLogMessage(classTag, methodName, logs);
        logDebug(message);
    }

    public static void e(@Nullable String classTag, @Nullable String methodName, String exceptionDesc, Exception e) {
        String message = assembleLogMessage(classTag, methodName, exceptionDesc, getCause(e));
        logDebug(message);
    }
}

```

## 3. 使用

```java
package com.myntai.slightech.testtimber;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import timber.log.Timber;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Timber.plant(new Tx2OtaLogTree());//种树
        Tx2OtaLogTree.i("hello","helloMethod", "测试") ;

    }

    private static class CrashReportingTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {

        }
    }
}

```

