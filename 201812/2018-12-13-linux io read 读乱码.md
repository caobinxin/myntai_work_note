# linux io read 读乱码

## 1 乱码的代码

```c
#include <stdio.h>                                                                                                                                             
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#define BUF_SIZE  2
int main(int argc, const char *argv[])
{
    int fd ;
    char buffer[BUF_SIZE] ;

    int num ;

    if( argc != 2)
    {   
        printf("Usage: %s filename", argv[0]) ;
        return 1 ; 
    }   

    if( (fd = open( argv[1], O_RDONLY)) == -1) 
    {   
        perror("Cannot open the file") ;
        return 1 ; 
    }   

    while( ( num = read(fd, buffer, BUF_SIZE)) > 0)
    {   
//      printf("num = %d\n", num) ;
//      buffer[num] = '\0' ;
        printf("%s", buffer) ;
    }   

    printf("num = %d\n", num) ;
    
    close( fd) ;

    return 0;
}

```

**打印效果**

```shell
colby@colby-myntai:~/work300GB/cbx-study/linux_app/io$ ./a.out read.c 
#i���nc���lu���de��� <���st���di���o.���h>���
#���in���cl���ud���e ���<u���ni���st���d.���h>���
#���in���cl���ud���e ���<s���ys���/t���yp���es���.h���>
���#i���nc���lu���de��� <���sy���s/���st���at���.h���>
���#i���nc���lu���de��� <���fc���nt���l.���h>���

���#d���ef���in���e ���BU���F_���SI���ZE���  ���2
���in���t ���ma���in���(i���nt��� a���rg���c,��� c���on���st��� c���ha���r ���*a���rg���v[���])���
{���
	���in���t ���fd��� ;���
	���ch���ar��� b���uf���fe���r[���BU���F_���SI���ZE���] ���;
���
	���in���t ���nu���m ���;
���
	���if���( ���ar���gc��� !���= ���2)���
	���{
���		���pr���in���tf���("���Us���ag���e:��� %���s ���fi���le���na���me���",��� a���rg���v[���0]���) ���;
���		���re���tu���rn��� 1��� ;���
	���}
���
	���if���( ���(f���d ���= ���op���en���( ���ar���gv���[1���],��� O���_R���DO���NL���Y)���) ���==��� -���1)���
	���{
���		���pe���rr���or���("���Ca���nn���ot��� o���pe���n ���th���e ���fi���le���")��� ;���
	���	r���et���ur���n ���1 ���;
���	}���

���	w���hi���le���( ���( ���nu���m ���= ���re���ad���(f���d,��� b���uf���fe���r,��� B���UF���_S���IZ���E)���) ���> ���0)���
	���{
���//���		���pr���in���tf���("���nu���m ���= ���%d���\n���",��� n���um���) ���;
���//���		���bu���ff���er���[n���um���] ���= ���'\���0'��� ;���
	���	p���ri���nt���f(���"%���s"���, ���bu���ff���er���) ���;
���	}���

���	p���ri���nt���f(���"n���um��� =��� %���d\���n"���, ���nu���m)��� ;���
	���
	���cl���os���e(��� f���d)��� ;���

���	r���et���ur���n ���0;���
}���
}���num = 0
colby@colby-myntai:~/work300GB/cbx-study/linux_app/io$ 

```



### 1.1 乱码原因分析

　　不是每次都能读满buffer长度的内容。最后一次读取文件时，可能读取的字符数(num)小于buffer中的空间。这时，输出buffer字符串的内容时，后面将存在一些其他的字符内容，就有可能造成乱码。

　　还有一种情况是，我们如果读的时候，没有将字符结束标志'\0'写入字符串的最后，也会出现乱码。

​	read(fd, buffer, BUF_SIZE)　这里指定读多少就是多少，不会自动将'\0'放入缓冲区的

## 2 真确的做法

​	每次读的时候，自己人为的写入　'\0'　这是字符串

​	如果是文件拷贝我们没有必要写入 '\0'　这样反而把数据给切开了。读多少写多少就是。

```c
#include <stdio.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#define BUF_SIZE  20
int main(int argc, const char *argv[])
{                                                                                                                                                              
    int fd ;
    char buffer[BUF_SIZE] ;

    int num ;

    if( argc != 2)
    {   
        printf("Usage: %s filename", argv[0]) ;
        return 1 ; 
    }   

    if( (fd = open( argv[1], O_RDONLY)) == -1) 
    {   
        perror("Cannot open the file") ;
        return 1 ; 
    }   

    while( ( num = read(fd, buffer, BUF_SIZE - 1)) > 0) //读取少一个
    {   
        buffer[num] = '\0' ;　//人为的添加　'\0'
        printf("%s", buffer) ; //这些改动都是因为，本身打印字符串的特性　所造成的
    }   

    printf("num = %d\n", num) ;
    
    close( fd) ;

    return 0;
}

```

