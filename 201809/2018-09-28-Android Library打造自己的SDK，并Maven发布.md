# Android Library打造自己的SDK，并Maven发布

# 1 需求

你是否用过友盟、微信、微博、支付宝的sdk？
 有没有想研究一下对方的代码，却发现已经混淆了？
 你有没有想过有一天，你也会进入一家牛逼的企业，需要发布自己SDK？
 又或者仅仅是满足自己的虚荣心，发布一个自己得意的工具？

这篇文章正是为实现这一目的

# 2 开发环境及工具

- MAC（Windows也无所谓，路径不同而已）
- Android Studio 2.3.1
- JDK 1.8
- Github
- Maven

# 3 实现步骤

## 3.1 新建工程

新建一个工程TestModule，选择empty activity，让Android studio生成一个最简单的activity，这个工程我们用来做什么的呢？
 大家知道，你交付的代码是需要有质量保证的，因此需要对他有过详尽的测试，这个工程就是我们的测试工程，简单来说就模拟用户的开发环境

## 3.2 新建module

新版本的Android Studio已经支持模块开发，我们选择File-->New-->New Module，此时会有一个弹框，我们选择Android Library，并起名为MySDK



![img](https:////upload-images.jianshu.io/upload_images/2048315-9ca3a6a29567e798.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000)

新建Android Module

新建成功后，你会发现左侧工程导航栏里，多出了一个模块工程，这个工程有着独立的一套目录结构，跟app一样。他有自己独立的gradle配置



![img](https:////upload-images.jianshu.io/upload_images/2048315-1bd3ad6615a41aff.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/460)

相同目录结构

## 3.2 建立功能类

在mysdk这个模块下的操作跟普通的Android工程没有任何区别，所以我们按平常一样，建立一个新的activity，起名叫mySDKTest，并自动建好layout。这篇文章的重点不在功能，所以我们在这个layout中随便建一个什么view，我们就建一个button吧，实现一个最简单的功能，点击这个button，就打印出一句“You clicked module button”。
 假设，这个就是我们最终要封装的功能。



![img](https:////upload-images.jianshu.io/upload_images/2048315-06eeb821e5e900b5.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/466)

Paste_Image.png

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#ffffff"
    xmlns:android="http://schemas.android.com/apk/res/android">

    <Button
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:id="@+id/button"
        android:text="Button"/>

</LinearLayout>
public class mySDKTest extends AppCompatActivity {

    private static final String TAG = "mySDKTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_sdktest);

        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i(TAG, "You clicked module button!!");
            }
        });
    }
}
```

## 3.3测试功能

此时，你会发现你根本run不起来这个工程，根本没有选项去跑这个功能，可选的只有app，没有mysdk。



![img](https:////upload-images.jianshu.io/upload_images/2048315-7555ef1d730c1843.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/906)

只有app可选.png

这就是我们在3.1新建工程时说这个工程是测试工程的原因，因为Module根本跑不起来的啊

## 3.4配置依赖

因此我们要用到app来测试SDK，所以我们要在app中配置依赖mysdk。
 方法是在app的build.gradle里加入依赖语句

```
compile project(':mysdk')
```

此时，再去app的MainActivity里，你就可以import我们刚刚建立的mySDKTest了。
 同样，我们实现一个button，点击跳转到mysdk的activity。

```
        Button button = (Button)findViewById(com.flame.mysdk.R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, mySDKTest.class);
                startActivity(intent);
            }
        });
```

这样就能跑起来测试了
 跑起来后，点击跳转按钮，会发现进入了sdk的页面，再点击按钮，会打出那句log。
 之后你可以根据自己的需求，继续开发，并测试。



![img](https:////upload-images.jianshu.io/upload_images/2048315-918eec4cdbe7d485.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000)

进入sdk页面，并打印log

## 3.5 管理依赖

### 3.5.1简单方式

我们假设，你已经完成了功能开发和测试，现在需要发布出去或者提交给用户，如何给到用户呢？
 我们打开mysdk的目录，在build-->outputs-->aar下面是有生成的AAR文件，你把这个文件拷贝一份给用户其实，也是可以的，大家搜一下导入AAR文件即可。



![img](https:////upload-images.jianshu.io/upload_images/2048315-40071c90719b05b6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/390)

生成文件

### 3.5.2 Maven + Github

Apache Maven是Apache开发的一个工具，提供了用于贡献library的文件服务器。
 通过Maven + Github的方式，我们可以更简单的发布，更便捷的做版本管理；用户可以更简单的导入。
 也是我们重点要讲的内容

#### 3.5.2.1配置打包gradle

在mysdk的目录下，新建一个名为maven-release-aar.gradle的文件，并在build.gradle中添加如下字段：

```
apply from: 'maven-release-kline-aar.gradle'
```



![img](https:////upload-images.jianshu.io/upload_images/2048315-74c3fef3af1834c0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000)

Paste_Image.png

#### 3.5.2.2配置maven-release-aar.gradle

可以这么理解，maven-release-aar.gradle就是我们用来设置打包的脚本，在文件中添加如下代码：

**注意看注释！！**

```
// 1.maven-插件
apply plugin: 'maven'

// 2.maven-信息
ext {// ext is a gradle closure allowing the declaration of global properties
    PUBLISH_GROUP_ID = 'com.flame'
    PUBLISH_ARTIFACT_ID = 'mySDK'
    PUBLISH_VERSION = android.defaultConfig.versionName
}

