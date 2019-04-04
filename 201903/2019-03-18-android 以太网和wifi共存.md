# android 以太网和wifi共存

要解决这个问题，首先必须解决一个网络起来后另一个网络被踢掉的问题，在网络的核心类ConnectivityService.java找到了调用


把nai.asyncChannel.disconnect()函数注释后， 另一个网络不会被踢掉
两个网络可以共存，通过命令dumpsys connectivity，可以看到当前正在使用的网络


 通过命令 ip ru 查看当前的路由表,

也可以通过netcfg来查看，通过这个方法有时不准确，最好不要使用


可以看到 wlan0 和以太网是同时存在的，功能已经基本上实现
但是这样存在问题,当wifi先打开的时候，以太网不能连接，通过跟代码发现。EthernetNetworkFactory.java是核心类，这里不会被调用


startNetwork()，在NetworkFactory.java中找到了这个方法的实现



想要调用startNetwork()，就必须调用needNetworkFor(NetworkRequest networkRequest, int score)


关键函数evalRequest，这里有if的的判断，也就是没调用成功就是没用进第一个if的判断，这里把括号里面所有的判断全都都打印出来，发现第二个判断 n.score < mScore 为false,当先连接wifi后 n.score =100，mScore的分数为-1，现在只要找到是哪里把 mScore 设置为 -1，这个问题就可以解决。最终发现是当以太网连接一次后，这个值就被设置为-1，所以第一次开机后先连接wifi,再连接以太网是没有问题的，但是之后只要是先连接wifi,以太网的不能连接的。



LocalNetworkFactory mFactory；LocalNetworkFactory实际上还是继承NetworkFactory， mFactory.setScoreFilter(110)的实现是在NetworkFactory类，把原来的值-1改成110。
这个问题得以解决。


下面就是测试的问题了
通过命令 ip ru flush ，清空所有的路由表

然后通过命令
添加指向eth0的路由规则、指向wlan0的路由规则
ip ru add from 192.168.123.0/24 lookup eth0 把内网设置成以太网
ip ru add from all lookup wlan0 其他的都设置为wlan0


这样就可以切换以太网和wifi,ping 以太网和wifi都能通
--------------------- 