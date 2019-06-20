# Android命令行测试BT，WIFI，Sensor工作状态

### 测试wlan

//加载驱动
#insmod /system/lib/modules/ath6kl/cfg80211.ko
#insmod /system/lib/modules/ath6kl/ath6kl_sdio.ko

//查看加载的情况
root@android:/ # lsmod
wlan 205174 0 - Live 0x00000000 (O)
cfg80211 202998 1 wlan, Live 0x00000000 (O)
sm_event_driver 8955 0 - Live 0x00000000
sm_event_log 7972 0 - Live 0x00000000

//看网卡设备是否生成
root@android:/ # busybox ifconfig -a                                           
lo        Link encap:Local Loopback  
          inet addr:127.0.0.1  Mask:255.0.0.0
          inet6 addr: ::1/128 Scope:Host
          UP LOOPBACK RUNNING  MTU:16436  Metric:1
          RX packets:0 errors:0 dropped:0 overruns:0 frame:0
          TX packets:0 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:0 
          RX bytes:0 (0.0 B)  TX bytes:0 (0.0 B)

//生成的网卡设备
wlan0     Link encap:Ethernet  HWaddr 02:03:7F:CE:8E:10  
          BROADCAST MULTICAST  MTU:1500  Metric:1
          RX packets:12 errors:0 dropped:0 overruns:0 frame:0
          TX packets:3 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1000 
          RX bytes:7015 (6.8 KiB)  TX bytes:24 (24.0 B)

### 测试蓝牙

1. 命令行控制蓝牙开关
adb shell
cd /data/data/com.android.providers.settings/databases
sqlite3 settings.db
select * from secure where name="bluetooth_on"; （查看是否打开）
update secure set value=1 where name="bluetooth_on";  (这里的value=1是打开，0是关闭)
select * from secure where name="bluetooth_on"; （确认是否更改成功）
reboot <重启手机生效>

2. 命令行操作蓝牙
Android原生包括高通QRD用的是blueZ的蓝牙协议栈，有提供两个工具：hciconfig和hcitool用于调试蓝牙，开始调试前首先需要将这些工具Push到手机上：
adb remount
adb push hciconfig /system/xbin
adb push hcitool /system/xbin
adb shell
chmod -R 777 /system/xbin
要注意的是，这些工具只适用于blueZ，象MTK用的是bluetoothangel就不适用了

常用的一些命令：
hciconfig -a （查看蓝牙地址，芯片状态等等）
hcitool scan （进行蓝牙搜索，并列出搜索到的设备名称和设备地址）
hciconfig hciX piscan （开启Inquiry Scan和Page Scan，使手机处于可被搜索和可连接状态）
可以使用hciconfig
 --help以及hcitool --help来查询其它的功能，尤其要提的是hcitool cmd这个命令，通过这个命令可以发送任何的HCI 
Command，大部分蓝牙功能都可以通过发送HCI Command来实现，具体HCI Command格式可以查询蓝牙Spec

进入测试模式的命令：
hcitool cmd 0x06 0x0003 （Enter Test Mode）
hcitool cmd 0x03 0x0005 0x02 0x00 0x02 （Auto Accept All Connections）
hcitool cmd 0x03 0x001A 0x03 （Page Inquiry Scans）
hcitool cmd 0x03 0x0020 0x00 （Disable Authentication）
hcitool cmd 0x03 0x0022 0x00 （Disable Encryption）

Qualcomm bt test :

the follow commands to bring up bt through adb shell:

 #echo 1 > /sys/class/rfkill/rfkill0/state

#hci_qcomm_init -vvv -e

#hciattach /dev/ttyHS0 qualcomm-ibs 3000000

#hciconfig hci0 up

#hcitool scan

The follow commands are used to enter test mode.

 #bttest disable

#bttest enable

#bttest enable_dut_mode

### 测试G-sensor

root@android:/sys/devices/virtual/input/input3 # ls
bandwidth
calibrate
capabilities
enable
event3
id
modalias
mode
name
phys
poll_delay
power
properties
range
subsystem
uevent
uniq
value
root@android:/sys/devices/virtual/input/input3 #  
＃echo 1 > enable
#cat value 

### 测试M-sensor

