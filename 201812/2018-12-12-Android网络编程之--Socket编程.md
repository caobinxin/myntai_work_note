# Android网络编程之--Socket编程

# 引言

Android网络编程一直都是我想记录的一篇文章，由于种种原因，一直推迟，终于在在今天开始写了。这是一个好的开始，O(∩_∩)O哈哈~。
 网络上有很多关于Android网络编程的文章，我感觉没有一个适当的总结合适我的。所以，今天我决定将Android网络编程的系列文章做一个总结，在这里与大家分享。
 这几篇系列文章总的分为两大模块：Socket编程与HTTP编程（关于在Android中的）。今天我们先来看看通过Socket编程实现的服务器与客户端（我们这里是手机端）之间的通信。

**这篇文章你能学到什么？**

- 了解网络通信的基本原理
- 学会最基础的Socket通信原理（万丈高楼平地起）
- 明白TCP协议与UDP协议的区别与适用场景

# 网络编程基础

## TCP/IP协议

我们先看看从宏观上来看两台机器是如何通信的。
 我们通过QQ和服务器进行通信，都需要哪些东西呢？
 两台主机进行通信，需要知道双方电脑的的地址（也就是IP地址）；知道两个电脑的地址之后，我们还需要知道我发送到目的电脑的目的软件（使用端口标记）。这样两台电脑连接成功之后就可以进行通信了。
 那么这些东西例如：目的地如何规定，发送的数据如何包装，放到哪里？这中间就需要有各种协议。大家都使用这个协议，统一成一个规范，这样符合这个规范的各种设备之间能够进行兼容性的通信。
 最为广泛的的协议就是OSI协议和TCP/IP协议了，但是OSI协议较为繁琐，未推广（想了解的自己Google）。反而TCP/IP(transfer control protocol/internet protocol,传输控制协议/网际协议)协议简单明了，得到现今的广泛使用。
 TCP/IP准确的说是一组协议，是很多协议的集合，是一组协议集合的简称。来看看：

| 名称       | 协议                    | 功能                 |      |
| ---------- | ----------------------- | -------------------- | ---- |
| 应用层     | HTTP、Telnet、FTP、TFTP | 提供应用程序网络接口 |      |
| 传输层     | TCP、UDP                | 建立端到端的连接     |      |
| 网络层     | IP                      | 寻址和路由           |      |
| 数据链路层 | Ethernet、802.3、PPP    | 物理介质访问         |      |
| 物理层     | 接口和电缆              | 二进制数据流传输     |      |

下面以QQ的数据传输为例子：



![img](https:////upload-images.jianshu.io/upload_images/3884536-c42b9f452a2f5741.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/854)

QQ的数据传输

## IP地址、端口

在上节中我们知道端到端的连接提到了几个关键的字眼：IP地址、端口；
 IP地址用来标记唯一的计算机位置，端口号用来标记一台电脑中的不同应用程序。
 其中IP地址是32为二进制，例如：192.168.0.0.1等等，这个组合方式是一种协议拼起来的，详情Google。
 端口号范围是065536，其中01023是系统专用，例如：

| 协议名称                                       | 协议功能           | 默认端口号 |
| ---------------------------------------------- | ------------------ | ---------- |
| HTTP(HypertextTransfer Protocol)超文本传输协议 | 浏览网页           | 80         |
| FTP(File TransferProtocol) 文件传输协议        | 用于网络上传输文件 | 21         |
| TELNET                                         | 远程终端访问       | 23         |
| POP3(Post OfficeProtocol)                      | 邮局协议版本       | 110        |

IP地址和端口号组成了我们的Socket，也就是“套接字”,Socket只是一个API。
 Socket原理机制：
 通信的两端都有Socket
 网络通信其实就是Socket间的通信
 数据在两个Socket间通过IO传输

单独的Socke是没用任何作用的,基于一定的协议（比如：TCP、UDP协议）下的socket编程才能使得数据畅通传输，下面我们就开始吧。

# 基于TCP（传输控制协议）协议的Socket编程

> 以下将“基于TCP（传输控制协议）协议的Socket编程”简称为TCP编程

既然基于TCP，那么就有着它的一套代码逻辑体系。我们只需要在Socket API的帮助下，使用TCP协议，就可以进行一个完整的TCP编程了。

主要API：
 **Socket，客户端相关**

- 构造方法
   `public Socket(String host, int port) throws UnknownHostException, IOException`
   释义：创建一个流套接字并将其连接到指定主机上的指定端口号（就是用来连接到host主机的port端口的）
- 方法

|方法名称 | 方法功能|
 | ------------- :|-------------:|
 |getInputStream()) | 拿到此套接字的输入流，收到的数据就在这里 |
 |getOutputStream()| 返回此套接字的输出流。 要发送的数据放到这里|

