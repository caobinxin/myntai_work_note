# (1)：CameraService启动流程

## 1. 前言

本文将分析android系统源码，从frameworks层到hal层，暂不涉及app层和kernel层。由于某些函数比较复杂，在贴出代码时会适当对其进行简化。本文属于自己对源码的总结，仅仅是贯穿代码流程，不会深入分析各个细节。


分析android系统源码，需要对android系统的某些知识点有所了解

涉及的知识点有：

（1）Android系统的智能指针 - 参考老罗的Android系统的智能指针（轻量级指针、强指针和弱指针）的实现原理分析

（2）Android进程间通信Binder - 参考老罗的Android进程间通信（IPC）机制Binder简要介绍和学习计划

（3）Android硬件抽象层(HAL) - 参考老罗的Android硬件抽象层（HAL）概要介绍和学习计划

## 2. frameworks层

Android的各个子模块的启动都是从它们的Service的启动开始的，所以我们将从CameraService的启动开始分析。CameraService的启动就在MediaServer的main函数中，代码路径在：frameworks/av/media/mediaserver/main_mediaserver.cpp

```c
int main(int argc __unused, char** argv)
{
    ......
    CameraService::instantiate();
    ......
}
```

CameraService类定义如下：

```c
class CameraService :
    public BinderService<CameraService>,
    public BnCameraService,
    public IBinder::DeathRecipient,
    public camera_module_callbacks_t
{
    static char const* getServiceName() { return "media.camera"; }
    ......
}
```

mediaserver的main函数中调用了CameraService的instantiate函数来创建实例，该函数的实现在其父类BinderService中实现

```c
template<typename SERVICE>
class BinderService
{
    static status_t publish(bool allowIsolated = false) {
        sp<IServiceManager> sm(defaultServiceManager());
        return sm->addService(
                String16(SERVICE::getServiceName()),
                new SERVICE(), allowIsolated);
    }   
 
    static void instantiate() { publish(); }
 
}
```

1. instantiate函数只是简单的调用了publish函数

2. publish函数先构造CameraService，再通过addService函数将它注册到ServiceManager当中，而getServiceName函数获取到的值为“media camera”。这一切都是为了binder通信做准备

3. 这里使用了c++模版，从上面的CameraService类定义中可以看出，这里的SERVICE等于CameraService，也就是说publish函数中的new SERVICE等于new CameraService

4. 同时还使用了智能指针，也就是说除了调用CameraService的构造函数外，还会调用onFirstRef函数

    ```c++
    CameraService::CameraService()
        :mSoundRef(0), mModule(0)
    {
        ALOGI("CameraService started (pid=%d)", getpid());
        gCameraService = this;
    
    
    	for (size_t i = 0; i < MAX_CAMERAS; ++i) {
        	mStatusList[i] = ICameraServiceListener::STATUS_PRESENT;
    	}
     
    	this->camera_device_status_change = android::camera_device_status_change;
    
    }
    
    void CameraService::onFirstRef()
    {
        LOG1("CameraService::onFirstRef");
    
    	BnCameraService::onFirstRef();
     
    	if (hw_get_module(CAMERA_HARDWARE_MODULE_ID,
                (const hw_module_t **)&mModule) < 0) { 
        	ALOGE("Could not load camera HAL module");
        	mNumberOfCameras = 0; 
    	}else {
        	ALOGI("Loaded \"%s\" camera module", mModule->common.name);
        	mNumberOfCameras = mModule->get_number_of_cameras();
        	if (mNumberOfCameras > MAX_CAMERAS) {
            	ALOGE("Number of cameras(%d) > MAX_CAMERAS(%d).",
                    mNumberOfCameras, MAX_CAMERAS);
            	mNumberOfCameras = MAX_CAMERAS;
        	}
        	for (int i = 0; i < mNumberOfCameras; i++) {
            	LOG1("setCameraFree(%d)", i);
            	setCameraFree(i);
        	}    
     
        	if (mModule->common.module_api_version >=
                CAMERA_MODULE_API_VERSION_2_1) {
            	mModule->set_callbacks(this);
        	}    
     
        	VendorTagDescriptor::clearGlobalVendorTagDescriptor();
     
        	if (mModule->common.module_api_version >= CAMERA_MODULE_API_VERSION_2_2) {
            	setUpVendorTags();
        	}    
     
        	CameraDeviceFactory::registerService(this);
    	}    
    
    }
    ```
    
    