root@android:/sys/class/compass/akm8975 # ls
accel
asa
bdata
delay_acc
delay_mag
delay_ori
dev
enable_acc
enable_mag
enable_ori
i2c
mode
power
subsystem
uevent
root@android:/sys/class/compass/akm8975 # 
#echo 1 > enable_mag
#cat bdate



### svc 

svc测试命令

svc命令，位置在/system/bin目录下，用来管理电源控制，无线数据，WIFI。
[plain] view plaincopy 

    svc  
    Available commands:  
    help     Show information about the subcommands  
    power    Control the power manager  
    data     Control mobile data connectivity  
    wifi     Control the Wi-Fi manager  
        
    #### svc power  
    svc power  
    Control the power manager  
    usage: svc power stayon [true|false|usb|ac]  
    Set the 'keep awake while plugged in'setting.  
    //设置屏幕的常亮，true保持常亮，false不保持，usb当插入usb时常亮，ac当插入电源时常亮  
        
    #### svc data  
    svc data  
    Control mobile data connectivity  
    usage: svc data [enable|disable]  
    Turn mobile data on or off.  
    //设置移动网络的数据是否启用  
    svc data prefer  
    Set mobile as the preferred data network  
    //设置移动网络的数据优先于WIFI  
        
    #### svc wifi  
    svc wifi  
    Control the Wi-Fi manager  
    usage: svc wifi [enable|disable]  
    Turn Wi-Fi on or off.//设置WIFI是否启用  
    svc wifi prefer  
    Set Wi-Fi as the preferred data network//设置WIFI优先于移动网络的数据，一般应设置成这样，除非你刻意使用移动网络数据传输  


上面的命令都要在shell中执行，需要root。
命令详细介绍：

svc命令
    这个脚本在/system/bin目录下，这个命令可以用来控制电源管理，wifi开关，数据开关(就是上网流量)

svc power stayon [true|false|usb|ac] 
    这个是控制usb插入的时候屏幕是否常亮，这个有地方设置，就不多说了

svc data disable


这个命令可以关闭数据连接，就是上网流量，大家都知道控制上网的开关很多，但是大都是通过在apn上的接入点加后缀来实现的，但是这个命令不会更改apn的任何设置，直接在底层关闭数据连接。

应该是最彻底的，而且又不影响apn的设置。这个跟apndroid有什么区别，apndroid是在关闭上网数据的时候，已经在下载的连接可能不会被强制关闭(这个在apndroid自己的说明中也有提到)。比如你在下载一个10M的电影，下载了1M，不响下载了。用apndroid关闭连接，可能这个下载还会继续，不会马上停掉。但是用这个命令，绝对毫不留情的咔嚓掉。

svc data enable 
    这个是打开上网数据连接，跟上一个命令是相反的。

svc data prefer
    这个命令是控制3g数据连接比wifi优先。我们都知道，正常情况下，有wifi的时候，不会用到3g连接。但是这个命令是相反，有3g的话先用3g流量，没有3g的时候采用wifi.我想大家不会这么傻吧，所以这个命令没什么用。

svc wifi disable 
    关闭wifi 连接
svc wifi enable 
    开启wifi连接

svc wifi prefer 
    设置wifi优先，跟5是相反的。
          



### 其他

 其它常见信息查看方法：

 查看CPU信息： cat /proc/cpuinfo
查看板卡信息：cat /proc/pci
查看PCI信息： lspci 
例子： lspci |grep Ethernet 查看网卡型号
查看内存信息：cat /proc/meminfo
查看USB设备： cat /proc/bus/usb/devices
查看键盘和鼠标:cat /proc/bus/input/devices
查看系统硬盘信息和使用情况：fdisk & disk - l & df
查看各设备的中断请求(IRQ): cat /proc/interrupts
查看系统体系结构：uname -a

dmidecode查看硬件信息，包括bios、cpu、内存等信息
dmesg | more 查看硬件信息

对于“/proc”中文件可使用文件查看命令浏览其内容，文件中包含系统特定信息：
Cpuinfo    主机CPU信息
Dma        主机DMA通道信息
Filesystems   文件系统信息
Interrupts    主机中断信息
Ioprots       主机I/O端口号信息
Meninfo       主机内存信息
Version       Linux内存版本信息（编译内核的编译器版本）