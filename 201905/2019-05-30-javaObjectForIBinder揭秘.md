# javaObjectForIBinder

## 1. 应用

```shell
haier/frameworks$ grep -inR "javaobjectfor"

base/media/jni/android_media_MediaHTTPConnection.cpp:131

base/core/jni/android_view_SurfaceControl.cpp:348

base/core/jni/android_view_SurfaceControl.cpp:356

base/core/jni/android_view_SurfaceControl.cpp:666

base/core/jni/android_util_Binder.cpp:547

base/core/jni/android_util_Binder.cpp:581

base/core/jni/android_util_Binder.cpp:592

base/core/jni/android_util_Binder.cpp:905

base/core/jni/android_util_Binder.cpp:1247

base/core/jni/android_util_Binder.cpp:1248

base/core/jni/android_os_Parcel.cpp:433

base/core/jni/android_util_Binder.h:28
```

从检索中，我们发现在这些地方调用了这个函数：

暂时揭秘：该函数是在c 中调用，来给java层创建一个BinderProxy对象的， 该对象是c层中 BpBinder在java层的分身。其中，BpBinder  和 Bnbinder是跨进程的通信。也就是说，java层可以拿着BinderProxy直接和Bnbinder建立通信（这是表明，实质，都是通过c层的Bpbinder和Bnbinder进行通信的）现在就来揭秘；**有一个不争的事实要铭记，c 和 java之间是有层级的，好比 七层网络模型，对应层之间通信。**

## 2. 案例分析

现在拿base/media/jni/android_media_MediaHTTPConnection.cpp:131 中的调用进行分析：

```c++
static jobject android_media_MediaHTTPConnection_native_getIMemory(
        JNIEnv *env, jobject thiz) {
    sp<JMediaHTTPConnection> conn = getObject(env, thiz);

    return javaObjectForIBinder(env, IInterface::asBinder(conn->getIMemory()));// 
}
```

**Q 1.** android_media_MediaHTTPConnection_native_getIMemory 在拿调用的?

jni 层的注册：

```c
static const JNINativeMethod gMethods[] = { 
    { "native_getIMemory", "()Landroid/os/IBinder;",
      (void *)android_media_MediaHTTPConnection_native_getIMemory },

    ...
};

int register_android_media_MediaHTTPConnection(JNIEnv *env) {
    return AndroidRuntime::registerNativeMethods(env,
                "android/media/MediaHTTPConnection", gMethods, NELEM(gMethods));
}
```

在java层的调用函数是 native_getIMemory

**Q 2.** native_getIMemory 在那被调用的？

base/media/java/android/media/MediaHTTPConnection.java:86

```java
public class MediaHTTPConnection extends IMediaHTTPConnection.Stub {
    
    @Override // 这是个被复写的函数，说明在  IMediaHTTPConnection.Stub 接口中           
    public IBinder connect(String uri, String headers) {
        return native_getIMemory();// 这个地方调用了
    }
}
```

这里仅仅是一个 返回，还没有真的就找到，使用的地方，继续查找：

首先这里的 connect 是 Override , 并且  class MediaHTTPConnection extends IMediaHTTPConnection.Stub 说明有两点：

1. connect是Ibinder进程通信的一个 方法接口
2. 这个接口是 由aidl自动生成的。

find -name IMediaHTTPConnection.aidl

```java
package android.media;

import android.os.IBinder;

/** MUST STAY IN SYNC WITH NATIVE CODE at libmedia/IMediaHTTPConnection.{cpp,h} */

/** @hide */
interface IMediaHTTPConnection
{
    IBinder connect(in String uri, in String headers);// 这个接口
    void disconnect();

    int readAt(long offset, int size);
    long getSize();
    String getMIMEType();
    String getUri();
}
```



现在在来看aidl自动生成的类

out$ gedit ./target/common/obj/JAVA_LIBRARIES/framework_intermediates/src/media/java/android/media/IMediaHTTPConnection.java

