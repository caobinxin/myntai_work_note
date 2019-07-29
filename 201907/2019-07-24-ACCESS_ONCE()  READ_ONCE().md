# ACCESS_ONCE() / READ_ONCE()

## 1. 资料以及说明：

https://blog.csdn.net/ganggexiongqi/article/details/24603363

在新版本的内核中　ACCESS_ONCE　不在使用，改用新的　READ_ONCE　接口。



如下是kernel 4.14.15 中的一段描述：

```shell
[`READ_ONCE()`]: https://lwn.net/Articles/624126/

If the filesystem may need to revalidate dcache entries, then 
`d_op->d_revalidate` may be called in RCU-walk too.  This interface
*is* passed the dentry but does not have access to the `inode` or the
`seq` number from the `nameidata`, so it needs to be extra careful
when accessing fields in the dentry.  This "extra care" typically
involves using `ACCESS_ONCE()` or the newer [`READ_ONCE()`] to access                                                                                                                                              
fields, and verifying the result is not NULL before using it.  This 
pattern can be see in `nfs_lookup_revalidate()`.
```

将将4.14.15中　的第三方驱动往　4.19.50中挪动的时候，　找不到ACCESS_ONCE，将其换做READ_ONCE即可编译通过。

### 1.1 kernel4.14.15中的源码

```c
#define __ACCESS_ONCE(x) ({ \
     __maybe_unused typeof(x) __var = (__force typeof(x)) 0; \
    (volatile typeof(x) *)&(x); })

#define ACCESS_ONCE(x) (*__ACCESS_ONCE(x))
```



## 2. ACCESS_ONCE讲解

