# GSensor问题查错：

## 1. log分析

logcat:

```shell
E SensorManager: sensor or listener is null
```

dmesg:

```shell
input: Lid Switch as /devices/LNXSYSTM:00/LNXSYBUS:00/PNP0C0D:00/input/input0
ACPI: Lid Switch [LID0]

lis3lv02d: unknown sensor type 0x0
```

**Lid Switch  代表的是 霍尔开关（霍尔传感器），这是一个很重要的信息。**

lis3lv02d： 从中我们发现我们的传感器用的芯片是这个：

进一步验证：

![](GSensor问题查错.assets/2019-07-09 10-38-07 的屏幕截图.png)

## 2. 解决方方案：

### 2.1 修改init.sh

**背景：**

根据测试，我们发现将android-x86-7.1.2中的init.sh拿到我们的里面后：发现 Gsensor是可以使用的，只不过是显示的屏幕是反方向的。根据这个init.sh 和 我们自己的init.sh 通过  init_hal_sensors 函数中执行路径进行打log分析，将没有的加上之后， 发现我们的Gsensor也是可以使用了，但是屏幕是反向的。

![](GSensor问题查错.assets/2019-07-09 14-03-38 的屏幕截图.png) 

此时的屏幕反向，是因为我们的重力感应没有配置对：

所以这里我们要配置 重力感应：

```shell
set_property hal.sensors.iio.accel.matrix 0,1,0,1,0,0,0,0,-1
```

修改后的结果：

![](GSensor问题查错.assets/2019-07-09 14-06-21 的屏幕截图.png)

其中*pnHPPavilionx360Convertible* 是为了表示 我们hp的机型。这个值是由 

```shell
intel_haier:/sys/class/dmi/id # cat uevent

MODALIAS=dmi:bvnInsyde:bvrF.02:bd01/11/2019:svnHP:pnHPPavilionx360Convertible14-dh0xxx:pvrType1ProductConfigId:rvnHP:rn85C4:rvr33.16:cvnHP:ct31:cvrChassisVersion:
```

这里附上完整的init.sh的 init_hal_sensors函数：

```shell
function init_hal_sensors()
{
	# if we have sensor module for our hardware, use it
	ro_hardware=$(getprop ro.hardware)
	[ -f /system/lib/hw/sensors.${ro_hardware}.so ] && return 0

	local hal_sensors=kbd
	local has_sensors=true
	case "$(cat $DMIPATH/uevent)" in
		*Lucid-MWE*)
			set_property ro.ignore_atkbd 1
			hal_sensors=hdaps
			;;
		*ICONIA*W5*)
			hal_sensors=w500
			;;
		*S10-3t*)
			hal_sensors=s103t
			;;
		*Inagua*)
			#setkeycodes 0x62 29
			#setkeycodes 0x74 56
			set_property ro.ignore_atkbd 1
			set_property hal.sensors.kbd.type 2
			;;
		*TEGA*|*2010:svnIntel:*)
			set_property ro.ignore_atkbd 1
			set_property hal.sensors.kbd.type 1
			io_switch 0x0 0x1
			setkeycodes 0x6d 125
			;;
		*DLI*)
			set_property ro.ignore_atkbd 1
			set_property hal.sensors.kbd.type 1
			setkeycodes 0x64 1
			setkeycodes 0x65 172
			setkeycodes 0x66 120
			setkeycodes 0x67 116
			setkeycodes 0x68 114
			setkeycodes 0x69 115
			setkeycodes 0x6c 114
			setkeycodes 0x6d 115
			;;
		*tx2*)
			setkeycodes 0xb1 138
			setkeycodes 0x8a 152
			set_property hal.sensors.kbd.type 6
			set_property poweroff.doubleclick 0
			set_property qemu.hw.mainkeys 1
			;;
		*MS-N0E1*)
			set_property ro.ignore_atkbd 1
			set_property poweroff.doubleclick 0
			setkeycodes 0xa5 125
			setkeycodes 0xa7 1
			setkeycodes 0xe3 142
			;;
		*Aspire1*25*)
			modprobe lis3lv02d_i2c
			echo -n "enabled" > /sys/class/thermal/thermal_zone0/mode
			;;
		*ThinkPad*Tablet*)
			modprobe hdaps
			hal_sensors=hdaps
			;;
		*i7Stylus*|*S10T*)
			set_property hal.sensors.iio.accel.matrix 1,0,0,0,-1,0,0,0,-1
			[ -z "$(getprop sleep.state)" ] && set_property sleep.state none
			;;
		*ST70416-6*)
			set_property hal.sensors.iio.accel.matrix 0,-1,0,-1,0,0,0,0,-1
			;;
		*ONDATablet*)
			set_property hal.sensors.iio.accel.matrix 0,1,0,1,0,0,0,0,-1
			;;
		*pnHPPavilionx360Convertible*)
			set_property hal.sensors.iio.accel.matrix 0,1,0,1,0,0,0,0,-1
			;;
		*)
			has_sensors=false
			;;
	esac

	# has iio sensor-hub?
	if [ -n "`ls /sys/bus/iio/devices/iio:device* 2> /dev/null`" ]; then
		busybox chown -R 1000.1000 /sys/bus/iio/devices/iio:device*/
		#lsmod | grep -q hid_sensor_accel_3d && hal_sensors=hsb || hal_sensors=iio
		[ -n "`ls /sys/bus/iio/devices/iio:device*/in_accel_x_raw 2> /dev/null`" ] && has_sensors=true
        hal_sensors=iio
	elif lsmod | grep -q lis3lv02d_i2c; then
		hal_sensors=hdaps
	fi

	# TODO close Surface Pro 4 sensor until bugfix
	case "$(cat $DMIPATH/uevent)" in
		*SurfacePro4*)
			hal_sensors=kbd
			;;
		*)
			;;
	esac

	set_property ro.hardware.sensors $hal_sensors
	#[ "$hal_sensors" != "kbd" ] && has_sensors=true
	#set_property config.override_forced_orient $has_sensors
	set_property config.override_forced_orient true
}
# config.override_forced_orient 是否允许翻转屏幕 
```

