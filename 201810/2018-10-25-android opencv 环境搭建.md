# android opencv 环境搭建

## 1. 导入模块

File ->  new -> import modules -> 

/home/colby/work300GB/OpenCV-android-sdk/sdk/java



## 2. 依赖模块

在 Android 视角下  右键单击 open modules setting  -> project structure -> app -> dependenicis -> + -> module dependenicis  结果是( 如果失败，手动添加 即可)

在 APP的 build.gradle 中 添加了 

```shell

dependencies {
    implementation project(':openCVLibrary320')
}
```



## 3. 错误解决

### 3.1 符号找不到

错误信息：

错误: 找不到符号
符号:   变量 GL_TEXTURE_EXTERNAL_OES
位置: 类 GLES11Ext

分析： 工程配置有问题

在 Android 视角下  右键单击 open modules setting  -> project structure -> app -> properties -> compile sdk version -> API 27       build tools version -> 27

openCVLibrarble  配置和APP一样。