# OTA升级界面manifests

```java
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.myntai.slightech.myntairobotromupdateservice">

    <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"/>
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
    <uses-permission android:name="android.permission.SYSTEM_OVERLAY_WINDOW" />

    <application
        android:name="com.myntai.slightech.myntairobotromupdateservice.MyApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.NoDisplay">

        <activity android:name=".MainActivity"
            android:excludeFromRecents="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <!--<category android:name="android.intent.category.LAUNCHER" />-->
            </intent-filter>
        </activity>
        <receiver android:name="BootReceiver">
            <intent-filter>
                <action android:name="android.intent.action.bootOta">
                </action>
            </intent-filter>
        </receiver>
        <receiver android:name="OtaManagementReceiver">
            <intent-filter>
                <action android:name="com.myntai.slightech.myntairobotromupdateservice.OtaManagementReceiver">
                </action>
            </intent-filter>
        </receiver>
        <service android:name=".OkDeleteDialogService">
            <intent-filter >
                <action android:name="com.myntai.slightech.myntairobotromupdateservice.dialogService"/>
            </intent-filter>
        </service>
    </application>

</manifest>
```

