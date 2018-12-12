# linux socket tcp 简单通讯

## 1. server端

### 1.1 代码

```c
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <errno.h>
#include <unistd.h>
#include <signal.h>
#include <sys/wait.h>
#include <netdb.h>

#define BUF_SIZE 1024

/**
 * 捕获子进程退出信号，在退出时给出提示信息
 */

void sig_handler( int signo)
{
    pid_t pid ;
    int stat ;

    pid = waitpid( -1, &stat, WNOHANG) ;
    while( pid > 0)
    {
        printf("child process terminated (PID:%ld)\n", (long) getpid()) ;
        pid = waitpid( -1, &stat, WNOHANG) ;
    }
    return ;
}

int main(int argc, const char *argv[])
{
    socklen_t clt_addr_len ;
    int listen_fd ;                                                                                                                                            
    int com_fd ;
    int ret ;
    int i ;
    static char recv_buf[BUF_SIZE] ;
    int len ;
    int port ;
    pid_t pid ;

    struct sockaddr_in clt_addr ;
    struct sockaddr_in srv_addr ;

    if( 2 != argc)
    {
        printf("Usage: %s port\n", argv[0]) ;
        return 1 ;
    }

    /***
     *　获得输入端口
     */
    port = atoi( argv[1]) ;

    /**
     * 设置信号处理函数，　也可以使用　sigaction 函数
     */

    if( signal( SIGCHLD, sig_handler) < 0)
    {
        perror("cannot set the signal.") ;
        return 1 ;
    }
    /**
     * 创建套接字　用于服务器的监听
     */
    listen_fd = socket(PF_INET, SOCK_STREAM, 0) ;
    if( listen_fd < 0)
    {                                                                                                                                                          
        perror("cannot create listening socket.") ;
        return 1 ;
    }

    memset( &srv_addr, 0, sizeof(srv_addr)) ;
    srv_addr.sin_family = AF_INET ;
    srv_addr.sin_addr.s_addr = htonl(INADDR_ANY) ;
    srv_addr.sin_port = htons(port) ;

    ret = bind( listen_fd, (struct sockaddr*)&srv_addr, sizeof(srv_addr)) ;
    if( -1 == ret)
    {
        perror("cannot bind server socket.") ;
        close(listen_fd) ;
        return 1 ;
    }

    /**
     * 监听指定端口，连接5个客户端
     */

    ret = listen(listen_fd, 5) ;
    if( -1 == ret)
    {
        perror("cannot listen the client connect request") ;
        close( listen_fd) ;
        return 1 ;
    }
    /**
     * 对每个连接的客户端创建一个进程，单独与其进行通信
     * 首先调用　read　函数读取客户端发送的信息
     * 将其转换成大写后发送回客户端
     * 当输入　＠　时，程序退出
     */
                                                                                                                                                               
    while(1)
    {
        len = sizeof(clt_addr) ;
        com_fd = accept( listen_fd, (struct sockaddr*)&clt_addr, &len) ;
        if( com_fd < 0)
        {
            if( EINTR == errno)
            {
                continue ;
            }else{
                perror("cannot accept client connect request") ;
                close( listen_fd) ;
                return 1 ;
            }
        }

        pid = fork() ;
        if( pid < 0)
        {
            perror("cannot create the child process") ;
            close(listen_fd) ;
            return 1 ;
        }else if( 0 == pid){
            while( ( len = read(com_fd, recv_buf, BUF_SIZE)) > 0)
            {
                printf("Message from client(%d): %s\n", len, recv_buf) ;

                if( '@' == recv_buf[0]) break ;

                for( i = 0; i < len; i++)                                                                                                                      
                {
                    recv_buf[i] = toupper(recv_buf[i]) ;
                }

                write(com_fd, recv_buf, len) ;

            }

            close( com_fd) ;
            return 0 ;
        }else{
            close(com_fd) ;
        }
    }
    return 0;
}

```

### 1.2 函数总结

```shell
perror("cannot listen the client connect request") ;
atoi() # 字符串转数字
signal( SIGCHLD, sig_handler) #注册信号回调函数 SIGCHLD 子进程结束信号
void sig_handler( int signo) #注意子函数回调　void (f*)(int)
waitpid( -1, &stat, WNOHANG)
socket(PF_INET, SOCK_STREAM, 0)
memset( &srv_addr, 0, sizeof(srv_addr)) ;
htonl(INADDR_ANY) ;
htons(port) ;
bind( listen_fd, (struct sockaddr*)&srv_addr, sizeof(srv_addr)) 
listen(listen_fd, 5) ;
accept( listen_fd, (struct sockaddr*)&clt_addr, &len) 
fork()
read(com_fd, recv_buf, BUF_SIZE))
write(com_fd, recv_buf, len)
close( com_fd)
```

## 2. client端

### 2.1 代码

