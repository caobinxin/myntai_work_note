#  			[Ubuntu 安装mysql和简单操作](https://www.cnblogs.com/zhuyp1015/p/3561470.html) 		



　　ubuntu上安装mysql非常简单只需要几条命令就可以完成。

　　1. sudo apt-get install mysql-server

 

　　2. apt-get isntall mysql-client

 

​        　　3.  sudo apt-get install libmysqlclient-dev

 

　　安装过程中会提示设置密码什么的，注意设置了不要忘了，安装完成之后可以使用如下命令来检查是否安装成功：

 

　　**sudo netstat -tap | grep mysql**

 

　　通过上述命令检查之后，如果看到有mysql 的socket处于 listen 状态则表示安装成功。

 

　　登陆mysql数据库可以通过如下命令：

 

　　**mysql -u root -p** 

 

　　-u 表示选择登陆的用户名， -p 表示登陆的用户密码，上面命令输入之后会提示输入密码，此时输入密码就可以登录到mysql。

 

　　![img](https://images0.cnblogs.com/blog/306217/201402/222309341896481.jpg)

 

　　然后通过 **show databases**; 就可以查看当前的数据库。

　　我们选择 mysql数据库就行下一步操作，使用**use mysql** 命令，显示当前数据库的表单：**show tables** 

　　![img](https://images0.cnblogs.com/blog/306217/201402/222310398145029.jpg)

　　

　　写一个简单的程序来访问该数据库，实现 show tables 功能：

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
#include <mysql/mysql.h>
#include <stdio.h>
#include <stdlib.h>
int main() 
{
    MYSQL *conn;
    MYSQL_RES *res;
    MYSQL_ROW row;
    char server[] = "localhost";
    char user[] = "root";
    char password[] = "mima";
    char database[] = "mysql";
    
    conn = mysql_init(NULL);
    
    if (!mysql_real_connect(conn, server,user, password, database, 0, NULL, 0)) 
    {
        fprintf(stderr, "%s\n", mysql_error(conn));
        exit(1);
    }
    
    if (mysql_query(conn, "show tables")) 
    {
        fprintf(stderr, "%s\n", mysql_error(conn));
        exit(1);
    }
    
    res = mysql_use_result(conn);
    
    printf("MySQL Tables in mysql database:\n");
    
    while ((row = mysql_fetch_row(res)) != NULL)
    {
        printf("%s \n", row[0]);
    }
    
    mysql_free_result(res);
    mysql_close(conn);
    
    printf("finish! \n");
    return 0;
}
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

　　编译代码的时候需要链接mysql的库，可以通过如下方式编译：

　　g++ -Wall mysql_test.cpp -o mysql_test **-lmsqlclient**

　　然后运行编译好的代码：

![img](https://images0.cnblogs.com/blog/306217/201402/222313575224563.jpg)

　　可见结果和使用SQL语句 show tables 是一样的。

make it simple, make it happen

## 初始化mysql

1. 查看默认的配置

```shell
colby@pc:~$ sudo cat /etc/mysql/debian.cnf 
[sudo] colby 的密码： 
# Automatically generated for Debian scripts. DO NOT TOUCH!
[client]
host     = localhost
user     = debian-sys-maint
password = Ifjkb12gIBvJJa8i
socket   = /var/run/mysqld/mysqld.sock
[mysql_upgrade]
host     = localhost
user     = debian-sys-maint
password = Ifjkb12gIBvJJa8i
socket   = /var/run/mysqld/mysqld.sock
colby@pc:~$ 
```

2. 再输入以下指令：

```
mysql -u debian-sys-maint -p//注意! //这条指令的密码输入是输入第一条指令获得的信息中的 password = Ifjkb12gIBvJJa8i 得来。//请根据自己的实际情况填写！
```

```shell
mysql>  #密码成功
```

3. 修改密码，本篇文章将密码修改成 root , 用户可自行定义。

    use mysql;
    // 下面这句命令有点长，请注意。
    update mysql.user set authentication_string=password('root') where user='root' and Host ='localhost';
    update user set plugin="mysql_native_password"; 
    flush privileges;
    quit;

4. 重新启动mysql:

    sudo service mysql restart
    mysql -u root -p // 启动后输入已经修改好的密码：root

