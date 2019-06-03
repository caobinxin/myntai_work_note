





# 流程：

1. 在github中创建一个专门的仓库（notes-manifest），用来存放manifest文件，参考标题一
2. 用自己的repo仓库，可以在我的repo仓库基础上克隆一个，https://github.com/caobinxin/repo.git
3. 然后就是初始化项目
4. 修改自己的manifest配置清单

资料参考：https://www.cnblogs.com/v2m_/p/7060832.html

email = caobinxin@phoenixos.com

# 标题一：manifest 文件

原始的：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<manifest>
  <remote  name="github"
           fetch="git://github.com/ossxp-com/"
           autodotgit="true" />
  <default revision="master"
           remote="github" />

  <project path="test/test1" name="test1">
    <copyfile src="root.mk" dest="Makefile" />
  </project>
  <project path="test/test2" name="test2" />

</manifest>
```

修改后：default.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<manifest>
  <remote  name="github"
           fetch="git://github.com/caobinxin/"
           autodotgit="true" />
  <default revision="master"
           remote="github" />

  <project path="note" name="myntai_work_note">
  </project>
  <project path="demo" name="daily-demo" />

</manifest>
```

# 2.初始化项目

```shell
# --repo-url: 用于设置 repo的版本地址
# --repo-branch: 用于设定要检出的分支
# --no-repo-verify: 设定不要对repo的里程碑签名进行严格的验证
repo init --repo-url=https://github.com/caobinxin/repo.git --repo-branch=master --no-repo-verify -u https://github.com/caobinxin/notes-manifest.git

repo sync -j8

repo sync tx2_update #同步单个项目

repo start --all 分支名 #在各个项目中，初始化，最初的分支
```

备注：

**repo同步项目问题：**

问题：在执行 repo init --repo-url=https://github.com/caobinxin/repo.git --repo-branch=master --no-repo-verify -u https://github.com/caobinxin/notes-manifest.git 会报错： fatal: Couldn't find remote ref refs/heads/phoenix-n 

环境：公司的电脑之前就有安装过repo，同步过公司的Android代码

解决方案记录在案：

1. git clone https://github.com/caobinxin/repo.git
2. cd repo;cp repo cbx_repo
3. vim ~/.bashrc

```shell
export PATH=~/work/repo/repo:$PATH # ~/work/repo/ 执行 clone的地方
```

4. 此时不知为何，就可以了，很奇怪，由于好了，问题也就没有进一步细究
5. 参考url:https://www.cnblogs.com/zndxall/p/9958457.html

```shell
$ which repo
/home/colby/work/repo/repo/repo
# 说明此时工作正常是用的新的repo，如果在工作中，我们发现 repo有问题了，那就把bashrc中的环境变量注销掉就ok了
```



# 3. 细分项目

```xml
<language>
    <c>https://github.com/caobinxin/c.git</c>
    <c++>https://github.com/caobinxin/c-.git</c++>
    <java>https://github.com/caobinxin/java.git</java>
    <python>https://github.com/caobinxin/python.git</python>
    <shell>https://github.com/caobinxin/shell.git</shell>
</language>

<application>
	<android-app>https://github.com/caobinxin/android-app.git</android-app>
    <linux-app>https://github.com/caobinxin/linux-app.git</linux-app>
</application>

<tool>
	<makefile>https://github.com/caobinxin/makefile.git</makefile>
    <git>https://github.com/caobinxin/git.git</git>
    <gdb>https://github.com/caobinxin/gdb.git</gdb>
    <cmake>https://github.com/caobinxin/cmake.git</cmake>
</tool>

<os>
	<driver>https://github.com/caobinxin/driver.git</driver>
</os>
```

```shell
git clone https://github.com/caobinxin/c-.git; cd c-; touch readme.md; git add .; git commit -m "add readme.md"; git push
```

Github 也提供了各种 `.gitignore` 模板配置文件:https://github.com/github/gitignore