如果你看过 Linux 内核中的 RCU 的实现，你应该注意到了这个叫做 ACCESS_ONCE() 宏，但是并没有很多人真正理解它的含义。网上[有的地方](http://blog.163.com/seven_7_one/blog/static/1626064122011513103514672/)甚至对此有错误的解释，所以特写此文来澄清一下。

虽然我早在读 perfbook 之前就了解了 ACCESS_ONCE() 的含义（通过询问大牛 Paul），但这本书中正好也没有很详细地介绍这个宏，所以就当是此书的读书笔记了。

**定义**

它的定义很简单，在 include/linux/compiler.h 的底部：

[PLAIN TEXT](http://wangcong.org/blog/archives/1941#)

C:

1.  \#define ACCESS_ONCE(x) (*(volatile typeof(x) *)&(x))

仅从语法上讲，这似乎毫无意义，先取其地址，在通过指针取其值。而实际上不然，多了一个关键词 volatile，所以它的含义就是强制编译器每次使用 x 都从内存中获取。

**原因**
 仅仅从定义来看基本上看不大出来为什么要引入这么一个东西。可以通过几个例子（均来自 Paul，我做了小的修改）看一下。

\1. 循环中有每次都要读取的全局变量：

[PLAIN TEXT](http://wangcong.org/blog/archives/1941#)

C:

1.  ...
2.  static int should_continue;
3.  static void do_something(void);
4.  ...
5. ​                while  (should_continue)
6. ​                        do_something();

假设 do_something() 函数中并没有对变量 should_continue 做任何修改，那么，编译器完全有可能把它优化成：

[PLAIN TEXT](http://wangcong.org/blog/archives/1941#)

C:

1.  ...
2. ​                if  (should_continue)
3. ​                        for  (;;)
4. ​                                do_something();

这很好理解，不是吗？对于单线程的程序，这么做完全没问题，可是对于多线程，问题就出来了：如果这个线程在执行do_something()  的期间，另外一个线程改变了 should_continue  的值，那么上面的优化就是完全错误的了！更严重的问题是，编译器根本就没有办法知道这段代码是不是并发的，也就无从决定进行的优化是不是正确的！

这里有两种解决办法：1) 给 should_continue 加锁，毕竟多个进程访问和修改全局变量需要锁是很自然的；2)  禁止编译器做此优化。加锁的方法有些过了，毕竟 should_continue 只是一个布尔，而且退一步讲，就算每次读到的值不是最新的  should_continue 的值也可能是无所谓的，大不了多循环几次，所以禁止编译器做优化是一个更简单也更容易的解决办法。我们使用  ACCESS_ONCE() 来访问 should_continue：

[PLAIN TEXT](http://wangcong.org/blog/archives/1941#)

C:

1.  ...
2. ​      while (ACCESS_ONCE(should_continue))
3. ​                        do_something();

\2. 指针读取一次，但要dereference多次：

[PLAIN TEXT](http://wangcong.org/blog/archives/1941#)

C:

1.  ...
2. ​     p = global_ptr;
3. ​     if (p  && p->s && p->s->func)
4. ​         p->s->func();

那么编译器也有可能把它编译成：

[PLAIN TEXT](http://wangcong.org/blog/archives/1941#)

C:

1.  ...
2. ​     if (global_ptr && global_ptr->s && global_ptr->s->func)
3. ​         global_ptr->s->func();

你可以谴责编译器有些笨了，但事实上这是C标准允许的。这种情况下，另外的进程做了 global_ptr = NULL; 就会导致后一段代码 segfault，而前一段代码没问题。同上，所以这时候也要用 ACCESS_ONCE()：

[PLAIN TEXT](http://wangcong.org/blog/archives/1941#)

C:

1.  ...
2. ​     p = ACCESS_ONCE(global_ptr);
3. ​     if (p  && p->s && p->s->func)
4. ​         p->s->func();

\3. watchdog 中的变量：

[PLAIN TEXT](http://wangcong.org/blog/archives/1941#)

C:

1.  for (;;) {
2. ​                        still_working =  1;
3. ​                        do_something();
4. ​                }

假设 do_something() 定义是可见的，而且没有修改 still_working 的值，那么，编译器可能会把它优化成：

[PLAIN TEXT](http://wangcong.org/blog/archives/1941#)

C:

1.  still_working = 1;
2. ​                for  (;;) {
3. ​                        do_something();
4. ​                }

如果其它进程同时执行了：

[PLAIN TEXT](http://wangcong.org/blog/archives/1941#)

C:

1.  for (;;) {
2. ​                        still_working =  0;
3. ​                        sleep(10);
4. ​                        if  (!still_working)
5. ​                                panic();
6. ​                }

通过 still_working 变量来检测 wathcdog 是否停止了，并且等待10秒后，它确实停止了，panic()！经过编译器优化后，就算它没有停止也会 panic！！所以也应该加上 ACCESS_ONCE()：

[PLAIN TEXT](http://wangcong.org/blog/archives/1941#)

C:

1.  for (;;) {
2. ​                        ACCESS_ONCE(still_working) = 1;
3. ​                        do_something();
4. ​                }

综上，我们不难看出，需要使用 ACCESS_ONCE() 的两个条件是：

\1. 在**无锁**的情况下访问全局变量；
 \2. 对该变量的访问可能被编译器优化成合并成一次（上面第1、3个例子）或者拆分成多次（上面第2个例子）。

**例子**

Linus [ 在邮件中](http://yarchive.net/comp/linux/ACCESS_ONCE.html)给出的另外一个例子是：

编译器有可能把下面的代码：

[PLAIN TEXT](http://wangcong.org/blog/archives/1941#)

C:

1.  if (a> MEMORY) {
2. ​         do1;
3. ​         do2;
4. ​         do3;
5. ​     } else  {
6. ​         do2;
7. ​     }

优化成：

[PLAIN TEXT](http://wangcong.org/blog/archives/1941#)

C:

1.  if (a> MEMORY)
2. ​         do1;
3. ​     do2;
4. ​     if (a> MEMORY)
5. ​         do3;

这里完全符合上面我总结出来的两个条件，所以也应该使用 ACCESS_ONCE()。正如 Linus 所说，不是编译器一定会这么优化，而是你无法证明它不会做这样的优化。

> So the rule is: if you access unlocked values, you use ACCESS_ONCE(). You
>  don't say "but it can't matter". Because you simply don't know.

再看实际中的例子：

```
commit 0ad92ad03aa444b312bd318b0341011a8be09d13
Author: Eric Dumazet
Date:   Tue Nov 1 12:56:59 2011 +0000

    udp: fix a race in encap_rcv handling

    udp_queue_rcv_skb() has a possible race in encap_rcv handling, since
    this pointer can be changed anytime.

    We should use ACCESS_ONCE() to close the race.

diff --git a/net/ipv4/udp.c b/net/ipv4/udp.c
index 131d8a7..ab0966d 100644
--- a/net/ipv4/udp.c
+++ b/net/ipv4/udp.c
@@ -1397,6 +1397,8 @@ int udp_queue_rcv_skb(struct sock *sk, struct sk_buff *skb)
 	nf_reset(skb);

 	if (up->encap_type) {
+		int (*encap_rcv)(struct sock *sk, struct sk_buff *skb);
+
 		/*
 		 * This is an encapsulation socket so pass the skb to
 		 * the socket's udp_encap_rcv() hook. Otherwise, just
@@ -1409,11 +1411,11 @@ int udp_queue_rcv_skb(struct sock *sk, struct sk_buff *skb)
 		 */

 		/* if we're overly short, let UDP handle it */
-		if (skb->len > sizeof(struct udphdr) &&
-		    up->encap_rcv != NULL) {
+		encap_rcv = ACCESS_ONCE(up->encap_rcv);
+		if (skb->len > sizeof(struct udphdr) && encap_rcv != NULL) {
 			int ret;

-			ret = (*up->encap_rcv)(sk, skb);
+			ret = encap_rcv(sk, skb);
 			if (ret <= 0) {
 				UDP_INC_STATS_BH(sock_net(sk),
 						 UDP_MIB_INDATAGRAMS,
```

**更多**

或许看了上面的会让你有一种错觉，volatile 可以解决同步的问题，[其实不然](http://software.intel.com/en-us/blogs/2007/11/30/volatile-almost-useless-for-multi-threaded-programming/)，它只解决其中一个方面。而且上面所有的例子有一个共同的特点：所有的写操作都是简单的赋值（相对于大于CPU字宽的结构体赋值），简单赋值操作在所有平台上都是原子性的，而如果是做加法操作，原子性未必可以保证，更不用说需要  memory barrier 的时候了。所以，不要滥用 volatile。