# android电源键分析

https://blog.csdn.net/michael312917/article/details/78089660

## 1. android 按键事件简介

    在android系统中，每一个App所呈现的UI都是一个Activity，而UI的呈现实际上则是由Window对象来实现的，每个Window对象实际上又是PhoneWindow对象；而每个PhonewWindow对象又是一个PhoneWindowManager（PWM）对象。在将按键处理操作分发到App之前，首选会回调PWM中的dispatchUnhandledKey()方法，该方法主要是用于在执行当前App处理之前的操作，处理一些特殊按键。

   PWM中有两个对按键比较重要的函数(按键预处理，针对特殊按键，如Power)：intercepetKeyBeforeDispatching()和interceptKeyBeforeQueueing()。

   interceptKeyBeforeQueueing()主要是用来处理音量和电源键，该函数中有一个重要的变量ACTION_PASS_TO_USER(变量解释：传递按键事件到用户app进行处理)，该函数的返回值如果是0则表示该按键事件被消费，按键事件不会传递到用户App，返回1则表示允许按键事件传递给用户App中进行处理。

   interceptKeyBeforeDispatching()主要是用来预处理home、menu、search按键事件；该函数的返回值如果是-1则表示的是该按键事件被消费，返回0则代表允许事件的往下传递。这些处理的实现都是在PWM，其调用者是InputDispatchThread，这样处理的好处就是把平台相关的东西都放置在PWM中，而InputDispatchThread则是平台通用的，厂商要定制特殊策略只需要更改PWM，这样就很好的做到了Mechanism和Policy的分离。

```java
hp-4.19/frameworks$ vim base/services/core/java/com/android/server/policy/PhoneWindowManager.java +6518
```

## 2. pwm 函数解析：

```java
static final String TAG = "WindowManager";
static final boolean DEBUG_WAKEUP = true;
static final boolean DEBUG_INPUT = true;

public KeyEvent dispatchUnhandledKey(WindowState win, KeyEvent event, int policyFlags) {
    //下面的逻辑主要是用于判断按键事件是否被消费；因为同一时刻可能有两个按键事件同时触发，如组合键(Power+音量下键实现截屏)，则必须判断该逻辑表明一个按键还未消费
    KeyEvent fallbackEvent = null;
    if ((event.getFlags() & KeyEvent.FLAG_FALLBACK) == 0) { // 如果按键被按下
        final KeyCharacterMap kcm = event.getKeyCharacterMap();// 获得键盘映射----字符映射KeyCharacterMap
        final int keyCode = event.getKeyCode();
        final int metaState = event.getMetaState();
        
        final boolean initialDown = event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0; // 判断是否为第一次按下：
        
        if (fallbackAction != null) {                                                                                                                                                                          
                if (DEBUG_INPUT) {
                    Slog.d(TAG, "Fallback: keyCode=" + fallbackAction.keyCode
                            + " metaState=" + Integer.toHexString(fallbackAction.metaState));
                }    

                final int flags = event.getFlags() | KeyEvent.FLAG_FALLBACK;
                fallbackEvent = KeyEvent.obtain(
                        event.getDownTime(), event.getEventTime(),
                        event.getAction(), fallbackAction.keyCode,
                        event.getRepeatCount(), fallbackAction.metaState,
                        event.getDeviceId(), event.getScanCode(),
                        flags, event.getSource(), null);

            　　　// interceptFallback 这个很关键
                if (!interceptFallback(win, fallbackEvent, policyFlags)) {
                    fallbackEvent.recycle();
                    fallbackEvent = null;
                }

                if (initialDown) {
                    mFallbackActions.put(keyCode, fallbackAction);
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    mFallbackActions.remove(keyCode);
                    fallbackAction.recycle();
                }
            }
    }
}
```

```java
// 截获回退 
private boolean interceptFallback(WindowState win, KeyEvent fallbackEvent, int policyFlags) {
    	// interceptKeyBeforeQueueing()主要是用来处理音量和电源键 返回值如果是0则表示该按键事件被消费
        int actions = interceptKeyBeforeQueueing(fallbackEvent, policyFlags);
        if ((actions & ACTION_PASS_TO_USER) != 0) {
            //  interceptKeyBeforeDispatching()主要是用来预处理home、menu、search按键事件
            long delayMillis = interceptKeyBeforeDispatching(
                    win, fallbackEvent, policyFlags);
            if (delayMillis == 0) {
                return true;
            }
        }
        return false;
    }
```