```java
/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/media/java/android/media/IMediaHTTPConnection.aidl
 */
package android.media;

public interface IMediaHTTPConnection extends android.os.IInterface
{
	
	public static abstract class Stub extends android.os.Binder implements android.media.IMediaHTTPConnection
	{
		private static final java.lang.String DESCRIPTOR = "android.media.IMediaHTTPConnection";
		
		public Stub()
		{
			this.attachInterface(this, DESCRIPTOR);
		}
	
		public static android.media.IMediaHTTPConnection asInterface(android.os.IBinder obj){}
		@Override public android.os.IBinder asBinder(){}
		@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
		{
			switch (code)
			{
				case INTERFACE_TRANSACTION:
					{}
				case TRANSACTION_connect:// 服务端会对应调用这个
					{
					// 从而可以调用到，上面分析到的connect 从而调用到  native_getIMemory 从而创建一个 java层的BinderProxy 对象返回给 client
						android.os.IBinder _result = this.connect(_arg0, _arg1);
						return true;
					}
				case TRANSACTION_disconnect:
					{
						this.disconnect();
						return true;
					}
				case TRANSACTION_readAt:
					{
						int _result = this.readAt(_arg0, _arg1);
						return true;
					}
				case TRANSACTION_getSize:
					{
						long _result = this.getSize();
						return true;
					}
				case TRANSACTION_getMIMEType:
					{
						java.lang.String _result = this.getMIMEType();
						return true;
					}
				case TRANSACTION_getUri:
					{
						java.lang.String _result = this.getUri();
						return true;
					}
			}
			return super.onTransact(code, data, reply, flags);
		}
		private static class Proxy implements android.media.IMediaHTTPConnection
		{
			private android.os.IBinder mRemote;
			Proxy(android.os.IBinder remote){}
			@Override public android.os.IBinder asBinder(){}
			public java.lang.String getInterfaceDescriptor(){}
			
			// client会调用这个
			@Override public android.os.IBinder connect(java.lang.String uri, java.lang.String headers) throws android.os.RemoteException
			{
					mRemote.transact(Stub.TRANSACTION_connect, _data, _reply, 0);
					_reply.readException();
					
					_reply.readException();
					_result = _reply.readStrongBinder();// 将BinderProxy返回给调用者
				return _result;
			}
			@Override public void disconnect() throws android.os.RemoteException
			{
					mRemote.transact(Stub.TRANSACTION_disconnect, _data, _reply, 0);
			}
			@Override public int readAt(long offset, int size) throws android.os.RemoteException
			{
					mRemote.transact(Stub.TRANSACTION_readAt, _data, _reply, 0);
					_reply.readException();
				return _result;
			}
			@Override public long getSize() throws android.os.RemoteException
			{
					mRemote.transact(Stub.TRANSACTION_getSize, _data, _reply, 0);
				return _result;
			}
			@Override public java.lang.String getMIMEType() throws android.os.RemoteException
			{
					mRemote.transact(Stub.TRANSACTION_getMIMEType, _data, _reply, 0);
				return _result;
			}
			@Override public java.lang.String getUri() throws android.os.RemoteException
			{
					mRemote.transact(Stub.TRANSACTION_getUri, _data, _reply, 0);
					_reply.readException();
				return _result;
			}
		}
		static final int TRANSACTION_connect = 0;
		static final int TRANSACTION_disconnect = 1;
		static final int TRANSACTION_readAt = 2;
		static final int TRANSACTION_getSize = 3;
		static final int TRANSACTION_getMIMEType = 4;
		static final int TRANSACTION_getUri = 5;
	}
	
	public android.os.IBinder connect(java.lang.String uri, java.lang.String headers) throws android.os.RemoteException;
	public void disconnect() throws android.os.RemoteException;
	public int readAt(long offset, int size) throws android.os.RemoteException;
	public long getSize() throws android.os.RemoteException;
	public java.lang.String getMIMEType() throws android.os.RemoteException;
	public java.lang.String getUri() throws android.os.RemoteException;
}
```

这里要想找到 BinderProxy被谁接收了，按照当前的分析可知道：

java proxy. 调用了connect -> 跨进程 -> java stub -> connect -> native_connect -> c++ 层 -> 创建java层的对象BinderProxy  然后层层返回给（跨进程） -> java proxy的connect函数。