第20行. 通过hw_get_module函数加载了一个hw_module_t模块，这个模块是与hal层对接的接口，ID为CAMERA_HARDWARE_MODULE_ID，并将它保存在mModule成员变量中。

第27行. 通过mModule->get_number_of_cameras函数进入到hal层，获取到了camera的个数。这个函数很重要，对于frameworks层来说只是拿到了camera的个数，但对于hal层和drivers层来说Camera的上电和初始化流程都是从这里开始的
3. hal层-基于MTK平台

先来看看mtk camera module的定义，代码路径在：vendor/mediatek/proprietary/hardware/mtkcam/module_hal/module/module.h

```c
static
camera_module
get_camera_module()
{
    camera_module module = { 
        common:{
             tag                    : HARDWARE_MODULE_TAG,
             #if (PLATFORM_SDK_VERSION >= 21)
             module_api_version     : CAMERA_MODULE_API_VERSION_2_3,
             #else
             module_api_version     : CAMERA_DEVICE_API_VERSION_1_0,
             #endif
             hal_api_version        : HARDWARE_HAL_API_VERSION,
             id                     : CAMERA_HARDWARE_MODULE_ID,
             name                   : "MediaTek Camera Module",
             author                 : "MediaTek",
             methods                : get_module_methods(),
             dso                    : NULL,
             reserved               : {0},
        },  
        get_number_of_cameras       : get_number_of_cameras,
        get_camera_info             : get_camera_info,
        set_callbacks               : set_callbacks,
        get_vendor_tag_ops          : get_vendor_tag_ops,
        #if (PLATFORM_SDK_VERSION >= 21)
        open_legacy                 : open_legacy,
        #endif
        reserved                    : {0},
    };  
    return  module;
};
```

1. 保存在frameworks层CameraService的成员变量mModule里面的就是上面这个module结构体

2. 当frameworks层调用mModule->get_number_of_cameras函数时，实际就是调用上面结构体的get_number_of_cameras函数

    ```c++
    CamDeviceManagerImp gCamDeviceManager;
    
    ICamDeviceManager*
    getCamDeviceManager()
    {
        return &gCamDeviceManager;
    }
    
    static
    int
    get_number_of_cameras(void)
    {
        return  NSCam::getCamDeviceManager()->getNumberOfDevices();
    }
    ```

    

1. 这里先通过getCamDeviceManager函数获取了CamDeviceManagerImp对象

2. CamDeviceManagerImp继承了CamDeviceManagerBase，这里的getNumberOfDevices方法将由父类CamDeviceManagerBase实现

    ```c++
    int32_t
    CamDeviceManagerBase::
    getNumberOfDevices()
    {
        mi4DeviceNum = enumDeviceLocked();
        return  mi4DeviceNum;
    }
    ```
    
    

这里只是调用了enumDeviceLocked函数，并将它的返回值（代表了camera的个数）返回到frameworks层。接着看enumDeviceLocked的实现

```c++
int32_t
CamDeviceManagerImp::
enumDeviceLocked()
{
    IHalSensorList*const pHalSensorList = IHalSensorList::get();
    size_t const sensorNum = pHalSensorList->searchSensors();
 
    for (size_t i = 0; i < sensorNum; i++)
    {
        int32_t const deviceId = i;
 
        sp<EnumInfo> pInfo = new EnumInfo;
        mEnumMap.add(deviceId, pInfo);
 
        IMetadataProvider> pMetadataProvider = IMetadataProvider::create(deviceId);
        pInfo->pMetadata            = pMetadataProvider->getStaticCharacteristics();
        pInfo->iFacing              = (pMetadataProvider->getDeviceFacing() == MTK_LENS_FACING_FRONT)
                                        ? CAMERA_FACING_FRONT
                                        : CAMERA_FACING_BACK
                                        ;
        pInfo->iWantedOrientation   = pMetadataProvider->getDeviceWantedOrientation();
        pInfo->iSetupOrientation    = pMetadataProvider->getDeviceSetupOrientation();
        i4DeviceNum++;
    }
 
    return i4DeviceNum;
}
```

