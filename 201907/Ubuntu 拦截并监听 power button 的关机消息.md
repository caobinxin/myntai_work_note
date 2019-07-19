# Ubuntu 拦截并监听 power button 的关机消息

system：ubuntu 18.04
platform：rockchip 3399
board：NanoPi M4
前言

物理上的电源按键短按之后，系统直接硬关机了，导致应用程序无法保护现场，就直接宕机了，查阅了大量资料，发现通过使用acpi可以禁止这个关机消息上传，但是看完acpi就放弃了这个想法，至少我在arm平台上没有具体成功实现这个消息的拦截，不然通过这个方式应该很便利，至少在intel上问题不大。
解决方法

google上搜索了很久，加上实践测试，终于找到了解决方案，具体如下所示；

    Instead, adding a single line to /etc/systemd/logind.conf was enough to do the trick:
    #HandlePowerKey=poweroff
    HandlePowerKey=suspend
    Now, pressing the power button causes instant suspend.

修改/etc/systemd/logind.conf文件，找到HandlePowerKey，并且可以使用下面几个参数：

    suspend 系统会被挂起，如果我的设备终端有屏幕，而且装了相应的桌面，还会进入锁屏界面；
    ignore 电源按键的消息会被忽略，即我按下按键之后什么事情都没有发生；
---------------------
## 监听按键

```shell
cat /proc/bus/input/devices
```

得到如下结果：

```shell
I: Bus=0019 Vendor=0000 Product=0003 Version=0000
N: Name="Sleep Button"
P: Phys=PNP0C0E/button/input0
S: Sysfs=/devices/LNXSYSTM:00/LNXSYBUS:00/PNP0C0E:00/input/input0
U: Uniq=
H: Handlers=kbd event0 
B: PROP=0
B: EV=3
B: KEY=4000 0 0

I: Bus=0019 Vendor=0000 Product=0001 Version=0000
N: Name="Power Button"
P: Phys=PNP0C0C/button/input0
S: Sysfs=/devices/LNXSYSTM:00/LNXSYBUS:00/PNP0C0C:00/input/input1
U: Uniq=
H: Handlers=kbd event1 
B: PROP=0
B: EV=3
B: KEY=10000000000000 0

I: Bus=0019 Vendor=0000 Product=0001 Version=0000
N: Name="Power Button"
P: Phys=LNXPWRBN/button/input0
S: Sysfs=/devices/LNXSYSTM:00/LNXPWRBN:00/input/input2
U: Uniq=
H: Handlers=kbd event2 
B: PROP=0
B: EV=3

## B: KEY=10000000000000 0
```

所以，可能需要监听`event1`或者`event2`，可以通过以下的代码直接监听事件的输入信息；

