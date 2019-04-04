# linux中对EINTR错误的处理【转】

慢系统调用(slow system call)：此术语适用于那些可能永远阻塞的系统调用。永远阻塞的系统调用是指调用有可能永远无法返回，多数网络支持函数都属于这一类。如：若没有客户连接到服务器上，那么服务器的accept调用就没有返回的保证。

   EINTR错误的产生：当阻塞于某个慢系统调用的一个进程捕获某个信号且相应信号处理函数返回时，该系统调用可能返回一个EINTR错误。例如：在socket服务器端，设置了信号捕获机制，有子进程，当在父进程阻塞于慢系统调用时由父进程捕获到了一个有效信号时，内核会致使accept返回一个EINTR错误(被中断的系统调用)。

   当碰到EINTR错误的时候，可以采取有一些可以重启的系统调用要进行重启，而对于有一些系统调用是不能够重启的。例如：accept、read、write、select、和open之类的函数来说，是可以进行重启的。不过对于套接字编程中的connect函数我们是不能重启的，若connect函数返回一个EINTR错误的时候，我们不能再次调用它，否则将立即返回一个错误。针对connect不能重启的处理方法是，必须调用select来等待连接完成。

 

在 linux 或者 unix 环境中， errno 是一个十分重要的部分。在调用的函 数出现问题的时候，我们可以通过 errno 
的值来确定出错的原因，这就会 涉及到一个问题，那就是如何保证 errno 
在多线程或者进程中安全？我们希望在多线程或者进程中，每个线程或者进程都拥有自己独立和唯一的一个 errno ，这样就能够保证不会有竞争条 
件的出现。一般而言，编译器会自动保证 errno 的安全性，但是为了妥善期间，我们希望在写 makefile 的时 候把 
_LIBC_REENTRANT 宏定义，比 如我们在检查 <bits/errno.h> 文件中发现如下的定义： 


C代码  

ifndef __ASSEMBLER__  

/* Function to get address of global `errno' variable.  */  
extern int *__errno_location (void) __THROW __attribute__ ((__const__));  

if !defined _LIBC || defined _LIBC_REENTRANT  

/* When using threads, errno is a per-thread value.  */  

define errno (*__errno_location ())  

endif  

endif /* !__ASSEMBLER__ */  

#endif /* _ERRNO_H */  

 

也就是说，在没有定义 __LIBC 或者定义 _LIBC_REENTRANT 的时候， errno 是多线程 / 进程安全的。 
一般而言，  __ASSEMBLER__, _LIBC 和 _LIBC_REENTRANT 都不会被编译器定义，但是如果我们定义 _LIBC_REENTRANT 一次又何妨那？ 
为了检测一下你编译器是否定义上述变量，不妨使用下面一个简单程序。 


C代码  
#include <stdio.h>  
#include <errno.h>  

int main( void )  
{  
#ifndef __ASSEMBLER__  
​        printf( "Undefine __ASSEMBLER__\n" );  
#else  
​        printf( "define __ASSEMBLER__\n" );  
#endif  

#ifndef __LIBC  
​        printf( "Undefine __LIBC\n" );  
#else  
​        printf( "define __LIBC\n" );  
#endif  

#ifndef _LIBC_REENTRANT  
​        printf( "Undefine _LIBC_REENTRANT\n" );  
#else  
​        printf( "define _LIBC_REENTRANT\n" );  
#endif  

​        return 0;  
}  

 

希望读者在进行移植的时候，读一下相关的 unix 版本的 <bits/errno.h> 文 件，来确定应该定义什么宏。不同的 unix 版本可能存在着一些小的差别！

 

有时候，在调用系统调用时，可能会接收到某个信号而导致调用退出。譬如使用system调用某个命令之后该进程会接收到SIGCHILD信号，然后如果这个进程的线程中有慢系统调用，那么接收到该信号的时候可能就会退出，返回EINTR错误码。