要想找到 java proxy 端谁保存了 BinderProxy，还得继续查找谁调用了  proxy的connect()

**完了完了，分析不下去了，然而并没有找到 proxy调用 connect 的地方**

**这里暂时不分析了**



base/media/java/android/media/MediaHTTPService.java:29

```java
public class MediaHTTPService extends IMediaHTTPService.Stub {
    private static final String TAG = "MediaHTTPService";

    public MediaHTTPService() {
    }   

    public IMediaHTTPConnection makeHTTPConnection() {// 在java中没有找到使用的地方，那一定是在cpp中使用了，c++ 调用java的对象和方法  // 在 Q 4 中对这个问题进行阐述
        return new MediaHTTPConnection();  // 这在使用                                                                         
    }   

    // 在 Q 3.中分析这个
    /* package private */static IBinder createHttpServiceBinderIfNecessary(
            String path) {
        if (path.startsWith("http://")
                || path.startsWith("https://")
                || path.startsWith("widevine://")) {
            return (new MediaHTTPService()).asBinder();//发现这个地方返回了 Ibinder的接口，此时就可以通讯了呢。，查找 在那调用了 createHttpServiceBinderIfNecessary
        }

        return null;
    }   
}
```

这里还是没有找到使用的地方，还需要继续查找：（越来越有意思了啊，居然有两个Stub 出现 ）

对应的aidl

frameworks/base/media/java/android/media/IMediaHTTPService.aidl:26

```java
package android.media;

import android.media.IMediaHTTPConnection;

/** MUST STAY IN SYNC WITH NATIVE CODE at libmedia/IMediaHTTPService.{cpp,h} */

/** @hide */
interface IMediaHTTPService
{
    IMediaHTTPConnection makeHTTPConnection();                   
}
```



在 out 目录下 target/common/obj/JAVA_LIBRARIES/framework_intermediates/src/media/java/android/media/IMediaHTTPService.java:49

对应的aidl文件

```java
/* 这个是aidl自动生成的
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/media/java/android/media/IMediaHTTPService.aidl
 */
package android.media;
/** MUST STAY IN SYNC WITH NATIVE CODE at libmedia/IMediaHTTPService.{cpp,h} *//** @hide */
public interface IMediaHTTPService extends android.os.IInterface
{
	/** Local-side IPC implementation stub class. */
	public static abstract class Stub extends android.os.Binder implements android.media.IMediaHTTPService
	{
		private static final java.lang.String DESCRIPTOR = "android.media.IMediaHTTPService";
		/** Construct the stub at attach it to the interface. */
		public Stub()
		{
			this.attachInterface(this, DESCRIPTOR);
		}
		/**
		 * Cast an IBinder object into an android.media.IMediaHTTPService interface,
		 * generating a proxy if needed.
		 */
		public static android.media.IMediaHTTPService asInterface(android.os.IBinder obj)
		{
			if ((obj==null)) {
				return null;
			}
			android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
			if (((iin!=null)&&(iin instanceof android.media.IMediaHTTPService))) {
				return ((android.media.IMediaHTTPService)iin);
			}
			return new android.media.IMediaHTTPService.Stub.Proxy(obj);
		}
		@Override public android.os.IBinder asBinder()
		{
			return this;
		}
		@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
		{
			switch (code)
			{
				case INTERFACE_TRANSACTION:
					{
						reply.writeString(DESCRIPTOR);
						return true;
					}
				case TRANSACTION_makeHTTPConnection:
					{
						data.enforceInterface(DESCRIPTOR);
						android.media.IMediaHTTPConnection _result = this.makeHTTPConnection();// 这个会调用 我们服务端的代码， 服务端 对这个函数进行了复写
						reply.writeNoException();
						reply.writeStrongBinder((((_result!=null))?(_result.asBinder()):(null)));
						return true;
					}
			}
			return super.onTransact(code, data, reply, flags);
		}
		private static class Proxy implements android.media.IMediaHTTPService
		{
			private android.os.IBinder mRemote;
			Proxy(android.os.IBinder remote)
			{
				mRemote = remote;
			}
			@Override public android.os.IBinder asBinder()
			{
				return mRemote;
			}
			public java.lang.String getInterfaceDescriptor()
			{
				return DESCRIPTOR;
			}
			@Override public android.media.IMediaHTTPConnection makeHTTPConnection() throws android.os.RemoteException
			{
				android.os.Parcel _data = android.os.Parcel.obtain();
				android.os.Parcel _reply = android.os.Parcel.obtain();
				android.media.IMediaHTTPConnection _result;
				try {
					_data.writeInterfaceToken(DESCRIPTOR);
					mRemote.transact(Stub.TRANSACTION_makeHTTPConnection, _data, _reply, 0);
					_reply.readException();
					_result = android.media.IMediaHTTPConnection.Stub.asInterface(_reply.readStrongBinder());
				}
				finally {
					_reply.recycle();
					_data.recycle();
				}
				return _result;
			}
		}
		static final int TRANSACTION_makeHTTPConnection = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
	}
	public android.media.IMediaHTTPConnection makeHTTPConnection() throws android.os.RemoteException;
}
```

