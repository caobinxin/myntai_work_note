这里主要分析 Camera2 app 应用程序

# AndroidManifest

分析一个应用程序首先从　AndroidManifest 中找到整个 app的入口

```shell
packages/apps/Camera2$ vim AndroidManifest.xml
```

```xml
<activity
            android:name="com.android.camera.CameraActivity"                               
            android:clearTaskOnLaunch="true"
            android:configChanges="orientation|screenSize|keyboardHidden"
            android:label="@string/app_name"
            android:launchMode="singleTask"
            android:taskAffinity="com.android.camera.CameraActivity"
            android:theme="@style/Theme.Camera"
            android:windowSoftInputMode="stateAlwaysHidden|adjustPan" >
            <intent-filter>
                <action android:name="android.media.action.STILL_IMAGE_CAMERA" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.MAIN" /> <!--　主入口-->
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>

            <meta-data
                android:name="com.android.keyguard.layout"
                android:resource="@layout/keyguard_widget" />
        </activity>
```

从中我们可知道　CameraActivity　是我们启动的第一个　Activity

# CameraActivity

类的继承关系如下：

```java
public class CameraActivity extends QuickActivity
        implements AppController, CameraAgent.CameraOpenCallback,
        ShareActionProvider.OnShareTargetSelectedListener {
            
        }

public abstract class QuickActivity extends Activity {
    
    protected final void onCreate(Bundle bundle) {
        mExecutionStartNanoTime = SystemClock.elapsedRealtimeNanos();
        logLifecycle("onCreate", true);
        mStartupOnCreate = true;
        super.onCreate(bundle);
        mMainHandler = new Handler(getMainLooper());
        onCreateTasks(bundle);
        logLifecycle("onCreate", false);
    }
}
```

继承关系：

```shell
CameraActivity -> QuickActivity -> Activity
```

根据Activity 的生命周期我们可知道，调用关系如下：

```shell

onCreate() [QuickActivity]
	-> onCreateTasks(bundle) [CameraActivity]
```

所以这里很清楚，应该从　onCreateTasks() [CameraActivity]　中开始分析。

　

