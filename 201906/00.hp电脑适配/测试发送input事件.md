# 测试发送input事件

```shell
7   input keyevent 27
8   input tap 1900 540 # 模拟发送 坐标 (1900 , 540)下的点击 

19  for i in `seq 1 200`;do input tap 1235 205; sleep 3;input tap 1235 205; sleep 20;echo $i; done


 while true;do cat in_accel_z_raw; sleep 1;done;
```

下面的for循环的意思就是 循环点击 （1235 ，205） 这个点 200次。

```shell
`seq 1 200` 生成 1 ~ 200 的一个序列
```



测试gsensor输出值：

```shell
while true;do cat in_accel_y_raw; cat in_accel_x_raw; sleep 000.1;done;


while true;do printf "x=%s y=%s z=%s\n" `cat in_accel_x_raw` `cat in_accel_y_raw` `cat in_accel_z_raw` ; sleep 000.1;done;

while true;do printf "x=%s y=%s z=%s\n" `cat in_gravity_x_raw` `cat in_gravity_y_raw` `cat in_gravity_z_raw` ; sleep 000.1;done;

while true;do printf "x=%s y=%s z=%s\n" `cat in_incli_x_raw` `cat in_incli_y_raw` `cat in_incli_z_raw` ; sleep 000.1;done;
```

/sys/bus/iio/devices/iio:device2/in_gravity_z_raw

x=999081 y=33941 z=-13068

scale=0.000010 x=9.796988 y=0.332825 z=-0.128145







hp 

正面： scale=0.000010 x=0.428728 y=-1.416987 z=-9.625844

反面：scale=0.000010 x=-0.329805 y=0.282795 z=9.783289



hair:

水平：scale=0.019163 x=0.057489 y=-0.019163 z=9.926434

反面：scale=0.019163 x=0.076652 y=-0.210793 z=-9.773129



立起来：scale=0.019163 x=-0.134141 y=9.638988 z=0.210793 屏幕面向自己

立起来：scale=0.019163 x=0.095815 y=-9.811456 z=0.306608



立起来: scale=0.019163 x=9.734804 y=-0.095815 z=-0.728194 屏幕面向左边

立起来：scale=0.019163 x=-9.849782 y=0.517401 z=-0.114978



```shell
hp:  in_gravity_x_raw

正面：x=-27397 y=938735 z=-342472  键盘面朝上

反面：x=-18421 y=890855 z=-453600


立起来：x=-21828 y=863818 z=-502881 键盘 朝自己

	   x=-22778 y=909144 z=-415117


立起来：x=-12572 y=894650 z=-446173 键盘面朝左侧

	   x=-33503 y=895490 z=-443312
```



```shell
hp:  in_accel_y_raw

正面： x=25388 y=-837355 z=377460
反面： x=25388 y=-837355 z=377460

立起来：保持一样：
```



## qq登录发送重复登录

```shell
while true; do input tap 728 1399; sleep 0.5;input tap 728 1399; sleep 0.5;done
```



## ubuntu 安装虚拟机

参考链接：https://www.jianshu.com/p/3bb99a3d57c9

```shell
sudo apt-get install virtualbox
 virtualbox
```



安装软件Genymotion
到官方网站([https://www.genymotion.com/](https://link.jianshu.com?t=https://www.genymotion.com/))下载Genymotion
其实接下来就是注册登录然后下载适用，Genymotion分为免费版和收费版，咱们使用免费版的就可以了。
[https://www.genymotion.com/download/](https://link.jianshu.com?t=https://www.genymotion.com/download/)

下载下来的是一个.bin 文件。
 咱们运行命令安装。

```shell
chmod +x 下载.bin 文件

./bin文件 -d /home  // 自己设置的一个安装位置 /home
```

这俩都安装完了，应该可以运行了，打开这个安装的软件。
这时候会报一个错：irtualbox-dkms

```
  // 运行命令
  sudo apt-get install virtualbox-dkms 
```



## 测试拨打电话

```shell
while true; do adb shell am start -a android.intent.action.CALL tel:19155155729; sleep 68;done
```

