# Android 项目加载时　gradle　出问题

Caused by: org.gradle.api.resources.ResourceException: Failed to download SHA1 for resource 'https://jcenter.bintray.com/com/android/tools/ddms/ddmlib/26.1.2/ddmlib-26.1.2.jar'. 



解决方法：

```gradle
allprojects {
    repositories {
        mavenCentral()        //新加
        jcenter()
        mavenLocal()        //新加
        google()
    }
}
```