从这而来看，此时要涉及到 java层中的跨 进程 的服务了。猜测是属于android app framework下的service 通讯了，看看能不能找到 依据 就是**看调用createHttpServiceBinderIfNecessary 的类有没有继承 Service，如果继承了说明走的是 app框架中的服务  通过 AMS 将 IBinder接口，传递给 Client端的 **,  如果不是这样的话，就不是app framework中的

这里回顾一下: app framework中的server 传递IBinder的过程：（假设走的是app framework）

- client 首先要通过 AMS 去绑定 server,  这里还要注册一个 回调函数，用于AMS绑定成功后，回传IBinder接口
- 此时 AMS找到  server.  此时的Server的构造中，才回去new 一个我们用于通信的 服务，也就是这里的MediaHTTPService, 然后，通过 函数 将Ibiner接口 回传给 clent端
- 然后 client端就可以拿着 Ibinder 直接和 MediaHTTPService 进行通信了。

**这里先找 服务端被实例话的地方，在找客户端调用的地方：**

**Q 3.** 这里先找出 IBinder在那使用的：谁调用的createHttpServiceBinderIfNecessary函数就是谁使用？

base/media/java/android/media/MediaPlayer.java:1079

```java

public class MediaPlayer extends PlayerBase
                         implements SubtitleController.Listener
{ 
private void setDataSource(String path, String[] keys, String[] values)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
            // handle non-file sources
            // 发现是在这里用了，但是可以发现这里将 IBinder接口 传递给了 c 层
            nativeSetDataSource(
                MediaHTTPService.createHttpServiceBinderIfNecessary(path),           
                path,
                keys,
                values);
    }
    
}
```

从IBinder 的获取中，我们发现， MediaPlayer 并没有继承Service类， 将创建的Ibinder接口通过 onBind 接口返回给 跨进程的 Client 进行直接 使用，而是直接给有传递给了  C 层，越来越有意思了啊。

- 从这段分析，可以推断出来，这里并没有使用 app framework 的service 的框架
- android_media_MediaPlayer.cpp

```c
static const JNINativeMethod gMethods[] = {
    {    
        "nativeSetDataSource",
        "(Landroid/os/IBinder;Ljava/lang/String;[Ljava/lang/String;"
        "[Ljava/lang/String;)V",
        (void *)android_media_MediaPlayer_setDataSourceAndHeaders
    },   
}


static void
android_media_MediaPlayer_setDataSourceAndHeaders(
        JNIEnv *env, jobject thiz, jobject httpServiceBinderObj, jstring path,
        jobjectArray keys, jobjectArray values) 
{
    
    sp<IMediaHTTPService> httpService;
    if (httpServiceBinderObj != NULL) {
        sp<IBinder> binder = ibinderForJavaObject(env, httpServiceBinderObj);
        httpService = interface_cast<IMediaHTTPService>(binder);// 暂时认为，我们把 binder 放在 httpService 中了
    }
    
    status_t opStatus =
        mp->setDataSource(
                httpService,// 这个有干嘛了呢
                pathStr,
                headersVector.size() > 0? &headersVector : NULL);// 将这个放在 java层中
}

static sp<MediaPlayer> getMediaPlayer(JNIEnv* env, jobject thiz)                       
{   
    Mutex::Autolock l(sLock);
    MediaPlayer* const p = (MediaPlayer*)env->GetLongField(thiz, fields.context);
    return sp<MediaPlayer>(p);
}
```

