# ubuntu-tfp服务器配置

## 第一步：在 Ubuntu 中安装 VSFTPD 服务器

首先，我们需要更新系统安装包列表，然后像下面这样安装 VSFTPD 二进制包：

```shell
$ sudo apt-get update
$ sudo apt-get install vsftpd
```

一旦安装完成，初始情况下服务被禁用。因此，我们需要手动开启服务，同时，启动它使得在下次开机时能够自动开启服务：

```shell
------------- On SystemD -------------
# 
sudo systemctl start vsftpd
# 
sudo systemctl enable vsftpd
------------- On SysVInit -------------
# 
sudo service vsftpd start
# 
sudo chkconfig --level 35 vsftpd on
```

接下来，如果你在服务器上启用了 UFW 防火墙（默认情况下不启用），那么需要打开端口 20 和 21 —— FTP 守护进程正在监听它们——从而才能允许从远程机器访问 FTP 服务，然后，像下面这样添加新的防火墙规则：

```
$ sudo ufw allow 20/tcp
$ sudo ufw allow 21/tcp
$ sudo ufw status
```

## 第二步：在 Ubuntu 中配置并保护 VSFTPD 服务器

让我们进行一些配置来设置和保护 FTP 服务器。首先，我们像下面这样创建一个原始配置文件 /etc/vsftpd/vsftpd.conf 的备份文件：

```
$ sudo cp /etc/vsftpd.conf /etc/vsftpd.conf.orig
```

接下来，打开 vsftpd 配置文件。

```
$ sudo vi /etc/vsftpd.conf
OR
$ sudo nano /etc/vsftpd.conf
```

把下面的这些选项添加/改成所展示的值：

```shell
anonymous_enable=NO             # 关闭匿名登录
local_enable=YES        # 允许本地用户登录
write_enable=YES        # 启用可以修改文件的 FTP 命令
local_umask=022             # 本地用户创建文件的 umask 值
dirmessage_enable=YES           # 当用户第一次进入新目录时显示提示消息
xferlog_enable=YES      # 一个存有详细的上传和下载信息的日志文件
connect_from_port_20=YES        # 在服务器上针对 PORT 类型的连接使用端口 20（FTP 数据）
xferlog_std_format=YES          # 保持标准日志文件格式
listen=NO               # 阻止 vsftpd 在独立模式下运行
listen_ipv6=YES             # vsftpd 将监听 ipv6 而不是 IPv4，你可以根据你的网络情况设置
pam_service_name=vsftpd         # vsftpd 将使用的 PAM 验证设备的名字
userlist_enable=YES             # 允许 vsftpd 加载用户名字列表
tcp_wrappers=YES        # 打开 tcp 包装器
```

现在，配置 VSFTPD ，基于用户列表文件/etc/vsftpd.userlist 来允许或拒绝用户访问 FTP。

注意，在默认情况下，如果通过userlist_enable=YES 启用了用户列表，且设置userlist_deny=YES 时，那么，用户列表文件/etc/vsftpd.userlist 中的用户是不能登录访问的。

但是，选项userlist_deny=NO 则反转了默认设置，这种情况下只有用户名被明确列出在/etc/vsftpd.userlist 中的用户才允许登录到 FTP 服务器。

```shell
userlist_enable=YES                   # vsftpd 将会从所给的用户列表文件中加载用户名字列表
userlist_file=/etc/vsftpd.userlist    # 存储用户名字的列表
userlist_deny=NO
```

重要的是，当用户登录 FTP 服务器以后，他们将进入 chrooted 环境，即当在 FTP 会话时，其 root 目录将是其 home 目录。

接下来，我们来看一看两种可能的途径来设置 chrooted（本地 root）目录，正如下面所展示的。

这时，让我们添加/修改/取消这两个选项来将 FTP 用户限制在其 home 目录

```
chroot_local_user=YES
allow_writeable_chroot=YES
```

选项chroot_local_user=YES 意味着本地用户将进入 chroot 环境，当登录以后默认情况下是其 home 目录。

并且我们要知道，默认情况下，出于安全原因，VSFTPD 不允许 chroot 目录具有可写权限。然而，我们可以通过选项 allow_writeable_chroot=YES 来改变这个设置

保存文件然后关闭。现在我们需要重启 VSFTPD 服务从而使上面的这些更改生效：

```
------------- On SystemD -------------
# systemctl restart vsftpd
------------- On SysVInit -------------
# service vsftpd restart
```

## 第三步：在 Ubuntu 上测试 VsFTP 服务器

现在，我们通过使用下面展示的 useradd 命令创建一个 FTP 用户来测试 FTP 服务器：

