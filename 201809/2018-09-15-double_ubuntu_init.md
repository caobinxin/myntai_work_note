# 双ubuntu系统安装

## 磁盘合理进行分区

/dev/sda1 ext4 / 40GB（固态）  ubuntu16.04->work

/dev/sda5 ext4 /home/colby/androidos 140GB （固态）

/dev/sda6 swap 10GB(固态)

/dev/sda3 ext4 / 40GB (固态)     ubuntu16.04->ai



/dev/sdb5 ext4 /home/ai/ai188GB 188GB （机械）->给 ai Ubuntu使用

/dev/sdb6 ext4 /home/colby/work300GB (机械)      ->给 myntai ubuntu 使用



## 分别选择/dev/sda1  和  /dev/sda3 安装两个ubuntu

## grub问题

重启直接就 grub resume>

1. 先临时配置grub，让我们的系统跑起来

```
ls (hd1,msdos1)/boot/grub   #以这种方式，先确定我们的引导分区在那

set root=（hd1,msdos1）
set prefix=(hd1,msdos1)/boot/grub

insmod normal
normal  #此时我们就成功引导进我们的Ubuntu了，此时还没有完，这只是临时

```

2. 变临时引导为默认引导

```
sudo update-grub
sudo grub-install /dev/sdb (这里不要加分区号，我原以为我的是sda，但每次都经grub,最后抱着试试的心态，sdb 居然成功跳过 grub)
```

## 装第二个ubuntn

装好后，重启，grub没有出现，选择进入那个系统：此时只需要进入第一个系统，将grub配置文件重新更新，重启，就能看到两个系统的选择了

```sh
sudo update-grub
reboot
```

## 此时双系统全部配置完毕 



## 在来看我们的分区的事

如果我们在装系统时，分区没有选择好，此时我们直接编辑我们的 /etc/fstab 文件，重新调整我们的分区

ai-ubuntu 的 /etc/fstab   我们这里只把 机械盘的200GB给我们的ai系统使用

```sh
# <file system> <mount point>   <type>  <options>       <dump>  <pass>
# / was on /dev/sda3 during installation
UUID=8fa6b4ae-b7d1-4dbc-b20c-a32266842a80 /               ext4    errors=remount-ro 0       1
# swap was on /dev/sda6 during installation
UUID=f450316b-63b9-455d-92d1-53b3089e2192 none            swap    sw              0       0

UUID=5d711096-7993-4818-ad7d-e313d1d5c82a /home/ai/ai200GB   ext4    defaults        0       2

```



androidos 的/etc/fstab 将140GB的固态盘和300GB 的固态盘 给这个系统使用

```sh
# <file system> <mount point>   <type>  <options>       <dump>  <pass>
# / was on /dev/sda1 during installation
UUID=e2c129cb-518c-4a04-8fa8-02117b4b8263 /               ext4    errors=remount-ro 0       1
# /home/ai200GB was on /dev/sdb6 during installation
#UUID=5d711096-7993-4818-ad7d-e313d1d5c82a /home/colby/ai200GB   ext4    defaults        0       2
# /home/androidos was on /dev/sda5 during installation
UUID=64888c54-61c8-47bb-94e5-af94168accfe /home/colby/androidos ext4    defaults        0       2
# /home/work300GB was on /dev/sdb5 during installation
UUID=40011a89-230a-4b02-b84e-46fc70d68f64 /home/colby/work300GB ext4    defaults        0       2
# swap was on /dev/sda6 during installation
UUID=f450316b-63b9-455d-92d1-53b3089e2192 none            swap    sw              0       0

```

我们10GB固态 的交换分区，是两个系统公用的

## 修改挂载点的权限（用户，用户组）

```sh
sudo chown -R 用户  文件夹
sudo chgrp -R 用户 文件夹
```

至此我们就能正常使用我们的挂载了