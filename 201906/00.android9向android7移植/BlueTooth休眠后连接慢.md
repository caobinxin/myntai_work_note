# BlueTooth休眠后连接慢

## 1. 问题

当 os 唤醒后， BlueTooth 回连比较慢。

## 2. 解决：

方案：

​	当我们收到 唤醒广播后，判断蓝牙是否为打开状态： 如果是，将它关了，重新启动即可。

```shell
hp-4.19/packages/apps/Settings$
```



AndroidManifest.xml

```xml
<receiver android:name="com.android.settings.bluetooth.OsWakeUpBluetoothStateCheckReceiver"
            android:exported="true"
            android:enabled="true">
    
            <intent-filter>
                <action android:name="com.android.powermanagerservice.wake.up"/>
            </intent-filter>
    
</receiver>
```

src/com/android/settings/bluetooth/OsWakeUpBluetoothStateCheckReceiver.java

```java
package com.android.settings.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OsWakeUpBluetoothStateCheckReceiver extends BroadcastReceiver {

    static String TAG = "OsWakeUpBluetoothStateCheckReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: ++++++++++++++++++++++++++"+intent.getAction().toString()+"++++++++");
        check();
        Log.i(TAG, "onReceive: ++++++++++++++\n\n\n");
    }

    void check(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return;
        }

        if (mBluetoothAdapter.isEnabled()) {/*判断蓝牙开关是否打开*/
            mBluetoothAdapter.disable();/*关闭*/
            mBluetoothAdapter.enable();/*打开*/
        }
    }
}
```

## 3. 参考资料：

应用层的编程：

http://www.demodashi.com/demo/12772.html