# 在Android中替换掉原有的HOME，实现自主的控制Launcher

 一:用户可自行添加HOME
​        Android2.3.7共有两个home，一个在packages/apps/Launcher2、另一个在development/samples/Home。
1：launcher2，系统默认的。去除launcher2 直接修改它的AndroidManifest.xml中的
<category android:name="android.intent.category.HOME" />注释掉
2：samples下的Home只是个样例，但是也在Intent过滤器里注册了Home，所以，如果单单干掉Launcher2，这个还是在的，还是会出现选择Home的选项，我们可以将两个Home都干掉，替换成自己的。修改samples/Home/AndroidManifest.xml。
将里面的<category android:name="android.intent.category.HOME" />这句注释掉
3：替换成自己的launcher

修改AndroidManifest.xml。在<intent-filter>里的值改为：

                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.HOME" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.MONKEY"/>

这一块可以直接用第三方apk，将第三方apk放在/vendor/mediatek/nollec73_gb/artifacts/out/target/product/nollec73_gb/system/app下
然后重新编译提取iamge烧到板子中就可以了
二：定制自己的Home 用户无法重新安装第三方home
如果你要定制一个Android系统，你想用你自己的Launcher(Home)作主界面来替换Android自己的Home，而且不希望用户安装的Launcher来替换掉你的Launcher. 
我们可以通过修改Framework来实现这样的功能。 

1)首先了解一下Android的启动过程。 
  Android系统的启动先从Zygote开始启动，然后......(中间的过程就不说了).....一直到了SystemServer(framework)这个地方，看到这段代码：

/**     * This method is called from Zygote to initialize the system. This will cause the native     * services (SurfaceFlinger, AudioFlinger, etc..) to be started. After that it will call back     * up into init2() to start the Android services.     */    native public static void init1(String[] args);    public static void main(String[] args) {        if (SamplingProfilerIntegration.isEnabled()) {            SamplingProfilerIntegration.start();            timer = new Timer();            timer.schedule(new TimerTask() {                @Override                public void run() {                    SamplingProfilerIntegration.writeSnapshot("system_server");                }            }, SNAPSHOT_INTERVAL, SNAPSHOT_INTERVAL);        }        // The system server has to run all of the time, so it needs to be        // as efficient as possible with its memory usage.        VMRuntime.getRuntime().setTargetHeapUtilization(0.8f);        System.loadLibrary("android_servers");        init1(args);    }    public static final void init2() {        Log.i(TAG, "Entered the Android system server!");        Thread thr = new ServerThread();        thr.setName("android.server.ServerThread");        thr.start();    }}

 

 

从SystemServer的main函数开始启动各种服务。 
首先启动init1,然后启动init2. 
从上面的注释可以看到：init1这个方法时被Zygote调用来初始化系统的，init1会启动native的服务如SurfaceFlinger,AudioFlinger等等，这些工作做完以后会回调init2来启动Android的service。

这里我们主要来关注init2的过程。 
init2中启动ServerThread线程， 
ServerThread中启动了一系列的服务，比如这些：

ActivityManagerService 
EntropyService 
PowerManagerService 
TelephonyRegistry 
PackageManagerService 
AccountManagerService 
BatteryService 
HardwareService 
Watchdog 
SensorService 
BluetoothService 
StatusBarService 
ClipboardService 
InputMethodManagerService 
NetStatService 
ConnectivityService 
AccessibilityManagerService 
NotificationManagerService 
MountService 
DeviceStorageMonitorService 
LocationManagerService 
SearchManagerService 
FallbackCheckinService 
WallpaperManagerService 
AudioService 
BackupManagerService 
AppWidgetService

这些大大小小的服务起来以后，开始 
((ActivityManagerService)ActivityManagerNative.getDefault()).systemReady() 
在systemReady后开始开始启动Launcher。

在寻找Launcher的时候是根据HOME的filter（在Manifest中定义的<category android:name="android.intent.category.HOME" />）来过滤。 
然后根据filter出来的HOME来启动，如果只有一个HOME，则启动这个HOME，如果用户自己装了HOME，那就会弹出来一个列表供用户选择。

我们现在希望从这里弹出我们自己定制的Launcher,同时也不希望弹出选择HOME的界面，我们不希望用户修改我们的home，比如我们的home上放了好多广告，以及强制安装的程序，不希望用户把它干掉。

我们可以通过这样来实现：

2) 定义一个私有的filter选项，然后用这个选项来过滤HOME. 
   一般情况下我们使用Manifest中定义的<category android:name="android.intent.category.HOME"来过滤的，我们现在增加一个私有的HOME_FIRST过滤。

     在Intent.java(frameworks/base/core/java/android/content/Intent.java)中添加两行代码
    
    //lixinso:添加CATEGORY_HOME_FIRST 
    @SdkConstant(SdkConstantType.INTENT_CATEGORY) 
    public static final String CATEGORY_HOME_FIRST = "android.intent.category.HOME_FIRST";

