# JNI从ByteBuffer获取数据，返回byte[]

实现类似Java的Bytebuffer.get(byte[] data);的功能

```c++
JNIEXPORT jbyteArray JNICALL BufferToByte(JNIEnv* env, jobject obj, jobject buffer)
{
    jbyte* pData    = (jbyte*) env->GetDirectBufferAddress(buffer); //获取buffer数据首地址
    jlong dwCapacity  = env->GetDirectBufferCapacity(buffer);         //获取buffer的容量
    if(!pData)
    {
        LOGE("GetDirectBufferAddress() return null");
        return NULL;
    }
    jbyteArray data = env->NewByteArray(dwCapacity);                  //创建与buffer容量一样的byte[]
    env->SetByteArrayRegion(data, 0, dwCapacity, pData);              //数据拷贝到data中
    return data;
}
```



Java对应的Native方法

```java
public native byte[] bufferToByte(ByteBuffer buffer);
```

