# kernel　休眠唤醒

http://www.wowotech.net/pm_subsystem/suspend_and_resume.html

http://www.wowotech.net/linux_kenrel/wakeup_events_framework.html

1. 

wakeup_count，wakeup source在suspend过程中产生wakeup 
event的话，就会终止suspend过程，该变量记录了wakeup 
source终止suspend过程的次数（如果发现系统总是suspend失败，检查一下各个wakeup 
source的该变量，就可以知道问题出在谁身上了）。

```shell
intel_haier:/sys # cat ./devices/pci0000:00/0000:00:14.0/power/wakeup_count
2
intel_haier:/sys # 
intel_haier:/sys # 
intel_haier:/sys # cat ./devices/LNXSYSTM:00/LNXSYBUS:00/PNP0C0D:00/power/wakeup_count
1
intel_haier:/sys # cat ./devices/LNXSYSTM:00/LNXSYBUS:00/PNP0C0C:00/power/wakeup_count
0
intel_haier:/sys # cat ./devices/LNXSYSTM:00/LNXSYBUS:00/PNP0A08:00/device:19/PNP0C09:00/PNP0C0A:00/power/wakeup_count
0
intel_haier:/sys # cat ./devices/LNXSYSTM:00/LNXSYBUS:00/PNP0A08:00/device:19/PNP0C09:00/ACPI0003:00/power_supply/ADP1/power/wakeup_count
0
intel_haier:/sys # cat ./devices/LNXSYSTM:00/LNXPWRBN:00/power/wakeup_count
9
intel_haier:/sys # 
```



2. 

pm_wakeup_event是pm_stay_awake和pm_relax的组合版，在上报event时，指定一个timeout时间，timeout后，自动relax，一般用于不知道何时能处理完成的场景。该接口比较简单，就不一一描述了。

## 1 . 问题以及测试:

## 2. debug:

通过查资料然后重点关注了这个函数：

**pm_wakeup_event**

pm_wakeup_event是pm_stay_awake和pm_relax的组合版，在上报event时，指定一个timeout时间，timeout后，自动relax，一般用于不知道何时能处理完成的场景。该接口比较简单，就不一一描述了。

android-x86_64_defconfig

```makefile
CONFIG_ACPI_BUTTON=y
```

```shell
hp-4.19/kernel/drivers$ vim base/power/wakeup.c +807
```

```c
static inline void pm_wakeup_event(struct device *dev, unsigned int msec)
{
    return pm_wakeup_dev_event(dev, msec, false);
}

void pm_wakeup_dev_event(struct device *dev,chuank unsigned int msec, bool hard)
{
    unsigned long flags;

    if (!dev)
        return;                                                                           
    printk(KERN_ERR"colby ----------\n");   
    dump_stack();
    printk(KERN_ERR"colby ----------\n\n\n\n"); 
    spin_lock_irqsave(&dev->power.lock, flags);
    pm_wakeup_ws_event(dev->power.wakeup, msec, hard);
    spin_unlock_irqrestore(&dev->power.lock, flags);
}
EXPORT_SYMBOL_GPL(pm_wakeup_dev_event);
```



通过打印调用栈的方式：我们找到了我们电源键的　上报的位置：

```shell
[ 2007.830336] colby ----------
[ 2007.830337] CPU: 0 PID: 5310 Comm: kworker/0:3 Not tainted 4.19.50-PhoenixOS-x86_64-gc173caa4e3cd-dirty #1
[ 2007.830338] Hardware name: HP HP Pavilion x360 Convertible 14-dh0xxx/85C4, BIOS F.02 01/11/2019
[ 2007.830340] Workqueue: kacpi_notify acpi_os_execute_deferred
[ 2007.830340] Call Trace:
[ 2007.830342]  dump_stack+0x63/0x85
[ 2007.830343]  pm_wakeup_dev_event+0x45/0x80
[ 2007.830344]  acpi_pm_wakeup_event+0x23/0x30
[ 2007.830346]  acpi_button_notify+0x48/0x130 # 这个就是　和按键相关的
[ 2007.830346]  acpi_device_notify_fixed+0x1b/0x20
[ 2007.830347]  acpi_os_execute_deferred+0x18/0x30
[ 2007.830349]  process_one_work+0x151/0x3a0
[ 2007.830350]  worker_thread+0x4c/0x3c0
[ 2007.830351]  kthread+0x102/0x140
[ 2007.830351]  ? process_one_work+0x3a0/0x3a0
[ 2007.830352]  ? kthread_park+0x90/0x90
[ 2007.830353]  ret_from_fork+0x1f/0x30
[ 2007.830354] colby ----------

```

