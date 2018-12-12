# android 观察者/被观察者模式 observer/observable

知识点：

1、Android观察者模式的简介；

2、Observer和Observable的使用实例；

3、(abstract)抽象类和抽象方法的使用；

4、新名词记录

{

abstract：抽象关键词

}


最近一直在看着观察者模式的rxjava/rxandroid，但是我却忽略了在Java中，也是有这个观察者的。这也是我在一个项目中偶然看到的一个名词，然后我就去搜索着来看了，不看不知道，一看吓一跳，原来这里头有这么深的水啊。只怪自己阅历不够了。


观察者模式：简单来说，就是当对象A对对象B进行进行了类似“订阅”关系，当对象B的数据发生改变时，就要通知对象A进行相应。很简单也很好理解。Android中的观察者需要实现Observer接口，当数据发生改变时，观察者的update()方法就会被调用，被观察者继承Observable类，在数据发生改变时，需要调用**setChanged();** **this.notifyObservers(obj);**这两个方法才可以通知观察者：你想要知道的数据发生了变化了。


在学习这个观察者模式的时候，脑袋突然开窍了，顺便加固了一把抽象类/抽象方法的使用，之前看了N多的关于abstract关键字的学习，但是苦于一直没有能够真正的理解abstract关键的实际使用用途，只停留在了知识层面，木有应用。


今天弄这个观察者模式的时候，想着我要把数据如何在单元测试中打印出来的问题时，我写了一个abstract抽象方法，然后然后然后我就懂了这个要怎么用了。实在可喜可贺啊！！！


好了，废话不多说了，下面直接上代码，我都在代码里头做了注释了，各位看官请直接看下面的代码。

首先定义一个继承Observable的被观察者LoginOutObservable.java抽象类。

    package com.yaojt.sdk.java.observer;
     
    import java.util.Observable;
     
    /**
     * desc:被观察者类，被观察者如果数据发生变化，则需要通知观察者，数据发生了改变，需要做相应的操作
     * <p>
     * author:kuyu.yaojt (tanksu)
     * <p>
     * email:yaojt@kuyumall.com
     * <p>
     * blog:http://blog.csdn.net/qq_16628781
     * <p>
     * date:17/3/16
     */
     
    public abstract class LoginOutObservable extends Observable {
        private String userName = "admin";
     
        /**
         * 设置是否登出，如果是，则需要通知观察者
         *
         * @param isLoginOut isLoginOut
         */
        public void setLoginOut(boolean isLoginOut) {
            if (isLoginOut) {
                String data = "被观察者告诉你：你已经退出登录，请重新登录！";
                /* 注意注意注意：下面这两个方法是要同时调用的，否则不会通知观察者数据发生了变化 */
                setChanged();
                this.notifyObservers(data);
                onUserLoginOut("这是在被观察者内部调用的方法：" + data);
            }
        }
     
        /**
         * 设置用户名，如果用户名和上次的不一样，则需要通知观察者
         *
         * @param name name
         */
        public void setUserName(String name) {
            if (!name.equals(userName)) {
                this.userName = name;
                String data = "被观察者告诉你：用户名已经改变了！新的名字为：" + name;
                /* 注意注意注意：下面这两个方法是要同时调用的，否则不会通知观察者数据发生了变化 */
                setChanged();
                this.notifyObservers(data);
                onUserNameChanged("这是在被观察者内部调用的方法：" + data);
            }
        }
     
        /**
         * 当用户名字发生改变时，用于在本类中将数据打印出来
         * <p>
         * 感悟：
         * 抽象方法，凡是继承此类的子类，都需要重写此方法
         * 但是我们可以再本类里调用此方法，然后实例化此类的时候，要求实现类实现本类里头的抽象方法
         *
         * @param newUserName
         */
        public abstract void onUserNameChanged(String newUserName);
     
        /**
         * 当用户登出时，在本类中用于将数据打印出来
         * <p>
         * 感悟：
         * 抽象方法，凡是继承此类的子类，都需要重写此方法
         * 但是我们可以再本类里调用此方法，然后实例化此类的时候，要求实现类实现本类里头的抽象方法
         *
         * @param message message
         */
        public abstract void onUserLoginOut(String message);
    }

