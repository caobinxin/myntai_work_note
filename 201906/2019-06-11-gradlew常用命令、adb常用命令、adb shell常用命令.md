# gradlew常用命令、adb常用命令、adb shell常用命令

gradlew



```shell
下面全是针对 windows 操作系统的。如果是 Linux / Mac 请直接用 ./ 前缀。

gradlew clean //类似Clean Project

gradlew -v //查看Gradle、Groovy、Ant、JVM等的版本

gradlew build --info //编译并打印日志

gradlew dependencies --info //查看详细的依赖信息

gradlew assembleDebug //编译并打Debug包

gradlew assembleRelease //编译打release包

gradlew installDebug //打debug包并安装

gradlew installRelease //Release模式打包并安装
```

adb

```shell
 adb devices//获取设备列表及设备状态
 adb get-state//设备状态 device(在线) offline(离线) unknown(未连接) 三种

 adb kill-server//结束adb服务
 adb start-server//启动adb服务

 adb install//安装应用
 adb uninstall//卸载应用

 adb reboot//重启Android设备
```





adb shell
adb shell就是调用Android系统命令。

//pm（软件包管理器）
adb shell pm list package//列出所有的应用的包名
adb shell pm list package -s //列出系统应用
adb shell pm list package -3 //列出第三方应用
adb shell pm list package -f //列出应用包名及对应的apk名及存放位置
adb shell pm list package -i //列出应用包名及其安装来源

input(输入)向Android设备发送相应内容



//输入keyevent，发送按键事件 下面是发送home按键事件
adb shell input keyevent KEYCODE_HOME



//输入tab，发送屏幕点击事件 下面是点击屏幕坐标500 500 位置
adb shell input tap 500 500



//截屏，保存至sdcard目录
adb shell screencap -p /sdcard/screen.png
//录制命令  ，ctrl + c结束录制
adb shell screenrecord sdcard/record.mp4



//在logcat里面打印你设定的信息 -p：优先级，-t：标签，标签，后面加上消息

adb shell log -p d -t xuxu "test adb shell log"