EINTR
　　linux中函数的返回状态，在不同的函数中意义不同：

1）write
　　表示：由于信号中断，没写成功任何数据。
　　The call was interrupted by a signal before any data was written.

2）read
　　表示：由于信号中断，没读到任何数据。
　　The call was interrupted by a signal before any data was read.

3）sem_wait
　　函数调用被信号处理函数中断
　　The call was interrupted by a signal handler.

4）recv
　　由于信号中断返回，没有任何数据可用。
　　function was interrupted by a signal that was caught, before any data was available.

 

调用系统调用的时候,有时系统调用会被中断.此时,系统调用会返回-1,并且错误码被置为EINTR.但是,有时并不将这样的情况作为错误.有两种处理方法:

1.如果错误码为EINTR则重新调用系统调用,例如Postgresql中有一段代码:

 
view plain
copy to clipboard
print
?
retry1:   
​    if (send(port->sock, &SSLok, 1, 0) != 1)   
​    {   
​        if (errno == EINTR)   
​            goto retry1;    /* if interrupted, just retry */  

[cpp] view plain
copy

retry1:  
​    if (send(port->sock, &SSLok, 1, 0) != 1)  
​    {  
​        if (errno == EINTR)  
​            goto retry1;    /* if interrupted, just retry */  


 

2.重新定义系统调用,忽略错误码为EINTR的情况.例如,Cherokee中的一段代码:

 
view plain
copy to clipboard
print
?
int  
cherokee_stat (const char *restrict path, struct stat *buf)   
{   
​    int re;   
​    do {   
​        re = stat (path, buf);   
​    } while ((re == -1) && (errno == EINTR));   
​    return re;   
}  





最近 socket 读数据老是遇到 Interrupted system call （EINTR），代码改为如下解决


while (1)

{
​    select(socket+1, &readfds, NULL, NULL, &tv);
​    if (FD_ISSET(socket, &readfds))

​      {
​       printf("connection got!\n");

​       break;

​      }
​    else{
​       if (errno == EINTR)
​          continue;
​       else
​          printf("Timed out.\n");
​        }
}


下面的列表显示常见的 Linux 系统错误代码。 
1 EPERM
Operation not permitted
操作不许可 
2 ENOENT
No such file or directory
无此文件或目录 
3 ESRCH
No such process
无此过程 
4 EINTR 
Interrupted system call
系统调用被禁止 
5 EIO 
I/O error
I/O 错误 
6 ENXIO 
No such device or address
无此器件或地址 
7 E2BIG 
Arg list too long
Arg 列表太长 
8 ENOEXEC 
Exec format error
Exec 格式错误 
9 EBADF 
Bad file number
文件数目错误
10 ECHILD
No child processes
无子过程
11 EAGAIN
Try again
再试一遍
12 ENOMEM
Out of memory 
内存溢出
13 EACCES
Permission denied 
许可拒绝
14 EFAULT
Bad address 
错误的地址
15 ENOTBLK
Block device required 
需要块设备
16 EBUSY
Device or resource busy 
设备或资源忙
17 EEXIST
File exists 
文件存在
18 EXDEV
Cross-device link 
跨器链接
19 ENODEV
No such device 
无此设备
20 ENOTDIR
Not a directory 
不是一个目录
21 EISDIR
Is a directory 
是一个目录
22 EINVAL
Invalid argument 
无效的函数自变量
23 ENFILE
File table overflow 
文件表溢出
24 EMFILE
Too many open files
打开的文件太多
25 ENOTTY
Inappropriate ioctl for device 

26 ETXTBSY
Text file busy 
文本文件忙
27 EFBIG
File too large
文件太大
28 ENOSPC
No space left on device 
磁盘空间不足
29 ESPIPE 
Illegal seek 
不合法的寻找
30 EROFS 
Read-only file system 
只读文件系统
31 EMLINK 
Too many links
太多的链接