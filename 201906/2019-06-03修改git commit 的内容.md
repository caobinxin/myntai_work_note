# 修改git commit 的内容

**问题描述：**

当我们发现 我们commit -m 提交的信息写错了，此时我们可以回退 commit_id 然后重新提交：

## 1. 

```shell
git commit -m "jj"

git log

```



```shell
commit 46307cf10450f560ea07e2fa42a357fce00852ef (HEAD -> master, m/master, github/master)
Author: caobinxin <caobinxin@phoenixos.com> # 错的
Date:   Mon Jun 3 10:40:22 2019 +0800

    jj

commit e826509e81f0028666758486876b37f17709641c # 对的
Author: caobinxin <caobinxin666@163.com>
Date:   Sat May 25 11:11:31 2019 +0800

    style: 添加commit提交规范

```

## 2. 

```shell
git reset --soft e826509e81f0028666758486876b # commit_id 是 对的那个

git log 

```



```shell
commit e826509e81f0028666758486876b37f17709641c
Author: caobinxin <caobinxin666@163.com>
Date:   Sat May 25 11:11:31 2019 +0800

    style: 添加commit提交规范

commit 910798332fd361f1ff82746133307949a9a3fb22
Author: caobinxin <caobinxin666@163.com>
Date:   Sat May 25 11:01:24 2019 +0800

    style: 添加.gitignore
```

## 3. 

```shell
git commit -m "feat: 添加单例模式"

git log
```



```shell
commit 220d1dd0eccde62198f5bb365a60a33c56121e13 (HEAD -> master, m/master, github/master)
Author: caobinxin <caobinxin@phoenixos.com>
Date:   Mon Jun 3 10:40:22 2019 +0800

    feat: 添加单例模式

commit e826509e81f0028666758486876b37f17709641c
Author: caobinxin <caobinxin666@163.com>
Date:   Sat May 25 11:11:31 2019 +0800

    style: 添加commit提交规范

commit 910798332fd361f1ff82746133307949a9a3fb22
Author: caobinxin <caobinxin666@163.com>
Date:   Sat May 25 11:01:24 2019 +0800

    style: 添加.gitignore

commit 91ae86633c45ee7600c8e974518440c113014ba7
Author: caobinxin <caobinxin666@163.com>
Date:   Mon May 20 23:37:03 2019 +0800

    add readme.md
```



## 总结：

```shell
git reset --soft
```

