# linux dup 和 dup2 的区别

## 1. dup

dup 函数用于复制指定的文件描述符。函数返回的新描述符是当前进程最小的未使用的描述符。

```c
#include <stdio.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>

int main(int argc, const char *argv[])
{
	
	int fd1 ;

	if( argc != 2)
	{
		printf("Usage: %s filename\n", argv[0]) ;
		return 1 ;
	}

	if( ( fd1 = open( argv[1], O_CREAT | O_RDWR, 0777)) == -1)
	{
		perror("Cannot create the file") ;
		return 1 ;
	}

	close(STDOUT_FILENO) ;

	if( dup( fd1) == -1)
	{
		perror("Cannot reserved the std out fd") ;
		return 1 ;
	}

	printf("Write the the file\n") ;
	
	close(fd1) ;

	return 0;
}
```

## 2. dup2

dup2　函数同样由于复制指定的文件描述符，只是参数与dup函数存在不同。如果新的文件描述符已经打开，将会先关闭新的文件描述符

```c
#include <stdio.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>

int main(int argc, const char *argv[])
{
	
	int fd1 ;

	if( argc != 2)
	{
		printf("Usage: %s filename\n", argv[0]) ;
		return 1 ;
	}

	if( ( fd1 = open( argv[1], O_CREAT | O_RDWR, 0777)) == -1)
	{
		perror("Cannot create the file") ;
		return 1 ;
	}



	if( dup2( fd1, STDOUT_FILENO) == -1)
	{
		perror("Cannot reserved the std out fd") ;
		return 1 ;
	}

	while(1){
		printf("kjdnkjjj\n") ;
	}	
	close(fd1) ;

	return 0;
}
```

## 3. 测试

### 3.1  收集log

测试能否收集同一个终端下不同程序的日志.

结论是　这种方式不能

```c
#include <stdio.h>
#include <unistd.h>

int main(int argc, const char *argv[])
{
	int i = 0 ;
	while( i < 1000)
	{
		i++ ;
		sleep(1) ;
		printf("sleep %d printf\n", i) ;
	}
	return 0;
}
```

**测试 流程**

```shell
./sleep & 
./dup2 test
结论是，　dup 和　dup2　只能改变当前程序的标准输出　不能改变其他的。这也更加验证了每个程序，都有自己独立的文件IO
```

