# 1. okhttp 下载不下来

```java
// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    
    repositories {
        mavenCentral()
        jcenter()
        google()

    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.1.2'
        

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        google()
        maven { url 'https://maven.google.com' }
        maven {
            url 'https://oss.sonatype.org/content/repositories/snapshots'
        }
        maven { url "https://jitpack.io" }
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```

# 2. lambda 支持

```java
apply plugin: 'com.android.application'

android {
    compileSdkVersion 28
    defaultConfig {
        applicationId "com.myntai.slightech.testservecdownloadrompackage"
        minSdkVersion 14
        targetSdkVersion 28
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        targetCompatibility 1.8 #添加这个
        sourceCompatibility 1.8 #添加这个
    }
}

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.android.support:appcompat-v7:28.0.0'
    implementation 'com.android.support.constraint:constraint-layout:1.1.3'
    testImplementation 'junit:junit:4.12'
    androidTestImplementation 'com.android.support.test:runner:1.0.2'
    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.2'
    implementation 'com.squareup.okhttp3:okhttp:3.8.1'
    compile 'io.reactivex.rxjava2:rxandroid:2.0.1'  #添加这个 有关rxandroid
    compile 'io.reactivex.rxjava2:rxjava:2.0.7'  #添加这个 有关rxjava
}

```