同时也有新的问题发现，　下面的调用栈，在不断的调用：

```shell
[ 2002.837476] colby ----------
[ 2002.837477] CPU: 0 PID: 1879 Comm: SensorService Not tainted 4.19.50-PhoenixOS-x86_64-gc173caa4e3cd-dirty #1
[ 2002.837477] Hardware name: HP HP Pavilion x360 Convertible 14-dh0xxx/85C4, BIOS F.02 01/11/2019
[ 2002.837477] Call Trace:
[ 2002.837478]  <IRQ>
[ 2002.837478]  dump_stack+0x63/0x85
[ 2002.837479]  pm_wakeup_dev_event+0x45/0x80
[ 2002.837480]  i8042_interrupt+0xdb/0x3f0 # 这个
[ 2002.837482]  __handle_irq_event_percpu+0x41/0x1e0
[ 2002.837483]  handle_irq_event_percpu+0x23/0x60
[ 2002.837483]  handle_irq_event+0x3d/0x60
[ 2002.837484]  handle_edge_irq+0xab/0x130
[ 2002.837485]  handle_irq+0x1d/0x30
[ 2002.837486]  do_IRQ+0x4c/0xe0
[ 2002.837487]  common_interrupt+0xf/0xf
[ 2002.837487]  </IRQ>
[ 2002.837488] RIP: 0033:0x79a5bae85d85
[ 2002.837488] Code: 64 48 8b 04 25 00 00 00 00 4c 8b 48 08 44 8b 47 04 b8 01 00 00 00 45 39 41 10 74 29 48 83 c4 10 5b 5d 41 5c c3 89 c8 66 87 07 <89> ca 83 ca 02 66 39 c2 74 31 31 c0 48 83 c4 10 5b 5d 41 5c c3
[ 2002.837489] RSP: 002b:000079a59fb00140 EFLAGS: 00000246 ORIG_RAX: ffffffffffffffd4
[ 2002.837489] RAX: 0000000000000001 RBX: 0000000000000000 RCX: 0000000000000000
[ 2002.837490] RDX: 0000000000000001 RSI: 0000000000000000 RDI: 000079a5af627ab0
[ 2002.837490] RBP: 000079a5b86575a0 R08: 000000000000000a R09: 0000000000000000
[ 2002.837490] R10: 000000000000000b R11: 0000000000000202 R12: 000079a5b9ac6c80
[ 2002.837491] R13: 000079a5b9ac6c80 R14: 0000000000000000 R15: 000079a5af627980
[ 2002.837491] colby ----------

```

通过看源码中的注释，我们知道　i8042 keyboard and mouse controller driver for Linux

## 3. fix

根据第2节的debug信息，我们可以轻松的找到　电源键上报的地方：

**acpi_button_notify**

电源键的驱动：

```shell
hp-4.19/kernel/drivers$ vim acpi/button.c
```

## 4. android层

暂时将休眠等级调成early_suspend

三.屏幕锁定超过一定时间后无法通过电源键唤醒
删掉/system/lib64/hw/power.x86.so，新建空白文件power.x86.so，这个文件的文件名就是power.x86.so
/system/lib/hw/power.x86.so也删掉，改成空白文件power.x86.so
修改build.prop（此文件在system根目录），加上

```shell
sleep.earlysuspend=1　# 在我们的　os中　我只加了这个，　下面的没有添加
poweroff.doubleclick=1
```

https://blog.csdn.net/wh_19910525/article/details/27890333

修改如下文件，才能将其加到　./out/target/product/x86_64/system/build.prop中

```shell
hp-4.19/build$ vim tools/buildinfo.sh
```





## 5. kernel:

```c
static struct acpi_driver acpi_button_driver = {               
    .name = "button",
    .class = ACPI_BUTTON_CLASS,
    .ids = button_device_ids,
    .ops = { 
        .add = acpi_button_add,
        .remove = acpi_button_remove,
        .notify = acpi_button_notify,
    },  
    .drv.pm = &acpi_button_pm,
};


module_driver(acpi_button_driver,acpi_button_register_driver,acpi_button_unregister_driver);
```

```c
static int acpi_button_register_driver(struct acpi_driver *driver)                      
{
    if (acpi_disabled)
        return 0;

    return acpi_bus_register_driver(driver);
}
```