## 3. 资料：

### 3.1 config.override_forced_orient  属性

config.override_forced_orient  ：

1. ture: 允许屏幕翻转
2. false: 不允许屏幕翻转

frameworks/base/core/res/res/values/config.xml

```xml
<!-- If true, the direction rotation is applied to get to an application's requested
         orientation is reversed.  Normally, the model is that landscape is
         clockwise from portrait; thus on a portrait device an app requesting
         landscape will cause a clockwise rotation, and on a landscape device an
         app requesting portrait will cause a counter-clockwise rotation.  Setting
         true here reverses that logic. -->
    <bool name="config_reverseDefaultRotation">true</bool>
```

系统中的调用处：

```shell
hp-4.19/frameworks$ vim base/services/core/java/com/android/server/policy/PhoneWindowManager.java +2067
```

![](GSensor问题查错.assets/2019-07-09 14-42-43 的屏幕截图.png)

### 3.2 hal.sensors.iio.accel.matrix

发现 屏幕是反的， 配置这个后，正常了， 这个属性是修正 重力传感器的值的。 现在很好奇，我在这里配置的值， 具体是什么含义：

```shell
*pnHPPavilionx360Convertible*)
            set_property hal.sensors.iio.accel.matrix 0,1,0,1,0,0,0,0,-1
            ;;
# 这里这个值配置后，导致hp的屏幕 开机后，是竖屏显示：
```

根据这个问题：现在就要搞明白这几个数字所代表的含义：

```shell
hp-4.19/hardware$ vim libsensors/iio-sensors.cpp +246 # 唯一使用这个属性的地方
```

```cpp
template<> Sensor<ID_ACCELERATION>::Sensor()
{
    static const char *ns0[] = { "in_accel_scale", 0 };// 从这个接口中读的 分辨率
    static const char **ns[] = { ns0, 0 };
    nodes = ns;

    name = "IIO Accelerometer Sensor";
    type = SENSOR_TYPE_ACCELEROMETER;
    maxRange = RANGE_A;
    resolution = RESOLUTION_A;
    power = 0.23f;
    minDelay = 10000;
}


template<> int Sensor<ID_ACCELERATION>::readEvents(sensors_event_t *data, int cnt)
{
    static float scale = read_sysfs_float((*nodes)[0]);
    int ret = SensorBase::readEvents(data, cnt);
    char cm[PROPERTY_VALUE_MAX];
    float m[9];
    int v[3];

    // 拿到我们的属性值
    property_get("hal.sensors.iio.accel.matrix", cm, "-1,0,0,0,1,0,0,0,-1" );                                                                                                            // 根据 格式 ，读出 cm中的值 放到 m 中                          
    sscanf(cm, "%f,%f,%f,%f,%f,%f,%f,%f,%f", &m[0], &m[1], &m[2], &m[3], &m[4], &m[5], &m[6], &m[7], &m[8]);

    for (int i = 0; i < ret; ++i) {
        v[0] = read_sysfs_int("in_accel_x_raw");
        v[1] = read_sysfs_int("in_accel_y_raw");
        v[2] = read_sysfs_int("in_accel_z_raw");
        // create matrix * vector product
        data[i].acceleration.x = scale * (m[0] * v[0] + m[1] * v[1] + m[2] * v[2]);
        data[i].acceleration.y = scale * (m[3] * v[0] + m[4] * v[1] + m[5] * v[2]);
        data[i].acceleration.z = scale * (m[6] * v[0] + m[7] * v[1] + m[8] * v[2]);
        data[i].acceleration.status = SENSOR_STATUS_ACCURACY_HIGH;
    }   
    return ret;
}
```

从对加速度传感器的使用中，我们可以得知，这几个值的含义：