然后再定义一个继承Observer的观察者LoginoutObserver.java。

    package com.yaojt.sdk.java.observer;
     
    import java.util.Observable;
    import java.util.Observer;
     
    /**
     * desc:
     * <p>
     * author:kuyu.yaojt (tanksu)
     * <p>
     * email:yaojt@kuyumall.com
     * <p>
     * blog:http://blog.csdn.net/qq_16628781
     * <p>
     * date:17/3/16
     */
     
    public abstract class LoginoutObserver implements Observer {
     
        @Override
        public void update(Observable o, Object arg) {
            /* 当被观察者因为数据发生了改变，并通知了相关的观察者后，观察者将会调用此方法进行相应
            * 我们这里调用的是本地的抽象方法进行数据输出 */
            onDataChanged("观察者从被观察者中的消息：", arg);
        }
     
        /**
         * 这里给出一个抽象方法给实现类实现，然后我们会在updat()方法被调用的时候，调用这个方法，给实现类相应
         *
         * @param message message
         * @param object  object
         */
        public abstract void onDataChanged(String message, Object object);
    }

最后我们就可以在单元测试里头进行测试了，如果又不熟悉IDE上面如何进行单元测试的，请看这篇文章点击打开链接，我就不细讲了。

    @Test
        public void observerTest() {
            /* 被观察者：登出/修改用户名类，实例 */
            LoginOutObservable loginOutObservable = new LoginOutObservable() {
                @Override
                public void onUserNameChanged(String newUserName) {
                    printer(newUserName);
                }
     
                @Override
                public void onUserLoginOut(String message) {
                    printer(message);
                }
            };
            /*观察者1：并实现了观察者类的抽象方法，实现在这里进行数据的打印输出 */
            LoginoutObserver loginoutObserver1 = new LoginoutObserver() {
                @Override
                public void onDataChanged(String message, Object object) {
                    printer("我是观察者1-->" + message + "，" + object.toString());
                }
            };
            /*观察者2：并实现了观察者类的抽象方法，实现在这里进行数据的打印输出 */
            LoginoutObserver loginoutObserver2 = new LoginoutObserver() {
                @Override
                public void onDataChanged(String message, Object object) {
                    printer("我是观察者2-->message：" + message + "，" + object.toString());
                }
            };
            LoginoutObserver loginoutObserver3 = new LoginoutObserver() {
                @Override
                public void onDataChanged(String message, Object object) {
                    printer("我是观察者3-->message：" + message + "，" + object.toString());
                }
            };
            /**
             * 这里像是被观察者订阅了观察者，看起来是有点别扭的哈；
             * 就像和反射里面调用invoke方法：method.invoke(obj, args);
             * 具体可以参考这篇文章：
             * 但是不要被混淆了，最好记的一点就是：外国人说话看起来都是和中文"反过来"的
             */
            loginOutObservable.addObserver(loginoutObserver1);
            loginOutObservable.addObserver(loginoutObserver3);
            loginOutObservable.addObserver(loginoutObserver2);
     
            /* 这里是模拟被观察者登出操作，登出了必须要通知用户重新登录啊 */
            loginOutObservable.setLoginOut(true);
            /* 这里是模拟被观察者修改了用户名操作，导致前后用户名不一致而引起数据变化了 */
            loginOutObservable.setUserName("tanksu");
        }
     
    public void printer(String content) {
            System.out.println(content);
        }

运行的结果如下：



如果你够细心的话，你会发现一个问题：我们观察者new的时候，是按着1->2->3的顺序来的，但是怎么数据改变时，调用的观察者方法却是反过来的呢，变成了2->3->1。猛地一看，发现这是我进行“订阅”被观察者的倒序呢。这里我们可以得到一个结论：在数据发生改变时，被观察者通知观察者的循序是遵循“先订阅后通知”的循序。这里要注意一下。


以上就是我的个人简介。

如果有任何疑问，请及时与我联系。谢谢。
--------------------- 
作者：姚镜堂 
来源：CSDN 
原文：https://blog.csdn.net/qq_16628781/article/details/62446146 
版权声明：本文为博主原创文章，转载请附上博文链接！