```c
int acpi_bus_register_driver(struct acpi_driver *driver)                        
{
    int ret; 

    if (acpi_disabled)chuank
        return -ENODEV;
    driver->drv.name = driver->name;
    driver->drv.bus = &acpi_bus_type;
    driver->drv.owner = driver->owner;

    ret = driver_register(&driver->drv);
    return ret; 
}
```

找到调用　notify的地方：

```c
acpi_bus_notify // 只有在初始化的时候调用到了，　所以这个不用看了
acpi_device_notify
```



谁调用　acpi_device_notify

```c
acpi_device_notify_fixed　　// 他又被　acpi_device_fixed_event　所调用
```

其中，创建了一个内核线程：　

```c
static u32 acpi_device_fixed_event(void *data)
{                    
    acpi_os_execute(OSL_NOTIFY_HANDLER, acpi_device_notify_fixed, data);
    return ACPI_INTERRUPT_HANDLED;
}
```



```c
acpi_status
acpi_os_execute(acpi_execute_type type,
        acpi_osd_exec_callback function, void *context)
{
    pthread_t thread;                                                                                                                                                                                          
    int ret; 

    ret =
        pthread_create(&thread, NULL, (PTHREAD_CALLBACK) function, context);
    if (ret) {
        acpi_os_printf("Create thread failed");
    }    
    return (0); 
}

```

谁有调用　acpi_device_fixed_event了：

```shell
acpi_device_install_notify_handler  # <- acpi_device_probe
acpi_device_remove_notify_handler # <- acpi_device_remove
```





### 5.1 修复：

```shell
commit 89c37eb426472659b4ae5de924a728de79110973
Author: Chen, Hu <hu1.chen@intel.com>
Date:   Fri Jan 4 16:45:20 2019 +0800

    ACPI / button: Propagate wakeup-from-suspend events during resume
    
    During the resume of ACPI_BUTTON_HID_POWER, propagate wakeup event to
    user space. Otherwise, the Android libsuspend will put the system to S3
    immediately. This doesn't happen on pure Linux.
    
    I suppose acpi_button_notify() should propagate such event when the
    power button is pressed. However, the function get ACPI_BUTTON_TYPE_LID
    and thus doesn't propagate the event.
    
    Please contact some ACPI experts to really fix this!
    
    Signed-off-by: Chen, Hu <hu1.chen@intel.com>

diff --git a/drivers/acpi/button.c b/drivers/acpi/button.c
index ed72aa223dc8..4722a4922cd5 100644
--- a/drivers/acpi/button.c
+++ b/drivers/acpi/button.c
@@ -452,6 +452,26 @@ static int acpi_button_resume(struct device *dev)
        button->suspended = false;
        if (button->type == ACPI_BUTTON_TYPE_LID && button->input->users)
                acpi_lid_initialize_state(device);
+
+       if (button->type == ACPI_BUTTON_TYPE_POWER &&
+           strstr(button->phys, ACPI_BUTTON_HID_POWER) != NULL) {
+               struct input_dev *input = button->input;
+               int keycode;
+
+               acpi_pm_wakeup_event(&device->dev);
+
+               keycode = test_bit(KEY_SLEEP, input->keybit) ?
+                                       KEY_SLEEP : KEY_POWER;
+               input_report_key(input, keycode, 1);
+               input_sync(input);
+               input_report_key(input, keycode, 0);
+               input_sync(input);
+
+               acpi_bus_generate_netlink_event(
+                       device->pnp.device_class,
+                       dev_name(&device->dev),
+                       ACPI_BUTTON_NOTIFY_STATUS, ++button->pushed);
+       }
        return 0;
 }
 #endif
```

这里的修复思路是这样的，　当我们的电源键这个设备被唤醒的时候，　我们向android层　上报一个　powerbtn inputevent事件。　　　这样，　就可以了。　没有必要等在　acpi_button_notify中发送。

**这里记录一个调式手段：**

**no_console_suspend**: 据说这个启动参数加上之后，　在休眠唤醒中，可以往终端中打印跟多的东西。　

之所以加这个参数：是因为我们　　猜测　acpi_button_notify　在我们第一次按电源键的时候，这个函数其实是被调用到了，　只不过是　printk没有打印出来。　但后来发现，这个函数确实没有调用到。　在有说回来了，

先暂时将这个内核传参记录下来吧，　　方便我们在调　休眠唤醒的时候，　可以让我们的串口终端正常打印



问题的原因：

```shell
当第一次点击powerbtn的时候　kernel是起来的，　　　此时android上层没有收到　powerbtn的上报事件，　有将系统重新置为s3级别，　
```

