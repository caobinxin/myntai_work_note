# TEMP_FAILURE_RETRY

```c
 
//这个例子取自， FireNow-Nougat/hardware$ find -name vibrator*

//路径： ./libhardware/modules/vibrator/vibrator.c


#include <unistd.h>
 |static int vibra_exists() {
 |    int fd;
 |
 |    fd = TEMP_FAILURE_RETRY(open(THE_DEVICE, O_RDWR));
 |    if(fd < 0) {
 |        ALOGE("Vibrator file does not exist : %d", fd);
 |        return 0;                                                                                                                                           
 |    }
 |
 |    close(fd);
 |    return 1;
 |}

```





prebuilts/ndk/current/platforms/android-23/arch-arm64/usr/include/unistd.h

```c
/* Used to retry syscalls that can return EINTR. */
#define TEMP_FAILURE_RETRY(exp) ({         \
    __typeof__(exp) _rc;                   \
    do {                                   \
        _rc = (exp);                       \                                                                                                                  
    } while (_rc == -1 && errno == EINTR); \
    _rc; })
```

结论：我们在调用系统调用时，为了防止在系统调用时，信号的干扰而导致系统调用失败，所以我们可以使用这种方式 TEMP_FAILURE_RETRY去过滤掉 信号导致的系统调用失败。



## EINTR

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