```
$ sudo useradd -m -c "Aaron Kili, Contributor" -s /bin/bash aaronkilik
$ sudo passwd aaronkilik
```

然后，我们需要像下面这样使用 echo 命令和 tee 命令来明确地列出文件/etc/vsftpd.userlist 中的用户 aaronkilik：

```
$ echo "aaronkilik" | sudo tee -a /etc/vsftpd.userlist
$ cat /etc/vsftpd.userlist
```

现在，是时候来测试上面的配置是否具有我们想要的功能了。我们首先测试匿名登录；我们可以从下面的输出中很清楚的看到，在这个 FTP 服务器中是不允许匿名登录的：

```
# ftp 192.168.56.102
Connected to 192.168.56.102  (192.168.56.102).
220 Welcome to TecMint.com FTP service.
Name (192.168.56.102:aaronkilik) : anonymous
530 Permission denied.
Login failed.
ftp> bye
221 Goodbye.
```

接下来，我们将测试，如果用户的名字没有在文件/etc/vsftpd.userlist 中，是否能够登录。从下面的输出中，我们看到，这是不可以的：

```
# ftp 192.168.56.102
Connected to 192.168.56.102  (192.168.56.102).
220 Welcome to TecMint.com FTP service.
Name (192.168.56.10:root) : user1
530 Permission denied.
Login failed.
ftp> bye
221 Goodbye.
```

现在，我们将进行最后一项测试，来确定列在文件/etc/vsftpd.userlist 文件中的用户登录以后，是否实际处于 home 目录。从下面的输出中可知，是这样的：

```
# ftp 192.168.56.102
Connected to 192.168.56.102  (192.168.56.102).
220 Welcome to TecMint.com FTP service.
Name (192.168.56.102:aaronkilik) : aaronkilik
331 Please specify the password.
Password:
230 Login successful.
Remote system type is UNIX.
Using binary mode to transfer files.
ftp> ls
```

![img](http://www.linuxidc.com/upload/2017_04/170404140680131.png)

**在 Ubuntu 中确认 FTP 登录**

警告：设置选项allow_writeable_chroot=YES 是很危险的，特别是如果用户具有上传权限，或者可以 shell 访问的时候，很可能会出现安全问题。只有当你确切的知道你在做什么的时候，才可以使用这个选项。

我们需要注意，这些安全问题不仅会影响到 VSFTPD，也会影响让本地用户进入 chroot 环境的 FTP daemon。

因为这些原因，在下一步中，我将阐述一个更安全的方法，来帮助用户设置一个非可写本地 root 目录。

**第四步：在 Ubuntu 中配置 FTP 用户的 Home 目录**

现在，再次打开 VSFTPD 配置文件。

```
$ sudo vi /etc/vsftpd.conf
OR
$ sudo nano /etc/vsftpd.conf
```

然后像下面这样用# 把不安全选项注释了：

```
#allow_writeable_chroot=YES
```

接下来，为用户创建一个替代的本地 root 目录（aaronkilik，你的可能和这不一样），然后设置目录权限，取消其他所有用户对此目录的写入权限：

```
$ sudo mkdir /home/aaronkilik/ftp
$ sudo chown nobody:nogroup /home/aaronkilik/ftp
$ sudo chmod a-w /home/aaronkilik/ftp
```

然后，在本地 root 目录下创建一个具有合适权限的目录，用户将在这儿存储文件：

```
$ sudo mkdir /home/aaronkilik/ftp/files
$ sudo chown -R aaronkilk:aaronkilik /home/aaronkilik/ftp/files
$ sudo chmod -R 0770 /home/aaronkilik/ftp/files/
```

之后，将 VSFTPD 配置文件中的下面这些选项添加/修改为相应的值：

```
user_sub_token=$USER          # 在本地 root 目录中插入用户名
local_root=/home/$USER/ftp    # 定义各个用户的本地 root 目录
```

保存文件并关闭。然后重启 VSFTPD 服务来使上面的设置生效：

```
------------- On SystemD -------------
# systemctl restart vsftpd
------------- On SysVInit -------------
# service vsftpd restart
```

现在，让我们来最后检查一下，确保用户的本地 root 目录是我们在他的 Home 目录中创建的 FTP 目录。

```
# ftp 192.168.56.102
Connected to 192.168.56.102  (192.168.56.102).
220 Welcome to TecMint.com FTP service.
Name (192.168.56.10:aaronkilik) : aaronkilik
331 Please specify the password.
Password:
230 Login successful.
Remote system type is UNIX.
Using binary mode to transfer files.
ftp> ls
```

![img](http://www.linuxidc.com/upload/2017_04/170404140680132.png)

**FTP 用户 Home 目录登录**