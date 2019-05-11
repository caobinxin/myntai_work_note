## mount挂载img文件提示：错误的文件类型

mount : wrong fs type , bad option, bad superblock on /dev/loop2 ,

  missing codepage or helper program, or other error

  In some cases useful info is found in syslog - try

  dmesg | tail or so



这种情况一个可能真的是img文件系统的类型写错了，另一个可能是这个img镜像含有多个分区。



后面这种情况有两种解决办法：

\1. losetup+kpartx

 （呵呵，复制和修改自：http://blog.csdn.net/qianlong4526888/article/details/8284074）

losetup /dev/loop0 test.img

sudo kpartx -av /dev/loop0

终端下会给出分区的信息，同时Ubuntu文件管理器左侧会出现几个可挂载的分区，你可以直接右击“挂载”，相应名字的分区就挂载到了media相同名字文件夹下，当然也可以用命令行挂载。

sudo mount /dev/mapper/loop0p1 /mnt

表示挂载test.img镜像中的第一分区到mnt。

sudo kpartx -d /dev/block/loop0

sudo losetup -d /dev/loop0



\2. fdisk查看并计算偏移，mount的时候加上偏移，原始参考：

http://my.oschina.net/toyandong/blog/65002

命令是这样的：

sudo mount -o loop,offset=$((34816 * 512)) ubuntu-desktop-12.04-1-miniand.com.img /mnt

其中的34816和512参考下面的。

我的操作过程，重点是红色部分。

查看信息：

$ fdisk -l ubuntu-desktop-12.04-1-miniand.com.img

Disk ubuntu-desktop-12.04-1-miniand.com.img: 4023 MB, 4023386112 bytes

255 heads, 63 sectors/track, 489 cylinders, total 7858176 sectors

Units = sectors of 1 * 512 = 512 bytes

Sector size (logical/physical): 512 bytes / 512 bytes

I/O size (minimum/optimal): 512 bytes / 512 bytes

Disk identifier: 0x8daf8ee2



​                                 Device Boot      Start         End      Blocks   Id  System

ubuntu-desktop-12.04-1-miniand.com.img1            2048       34815       16384   83  Linux

ubuntu-desktop-12.04-1-miniand.com.img2           34816     7858175     3911680   83  Linux



当然，直接把偏移算出来写入命令中也是可以滴。一个扇区512bytes，所以挂载第二个分区的偏移是34816*512=1782579：

sudo mount -o loop,offset=17825792 ubuntu-desktop-12.04-1-miniand.com.img /mnt