**ServerSocket,服务器相关**

- 构造方法
   `ServerSocket(int port)`
   释义：创建服务端的监听port端口的套接字
- 方法
   `Socket accept() throws IOException`侦听并接受到此套接字的连接。此方法在连接传入之前一直阻塞。服务端通过这个方法拿到与客户端建立端到端的连接的socket。

总体流程图示：



![img](https:////upload-images.jianshu.io/upload_images/3884536-222af7aa4f5be86b.PNG?imageMogr2/auto-orient/strip%7CimageView2/2/w/869)

Socket通信流程

- TCP编程的服务端流程：
   1.创建ServerSocket类对象-serverSocket
   2.使用serverSocket开始一直阻塞监听，等待客户端发送来数据并且得到socket
   3.根据socket的输入流读取客户端的数据，根据socket的输出流返回给客户端数据
   4.关闭socket以及IO流
- TCP编程的客户端对象
   1.创建客户端的socket对象
   2.使用客户端的socket对象的输出流发送给服务器数据，使用客户端的socket对象的输入流得到服务端的数据

### TCP编程

下面我们使用上面的TCP编程的流程来实现：手机发送信息到服务器，服务器返回给我们数据。

> 服务端的话，这里使用eclipse。使用Eclipse新建一个Server.java来处理服务器端的逻辑。客户端的话使用AS来新建一个Client.java文件。然后运行服务器，在运行手机上的程序，从手机上发送一段内容到服务器端接收。大概就是这里流程。



![img](https:////upload-images.jianshu.io/upload_images/3884536-3bbb0ecc2ddb9e08.gif?imageMogr2/auto-orient/strip%7CimageView2/2/w/452)

手机发送信息到服务器，服务器返回给我们数据

**服务器端：**



![img](https:////upload-images.jianshu.io/upload_images/3884536-adeefc585d807024.PNG?imageMogr2/auto-orient/strip%7CimageView2/2/w/774)

服务器端新建TcpSocketDemo工程

Code：

```java
package com.hui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {
     
        try {
            // 为了看流程，我就把所有的代码都放在main函数里了,也没有捕捉异常，直接抛出去了。实际开发中不可取。
            // 1.新建ServerSocket对象，创建指定端口的连接
            ServerSocket serverSocket = new ServerSocket(12306);
            System.out.println("服务端监听开始了~~~~");
            // 2.进行监听
            Socket socket = serverSocket.accept();// 开始监听9999端口，并接收到此套接字的连接。
            // 3.拿到输入流（客户端发送的信息就在这里）
            InputStream is = socket.getInputStream();
            // 4.解析数据
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader bufReader = new BufferedReader(reader);
            String s = null;
            StringBuffer sb = new StringBuffer();
            while ((s = bufReader.readLine()) != null) {
                sb.append(s);
            }
            System.out.println("服务器：" + sb.toString());
            // 关闭输入流
            socket.shutdownInput();

            OutputStream os = socket.getOutputStream();
            os.write(("我是服务端,客户端发给我的数据就是："+sb.toString()).getBytes());
            os.flush();
            // 关闭输出流
            socket.shutdownOutput();
            os.close();

            // 关闭IO资源
            bufReader.close();
            reader.close();
            is.close();

            socket.close();// 关闭socket
            serverSocket.close();// 关闭ServerSocket

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

> **注意：**
>  在使用TCP编程的时候，最后需要释放资源，关闭socket(`socket.close()`)；关闭socket输入输出流（`socket.shutdownInput()以及socket.shutdownOutput()`）；关闭IO流(`is.close() os.close()`)。需要注意的是：关闭socket的输入输出流需要放在关闭io流之前。因为， <u>**关闭IO流会同时关闭socket，一旦关闭了socket的，就不能再进行socket的相关操作了。而，只关闭socket输入输出流（`socket.shutdownInput()以及socket.shutdownOutput()`）不会完全关闭socket，此时任然可以进行socket方面的操作。  **</u>所以要先调用socket.shutdownXXX，然后再调用io.close();

**客户端：**

> 页面文件没什么好看的。然后就是点击button的时候发送数据，收到数据展示出来。我们这里主要看点击按钮时做的事情。

```java
public void onClick(View view){
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    //1.创建监听指定服务器地址以及指定服务器监听的端口号
                    Socket socket = new Socket("111.111.11.11", 12306);//111.111.11.11为我这个本机的IP地址，端口号为12306.
                    //2.拿到客户端的socket对象的输出流发送给服务器数据
                    OutputStream os = socket.getOutputStream();
                    //写入要发送给服务器的数据
                    os.write(et.getText().toString().getBytes());
                    os.flush();
                    socket.shutdownOutput();
                    //拿到socket的输入流，这里存储的是服务器返回的数据
                    InputStream is = socket.getInputStream();
                    //解析服务器返回的数据
                    InputStreamReader reader = new InputStreamReader(is);
                    BufferedReader bufReader = new BufferedReader(reader);
                    String s = null;
                    final StringBuffer sb = new StringBuffer();
                    while((s = bufReader.readLine()) != null){
                        sb.append(s);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv.setText(sb.toString());
                        }
                    });
                    //3、关闭IO资源（注：实际开发中需要放到finally中）
                    bufReader.close();
                    reader.close();
                    is.close();
                    os.close();
                    socket.close();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }
```

> **注意！**
>  实际开发中的关闭IO资源需要放到finally中。这里主要是为了先理解TCP编程的socket通信。还有，上面讲过的`io.close()`需要放到`socket.showdownXX()`后面。
>  关于`new Socket("111.111.11.11", 12306)`，如何查看本机地址，自己百度哦~~~

整体运行结果如下：



![img](https:////upload-images.jianshu.io/upload_images/3884536-73c3bdfe687c7da4.gif?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000)

TCP的单线程编程

在上图中，我们手机端发送完一个请求后，服务端（Server）拿到数据，解析数据，返回给客户端数据，关闭所有资源，也就是服务器关闭了。这时，如果另一个客户端再想跟服务器进行通信时，发现服务器已经关闭了，无法与服务器再次进行通信。换句话说，*只能跟服务器通信一次，服务端 只能支持单线程数据处理。*也就是说，上面的服务器的代码无法实现多线程编程，只能进行一次通信。
 那么如果我们想实现server的多线程数据处理，使得server处理完我这个请求后不会关闭，任然可以处理其他客户端的请求，怎么办呢？

### TCP的多线程编程

**思路：**
 在上面例子中，我们执行`serversocket.accept()`等待客户端去连接，与客户建立完连接后，拿到对应的socket，然后进行相应的处理。那么多个客户端的请求，我们就一直不关闭ServerSocket，一直等待客户端连接，一旦建立连接拿到socket，就可以吧这个socket放到单独的线程中，从而实现这个建立连接的端到端通信的socket在自己单独的线程中处理。这样就能实现Socket的多线程处理。

- step1:
   创建ServerThread，单独处理拿到的socket，使得客户端到服务器端的这个socket会话在一个单独的线程中。

```java
package com.hui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class ServerThread extends Thread{

    private Socket socket;

    //在构造中得到要单独会话的socket
    public ServerThread(Socket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        super.run();
        InputStreamReader reader = null;
        BufferedReader bufReader = null;
        OutputStream os = null; 
        try {
            reader = new InputStreamReader(socket.getInputStream());
            bufReader = new BufferedReader(reader);
            String s = null;
            StringBuffer sb = new StringBuffer();
            while((s = bufReader.readLine()) != null){
                sb.append(s);
            }
            System.out.println("服务器："+sb.toString());
            //关闭输入流
            socket.shutdownInput();
            
            //返回给客户端数据
            os = socket.getOutputStream();
            os.write(("我是服务端,客户端发给我的数据就是："+sb.toString()).getBytes());
            os.flush();
            socket.shutdownOutput();
        } catch (IOException e2) {
            e2.printStackTrace();
        } finally{//关闭IO资源
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            if(bufReader != null){
                try {
                    bufReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
         
          
    }
    
}
```

- step2：
   创建MultiThreadServer

```java
package com.hui;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiThreadServer {

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(12306);
            //死循环
            while(true){
                System.out.println("MultiThreadServer~~~监听~~~");
                //accept方法会阻塞，直到有客户端与之建立连接
                Socket socket = serverSocket.accept();
                ServerThread serverThread = new ServerThread(socket);
                serverThread.start();
            }
            
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

}
```

下面我使用两个手机，多次进行与服务器的连接，演示如下：
 总体结果：



![img](https:////upload-images.jianshu.io/upload_images/3884536-70d6c8cf8358605f.gif?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000)

TCP 的多线程通信



![img](https:////upload-images.jianshu.io/upload_images/3884536-6522872ba54def04.gif?imageMogr2/auto-orient/strip%7CimageView2/2/w/1000)

单独看两个手机

> 重要的事情说三遍！万丈高楼平地起！万丈高楼平地起！！万丈高楼平地起！！！只有当我们明白了最底层的，知识才是最牢固的。上面的讲解的是基于TCP协议的socket编程。而我们后来将要讲的HTTP相关的大都是基于TCP/IP协议的。一个TCP/IP协议我们又不能直接使用，Socket可以说是TCP/IP协议的抽象与包装，然后我们就可以做相对于TCP/IP的网络通信与信息传输了。

# UDP编程

> 上面我们讲解了基于TCP协议的Socket编程，现在开始我们就开始讲解基于UDP协议的Socket编程了。
>  UDP，是User Datagram Protocol，也就是用户**数据包**协议。关键点在于“数据包”。主要就是把数据进行打包然后丢给目标，而不管目标是否接收到数据。主要的流程就是：<u>发送者打包数据（DatagramPacket）然后通过DatagramSocket发送，接收者收到数据包解开数据。</u>

主要API:
 **DatagramPacket**，用来包装发送的数据
 **构造方法**

- 发送数据的构造
   `DatagramPacket(byte[] buf, int length,SocketAddress address)`
   `DatagramPacket(byte[] buf, int length, InetAddress address, int port)`
   用来将长度为 length 的包发送到指定主机上的指定端口号。length 参数必须小于等于 buf.length。
- 接收数据的构造：
   `public DatagramPacket(byte[] buf, int length)`
   用来接收长度为 length 的数据包。

**DatagramSocket：**

**构造方法**
 DatagramSocket()
 构造数据报套接字并将其绑定到  <u>本地主机上任何可用的端口 </u>。套接字将被绑定到通配符地址，IP 地址由内核来选择。

DatagramSocket(int port)
 创建数据报套接字并将其绑定到<u>本地主机上的指定端口</u>。套接字将被绑定到通配符地址，IP 地址由内核来选择。

**发送数据**
 send(DatagramPacket p)
 从此套接字发送数据报包。DatagramPacket 包含的信息指示：将要发送的数据、其长度、远程主机的 IP 地址和远程主机的端口号。
 **接收数据**
 receive(DatagramPacket p)
 从此套接字接收数据报包。当此方法返回时，DatagramPacket 的缓冲区填充了接收的数据。数据报包也包含发送方的 IP 地址和发送方机器上的端口号。

> 下面开始代码了

## 客户端

主要页面与上面的tcp一致，只不过是通讯时的方法改了。如下：

```java
private void udp() {
        byte[] bytes = et.getText().toString().getBytes();
        try {
            /*******************发送数据***********************/
            InetAddress address = InetAddress.getByName("192.168.232.2");
            //1.构造数据包
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 12306);
            //2.创建数据报套接字并将其绑定到本地主机上的指定端口。
            DatagramSocket socket = new DatagramSocket();
            //3.从此套接字发送数据报包。
            socket.send(packet);
            /*******************接收数据***********************/
        //1.构造 DatagramPacket，用来接收长度为 length 的数据包。
            final byte[] bytes1 = new byte[1024];
            DatagramPacket receiverPacket = new DatagramPacket(bytes1, bytes1.length);
            socket.receive(receiverPacket);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv.setText(new String(bytes1, 0, bytes1.length));
                }
            });

//            socket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
```

## 服务端

UDPServer

```java
package com.hui;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class UDPServer {
    public static void main(String[] args) throws IOException {

        byte[] buf = new byte[1024];
        // 一：接受数据
        // 1.创建接受数据的数据包
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        // 2.创建UPD 的 socket
        DatagramSocket socket = new DatagramSocket(12306);
        // 3.接收数据
        System.out.println("服务端开始监听！~~~~");
        socket.receive(packet);
        // 4.处理数据
        System.out.println("服务端：" + new String(buf, 0, buf.length));

        // 二：返回数据
        DatagramPacket p = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
        socket.send(p);
        socket.close();
    }
}
```



![img](https:////upload-images.jianshu.io/upload_images/3884536-debeab997860c734.gif?imageMogr2/auto-orient/strip%7CimageView2/2/w/978)

UDP通信

# TCP与UDP区别与使用场景

> 至此，基于TCP、UDP协议的Socket通信已经讲完了基础部分。那么这两个协议在实际中有什么区别，分别适用于什么场景呢？

## TCP

对于TCP的数据传输而言，传输数据之前需要进行三次握手建立稳定的连接。建立连接通道后，数据包会在这个通道中以字节流的形式进行数据的传输。由于建立稳定连接后才开始传输数据，而同时还是以字节流的形式发送数据，所以发送数据速度较慢，但是不会造成数据包丢失。即使数据包丢失了，会进行数据重发。同时，如果收到的数据包顺序错乱，会进行排序纠正。

> 三次握手？？
>  这个网络上的解释太多了，想详细了解的自行去百度上Google一下。<u>简单理解</u>的就是这样的：我家是农村的，记得小时后爷爷在田里种地。到了晌午时间，奶奶快烧好饭后我都要去喊爷爷吃饭，因为干农活的地离家里不远不近的，我就跑到隔壁家里的平顶房上喊爷爷吃饭。我先大喊一声“爷爷，回家吃饭啦”。爷爷如果听到我说的话就会给我一个应答“好的！知道了，马上就回去，你们先吃吧！”我只有听到了这句话，才知道爷爷这个时候能听到我说的话，我然后就再次回答爷爷：“好的！那你快点！”这**三句话**说完，就确定了我能听到爷爷的应答，爷爷也能听到我的回复。这样我就确定我跟爷爷之间的喊话通道是正常的，如果还想对爷爷说什么话，直接说就好了。最后，爷爷听到了我说的话，就不再回复我的话了，然后，拿起锄头回来了。

总结下来，就是**面向连接、数据可靠，速度慢，有序的**。
 <u>适用于需要安全稳定传输数据的场景。例如后面要讲解的HTTP、HTTPS网络协议，FTP文件传输协议以及POP、SMTP邮件传输协议。或者开发交易类、支付类等软件时，都需要基于TCP协议的Socket连接进行安全可靠的数据传输等等</u>

## UDP

对于UDP的数据传输而言，UDP不会去建立连接。它不管目的地是否存在，直接将数据发送给目的地，同时不会过问发送的数据是否丢失，到达的数据是否顺序错乱。如果你想处理这些问题的话，需要自己在应用层自行处理。
 总结下来，**不面向连接、数据不可靠、速度快、无序的**。
 <u>适用于需要实时性较高不较为关注数据结果的场景，例如：打电话、视频会议、广播电台，等。</u>

作者：徐爱卿

链接：https://www.jianshu.com/p/fb4dfab4eec1

來源：简书

简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。