```c
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/types.h>
#include <fcntl.h>
#include <errno.h>
#include <time.h>
#include <linux/input.h>
 
struct input_event event;
 
int main(int argc, char **argv)
{
    char          name[64];           /* RATS: Use ok, but could be better */
    char          buf[256] = { 0, };  /* RATS: Use ok */
    unsigned char mask[EV_MAX/8 + 1]; /* RATS: Use ok */
    int           version;
    int           fd = 0;
    int           rc;
    int           i, j;
    char          *tmp;
 
#define test_bit(bit) (mask[(bit)/8] & (1 << ((bit)%8)))
 
    for (i = 0; i < 32; i++) {
        sprintf(name, "/dev/input/event%d", i);
        if ((fd = open(name, O_RDONLY, 0)) >= 0) {
            ioctl(fd, EVIOCGVERSION, &version);
            ioctl(fd, EVIOCGNAME(sizeof(buf)), buf);
            ioctl(fd, EVIOCGBIT(0, sizeof(mask)), mask);
            printf("%s\n", name);
            printf("    evdev version: %d.%d.%d\n",
                   version >> 16, (version >> 8) & 0xff, version & 0xff);
            printf("    name: %s\n", buf);
            printf("    features:");
            for (j = 0; j < EV_MAX; j++) {
                if (test_bit(j)) {
                    const char *type = "unknown";
                    switch(j) {
                    case EV_KEY: type = "keys/buttons"; break;
                    case EV_REL: type = "relative";     break;
                    case EV_ABS: type = "absolute";     break;
                    case EV_MSC: type = "reserved";     break;
                    case EV_LED: type = "leds";         break;
                    case EV_SND: type = "sound";        break;
                    case EV_REP: type = "repeat";       break;
                    case EV_FF:  type = "feedback";     break;
                    }
                    printf(" %s", type);
                }
            }
            printf("\n");
            close(fd);
        }
    }
 
    if (argc > 1) {
        sprintf(name, "/dev/input/event%d", atoi(argv[1]));
        if ((fd = open(name, O_RDWR, 0)) >= 0) {
            printf("%s: open, fd = %d\n", name, fd);
            for (i = 0; i < LED_MAX; i++) {
                event.time.tv_sec  = time(0);
                event.time.tv_usec = 0;
                event.type         = EV_LED;
                event.code         = i;
                event.value        = 0;
                write(fd, &event, sizeof(event));
            }
            
            while ((rc = read(fd, &event, sizeof(event))) > 0) {
                printf("%-24.24s.%06lu type 0x%04x; code 0x%04x;"
                       " value 0x%08x; ",
                       ctime(&event.time.tv_sec),
                       event.time.tv_usec,
                       event.type, event.code, event.value);
                switch (event.type) {
                case EV_KEY:
                    if (event.code > BTN_MISC) {
                        printf("Button %d %s",
                               event.code & 0xff,
                               event.value ? "press" : "release");
                    } else {
                        printf("Key %d (0x%x) %s",
                               event.code & 0xff,
                               event.code & 0xff,
                               event.value ? "press" : "release");
                               
                    }
                    break;
                case EV_REL:
                    switch (event.code) {
                    case REL_X:      tmp = "X";       break;
                    case REL_Y:      tmp = "Y";       break;
                    case REL_HWHEEL: tmp = "HWHEEL";  break;
                    case REL_DIAL:   tmp = "DIAL";    break;
                    case REL_WHEEL:  tmp = "WHEEL";   break;
                    case REL_MISC:   tmp = "MISC";    break;
                    default:         tmp = "UNKNOWN"; break;
                    }
                    printf("Relative %s %d", tmp, event.value);
                    break;
                case EV_ABS:
                    switch (event.code) {
                    case ABS_X:        tmp = "X";        break;
                    case ABS_Y:        tmp = "Y";        break;
                    case ABS_Z:        tmp = "Z";        break;
                    case ABS_RX:       tmp = "RX";       break;
                    case ABS_RY:       tmp = "RY";       break;
                    case ABS_RZ:       tmp = "RZ";       break;
                    case ABS_THROTTLE: tmp = "THROTTLE"; break;
                    case ABS_RUDDER:   tmp = "RUDDER";   break;
                    case ABS_WHEEL:    tmp = "WHEEL";    break;
                    case ABS_GAS:      tmp = "GAS";      break;
                    case ABS_BRAKE:    tmp = "BRAKE";    break;
                    case ABS_HAT0X:    tmp = "HAT0X";    break;
                    case ABS_HAT0Y:    tmp = "HAT0Y";    break;
                    case ABS_HAT1X:    tmp = "HAT1X";    break;
                    case ABS_HAT1Y:    tmp = "HAT1Y";    break;
                    case ABS_HAT2X:    tmp = "HAT2X";    break;
                    case ABS_HAT2Y:    tmp = "HAT2Y";    break;
                    case ABS_HAT3X:    tmp = "HAT3X";    break;
                    case ABS_HAT3Y:    tmp = "HAT3Y";    break;
                    case ABS_PRESSURE: tmp = "PRESSURE"; break;
                    case ABS_DISTANCE: tmp = "DISTANCE"; break;
                    case ABS_TILT_X:   tmp = "TILT_X";   break;
                    case ABS_TILT_Y:   tmp = "TILT_Y";   break;
                    case ABS_MISC:     tmp = "MISC";     break;
                    default:           tmp = "UNKNOWN";  break;
                    }
                    printf("Absolute %s %d", tmp, event.value);
                    break;
                case EV_MSC: printf("Misc"); break;
                case EV_LED: printf("Led");  break;
                case EV_SND: printf("Snd");  break;
                case EV_REP: printf("Rep");  break;
                case EV_FF:  printf("FF");   break;
                    break;
                }
                printf("\n");
            }
            printf("rc = %d, (%s)\n", rc, strerror(errno));
            close(fd);
        }
    }
    return 0;
}

```

按下电源按键之后，最终实践发现是`event2`，可以监听到`key code`以及按键的动作；

```shell
	...
	/dev/input/event2: open, fd = 3
Thu Mar 21 21:38:28 2019.801129 type 0x0001; code 0x0074; value 0x00000001; Key 116 (0x74) press
Thu Mar 21 21:38:28 2019.801129 type 0x0000; code 0x0000; value 0x00000000; 
Thu Mar 21 21:38:28 2019.801159 type 0x0001; code 0x0074; value 0x00000000; Key 116 (0x74) release
Thu Mar 21 21:38:28 2019.801159 type 0x0000; code 0x0000; value 0x00000000; 
Thu Mar 21 21:38:35 2019.038000 type 0x0001; code 0x0074; value 0x00000001; Key 116 (0x74) press
Thu Mar 21 21:38:35 2019.038000 type 0x0000; code 0x0000; value 0x00000000; 
Thu Mar 21 21:38:35 2019.038028 type 0x0001; code 0x0074; value 0x00000000; Key 116 (0x74) release
Thu Mar 21 21:38:35 2019.038028 type 0x0000; code 0x0000; value 0x00000000; 
```

