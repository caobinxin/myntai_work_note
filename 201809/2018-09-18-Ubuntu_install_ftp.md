# Ubuntu_install_ftp

**1、查看是否安装 ftp服务器**

```sh
vsftpd -v
```

**2、安装ftp服务器**

```sh
sudo apt-get install vsftpd
```

**3、如果安装失败或者配置出现问题，可以卸载 ftp服务器**

```sh
sudo apt-get purge vsftpd
```

**4、创建共享文件夹**

```sh
sudo mkdir ~/work300GB/ftp
sudo chmod 777 ~/work300GB/ftp
```

**5、修改 vsftpd.conf 配置文件**

```sh
sudo vim /etc/vsftpd.conf
```

修改如下：

```sh
# 阻止 vsftpd 在独立模式下运行
listen=NO                 
# vsftpd 将监听 ipv6 而不是 IPv4
listen_ipv6=YES           
# 关闭匿名登录
anonymous_enable=NO       
# 允许本地用户登录
local_enable=YES          
# 启用可以修改文件的 FTP 命令
write_enable=YES          
# 本地用户新增档案时的umask 值
local_umask=022           
# 当用户第一次进入新目录时显示提示消息 
dirmessage_enable=YES     
# 显示在您的本地时区的时间目录列表
use_localtime=YES         
# 一个存有详细的上传和下载信息的日志文件
xferlog_enable=YES        
# 在服务器上针对 PORT 类型的连接使用端口 20（FTP 数据）
connect_from_port_20=YES  
# 不单独建立ftp用户，直接使用Ubuntu桌面用户就可以登陆
chroot_local_user=YES
chroot_list_enable=NO
# 能够登录的用户名单
chroot_list_file=/etc/vsftpd.chroot_list   
# 使用uft8文件系统
utf8_filesystem=YES
```

在文件最后添加下列内容：

```sh
# 锁定一个共享目录
local_root=/home/eagle/ftp
# 给共享目录添加写权限
allow_writeable_chroot=YES
```

**6、创建 vsftpd.chroot_list 文件**

```sh
sudo vim /etc/vsftpd.chroot_list
```

添加能够登录ftp服务器的用户名，一行一个

**7、重启 ftp 服务器**

```sh
sudo service vsftpd restart
```

**8、浏览器登录ftp服务器**

```sh
ftp://192.168.1.107
```

输入用户名和密码，登录成功，可以看到共享的文件

