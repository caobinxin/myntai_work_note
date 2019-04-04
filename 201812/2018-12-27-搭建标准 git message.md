# 搭建标准 git message

- [1.为什么需要](http://192.168.1.200:8090/pages/viewpage.action?pageId=10226893#id-2.9优雅的提交GitMessage-1.为什么需要)
- [2.GitHub Angular Demo](http://192.168.1.200:8090/pages/viewpage.action?pageId=10226893#id-2.9优雅的提交GitMessage-2.GitHubAngularDemo)
- [3.遵循什么规范    ](http://192.168.1.200:8090/pages/viewpage.action?pageId=10226893#id-2.9优雅的提交GitMessage-3.遵循什么规范)
  - [3.1 Header](http://192.168.1.200:8090/pages/viewpage.action?pageId=10226893#id-2.9优雅的提交GitMessage-3.1Header)
    - [3.1.1 type](http://192.168.1.200:8090/pages/viewpage.action?pageId=10226893#id-2.9优雅的提交GitMessage-3.1.1type)
    - [3.1.2 scope](http://192.168.1.200:8090/pages/viewpage.action?pageId=10226893#id-2.9优雅的提交GitMessage-3.1.2scope)
    - [3.1.3 subject](http://192.168.1.200:8090/pages/viewpage.action?pageId=10226893#id-2.9优雅的提交GitMessage-3.1.3subject)
  - [3.2 Body](http://192.168.1.200:8090/pages/viewpage.action?pageId=10226893#id-2.9优雅的提交GitMessage-3.2Body)
  - [3.3 Footer](http://192.168.1.200:8090/pages/viewpage.action?pageId=10226893#id-2.9优雅的提交GitMessage-3.3Footer)
    - [3.3.1 不兼容变动](http://192.168.1.200:8090/pages/viewpage.action?pageId=10226893#id-2.9优雅的提交GitMessage-3.3.1不兼容变动)
    - [3.3.2 关闭问题](http://192.168.1.200:8090/pages/viewpage.action?pageId=10226893#id-2.9优雅的提交GitMessage-3.3.2关闭问题)
- [4.工具约束](http://192.168.1.200:8090/pages/viewpage.action?pageId=10226893#id-2.9优雅的提交GitMessage-4.工具约束)
  - [4.1 Commitizen](http://192.168.1.200:8090/pages/viewpage.action?pageId=10226893#id-2.9优雅的提交GitMessage-4.1Commitizen)
  - [4.2 Commitlint + Husky](http://192.168.1.200:8090/pages/viewpage.action?pageId=10226893#id-2.9优雅的提交GitMessage-4.2Commitlint+Husky)
  - [4.3 Standard Version](http://192.168.1.200:8090/pages/viewpage.action?pageId=10226893#id-2.9优雅的提交GitMessage-4.3StandardVersion)
- [5.项目中如何使用](http://192.168.1.200:8090/pages/viewpage.action?pageId=10226893#id-2.9优雅的提交GitMessage-5.项目中如何使用)



# 1.为什么需要

- 降低Review成本，可以明确知道本次提交的改变和影响
- 规范整个Team的提交习惯，对技术素养的养成有益
- 可以通过统一工具，抽取规范的message自动形成change log

# 2.GitHub Angular Demo

​    目前Github的Angular项目，就是完全采用规范的Git Message来进行日常的提交管理和发布管理的，下面是这个项目的Commit记录，和自动根据commit生成的change log

![img](http://192.168.1.200:8090/download/attachments/10226893/1.png?version=1&modificationDate=1544696388000&api=v2)

![img](http://192.168.1.200:8090/download/attachments/10226893/2.png?version=1&modificationDate=1544696391000&api=v2)

# 3.遵循什么规范    

​    目前，使用较多的是[AngularJS规范](https://github.com/angular/angular.js/blob/master/DEVELOPERS.md#-git-commit-guidelines)

```
# 包括三个部分：Header，Body 和 Footer

<type>(<scope>): <subject>
// 空一行
<body>
// 空一行
<footer>
```

## 3.1 Header

​    包括三个字段：type（必需）、scope（可选）和subject（必需）

​    任何一行都不能超过100个字符

### 3.1.1 type

​    用于说明 commit 的类别，类型包含如下几种

- feat: A new feature
- fix: A bug fix
- docs: Documentation only changes
- style: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)
- refactor: A code change that neither fixes a bug nor adds a feature
- perf: A code change that improves performance
- test: Adding missing or correcting existing tests
- chore: Changes to the build process or auxiliary tools and libraries such as documentation generation
- revert: Reverts a previous commit
- build: Changes that affect the build system or external dependencies (example scopes: gulp, broccoli, npm)
- ci: Changes to our CI configuration files and scripts (example scopes: Travis, Circle, BrowserStack, SauceLabs)

​    如果type为feat和fix，则该 commit 将肯定出现在 Change log 之中。其他情况由你决定，要不要放入 Change log。

### 3.1.2 scope

​    用于说明 commit 影响的范围，比如数据层、控制层、视图层等等，视项目不同而不同

### 3.1.3 subject

​    subject是 commit 目的的简短描述

## 3.2 Body

​    Body 部分是对本次 commit 的详细描述，可以分成多行

## 3.3 Footer

​    Footer 部分只用于两种情况

### 3.3.1 不兼容变动

​    如果当前代码与上一个版本不兼容，则 Footer 部分以BREAKING CHANGE开头，后面是对变动的描述、以及变动理由和迁移方法

### 3.3.2 关闭问题

​    如果当前 commit 针对某个issue，那么可以在 Footer 部分关闭这个 issue。

​    如：Closes #123, #245, #992

# 4.工具约束

​    我们的目标还是要通过工具生成和约束

## 4.1 Commitizen

​    [commitizen/cz-cli](https://github.com/commitizen/cz-cli) 代替git commit

​    我们需要借助它提供的 git cz 命令替代我们的 git commit 命令, 帮助我们生成符合规范的 commit message

```
# 如何安装，在安装之前请先安装npm 和 node
# 全局安装 commitizen
npm install commitizen -g
```

​    除此之外, 我们还需要为  commitizen 指定一个 Adapter 比如: cz-conventional-changelog (一个符合 Angular团队规范的  preset). 使得 commitizen 按照我们指定的规范帮助我们生成 commit message

```
# 进入到我们项目的根目录

cd your_repo_root_path
# 初始化package.json
npm init --yes
# 为commitizen指定适配器
commitizen init cz-conventional-changelog --save-dev --save-exact
```

​    现在我们就可以用git cz去进行提交了



但是问题来了，如果我们此时还用git commit -m "" 去提交，也是允许的，但是这肯定不是我们想要的，因为我们需要对message进行格式限制，所以，我们需要下面的检验插件commitlint + Husky

## 4.2 Commitlint + Husky

   [commitlint](https://github.com/marionebl/commitlint)可以帮助我们检查校验提交的message

   如果我们提交的不符合指向的规范, 直接拒绝提交

   校验 commit message 的最佳方式是结合 git hook, 所以需要配合Husky

```
# 在我们的项目根目录
npm install --save-dev @commitlint/{config-conventional,cli}
npm install husky --save-dev
# 在package.json尾部加入如下结构
"husky": {
	"hooks": {
      "commit-msg": "commitlint -E HUSKY_GIT_PARAMS"
    }  
}


# 在项目根目录新增文件commitlint.config.js
module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
  'type-enum': [2, 'always', [
     "feat", "fix", "docs", "style", "refactor", "perf", "test", "build", "ci", "chore", "revert"
   ]],
  'scope-empty': [2, 'never'],
  'subject-full-stop': [0, 'never'], 
  'subject-case': [0, 'never']
  }
}; 
```

​    现在，我们可以在试试git commit -m "test"看看是否可以正常提交，应该会得到下面的拦截记录



## 4.3 Standard Version

​    通过以上工具的帮助, 我们的工程 commit message 应该是符合Angular团队那套，这样也便于我们借助[standard-version](https://github.com/conventional-changelog/standard-version)这样的工具, 自动生成 CHANGELOG, 甚至是语义化的版本号(Semantic Version)

```
# 在项目根目录
npm install --save-dev standard-version
# 在scripts结构体中加入执行脚本 package.json 
{
  "scripts": {
    "release": "standard-version"
  }
}
# 生成changelog
# 第一次生成
npm run release -- --first-release
# 后续生成
npm run release
```

会在我们项目根目录生成一个CHANGELOG.md文件，如下所示

![img](http://192.168.1.200:8090/download/attachments/10226893/WX20181218-140133.png?version=1&modificationDate=1545112925000&api=v2)

# 5.项目中如何使用

​    如果我们已经完成了上述操作，会发现我们最终会得到一个package.json，我们只需要把package.json / commitlint.config.js提交版本库即可

​    把node_modules 和 package-lock.json都加入git忽略文件

​    下次再新clone项目后，直接在项目根目录运行npm install即可完成上述所有步骤



PS：NPM有时候国外镜像不稳定，可以切换淘宝镜像

```
npm config set registry https://registry.npm.taobao.org
```



# 6.补充

## 6.1 node 版本过低解决方案

### 6.1.1 源码安装

已配置的项目：

1）安装 node

Ubuntu 上请从官网 <https://nodejs.org/> 下载安装，而不用 apt-get install （版本过旧）。

例如解压到 ~ 目录，可以添加 PATH 进 ~/.bashrc ：

```
export PATH="/home/john/node-v10.14.2-linux-x64/bin:$PATH"
```

2）安装开发依赖

进入项目根目录，执行：

```
npm install commitizen -g; npm install
```

此后，git cz 就可以提交了。

p.s. commitizen (提供 git cz) 装进了 global ，仅 npm install (提供 commitlint) 不会安装它。

### 6.1.2 卸载安装

系统信息：

操作系统：Ubuntu16.04
问题

通过sudo apt-get install nodejs-legacy 安装node.js后，查看node -v 会发现安装的是4.2.6的低版本，而目前最新稳定版本是v9.4.0
解决方案

在root下运行：

```shell
sudo npm cache clean -f
sudo npm install -g n
sudo n stable  //升级为最新稳定版本的node.js
```

或者可以将最后一行替换为

sudo n latest  //升级为最新版本的node.js