这里有暴露出另外一个函数： ibinderForJavaObject 估计应该很重要吧，在最后，分析一下，揭秘



**Q 4.** 现在分析 是谁在c层使用的 makeHTTPConnection 函数？

既然这个makeHTTPConnection是通过 IBinder 才能被调用到，这里暂时回顾一下：

我们现在看的是 stub 也就是服务端， 这个服务端 是用 java来实现的。而将 和这个 stub能够通信的 IBinder接口却发给了  c 层中，也就是说  他的客户端  暂时在 c 层中，这是一种猜测，继续分析；

这里选择一个进行分析：av/media/libstagefright/DataSource.cpp:301

```c
sp<DataSource> DataSource::CreateMediaHTTP(const sp<IMediaHTTPService> &httpService) {
    if (httpService == NULL) {
        return NULL;
    }   

    sp<IMediaHTTPConnection> conn = httpService->makeHTTPConnection();                                                
    if (conn == NULL) {
        return NULL;
    } else {
        return new MediaHTTP(conn);
    }   
}
```

这里有个疑问 httpService是谁传递过来的？

libmediaplayerservice/nuplayer/GenericSource.cpp:393

```c
void NuPlayer::GenericSource::onPrepareAsync() {
	mHttpSource = DataSource::CreateMediaHTTP(mHTTPService); 
}
```

这里的mHttpService是谁赋值的？

```c
status_t NuPlayer::GenericSource::setDataSource(
        const sp<IMediaHTTPService> &httpService,
        const char *url,
        const KeyedVector<String8, String8> *headers) {

    mHTTPService = httpService;   // 这个httpService是从那来的                               
}
```

是谁调用的 setDataSource？

这里那 libmedia/mediaplayer.cpp:159 进行分析

```c
status_t MediaPlayer::setDataSource(
        const sp<IMediaHTTPService> &httpService,
        const char *url, const KeyedVector<String8, String8> *headers)
{
    ALOGV("setDataSource(%s)", url);
    status_t err = BAD_VALUE;
    if (url != NULL) {
        const sp<IMediaPlayerService> service(getMediaPlayerService());
        if (service != 0) {
            sp<IMediaPlayer> player(service->create(this, mAudioSessionId));
            if ((NO_ERROR != doSetRetransmitEndpoint(player)) ||
                (NO_ERROR != player->setDataSource(httpService, url, headers))) {                                     
                player.clear();
            }
            err = attachNewPlayer(player);
        }
    }   
    return err;
}
```

完了这里有点复杂，居然不太容易去找了，那暂时就不找了，先暂时就这样吧



## **4. 绑定Service**

参考资料： https://www.jianshu.com/p/95ec2a23f300

应用组件（客户端）通过调用 **bindService()** 绑定到服务，绑定是异步的，系统随后调用服务的 **onBind()** 方法，该方法返回用于与服务交互的 **IBinder**。要接收 **IBinder**，客户端必须提供一个 **ServiceConnection** 实例用于监控与服务的连接，并将其传递给 **bindService()**。当 Android 系统创建了客户端与服务之间的连接时，会回调**ServiceConnection** 对象的**onServiceConnected()**方法，向客户端传递用来与服务通信的 **IBinder**

多个客户端可同时连接到一个服务。不过，只有在第一个客户端绑定时，系统才会调用服务的 onBind() 方法来检索 IBinder。系统随后无需再次调用 onBind()，便可将同一 IBinder 传递至其他绑定的客户端。当所有客户端都取消了与服务的绑定后，系统会将服务销毁（除非 startService() 也启动了该服务）

