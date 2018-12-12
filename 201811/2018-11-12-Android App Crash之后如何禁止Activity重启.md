# Android App Crash之后如何禁止Activity重启

针对Crash，Android默认的处理方式是，退出App、弹一个提示框。





![img](https:////upload-images.jianshu.io/upload_images/300515-cfe6569c0ed14f51.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/338)

系统默认Crash提示

 这样的反馈难免有些

暴力

，

还丑

，我们需要更温和一点的提示，于是自定义UncatchExceptionHandler来截获处理Crash：



```
/** 
 * 初始化 
 *  
 * @param context 
 */  
public void init(Context context) {  
    mContext = context;  
    // 获取系统默认的UncaughtException处理器  
    mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();  
    // 设置该CrashHandler为程序的默认处理器  
    Thread.setDefaultUncaughtExceptionHandler(this);  
}

/** 
 * 当UncaughtException发生时会转入该函数来处理 
 */  
@Override  
public void uncaughtException(Thread thread, Throwable ex) {
    if (!handleException(ex) && mDefaultHandler != null) {
        //如果用户没有处理则让系统默认的异常处理器来处理
        mDefaultHandler.uncaughtException(thread, ex);
    } else {
        try {
            // 暂停3秒
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Log.e(TAG, "error : ", e);
        }
        // 退出程序
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}

/** 
 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 
 *  
 * @param ex 
 * @return true:如果处理了该异常信息;否则返回false. 
 */  
private boolean handleException(Throwable ex) {  
    if (ex == null) {  
        return false;  
    }
    // 使用Toast来显示异常信息  
    new Thread() {
        @Override
        public void run() {
            Looper.prepare();
            Toast.makeText(mContext, "程序出现异常，即将退出～", Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }.start();

    return true;  
}
```

效果如下：



![img](https:////upload-images.jianshu.io/upload_images/300515-7241570b50feffd6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/330)

自定义Crash提示

但是问题也随之而来，自定义CrashHandler在退出App之后，<b>会重启当前奔溃的Activity</b>，如果奔溃的是LaunchActivity，那么会不断奔溃、重启、奔溃、重启……当然，也不用担心进入死循环（系统有做限制），但是体验极差。

于是对比Logcat日志：
 <pre>
 系统默认Handler

I/ActivityManager( 1238): Process com.netease.mail.oneduobaohydrid.debug (pid 2780) has died
 W/ActivityManager( 1238): Scheduling restart of crashed service com.netease.mail.oneduobaohydrid.debug/com.netease.mail.oneduobaohydrid.service.CommonService in 1000ms
 ……
 I/ActivityManager( 1238): Start proc com.netease.mail.oneduobaohydrid.debug for service com.netease.mail.oneduobaohydrid.debug/com.netease.mail.oneduobaohydrid.service.CommonService: pid=2819 uid=10054 gids={50054, 9997, 1028, 1015, 3003} abi=x86
 </pre>

<pre>

自定义Handler

I/ActivityManager( 1238): Process com.netease.mail.oneduobaohydrid.debug (pid 2993) has died
 W/ActivityManager( 1238): Scheduling restart of crashed service com.netease.mail.oneduobaohydrid.debug/com.netease.mail.oneduobaohydrid.service.CommonService in 64000ms
 ……
 I/ActivityManager( 1238): Start proc com.netease.mail.oneduobaohydrid.debug for activity com.netease.mail.oneduobaohydrid.debug/com.netease.mail.oneduobaohydrid.activity.MainActivity: pid=3030 uid=10054 gids={50054, 9997, 1028, 1015, 3003} abi=x86
 </pre>

相同点：Crash之后App对应的Process都被杀死，然后都安排重启Service。

不同点：自定义CrashHandler，存在一个由ActivityManager启动对应Activity的系统行为。

多放查阅资料，发现App Crash之后系统会重新启动Task栈顶的Activity，具体请自行google！

<b>解决方法是：</b>在杀死App对应Process之前，结束掉Task栈中所有的Activity。

```
/** 
 * 当UncaughtException发生时会转入该函数来处理 
 */  
@Override  
public void uncaughtException(Thread thread, Throwable ex) {
    if (!handleException(ex) && mDefaultHandler != null) {
        //如果用户没有处理则让系统默认的异常处理器来处理
        mDefaultHandler.uncaughtException(thread, ex);
    } else {
        try {
            // 暂停3秒
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Log.e(TAG, "error : ", e);
        }
        // 结束所有Activity
        OneApplication.finishAllActivities();
        // 退出程序
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}
```

核心代码：OneApplication.finishAllActivities();

[参考资料]
 [http://stackoverflow.com/questions/22429197/controlling-android-activity-restart-after-a-process-stops](https://link.jianshu.com?t=http://stackoverflow.com/questions/22429197/controlling-android-activity-restart-after-a-process-stops)

[http://stackoverflow.com/questions/5423571/prevent-activity-stack-from-being-restored](https://link.jianshu.com?t=http://stackoverflow.com/questions/5423571/prevent-activity-stack-from-being-restored)

[https://groups.google.com/forum/#!topic/android-developers/snTPpksX9TU](https://link.jianshu.com?t=https://groups.google.com/forum/#!topic/android-developers/snTPpksX9TU)

作者：Cavabiao

链接：https://www.jianshu.com/p/636edca24443

來源：简书

简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。