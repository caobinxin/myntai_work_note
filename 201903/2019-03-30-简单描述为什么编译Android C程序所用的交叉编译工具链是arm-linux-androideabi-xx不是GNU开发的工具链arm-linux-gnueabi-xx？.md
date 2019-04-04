# 简单描述为什么编译Android C程序所用的交叉编译工具链是arm-linux-androideabi-xx不是GNU开发的工具链arm-linux-gnueabi-xx？

如果下载了完整的Android项目的源代码，则可以在/prebuilt目录下找到交叉编译工具，比如Android所用的是arm-linux-androideabi-xx而非arm-linux-gnueabi-xx。Android并没有采用glibc作为C库，而是采用了Google自己开发的Bionic Libc；并以Skia取代Cairo，以OpenCORE取代FFmpeg，为的是让Android达到商用应用，必须移除被GNU  GPL授权证书所约束的部分。这使得其他Toolchain来用于Android要比较麻烦。

​      多数的Linux嵌入式玩家使用的Toolchain是GNU提供的交叉编译工具链，它用来编译和移植Android的Linux内核是可行的，因为内核并不需要C库，但是开发Android应用程序时，直接采用GNU工具链就有点水土不服。

​       之前，我在做一个加密TF卡移植到Android系统项目时并没有注意到这一点，在用GNU工具链编译的TF卡驱动和相关KO可以顺利执行；但在编译.so和C代码写的TF卡测试程序时我依然用的GNU的工具链，发现执行时出了莫名问题；后来改用了Android  NDK中提取出来的工具链再次编译.so和C代码写的TF卡测试程序，最终才可以顺利执行。

​     **与glibc相比，Bionic Libc有如下一些特点：**
​       1 采用BSD License，而不是glibc的GPL License; 
​       2 大小只有大约200k，比glibc差不多小一半，且比glibc更快; 
​       3 实现了一个更小、更快的pthread; 
​       4 提供了一些Android所需要的重要函数，如”getprop”，“LOGI”等; 
​       5 不完全支持POSIX标准，比如C++ exceptions，wide chars等; 
​       6 不提供libthread_db和libm的实现。

​      **另外，Android中所用的其他一些二进制工具也比较特殊：**  
​       1 加载动态库时使用的是/system/bin/linker，而不是常用的/lib/ld.so; 
​       2 prelink工具不是常用的prelink，而是 apriori，其源代码位于build/tools/apriori； 
​       3  strip工具也没有采用常用的strip，即prebuilt/linux-x86/toolchain/arm-eabi-4.2.1/bin目录下的arm-eabi-strip，而是位于out/host/linux-x86/bin/的soslim工具。





相关链接：

从NDK中单独提取ToolChain方法：http://www.blogs8.cn/posts/WLnyb20

参考：

《Android底层开发实战》周庆国、郑灵翔等 机械工业出版社