另外，只有 Activity、服务和内容提供者可以绑定到服务，无法从广播接收器绑定到服务

可以通过以下三种方法定义IBinder接口：

- 扩展 Binder 类
   如果服务是供本应用专用，并且运行在与客户端相同的进程中，则应通过扩展 Binder 类并从 onBind() 返回它的一个实例来创建接口。客户端收到 Binder 后，可利用它直接访问 Service 中可用的公共方法
- 使用 Messenger
   如需让接口跨不同的进程工作，则可使用 Messenger 为服务创建接口。服务可以这种方式定义对应于不同类型 Message 对象的 Handler。此 Handler 是 Messenger 的基础，后者随后可与客户端分享一个 IBinder，从而让客户端能利用 Message 对象向服务发送命令。此外，客户端还可定义自有 Messenger，以便服务回传消息。这是执行进程间通信 (IPC) 的最简单方法，因为 Messenger 会在单一线程中创建包含所有请求的队列，这样就不必对服务进行线程安全设计
- 使用 AIDL
   AIDL（Android 接口定义语言）执行所有将对象分解成原语的工作，操作系统可以识别这些原语并将它们编组到各进程中，以执行 IPC。 之前采用 Messenger 的方法实际上是以 AIDL 作为其底层结构。 如上所述，Messenger 会在单一线程中创建包含所有客户端请求的队列，以便服务一次接收一个请求。 不过，如果想让服务同时处理多个请求，则可直接使用 AIDL。 在此情况下，服务必须具备多线程处理能力，并采用线程安全式设计。如需直接使用 AIDL，必须创建一个定义编程接口的 .aidl 文件。Android SDK 工具利用该文件生成一个实现接口并处理 IPC 的抽象类，随后可在服务内对其进行扩展

### **4.1、绑定服务的具体步骤：**

#### **4.1.1、扩展 Binder 类**

如果服务仅供本地应用使用，不需要跨进程工作，则可以实现自有 Binder 类，让客户端通过该类直接访问服务中的公共方法。此方法只有在客户端和服务位于同一应用和进程内这一最常见的情况下方才有效
 以下是具体的设置方法：

- 在服务中创建一个可满足下列任一要求的 Binder 实例：
  - 包含客户端可调用的公共方法
  - 返回当前 Service 实例，其中包含客户端可调用的公共方法
  - 或返回由服务承载的其他类的实例，其中包含客户端可调用的公共方法
- 从 onBind() 回调方法返回此 Binder 实例
- 在客户端中，从 onServiceConnected() 回调方法接收 Binder，并使用提供的方法调用绑定服务

#### **4.1.2、实现 ServiceConnection接口**

重写两个回调方法：

- onServiceConnected()
   系统会调用该方法以传递服务的onBind() 方法返回的 IBinder
- onServiceDisconnected()
   Android 系统会在与服务的连接意外中断时，例如当服务崩溃或被终止时调用该方法。当客户端取消绑定时，系统不会调用该方法

#### **4.1.3、调用 bindService()，传递 ServiceConnection 对象**

#### **4.1.4、当系统调用了 onServiceConnected() 的回调方法时，就可以通过IBinder对象操作服务了**

#### **4.1.5、要断开与服务的连接需调用 unbindService()方法。如果应用在客户端仍处于绑定状态时销毁客户端，会导致客户端取消绑定，更好的做法是在客户端与服务交互完成后立即取消绑定客户端，这样可以关闭空闲服务**



示例代码： 这个实例 并不是 跨进程的，是在同一个进程中进行编写的，但是很全面的反映了各个接口的使用

```java
public class MyBindService extends Service {

    private IBinder myBinder;

    private Random mGenerator;

    private final String TAG = "MyBindService";

    public class MyBinder extends Binder { // 这里可以对比 MediaHTTPService extends IMediaHTTPService.Stub
        MyBindService getService() {
            return MyBindService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        myBinder = new MyBinder();// 这里就可以对应 MediaHTTPService
        mGenerator = new Random();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        return myBinder;// 将 IBinder 口返回给 Client
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.e(TAG, "onRebind");
        super.onRebind(intent);
    }

    public int getRandomNumber() {
        return mGenerator.nextInt(100);
    }

}
```

