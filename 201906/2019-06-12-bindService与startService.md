# bindService与startService

资料：https://blog.csdn.net/hhitom/article/details/50659920

1. bindService既可以启动一个跨进程的service，也可以启动一个本进程内的service。

    如果没有用aidl接口，则bindService不能bind另一个进程的service

    如果使用了aidl接口，则在bindService()时，若service还没有启动，则系统会先启动service运行在的新进程。

    当然，如果没用aidl，bindservice可以去bind一个本进程内的service，此时，这个service运行在与调用bindService()

    的客户端所在的同一个进程中，而通过serviceConnection返回的IBinder也是那个Stub,而不是BindProxy。

　一般的调用方式为：

```java
Intent intent = new Intent();
mContext = this;
intent.setClass(mContext, AccessoryOTAService.class);
bindService(intent, conn, BIND_AUTO_CREATE);
```



　在frameworks/base/core/java/android/bluetooth/BluetoothA2dp.java中有个较典型的调用示例：

```java
synchronized (mConnection) {
    try {
        if (mService == null) {
            if (VDBG) Log.d(TAG,"Binding service...");
            doBind();
        }
    } catch (Exception re) {
        Log.e(TAG,"",re);
    }
}

boolean doBind() {
    Intent intent = new Intent(IBluetoothA2dp.class.getName());
    ComponentName comp = intent.resolveSystemService(mContext.getPackageManager(), 0);
    intent.setComponent(comp);
    if (comp == null || !mContext.bindServiceAsUser(intent, mConnection, 0,
            android.os.Process.myUserHandle())) {
        Log.e(TAG, "Could not bind to Bluetooth A2DP Service with " + intent);
        return false;
    }
    return true;
}

synchronized (mConnection) {
    if (mService != null) {
        try {
            mService = null;
            mContext.unbindService(mConnection);
        } catch (Exception re) {
            Log.e(TAG,"",re);
        }
    }
}

private final ServiceConnection mConnection = new ServiceConnection() {
    public void onServiceConnected(ComponentName className, IBinder service) {
        if (DBG) Log.d(TAG, "Proxy object connected");
        mService = IBluetoothA2dp.Stub.asInterface(service);
        if (mServiceListener != null) {
        mServiceListener.onServiceConnected(BluetoothProfile.A2DP, BluetoothA2dp.this);
    }
}
public void onServiceDisconnected(ComponentName className) {
    if (DBG) Log.d(TAG, "Proxy object disconnected");
    mService = null;
    if (mServiceListener != null) {
        mServiceListener.onServiceDisconnected(BluetoothProfile.A2DP);
    }
}
    };    
```



2. startService也可以启动一个跨进程的service，不同的是，startService()启动的service与调用方的生命绑定在一起，

    即，如果调用方退出了，service也就退出了，bindService不是这样的，即使调用方退出了，service还可以存在。

