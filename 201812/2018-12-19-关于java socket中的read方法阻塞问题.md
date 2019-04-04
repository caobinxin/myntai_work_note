# 关于java socket中的read方法阻塞问题

前几天一个有个同学咨询我关于java socket编程的一些问题，因为我这个同学今年刚从.NET转到java 对于java的IO体系不是很清楚,在给他解答一些问题时我自己也总结了比较容易出错的问题。

  我们直接贴一段socket代码看一下

```java
`客户端：``public` `class` `SocketClient {``    ` `    ``public` `static` `void` `main(String[] args) ``throws` `UnknownHostException, IOException, InterruptedException {``        ``Socket client = ``new` `Socket(``"localhost"``,``8888``);``        ``OutputStream out = client.getOutputStream();``        ``InputStream input = client.getInputStream();` `        ``out.write(``"sender say hello socket"``.getBytes());``        ``out.flush();``        ` `        ``read(input);``        ``out.close();``    ``}``    ``public` `static` `void` `read(InputStream input) ``throws` `IOException {``         ``byte``[] buf = ``new` `byte``[``128``];``         ``int` `size = ``0``;``         ``while` `((size = input.read(buf,``0``,buf.length)) != -``1``) {``             ``System.out.print(``new` `String(buf));``         ``}``    ``}``}`  `服务端：``public` `class` `SocketServer {``    ``public` `static` `void` `main(String[] args) {``        ``SocketServer ss = ``new` `SocketServer();``        ``int` `port = ``8888``;``        ``try` `{``            ``ss.startServer(port);``        ``} ``catch` `(Exception e) {``            ``e.printStackTrace();``        ``}``    ``}``    ``public` `void` `startServer(``int` `port) ``throws` `Exception {``        ``ServerSocket serverSocket = ``new` `ServerSocket(port);``        ``Socket server = ``null``;``        ``try` `{``            ``while` `(``true``) {``                ``server = serverSocket.accept();``                ``System.out.println(``"server socket is start……"``);``                ``try` `{``                    ``BufferedReader input = ``new` `BufferedReader(``new` `InputStreamReader(``new` `ByteArrayInputStream(``"服务端发给客户端的信息"``.getBytes())));``                    ``BufferedInputStream in = ``new` `BufferedInputStream(server.getInputStream());``                    ``PrintWriter out = newPrintWriter(newOutputStreamWriter(server.getOutputStream()));``                    ` `                    ``String clientstring = ``null``;``                    ``out.println(``"欢迎客户端连入"``);``                    ``out.flush();``                    ``byte``[] buf = ``new` `byte``[``128``];``                    ``int` `size = ``0``;``                    ``while` `(( size = in.read(buf,``0``,buf.length)) != -``1``) {``                        ``System.out.println(``new` `String(buf));``                    ``}``                    ``out.print(``" client is over"``);``                    ``out.flush();``                    ``out.close();``                    ``System.out.println(``" client is over"``);``                ``} ``catch` `(Exception e) {``                    ``e.printStackTrace();``                ``} ``finally` `{``                    ``server.close();``                ``}``            ``}``        ``} ``catch` `(Exception e) {``            ``e.printStackTrace();``        ``} ``finally` `{``            ``serverSocket.close();``        ``}``    ``}``    ` `   ` `}`
```

　　

   这段代码执行以后会发现server类 read()方法发生了阻塞，经过查找资料发现 read() 是一个阻塞函数，如果客户端没有声明断开outputStream那么它就会认为客户端仍旧可能发送数据，像read()这种阻塞读取函数还有BufferedReader类种的 readLine()、DataInputStream种的readUTF()等。

  下面我们来看下一些解决方案

   1）Socket任意一端在调用完write()方法时调用shutdownOutput()方法关闭输出流，这样对端的inputStream上的read操作就会返回-1， 这里我们要注意下不能调用socket.getInputStream().close()。因为它会导致socket直接被关闭。 当然如果不需要继续在socket上进行读操作，也可以直接关闭socket。但是这个方法不能用于通信双方需要多次交互的情况。

```java
`out.write(``"sender say hello socket"``.getBytes());``out.flush();``client.shutdownOutput();  ``//调用shutdown 通知对端请求完毕`
```

　　

 

 这个解决方案缺点非常明显，socket任意一端都依赖于对方调用shutdownOutput()来完成read返回 -1，如果任意一方没有执行shutdown函数那么就会出现问题。所以一般我们都会在socket请求时设置连接的超时时间 socket.setSoTimeout(5000);以防止长时间没有响应造成系统瘫痪。

```java
`while` `(``true``) {``     ``server = serverSocket.accept();``     ``System.out.println(``"server socket is start……"``);``     ``server.setSoTimeout(``5000``);``     ``.....`` ``}`
```

　　

2）发送数据时，约定数据的首部固定字节数为数据长度。这样读取到这个长度的数据后，就不继续调用read方法

 这种方式优点是不依赖对方调用shutdown函数，响应较快，缺点是数据传输是最大字节数固定，需双方事先约定好长度，伸缩性差。

 

3）发送数据时，约定前几位返回数据byte[]长度大小或最后输出 \n 或 \r 作为数据传输终止符。

```java
`客户端` `        ``out.write(``"sender say hello socket \n"``.getBytes());``    ``out.flush();` `服务器端` `        ``byte``[] buf = ``new` `byte``[``1``];``        ``int` `size = ``0``;``        ``StringBuffer sb = ``new` `StringBuffer();``        ``while` `(( size = in.read(buf,``0``,buf.length)) != -``1``) {``            ``String str = ``new` `String(buf);``            ``if``(str.equals(``"\n"``)) {``                ``break``;``            ``}``            ``sb.append(str); ``            ``System.out.print(str);``        ``}`
```


 这种方式是对第二种方案的改良版，但不得不说 这是目前socket数据传输的最常用处理read()阻塞的解决方案。