下面的代码是跨进程的client的写法

```java
public class BindServiceActivity extends AppCompatActivity {

    private MyBindService mService;

    private boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_service);
    }

    //这个是那个 注册回调
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //这个函数返回的时候，就会回传 服务端的IBinder函数
            MyBindService.MyBinder binder = (MyBindService.MyBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    
    public void bindService(View view) {
        // 通过AMS去查询服务
        Intent intent = new Intent(this, MyBindService.class);
        // bindService 绑定服务
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void unBindService(View view) {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    public void getData(View view) {
        if (mBound) {
            Toast.makeText(this, "获取到的随机数：" + mService.getRandomNumber(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "服务未绑定", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

}
```



### **4.2、绑定服务的生命周期**

绑定服务的生命周期在同时启动服务的情况下比较特殊，想要终止服务，除了需要取消绑定服务外，还需要服务通过 stopSelf() 自行停止或其他组件调用 stopService()

其中，如果服务已启动并接受绑定，则当系统调用了onUnbind() 方法，想要在客户端下一次绑定到服务时调用 onRebind() 方法的话，则onUnbind() 方法需返回 true。onRebind() 返回空值，但客户端仍可以在其 onServiceConnected() 回调中接收到 IBinder对象



![img](https:////upload-images.jianshu.io/upload_images/2552605-568542042002a6d1?imageMogr2/auto-orient/strip%7CimageView2/2/w/526)

这里写图片描述

### **4.3、绑定时机**

- 如果只需要在 Activity 可见时与服务交互，则应在 onStart() 期间绑定，在 onStop() 期间取消绑定
- 如果希望 Activity 在后台停止运行状态下仍可接收响应，则可在 onCreate() 期间绑定，在 onDestroy() 期间取消绑定。这意味着 Activity 在其整个运行过程中（包括后台运行期间）都需要使用此服务
- 通常情况下，切勿在 Activity 的 onResume() 和 onPause() 期间绑定和取消绑定，因为每一次生命周期转换都会发生这些回调，应该使发生在这些转换期间的处理保持在最低水平。假设有两个Activity需要绑定到同一服务，从Activity  A跳转到Activity  B，这个过程中会依次执行A-onPause，B-onCreate，B-onStart，B-onResume，A-onStop。这样系统会在A-onPause的时候销毁服务，又在B-onResume的时候重建服务。当Activity  B回退到Activity  A时，会依次执行B-onPause，A-onRestart，A-onStart，A-onResume，B-onStop，B-onDestroy。此时，系统会在B-onPause时销毁服务，又在A-onResume时重建服务。这样就造成了多次的销毁与重建，因此需要选定好绑定服务与取消绑定服务的时机

### **4.4、在前台运行Service**

前台服务被认为是用户主动意识到的一种服务，因此在内存不足时，系统也不会考虑将其终止。 前台服务必须在状态栏提供通知，放在“正在进行”标题下方，这意味着除非服务停止或从前台移除，否则不能清除通知

要请求让服务运行于前台，要调用 **startForeground()**方法，两个参数分别是：唯一标识通知的int类型整数和Notification对象

修改MyService当中的play()方法

```
    private void play() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setSmallIcon(R.drawable.bird);
            mBuilder.setContentTitle("这是标题吧~叶应是叶");
            mBuilder.setContentText("http://blog.csdn.net/new_one_object");
            startForeground(1, mBuilder.build());
        }
    }
```

点击播放音乐后，状态栏就出现了一个通知



![img](https:////upload-images.jianshu.io/upload_images/2552605-699e4b1e1c8cfef4?imageMogr2/auto-orient/strip%7CimageView2/2/w/720)

这里写图片描述

当中，提供给 startForeground() 的整型参数不得为 0。要从前台移除服务，需调用 stopForeground()方法，此方法不会停止服务。 但是，如果前台服务被停止，则通知也会被移除

作者：leavesC

链接：https://www.jianshu.com/p/95ec2a23f300

来源：简书

简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。