3）修改和CATEGORY_HOME相关的所有的地方，都改成HOME_FIRST，主要是framework中的这几个地方：

    frameworks/base/services/java/com/android/server/am/ActivityManagerService.java中 
    //intent.addCategory(Intent.CATEGORY_HOME); 
    改成intent.addCategory(Intent.CATEGORY_HOME_FIRST); //lixinso: 
    //if (r.intent.hasCategory(Intent.CATEGORY_HOME)) { 
    改成if (r.intent.hasCategory(Intent.CATEGORY_HOME_FIRST)) { //lixinso: Intent.CATEGORY_HOME -> Intent.CATEGORY_HOME_FIRST 
   frameworks/base/services/java/com/android/server/am/HistoryRecorder.java中 
   // _intent.hasCategory(Intent.CATEGORY_HOME) && 
   改成 _intent.hasCategory(Intent.CATEGORY_HOME_FIRST) && //lixinso: Intent.CATEGORY_HOME->Intent.CATEGORY_HOME_FIRST

   frameworks/policies/base/mid/com/android/internal/policy/impl/MidWindowManager.java中 
   //mHomeIntent.addCategory(Intent.CATEGORY_HOME); 
   改成 mHomeIntent.addCategory(Intent.CATEGORY_HOME_FIRST); //lixinso 
  frameworks/policies/base/mid/com/android/internal/policy/impl/RecentApplicationsDialog.java中 
   //new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),0); 
   改成 new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME_FIRST),0); //lixinso

  frameworks/policies/base/phone/com/android/internal/policy/impl/PhoneWindowManager.java中 
   //mHomeIntent.addCategory(Intent.CATEGORY_HOME); 
   改成 mHomeIntent.addCategory(Intent.CATEGORY_HOME_FIRST); //lixinso

  frameworks/policies/base/phone/com/android/internal/policy/impl/RecentApplicationsDialog.java中 
   //ResolveInfo homeInfo = pm.resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),0); 
   改成 ResolveInfo homeInfo = pm.resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME_FIRST),0); //lixinso

4) 写一个自己的Launcher. 
   可以参考android sample中的Launcher，或者android源代码中的 /packages/apps/Launcher 来写。 
   在Launcher中标记其是不是Launcher的最关键的代码时Manifest中的filter:android:name="android.intent.category.HOME" 
   现在我们定义了自己的filter,那么，我们在我们自己写的Launcher中将Manifest改为： 
​    <application  android:process="android.process.acore3" android:icon="@drawable/icon" android:label="@string/app_name"> 
​        <activity android:name=".FirstAppActivity" 
​                  android:label="@string/app_name"> 
​            <intent-filter> 
​                                <action android:name="android.intent.action.MAIN" /> 
​                <category android:name="android.intent.category.HOME_FIRST" /> 
​                <category android:name="android.intent.category.DEFAULT" /> 
​                <category android:name="android.intent.category.MONKEY" /> 
​            </intent-filter> 
​        </activity> 
​    </application>

然后将编译好的apk放到/out/target/product/generic/system/app目录下。

5)将Android自带的Launcher删除掉，包括源代码(packages/apps/Launcher)和apk(/out/target/product/generic/system/app/Launcher.apk)。

6) 
做完这些工作，就可以重新编译Android了，我们可以编译修改过的几个相关的包。 
如果之前编译过了Android源码，可以用mmm命令来编译部分的改动。 
这里需要这样编译：

$ . build/envsetup.sh 
$ mmm frameworks/base 
$ mmm frameworks/base/services/java 
$ mmm frameworks/policies/base/mid 
$ mmm frameworks/policies/base/phone

7) 
编译完成后重新生成img文件。 
$ make snod

8) 现在可以启动Android模拟器来看效果了。 
首先设置环境变量： 
$ export ANDROID_PRODUCT_OUT= ./out/target/product/generic 
然后切换到 
$ cd ./out/host/linux-x86/bin 
运行 
$ ./emulator

这样我们启动的模拟器里面用的image就是我们刚才编译好的自己定制的东西了。 
从模拟器上可以看到启动的Launcher是我们自己的Launcher，不会出现默认的Launcher了，也不会出现选择界面。

9)我们再验证一下，如果用户装上了一个其他的Launcher(Home)会怎么样。 
  从网上找一个一般的Launcher或者自己写一个一般的Launcher装上去，重新启动，不会出现选择界面。 
  按HOME键也不会出来两个HOME来选择。

这样我们就牢牢控制了用户的桌面。 
只有我们自己定制的HOME才能装上。 这对于定制Android设备的厂商很有用处.
--------------------- 
作者：androiddevwjm 
来源：CSDN 
原文：https://blog.csdn.net/androiddevwjm/article/details/7764740 
版权声明：本文为博主原创文章，转载请附上博文链接！