第5-6行. 这里需要重点关注pHalSensorList->searchSensors函数，它的返回值就是camera的个数
第8-24行. 循环构造并初始化一个EnumInfo对象，并把它保存在mEnumMap中

```c++
MUINT
HalSensorList::
enumerateSensor_Locked()
{   
    int ret_count = 0;
    SensorDrv *const pSensorDrv = SensorDrv::get();
    int const iSensorsList = pSensorDrv->impSearchSensor(NULL);
 
    if((iSensorsList & SENSOR_DEV_MAIN) == SENSOR_DEV_MAIN)
    {
        halSensorDev = SENSOR_DEV_MAIN;
        pSensorInfo = pSensorDrv->getMainSensorInfo();
        addAndInitSensorEnumInfo_Locked(halSensorDev, ret_count, mapToSensorType(pSensorInfo->GetType()), pSensorInfo->getDrvMacroName());
        ret_count++;
    }
 
    if((iSensorsList & SENSOR_DEV_SUB) == SENSOR_DEV_SUB)
    {
        halSensorDev = SENSOR_DEV_SUB;
        pSensorInfo = pSensorDrv->getSubSensorInfo();
        addAndInitSensorEnumInfo_Locked(halSensorDev, ret_count, mapToSensorType(pSensorInfo->GetType()), pSensorInfo->getDrvMacroName());
        ret_count++;
    }
 
    mEnumSensorCount = ret_count;
    return  ret_count;
}
 
MUINT
HalSensorList::
searchSensors()
{   
    return  enumerateSensor_Locked();
}   
```

第33行. searchSensors函数只是调用了enumerateSensor_Locked函数，这里并没有贴出enumerateSensor_Locked函数的所有代码，删减了一些我们暂时不关注的东西

第7行. 重点函数pSensorDrv->impSearchSensor，它的返回值决定了enumerateSensor_Locked的返回值，也就是camera的个数

```c++
MINT32
ImgSensorDrv::impSearchSensor(pfExIdChk pExIdChkCbf)
{
    MUINT32 SensorEnum = (MUINT32) DUAL_CAMERA_MAIN_SENSOR;
    MUINT32 i,id[KDIMGSENSOR_MAX_INVOKE_DRIVERS] = {0,0};
    MINT32 sensorDevs = SENSOR_NONE;
 
    GetSensorInitFuncList(&m_pstSensorInitFunc);
    m_fdSensor = ::open("/dev/kd_camera_hw", O_RDWR);
 
    for (SensorEnum = DUAL_CAMERA_MAIN_SENSOR; SensorEnum <= DUAL_CAMERA_SUB_SENSOR; SensorEnum <<= 1) {
        for (i = 0; i < MAX_NUM_OF_SUPPORT_SENSOR; i++) {
            //end of driver list
            if (m_pstSensorInitFunc[i].getCameraDefault == NULL) {
                LOG_MSG("m_pstSensorInitFunc[i].getCameraDefault is NULL: %d \n", i);
                break;
            }
 
            id[KDIMGSENSOR_INVOKE_DRIVER_0] = (SensorEnum << KDIMGSENSOR_DUAL_SHIFT) | i;
            err = ioctl(m_fdSensor, KDIMGSENSORIOC_X_SET_DRIVER,&id[KDIMGSENSOR_INVOKE_DRIVER_0] );
            err = ioctl(m_fdSensor, KDIMGSENSORIOC_T_CHECK_IS_ALIVE);
 
            if (err < 0 || err2 < 0) {
                LOG_MSG("sensor ID mismatch\n");
                continue;
            }
 
            if (SensorEnum == DUAL_CAMERA_MAIN_SENSOR) {
                m_mainSensorDrv.index[m_mainSensorDrv.number] = i;
                m_mainSensorDrv.type[m_mainSensorDrv.number] = sensorType;
                m_mainSensorDrv.position = socketPos;
                m_mainSensorDrv.sensorID = m_pstSensorInitFunc[m_mainSensorDrv.index[m_mainSensorDrv.number]].SensorId;
                m_mainSensorDrv.number++;
            } else if (SensorEnum == DUAL_CAMERA_SUB_SENSOR) {
                m_subSensorDrv.index[m_subSensorDrv.number] = i;
                m_subSensorDrv.type[m_subSensorDrv.number] = sensorType;
                m_subSensorDrv.position = socketPos;
                m_subSensorDrv.sensorID = m_pstSensorInitFunc[m_subSensorDrv.index[m_subSensorDrv.number]].SensorId;
                m_subSensorDrv.number++;
            }
        }
    }
 
    if (BAD_SENSOR_INDEX != m_mainSensorDrv.index[0]) {
        m_mainSensorId = m_mainSensorDrv.sensorID;
        m_mainSensorIdx = m_mainSensorDrv.index[0];
        sensorDevs |= SENSOR_MAIN;
    }
    if (BAD_SENSOR_INDEX != m_subSensorDrv.index[0]) {
        m_subSensorId = m_subSensorDrv.sensorID;
        m_subSensorIdx = m_subSensorDrv.index[0];
        sensorDevs |= SENSOR_SUB;
    }
 
    return sensorDevs;
}
```

