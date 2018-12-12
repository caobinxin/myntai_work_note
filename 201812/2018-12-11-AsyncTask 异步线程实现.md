# AsyncTask 异步线程实现

# 前言

- 多线程的应用在Android开发中是非常常见的，常用方法主要有：
  1. 继承Thread类
  2. 实现Runnable接口
  3. Handler
  4. AsyncTask
  5. HandlerThread
- 今天，我将献上一份`AsyncTask`使用教程，希望大家会喜欢

------

# 目录



![img](https:////upload-images.jianshu.io/upload_images/944365-17b1bb1095f4fdaa.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000)

示意图

------

# 1. 定义

- 一个`Android` 已封装好的轻量级异步类
- 属于抽象类，即使用时需 实现子类

```java
public abstract class AsyncTask<Params, Progress, Result> { 
 ... 
 }
```

------

# 2. 作用

1. 实现多线程
    在工作线程中执行任务，如 耗时任务
2. 异步通信、消息传递
    **实现工作线程 & 主线程（UI线程）之间的通信**，即：将工作线程的执行结果传递给主线程，从而在主线程中执行相关的`UI`操作

> 从而保证线程安全

------

# 3. 优点

- 方便实现异步通信
   不需使用 “任务线程（如继承`Thread`类） + `Handler`”的复杂组合
- 节省资源
   采用线程池的缓存线程 + 复用线程，避免了频繁创建 & 销毁线程所带来的系统资源开销

------

# 4. 类 & 方法介绍

### 4.1 类定义

`AsyncTask`类属于抽象类，即使用时需 实现子类

```java
public abstract class AsyncTask<Params, Progress, Result> { 
 ... 
}

// 类中参数为3种泛型类型
// 整体作用：控制AsyncTask子类执行线程任务时各个阶段的返回类型
// 具体说明：
    // a. Params：开始异步任务执行时传入的参数类型，对应excute（）中传递的参数
    // b. Progress：异步任务执行过程中，返回下载进度值的类型
    // c. Result：异步任务执行完成后，返回的结果类型，与doInBackground()的返回值类型保持一致
// 注：
    // a. 使用时并不是所有类型都被使用
    // b. 若无被使用，可用java.lang.Void类型代替
    // c. 若有不同业务，需额外再写1个AsyncTask的子类
}
```

### 4.2 核心方法

-  `AsyncTask` 核心 & 常用的方法如下：



![img](https:////upload-images.jianshu.io/upload_images/944365-153fb37764704129.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000)

示意图

- 方法执行顺序如下



![img](https:////upload-images.jianshu.io/upload_images/944365-31df794006c69621.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000)

示意图

------

# 5. 使用步骤

-  `AsyncTask`的使用步骤有4个：

1. 创建 `AsyncTask` 子类 & 根据需求实现核心方法
2. 创建 `AsyncTask`子类的实例对象（即 任务实例）
3. 手动调用`execute(（）`从而执行异步线程任务

- 具体介绍如下

```java
/**
  * 步骤1：创建AsyncTask子类
  * 注： 
  *   a. 继承AsyncTask类
  *   b. 为3个泛型参数指定类型；若不使用，可用java.lang.Void类型代替
  *   c. 根据需求，在AsyncTask子类内实现核心方法
  */

  private class MyTask extends AsyncTask<Params, Progress, Result> {

        ....

      // 方法1：onPreExecute（）
      // 作用：执行 线程任务前的操作
      // 注：根据需求复写
      @Override
      protected void onPreExecute() {
           ...
        }

      // 方法2：doInBackground（）
      // 作用：接收输入参数、执行任务中的耗时操作、返回 线程任务执行的结果
      // 注：必须复写，从而自定义线程任务
      @Override
      protected String doInBackground(String... params) {

            ...// 自定义的线程任务

            // 可调用publishProgress（）显示进度, 之后将执行onProgressUpdate（）
             publishProgress(count);
              
         }

      // 方法3：onProgressUpdate（）
      // 作用：在主线程 显示线程任务执行的进度
      // 注：根据需求复写
      @Override
      protected void onProgressUpdate(Integer... progresses) {
            ...

        }

      // 方法4：onPostExecute（）
      // 作用：接收线程任务执行结果、将执行结果显示到UI组件
      // 注：必须复写，从而自定义UI操作
      @Override
      protected void onPostExecute(String result) {

         ...// UI操作

        }

      // 方法5：onCancelled()
      // 作用：将异步任务设置为：取消状态
      @Override
        protected void onCancelled() {
        ...
        }
  }

/**
  * 步骤2：创建AsyncTask子类的实例对象（即 任务实例）
  * 注：AsyncTask子类的实例必须在UI线程中创建
  */
  MyTask mTask = new MyTask();

/**
  * 步骤3：手动调用execute(Params... params) 从而执行异步线程任务
  * 注：
  *    a. 必须在UI线程中调用
  *    b. 同一个AsyncTask实例对象只能执行1次，若执行第2次将会抛出异常
  *    c. 执行任务中，系统会自动调用AsyncTask的一系列方法：onPreExecute() 、doInBackground()、onProgressUpdate() 、onPostExecute() 
  *    d. 不能手动调用上述方法
  */
  mTask.execute()；
```

------

# 6. 实例讲解

下面，我将用1个实例讲解 具体如何使用 `AsyncTask`

### 6.1 实例说明

1. 点击按钮 则 开启线程执行线程任务
2. 显示后台加载进度
3. 加载完毕后更新UI组件
4. 期间若点击取消按钮，则取消加载

如下图



![img](https:////upload-images.jianshu.io/upload_images/944365-23bdf9a3bc62e825.gif?imageMogr2/auto-orient/strip%7CimageView2/2/w/380)

示意图

### 6.2 具体实现

> 建议先下载源码再看：[Carson_Ho的Github地址：AsyncTask](https://link.jianshu.com?t=https%3A%2F%2Fgithub.com%2FCarson-Ho%2FMultiThread_learning)

- 主布局文件：*activity_main.xml* 

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    tools:context="com.example.carson_ho.handler_learning.MainActivity">

    <Button
        android:layout_centerInParent="true"
        android:id="@+id/button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="点我加载"/>

    <TextView
        android:id="@+id/text"
        android:layout_below="@+id/button"
        android:layout_centerInParent="true"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="还没开始加载!" />

    <ProgressBar
        android:layout_below="@+id/text"
        android:id="@+id/progress_bar"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:progress="0"
        android:max="100"
        style="?android:attr/progressBarStyleHorizontal"/>

    <Button
        android:layout_below="@+id/progress_bar"
        android:layout_centerInParent="true"
        android:id="@+id/cancel"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="cancel"/>
</RelativeLayout>
```

- 主逻辑代码文件：*MainActivity.java* 

```java
public class MainActivity extends AppCompatActivity {

    // 线程变量
    MyTask mTask;

    // 主布局中的UI组件
    Button button,cancel; // 加载、取消按钮
    TextView text; // 更新的UI组件
    ProgressBar progressBar; // 进度条
    
    /**
     * 步骤1：创建AsyncTask子类
     * 注：
     *   a. 继承AsyncTask类
     *   b. 为3个泛型参数指定类型；若不使用，可用java.lang.Void类型代替
     *      此处指定为：输入参数 = String类型、执行进度 = Integer类型、执行结果 = String类型
     *   c. 根据需求，在AsyncTask子类内实现核心方法
     */
    private class MyTask extends AsyncTask<String, Integer, String> {

        // 方法1：onPreExecute（）
        // 作用：执行 线程任务前的操作
        @Override
        protected void onPreExecute() {
            text.setText("加载中");
            // 执行前显示提示
        }


        // 方法2：doInBackground（）
        // 作用：接收输入参数、执行任务中的耗时操作、返回 线程任务执行的结果
        // 此处通过计算从而模拟“加载进度”的情况
        @Override
        protected String doInBackground(String... params) {

            try {
                int count = 0;
                int length = 1;
                while (count<99) {

                    count += length;
                    // 可调用publishProgress（）显示进度, 之后将执行onProgressUpdate（）
                    publishProgress(count);
                    // 模拟耗时任务
                    Thread.sleep(50);
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        // 方法3：onProgressUpdate（）
        // 作用：在主线程 显示线程任务执行的进度
        @Override
        protected void onProgressUpdate(Integer... progresses) {

            progressBar.setProgress(progresses[0]);
            text.setText("loading..." + progresses[0] + "%");

        }

        // 方法4：onPostExecute（）
        // 作用：接收线程任务执行结果、将执行结果显示到UI组件
        @Override
        protected void onPostExecute(String result) {
            // 执行完毕后，则更新UI
            text.setText("加载完毕");
        }

        // 方法5：onCancelled()
        // 作用：将异步任务设置为：取消状态
        @Override
        protected void onCancelled() {

            text.setText("已取消");
            progressBar.setProgress(0);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 绑定UI组件
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        cancel = (Button) findViewById(R.id.cancel);
        text = (TextView) findViewById(R.id.text);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        /**
         * 步骤2：创建AsyncTask子类的实例对象（即 任务实例）
         * 注：AsyncTask子类的实例必须在UI线程中创建
         */
        mTask = new MyTask();

        // 加载按钮按按下时，则启动AsyncTask
        // 任务完成后更新TextView的文本
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * 步骤3：手动调用execute(Params... params) 从而执行异步线程任务
                 * 注：
                 *    a. 必须在UI线程中调用
                 *    b. 同一个AsyncTask实例对象只能执行1次，若执行第2次将会抛出异常
                 *    c. 执行任务中，系统会自动调用AsyncTask的一系列方法：onPreExecute() 、doInBackground()、onProgressUpdate() 、onPostExecute()
                 *    d. 不能手动调用上述方法
                 */
                mTask.execute();
            }
        });

        cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 取消一个正在执行的任务,onCancelled方法将会被调用
                mTask.cancel(true);
            }
        });

    }

}
```

- 运行结果



![img](https:////upload-images.jianshu.io/upload_images/944365-23bdf9a3bc62e825.gif?imageMogr2/auto-orient/strip%7CimageView2/2/w/380)

示意图

- 源码地址
   [Carson_Ho的Github地址：AsyncTask](https://link.jianshu.com?t=https%3A%2F%2Fgithub.com%2FCarson-Ho%2FMultiThread_learning) 

------

# 7. 使用时的注意点

在使用`AsyncTask`时有一些问题需要注意的：

### 7.1 关于 生命周期

- 结论
   `AsyncTask`不与任何组件绑定生命周期
- 使用建议
   在`Activity` 或 `Fragment`中使用 `AsyncTask`时，最好在`Activity` 或 `Fragment`的`onDestory（）`调用 `cancel(boolean)`；

### 7.2 关于 内存泄漏

- 结论
   若`AsyncTask`被声明为`Activity`的非静态内部类，当`Activity`需销毁时，会因`AsyncTask`保留对`Activity`的引用 而导致`Activity`无法被回收，最终引起内存泄露
- 使用建议
   `AsyncTask`应被声明为`Activity`的静态内部类

### 7.3 线程任务执行结果 丢失

- 结论
   当`Activity`重新创建时（屏幕旋转 / `Activity`被意外销毁时后恢复），之前运行的`AsyncTask`（非静态的内部类）持有的之前`Activity`引用已无效，故复写的`onPostExecute()`将不生效，即无法更新UI操作
- 使用建议
   在`Activity`恢复时的对应方法 重启 任务线程

------

# 8. 源码分析

- 知其然 而须知其所以然，了解 `AsyncTask` 的源码分析有利于更好地理解`AsyncTask`的工作原理
- 具体请看文章：[Android 多线程：AsyncTask的原理 及其源码分析](https://www.jianshu.com/p/37502bbbb25a) 

------

# 9. 总结

- 本文全面介绍了多线程中的`AsyncTask`，含使用方法、工作原理 & 源码分析
- 接下来，我会继续讲解`AsyncTask`的源码分析，有兴趣可以继续关注[Carson_Ho的安卓开发笔记](https://www.jianshu.com/users/383970bef0a0/latest_articles)

作者：Carson_Ho

链接：https://www.jianshu.com/p/ee1342fcf5e7

來源：简书

简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。