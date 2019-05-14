# android-framework编译调试

## 1. 资料准备

url :https://www.cnblogs.com/liumce/p/8027559.html(详细记录从源码下载到framework调试的详细过程)

https://blog.csdn.net/gjy_it/article/details/80733904

调试参考代码：https://blog.csdn.net/turtlejj/article/details/83860708

## 2. 环境搭建

### 2.0 少辉哥搭建流程

![](2019-05-13-android-framework编译调试.assets/调试系统源码-少辉.png)



## 3. 其它：

#### 4.设置USB接口访问设备

在linux下，默认情况是不允许普通用户直接通过USB接口来访问设备的.

推荐方法是以根用户身份在 `/etc/udev/rules.d/51-android.rules` 路径创建文件。

我们可以通过如下指令来实现（**注意用你的系统username替换指令中的**）:

![img](http://mmbiz.qpic.cn/mmbiz_png/CvQa8Yf8vq1ibA4bX18sxZKvKhys8lz26edwENtzibc9Ycd0CSicHGGK0ibTMf1cTn95vUj2MYhqSCLHNxDujOE6fQ/0?wx_fmt=png)