这个函数比较长，所以只贴出关键代码

第8行, 调用GetSensorInitFuncList函数来获取hal层的sersors列表，并把它保存在m_pstSensorInitFunc变量中

第9行, 通过系统调用open函数打开camera的设备节点，后面会通过这个节点来进入到kernel层

第11-12行, 通过两个for循环来遍历sensorlist中所有可能存在的camera

第20行, 通过ioctl下达setDriver指令，并下传正在遍历的sensorlist中的ID。Driver层根据这个ID，挂载Driver层sensorlist中对应的操作接口

第21行, 通过ioctl下达check ID指令，Driver层为对应sensor上电，通过I2C读取预存在寄存器中的sensor id。然后比较读取结果，如果不匹配return error后继续遍历

第29-41行, 将sensor相关的信息保存在m_mainSensorDrv和m_subSensorDrv中

第45-56行, 给sensroDevs变量赋值，并将它返回给上一级

这里暂不分析kernel层的代码，先来看看GetSensorInitFuncList函数，代码在sensorlist.cpp中

```c++
MSDK_SENSOR_INIT_FUNCTION_STRUCT SensorList[] =
{
#if defined(IMX175_MIPI_RAW)
    RAW_INFO(IMX175_SENSOR_ID, SENSOR_DRVNAME_IMX175_MIPI_RAW,NULL),
#endif
#if defined(IMX179_MIPI_RAW)
    RAW_INFO(IMX179_SENSOR_ID, SENSOR_DRVNAME_IMX179_MIPI_RAW,NULL),
#endif
#if defined(IMX219_MIPI_RAW)
    RAW_INFO(IMX219_SENSOR_ID, SENSOR_DRVNAME_IMX219_MIPI_RAW, NULL),
#endif
#if defined(IMX214_MIPI_RAW)
    RAW_INFO(IMX214_SENSOR_ID, SENSOR_DRVNAME_IMX214_MIPI_RAW,NULL),
#endif
#if defined(GC2235_RAW)
    RAW_INFO(GC2235_SENSOR_ID, SENSOR_DRVNAME_GC2235_RAW, NULL),
#endif
#if defined(GC2035_YUV)
    YUV_INFO(GC2035_SENSOR_ID, SENSOR_DRVNAME_GC2035_YUV, NULL),
#endif
    ......
}
 
UINT32 GetSensorInitFuncList(MSDK_SENSOR_INIT_FUNCTION_STRUCT **ppSensorList)
{       
    *ppSensorList = &SensorList[0];
    return MHAL_NO_ERROR;
}
```

hal层的sensorList，再熟悉不过的代码，需要注意的是hal层sensorList和kernel层的sensorList顺序必须保持一致

4. 总结
至此，除kernel层外，简述了CameraService的启动流程，大概过程如下图所示


