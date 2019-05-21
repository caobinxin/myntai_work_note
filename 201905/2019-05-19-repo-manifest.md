# 1. manifest 文件

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