这里拿默认值 -1,0,0,0,1,0,0,0,-1 举例

| -1,0,0     | 0,1,0,    | 0,0,-1    |
| ---------- | --------- | --------- |
| x 轴的方向 | y轴的方向 | z轴的方向 |

从上面的定义中，也可以看到，当前的分辨率是从 in_accel_scale 中获取的。

### 3.3 用户态，直接读 sys文件接口的函数实现

**这里顺便记录几个比较有意义的函数：**

```c
int SensorBase::read_sysfs_int(const char *file)                            
{
    char buf[4096];
    return read_sysfs_str(file, buf) ? 0 : atoi(buf);
}

float SensorBase::read_sysfs_float(const char *file)
{
    char buf[4096];
    return read_sysfs_str(file, buf) ? 0 : atof(buf);
}

int SensorBase::read_sysfs_str(const char *file, char *buf)                        
{
    int res = 0;
    char filename[PATH_MAX];
    snprintf(filename, PATH_MAX, "%s/%s", path, file);
    int fd = open(filename, O_RDONLY);
    if (fd >= 0) {
        ssize_t sz = read(fd, buf, 4096);
        if (sz < 0) {
            ALOGE("failed to read from %s: %s", filename, strerror(errno));
            res = -errno;
        }
        close(fd);
    }
    return res;
}
```



### 3.3 屏幕翻转的资料

这个条款中的内容都是摘抄自如下链接：

http://imxuening.cn/?p=315

网上搜到一份修改4.1和4.2屏幕方向的方法，可惜4.4所有的文件或函数都变了。好在从这位网友的文章里还是能看出大致思路，最终还是顺利找到几个文件，改完编译验证过所有界面都已经旋转180度，并且触摸屏也能够如常使用，没有出现上下颠倒。

步骤如下：

1. **修改屏幕旋转角度**
    **1）修改设置的属性值**
    打开文件：device/softwinner/kylin-cubieboard4/kylin_cubieboard4.mk
    修改属性：ro.sf.hwrotation=180
    **2）设置窗体默认显示方向**
    frameworks/native/services/surfaceflinger/SurfaceFlinger.cpp中找到字段：d.orientation  = DisplayState::eOrientationDefault;，修改为d.orientation =  DisplayState::eOrientation180;
    **3）设置窗体动画旋转方向**
    在frameworks/base/core/java/android/view/Surface.java加入方法
    /** @hide */
    public static int getDefaultRotation() {
    return android.os.SystemProperties.getInt(“ro.sf.hwrotation”,0);
    }
    /** @hide */
    public static int getDefaultRotationIndex() {
    int rotation=getDefaultRotation();
    switch(rotation){
    case 0:
    return ROTATION_0;
    case 90:
    return ROTATION_90;
    case 180:
    return ROTATION_180;
    case 270:
    return ROTATION_270;
    }
    return ROTATION_0;
    }
    同时在frameworks/base/services/java/com/android/server/wm/ScreenRotationAnimation.java
    修改deltaRoataion为deltaRoataion(rotation.Surface.getDefaultRotationIndex())

## 4. 解决GSensor反应慢的问题：

### 4.1 Gsensor架构：

![](GSensor问题查错.assets/2019-07-09 17-22-46 的屏幕截图.png)

### 

传感器相关的代码路径：

![](GSensor问题查错.assets/2019-07-09 17-27-19 的屏幕截图.png)

![](GSensor问题查错.assets/2019-07-09 17-27-57 的屏幕截图.png)

在java层中的 Sensor的状态控制是由SensorService来负责的，他的java代码 和Jni 代码 分别位于如下文件中。

**set_delay(): 的功能是设置延时，实际上就是设置了传感器的精度，这个精度对应数据设备Poll阻塞返回的时间。**



### 4.2 屏幕翻转

在framework层的内容在：

```shell
com/android/server/policy/WindowOrientationListener.java
```

```java
public void onSensorChanged(SensorEvent event) {
    ...
    // Tell the listener.
            if (proposedRotation != oldProposedRotation && proposedRotation >= 0) {
                if (LOG) {
                    Slog.v(TAG, "Proposed rotation changed!  proposedRotation=" + proposedRotation
                            + ", oldProposedRotation=" + oldProposedRotation);
                }
                // CHAOZHUO BEGIN
                if (proposedRotation == Surface.ROTATION_0 || proposedRotation == Surface.ROTATION_180) {
                    onProposedRotationChanged(proposedRotation);
                }
                // CHAOZHUO END
            }
}
```

这里仅仅是 允许 0 ~ 180 的旋转， 其他角度是不允许旋转的。这里仅仅是找到代码的点，先暂时不修改

### 4.3 修复反应慢：

原来反应慢，还是因为参数没有调节对：

![](GSensor问题查错.assets/2019-07-10 18-57-46 的屏幕截图.png)

```shell
set_property hal.sensors.iio.accel.matrix -1,0,0,0,-1,0,0,0,-1
```