// 3.maven-输出路径
uploadArchives {
    repositories.mavenDeployer {
        //这里就是最后输出地址，在自己电脑上新建个文件夹，把文件夹路径粘贴在此
        //注意”file://“ + 路径，有三个斜杠，别漏了
        repository(url: "file:///Users/flame/Documents/sourceTree/mysdk")
        
        pom.project {
            groupId project.PUBLISH_GROUP_ID
            artifactId project.PUBLISH_ARTIFACT_ID
            version project.PUBLISH_VERSION
        }
    }
}

//以下代码会生成jar包源文件，如果是不开源码，请不要输入这段
//aar包内包含注释
task androidSourcesJar(type: Jar) {
    classifier = 'sources'
    from android.sourceSets.main.java.sourceFiles
}

artifacts {
    archives androidSourcesJar
}
```

#### 3.5.2.3生成AAR文件

- 接下来就是生成最终的AAR文件了，在Android studio右侧有个gradle侧边栏，点击会有如下画面，选择mysdk，点击uploadArchives



![img](https:////upload-images.jianshu.io/upload_images/2048315-51bd15e1cb2a9873.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/614)

uploadArchives

- 最后build成功



![img](https:////upload-images.jianshu.io/upload_images/2048315-d470df154d423c0b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000)

build success

- 再去上面配置好的maven输出路径下看，会发现已经有生成文件了。
- 注意看，version是1.0，而且有jar包，解压jar包就会得到全部源文件
- 如果在上一步不开源，注释掉生成jar包的代码，这里就不会有jar包



![img](https:////upload-images.jianshu.io/upload_images/2048315-21038a42a9bae9d8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000)

生成路径

#### 3.5.2.4上传至github

- 将整个文件夹上传至Github，注意，要上传的是完整的路径
- 在github中新建organization，create new-new organization.
- 此处需要注意，create new有两个选项，new repository和new organization，此处务必要选择organization，虽然区别不大，但是假如选择repository的话不能够作为私有仓库导入到新项目（报Failed to resolve错误）

#### 3.5.2.5 管理版本

刚刚提到之所以用maven的一个原因就是版本控制，这里我们就演示一下，所谓的版本控制

- 打开mysdk的build.gradle，修改defaultConfig下的versionName 为"1.1"



![img](https:////upload-images.jianshu.io/upload_images/2048315-2f0c736ef8837f23.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000)

修改版本

- 再来一次#3.5.2.3 - uploadArchives，build成功后再看目录，已经生成了新的版本1.1
- 再上传至Github
- 这样我们就有了清晰的版本控制



![img](https:////upload-images.jianshu.io/upload_images/2048315-1c806b857500436c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/779)

新的版本

## 3.6 如何引用

在之前的章节中，我们已经生成了对应代码，那么如何引用呢？

首先我们先要在app的build.gradle里面移除之前的依赖方法

```
//注释掉这段，不需要了
//compile project(':mysdk')
```

### 3.6.1 本地模式

代码就在我们本地，我们当然可以就近引用咯

我们在app的build.gradle中加入如下代码，这里已经省略了无关代码

```
 repositories {

    jcenter()

    //略

    //指定绝对路径
    maven { url "file:///Users/flame/Documents/sourceTree/mysdk" }
}

dependencies {
    //略

    //mysdk，这里可以指定版本，我们有1.0,1.1两个版本可选
    compile('com.flame:mySDK:1.1')
}
```

### 3.6.2 网络模式

上面提到的方法，当然是少数，毕竟我们大多数都是在网络导入依赖库，这里就是需要用到之前上传至Github的代码了。

只需把路径指向Github即可

```
 repositories {

    jcenter()

    //略

    //指定Github路径
    maven { url "https://github.com/flameandroid/mysdk/raw/master" }
}

dependencies {
    //略
å
    //mysdk，这里可以指定版本，我们有1.0,1.1两个版本可选
    compile('com.flame:mySDK:1.1')
}
```

## 3.7 混淆

我们打开External Libraries，会发现mySDK已经导入工程，而且还是完全开源的。

而很多时候我们是不需要开源的，那么如何做到呢？其实和普通的Android打包混淆一模一样

把混淆过的代码上传至Github，这样我们就完成了SDK的制作和发布

```
        release {
            // 不显示Log
            buildConfigField "boolean", "LOG_DEBUG", "false"
            //混淆
            minifyEnabled true
            //Zipalign优化
            zipAlignEnabled true

            // 移除无用的resource文件
            shrinkResources true
            //前一部分代表系统默认的android程序的混淆文件，该文件已经包含了基本的混淆声明，后一个文件是自己的定义混淆文件
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
}
```

# 4 注意事项

- 注意路径一定要写对，特别是大小写，路径的大小写不对是找不到文件的，这虽然是低级错误，但其实挺常见的
- 注意Github路径不是常见的[https://github.com/flameandroid/mysdk.git](https://link.jianshu.com?t=https://github.com/flameandroid/mysdk.git)，
   而是[https://github.com/flameandroid/mysdk/raw/master](https://link.jianshu.com?t=https://github.com/flameandroid/mysdk/raw/master) 
- 注意如果选择了混淆，切勿在打包那步生成了jar，还上传到Github了，否则白混淆了

作者：点燃火焰

链接：https://www.jianshu.com/p/6c1d2688ed2d

來源：简书

简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。