package com.myntai.slightech.myntairobotromupdateservice.common;

import android.os.Handler;
import android.os.Message;
import java.lang.ref.WeakReference;

public class MessageHandler extends Handler {

    private final WeakReference<Handler.Callback> reference;

    public MessageHandler(Handler.Callback callback) {
        reference = new WeakReference<>(callback);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        Callback instance = reference.get();
        if (instance != null) {
            instance.handleMessage(msg);
        }
    }
}