```c
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <netdb.h>
#include <unistd.h>

#define BUF_SIZE 1024

int main( int argc, char** argv )
{
	int connect_fd ;
	int ret ;
	char snd_buf[BUF_SIZE] ;
	int i ;
	int port ;
	int len ;

	static struct sockaddr_in srv_addr ;

	/**
	 *　客户端运行需要给出具体的连接地址和端口
	 */
	if( 3 != argc)
	{
		printf("Usage: %s server_ip_address port\n", argv[0]) ;
		return 1 ;
	}

	port = atoi( argv[2]) ;

	connect_fd = socket(PF_INET, SOCK_STREAM, 0) ;
	if( connect_fd < 0)
	{
		perror("cannot create communication socket") ;
		return 1 ;
	}

	memset( &srv_addr, 0, sizeof(srv_addr)) ;
	srv_addr.sin_family = AF_INET ;
	srv_addr.sin_addr.s_addr = inet_addr( argv[1]) ;
	srv_addr.sin_port = htons(port) ;

	/**
	 * 连接指定的服务器
	 */
	ret = connect( connect_fd, ( struct sockaddr*)&srv_addr, sizeof(srv_addr)) ;
	if( 1 == ret)
	{
		perror("cannot connect to the server") ;
		close(connect_fd) ;
		return 1 ;
	}

	memset( snd_buf, 0, BUF_SIZE) ;

	/***
	 * 用户输入信息后，　程序将输入的信息通过套接字发送给服务器
	 * 然后调用　read函数从服务器中读取发送过来的消息
	 * 当输入　@　时，程序退出
	 */

	while(1)
	{
		write( STDOUT_FILENO, "input message:", 14) ;

		len = read( STDIN_FILENO, snd_buf, BUF_SIZE) ;

		if( len > 0)
		{
			write( connect_fd, snd_buf, len) ;
		}

		len = read( connect_fd, snd_buf, len) ;
		if( len > 0)
		{
			printf("Message form server: %s\n", snd_buf) ;
		}

		if('@' == snd_buf[0]) break ;
	}

	close( connect_fd) ;

	 return 0 ;
}
```

### 2.2 函数总结

```shell
write( STDOUT_FILENO, "input message:", 14)  # 直接输出到　标准输出设备上 STDOUT_FILENO == 1
read( STDIN_FILENO, snd_buf, BUF_SIZE)　# 直接从标准　输入中读入字符 STDIN_FILENO == 0
```

### 2.3 编译测试

```shell
00.简单通讯$ ./server 2222
Message from client(13): cao bin xin

00.简单通讯$ ./client 127.0.0.1 2222
input message:cao bin xin 
Message form server: CAO BIN XIN 

00.简单通讯$ netstat | grep 2222 #　查看监听到的　端口号
tcp        0      0 localhost:60428         localhost:2222          ESTABLISHED
tcp        0      0 localhost:2222          localhost:60428         ESTABLISHED
```



## 3.附录

### 3.1 waitpid()函数的用法

大家知道，当用fork启动一个新的子进程的时候，子进程就有了新的生命周期，并将在其自己的地址空间内独立运行。但有的时候，我们希望知道某一个自己创建的子进程何时结束，从而方便父进程做一些处理动作。同样的，在用ptrace去attach一个进程滞后，那个被attach的进程某种意义上说可以算作那个attach它进程的子进程，这种情况下，有时候就想知道被调试的进程何时停止运行。

以上两种情况下，都可以使用Linux中的waitpid()函数做到。先来看看waitpid函数的定义：

```c
#include <sys/types.h> 
#include <sys/wait.h>
pid_t waitpid(pid_t pid,int *status,int options);
```

如果在调用waitpid()函数时，当指定等待的子进程已经停止运行或结束了，则waitpid()会立即返回；但是如果子进程还没有停止运行或结束，则调用waitpid()函数的父进程则会被阻塞，暂停运行。

下面来解释以下调用参数的含义：

1）pid_t pid

参数pid为欲等待的子进程识别码，其具体含义如下：

参数值 	说明
pid<-1 	等待进程组号为pid绝对值的任何子进程。
pid=-1 	等待任何子进程，此时的waitpid()函数就退化成了普通的wait()函数。
pid=0 	等待进程组号与目前进程相同的任何子进程，也就是说任何和调用waitpid()函数的进程在同一个进程组的进程。
pid>0 	等待进程号为pid的子进程。

2）int *status
这个参数将保存子进程的状态信息，有了这个信息父进程就可以了解子进程为什么会推出，是正常推出还是出了什么错误。如果status不是空指针，则状态信息将被写入
器指向的位置。当然，如果不关心子进程为什么推出的话，也可以传入空指针。
Linux提供了一些非常有用的宏来帮助解析这个状态信息，这些宏都定义在sys/wait.h头文件中。主要有以下几个：
宏 	说明
WIFEXITED(status) 	如果子进程正常结束，它就返回真；否则返回假。
WEXITSTATUS(status) 	如果WIFEXITED(status)为真，则可以用该宏取得子进程exit()返回的结束代码。
WIFSIGNALED(status) 	如果子进程因为一个未捕获的信号而终止，它就返回真；否则返回假。
WTERMSIG(status) 	如果WIFSIGNALED(status)为真，则可以用该宏获得导致子进程终止的信号代码。
WIFSTOPPED(status) 	如果当前子进程被暂停了，则返回真；否则返回假。
WSTOPSIG(status) 	如果WIFSTOPPED(status)为真，则可以使用该宏获得导致子进程暂停的信号代码。

3）int options
参数options提供了一些另外的选项来控制waitpid()函数的行为。如果不想使用这些选项，则可以把这个参数设为0。
主要使用的有以下两个选项：
参数 	说明
WNOHANG 	如果pid指定的子进程没有结束，则waitpid()函数立即返回0，而不是阻塞在这个函数上等待；如果结束了，则返回该子进程的进程号。
WUNTRACED 	如果子进程进入暂停状态，则马上返回。
这些参数可以用“|”运算符连接起来使用。
如果waitpid()函数执行成功，则返回子进程的进程号；如果有错误发生，则返回-1，并且将失败的原因存放在errno变量中。
失败的原因主要有：没有子进程（errno设置为ECHILD），调用被某个信号中断（errno设置为EINTR）或选项参数无效（errno设置为EINVAL）

如果像这样调用waitpid函数：waitpid(-1, status, 0)，这此时waitpid()函数就完全退化成了wait()函数。