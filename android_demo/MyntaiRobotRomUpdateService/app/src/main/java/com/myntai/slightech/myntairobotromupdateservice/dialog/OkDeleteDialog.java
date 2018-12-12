package com.myntai.slightech.myntairobotromupdateservice.dialog;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.myntai.slightech.myntairobotromupdateservice.MyApplication;
import com.myntai.slightech.myntairobotromupdateservice.R;

public class OkDeleteDialog {
    final static String TAG = "OkDeleteDialog";
    Context context = MyApplication.getContext();
    WindowManager windowManager = null;
    View mView = null;
    WindowManager.LayoutParams params = null;
    public  void onshow(){
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        mView = LayoutInflater.from(context).inflate(R.layout.otasystemdialoglayout, null);
        Button button = (Button) mView.findViewById(R.id.button);

        params = new WindowManager.LayoutParams();

// 类型
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

// WindowManager.LayoutParams.TYPE_SYSTEM_ALERT

// 设置flag

        int flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
// 如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
        params.flags = flags;
// 不设置这个弹出框的透明遮罩显示为黑色
        params.format = PixelFormat.TRANSLUCENT;
// FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
// 设置 FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按
// 不设置这个flag的话，home页的划屏会有问题

        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

/*params.width = mView.getWidth();
params.height = mView.getHeight();*/

        params.gravity = Gravity.BOTTOM|Gravity.RIGHT;
        params.x = 20;  //相当于margin设置
        params.y = 20;
        windowManager.addView(mView, params);
    }
}
