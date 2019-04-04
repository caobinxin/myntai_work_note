# ARM Linux内核驱动异常定位调试--反汇编arm-linux-objdump

##  ARM Linux内核驱动异常定位方法分析--反汇编方式

最近在搞Atmel 的SAM9x25平台，Linux系统，用于工业设备。这也是我首次参与工业设备的研发。在调试Atmel SAM9x25的Linux串口设备的时候，发现无论是读还是写，都会产生异常。相关的异常信息如下：

==================================================================================================================

Unable to handle kernel NULL pointer dereference at virtual address 00000000

pgd = c0004000

[00000000] *pgd=00000000

Internal error: Oops: 17 [#1]

last sysfs file: /sys/devices/virtual/vc/vcsa1/dev

Modules linked in:

CPU: 0    Not tainted  (2.6.39 #1)
PC is at atmel_tasklet_func+0x110/0x69c
LR is at atmel_tasklet_func+0x10/0x69c
pc : [<c01a4f30>]    lr : [<c01a4e30>]     psr: 20000013

sp : c7825f50  ip : c045e0bc  fp : 00000000

r10: c0456a80  r9 : 0000000a  r8 : 00000000

r7 : c7874568  r6 : c045e0a8  r5 : 00000100  r4 : c045dfb4

r3 : 00000002  r2 : 00000ffc  r1 : 00000001  r0 : 00000001

Flags: nzCv  IRQs on  FIQs on  Mode SVC_32  ISA ARM  Segment kernel

Control: 0005317f  Table: 27aec000  DAC: 00000017

Process ksoftirqd/0 (pid: 3, stack limit = 0xc7824270)
Stack: (0xc7825f50 to 0xc7826000)

5f40:                                     00000100 c7824000 00000001 00000018

5f60: 0000000a c0456a80 c7825f84 00000000 00000100 c7824000 00000001 00000018

5f80: c0456a80 c0047b70 00000006 c0047650 c0432e50 00000000 c7824000 00000000

5fa0: 00000000 c0047938 00000000 00000000 00000000 c00479a0 c7825fd4 c7819f60

5fc0: 00000000 c0058c64 c00335f4 00000000 00000000 00000000 c7825fd8 c7825fd8

5fe0: 00000000 c7819f60 c0058be0 c00335f4 00000013 c00335f4 0c200050 fc3b9beb

[<c01a4f30>] (atmel_tasklet_func+0x110/0x69c) from [<c0047b70>] (tasklet_action+0x80/0xe4)

[<c0047b70>] (tasklet_action+0x80/0xe4) from [<c0047650>] (__do_softirq+0x74/0x104)

[<c0047650>] (__do_softirq+0x74/0x104) from [<c00479a0>] (run_ksoftirqd+0x68/0x108)

[<c00479a0>] (run_ksoftirqd+0x68/0x108) from [<c0058c64>] (kthread+0x84/0x8c)

[<c0058c64>] (kthread+0x84/0x8c) from [<c00335f4>] (kernel_thread_exit+0x0/0x8)

Code: 1a000002 e59f057c e59f157c ebfa416c (e5983000) 

---[ end trace 6b8e1841ba3a56c9 ]---
Kernel panic - not syncing: Fatal exception in interrupt

[<c0037784>] (unwind_backtrace+0x0/0xf0) from [<c00429f4>] (panic+0x54/0x178)

[<c00429f4>] (panic+0x54/0x178) from [<c0035a18>] (die+0x17c/0x1bc)

[<c0035a18>] (die+0x17c/0x1bc) from [<c00386c4>] (__do_kernel_fault+0x64/0x84)

[<c00386c4>] (__do_kernel_fault+0x64/0x84) from [<c003889c>] (do_page_fault+0x1b8/0x1cc)

[<c003889c>] (do_page_fault+0x1b8/0x1cc) from [<c002c2f0>] (do_DataAbort+0x38/0x9c)

[<c002c2f0>] (do_DataAbort+0x38/0x9c) from [<c003234c>] (__dabt_svc+0x4c/0x60)

Exception stack(0xc7825f08 to 0xc7825f50)

5f00:                   00000001 00000001 00000ffc 00000002 c045dfb4 00000100

5f20: c045e0a8 c7874568 00000000 0000000a c0456a80 00000000 c045e0bc c7825f50

5f40: c01a4e30 c01a4f30 20000013 ffffffff

[<c003234c>] (__dabt_svc+0x4c/0x60) from [<c01a4f30>] (atmel_tasklet_func+0x110/0x69c)

[<c01a4f30>] (atmel_tasklet_func+0x110/0x69c) from [<c0047b70>] (tasklet_action+0x80/0xe4)

[<c0047b70>] (tasklet_action+0x80/0xe4) from [<c0047650>] (__do_softirq+0x74/0x104)

[<c0047650>] (__do_softirq+0x74/0x104) from [<c00479a0>] (run_ksoftirqd+0x68/0x108)

[<c00479a0>] (run_ksoftirqd+0x68/0x108) from [<c0058c64>] (kthread+0x84/0x8c)

[<c0058c64>] (kthread+0x84/0x8c) from [<c00335f4>] (kernel_thread_exit+0x0/0x8)

==================================================================================================================

通常认为，**产生异常的地址是lr寄存器的值**，从上面的异常信息可以看到[lr]的值是c01a4e30。

接下来，我们可以通过内核镜像文件反汇编来找到这个地址。内核编译完成后，会在内核代码根目录下生成vmlinux文件，我们可以通过以下命令来反汇编：

**arm-none-eabi-objdump -Dz -S vmlinux >linux.dump**

值得注意的是，arm-none-eabi-objdump的参数-S表示尽可能的把原来的代码和反汇编出来的代码一起呈现出来，-S参数需要结合   arm-linux-gcc编译参数-g，才能达到反汇编时同时输出原来的代码。所以，我在linux内核代码根目录的Makefile中增加-g编译参  数：

KBUILD_CFLAGS   := **-g** -Wall -Wundef -Wstrict-prototypes -Wno-trigraphs \
​      -fno-strict-aliasing -fno-common \
​      -Werror-implicit-function-declaration \
​      -Wno-format-security \
​      -fno-delete-null-pointer-checks

修改Makefile后，重新编译内核，在根目录中生成的vmlinux文件就会包含了原来的代码信息，因此，该文件的大小也比原来大一倍！

最后执行“**arm-none-eabi-objdump -Dz-S vmlinux >linux.dump**”，由于加入了-g编译参数，执行这个反汇编命令需要很长时间（本人在虚拟机上执行，花了近6个小时！），反汇编出来的linux.dump文件也比原来的44MB增大到惊人的503MB。

(博主加：这里有一点要注意，如果是ko模块文件，反汇编时如果想看到ko文件的某函数反汇编代码，该函数不能加static关键字修饰，而且module_init修饰的入口函数，其名字即为module_init)

接下来可以用UltraEdit打开linux.dump文件，查找“c01a4e30”字符串。

最后定位到的信息是：

==================================================================================================================

/*
  \* tasklet handling tty stuff outside the interrupt handler.
  */
 static void atmel_tasklet_func(unsigned long data)
 {
 c01a4e20: e92d45f0  push {r4, r5, r6, r7, r8, sl, lr}
 c01a4e24: e24dd01c  sub sp, sp, #28 ; 0x1c
 c01a4e28: e1a04000  mov r4, r0
  /* The interrupt handler does not take the lock */
  spin_lock(&port->lock);

 if (atmel_use_pdc_tx(port))
   atmel_tx_pdc(port);
 **else if (atmel_use_dma_tx(port))**

c01a4e2c: ebfffda1  bl c01a44b8 <atmel_use_dma_tx>
**c01a4e30: e3500000  cmp r0, #0 ; 0x0**

c01a4e34: e5943034  ldr r3, [r4, #52]
 c01a4e38: 0a00007b  beq c01a502c <atmel_tasklet_func+0x20c>

==================================================================================================================

可以看出来，异常的产生位于atmel_tasklet_func函数的 **else if (atmel_use_dma_tx(port))一行**。

估计atmel_use_dma_tx(port)的“port”参数为空指针所致！

 

最后，我把串口的DMA功能去掉，改为直接传送，这样做虽然效率低了点，但产生异常的现象消失了。

到后面再仔细分析为什么会产生这个异常，彻底解决这个问题。

 

原文链接：http://blog.csdn.net/hunhunzi/article/details/7052032