# ubuntu下安装gitbook并生成电子书

## 一、安装 Node.js

​        由于 GitBook 是基于 Node.js 开发的，所以依赖 Node.js 环境。如果您的系统中还未安装 Node.js，去官网下载nodejs，根据你所使用的系统下载对应的版本。如果已安装则略过本步骤。

​        解压到software文件夹：

![img](https://img-blog.csdn.net/20180912101615327?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3NodWFpZ2V6aG91MTIz/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

​        在bin目录下就有了node，npm，npx三个文件，进入bin文件夹，输入以下命令

![img](https://img-blog.csdn.net/20180912102004161?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3NodWFpZ2V6aG91MTIz/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

​        想要在任何目录下都能运行，只需要建立符号链接到/usr/local/bin下就行了 
​        **注意符号链接 写成绝对路径，不要用相对路径**

```
sudo ln -s /home/gz/software/node-v8.12.0-linux-x86/bin/node /usr/local/bin/node



sudo ln -s /home/gz/software/node-v8.12.0-linux-x86/bin/npm /usr/local/bin/npm
```

## 二、安装 GitBook

​        gitbook Ubuntu下，在安装好nodejs后直接使用如下命令安装,依据自己安装的目录可能有权限问题，自行加上

```
npm install -g gitbook-cli
```

​        由于网络的原因，安装的时间可能会较长一些，请耐心等待直到安装完成。安装完成后，同样的在bin目录下（其他路径不能运行）设置一个符号链接。 

```
sudo ln -s /home/gz/software/node-v8.12.0-linux-x86/bin/gitbook /usr/local/bin/gitbook
```

​        然后查看gitbook版本，检查是否安装成功。

```
gitbook -V #查看gitbook版本
```

## 三、创建电子书项目

​        新建一个目录，并进入该目录使用 gitbook 命令初始化电子书项目。举个例子，现在要创建一个名为“MyFirstBook”的空白电子书项目，如下所示：

```
mkdir MyFirstBook



cd MyFirstBook



gitbook init
```

## 四、编辑电子书内容

​         初始化后的目录中会出现“README.md（电子书简介文件）”和“SUMMARY.md（导航目录文件）”两个基本文件。除此之外还可以手动新建其它“Glossary.md（书尾的词汇表）”、“book.json（电子书配置文件）”。电子书的正文内容可以根据自己的喜好创建新的后缀为  .md 文件，如“chapter01.md”，然后用 MarkDown 编写具体的文本内容即可。下面对这些文件分别做详细介绍。

### 1、README.md

​        此文件是简单的电子书介绍，可以把您所制作的电子书做一下简单的描述：

```
# 简介



 



这是我的第一本使用 GitBook 制作的电子书。
```

### 2、SUMMARY.md

​        此为电子书的导航目录文件，每当新增一个章节文件就需要向此文件中添加一条记录。对于 Kindle 电子书来说，此文件所呈现的目录结构就是开头的目录内容和“前往”的目录导航。

```
# Summary



 



* [简介](README.md)



* [第一章](section1/README.md)



* [第二章](section2/README.md)
```

​        如果需要“子章节”可以使用 `Tab` 缩进来实现（最多支持三级标题），如下所示：

```
# Summary



 



* [第一章](section1/README.md)



    * [第一节](section1/example1.md)



    * [第二节](section1/example2.md)



* [第二章](section2/README.md)



    * [第一节](section2/example1.md)
```

### 3、Glossary.md

​        对于电子书内容中需要解释的词汇可在此文件中定义。词汇表会被放在电子书末尾。其格式如下所示：

```
# 电子书



电子书是指将文字、图片、声音、影像等讯息内容数字化的出版物和植入或下载数字化文字、图片、声音、影像等讯息内容的集存储和显示终端于一体的手持阅读器。



 



# Kindle



Amazon Kindle 是由 Amazon 设计和销售的电子书阅读器（以及软件平台）。用户可以通过无线网络使用 Amazon Kindle 购买、下载和阅读电子书、报纸、杂志、博客及其他电子媒体。
```

### 4、book.json

​        “book.json”是电子书的配置文件，可以看作是电子书的“原数据”，比如 title、description、isbn、language、direction、styles 等。它的基本结构如下所示：

```
{



    "title": "我的第一本電子書",



    "description": "用 GitBook 制作的第一本電子書！",



    "isbn": "978-3-16-148410-0",



    "language": "zh-tw",



    "direction": "ltr"



}
```

### 5、普通章节.md 文件

​        普通章节.md 文件可以使用您感觉顺手的文本编辑器编写。MarkDown 的写法可以查看相关示例。每编写一个 .md 文件，不要忘了在“SUMMARY.md”文件中添加一条记录哦。

### 6、电子书封面图片

​        [GitBook 帮助文档](http://help.gitbook.com/format/cover.html)建议封面图片的尺寸为 1800*2360 像素并且遵循建议：

- 没有边框
- 清晰可见的书本标题
- 任何重要的文字在小版本中应该可见

​        图片的格式为 jpg 格式。把图片重命名为“cover.jpg”放到电子书项目文件夹即可。

## 五、预览电子书内容

​        电子书内容编写完毕后可以使用浏览器预览一下。先输入下面的命令据 .md 文件生成 HTML 文档：

```
$ gitbook build
```

​        生成完毕后，会在电子书项目目录中出现一个名为“_book”的文件夹。进入该文件夹，直接用浏览器打开“index.html”，或先输入下面的命令：

```
$ gitbook serve
```

​        然后在浏览器中输入“[http://localhost:4000](http://localhost:4000/)”即可预览电子书内容，预览完毕后按 `Ctrl + C` 结束。

## 六、生成电子书文件

​        确定电子书没有问题后，可以通过输入以下命令生成 mobi 电子书：

```
$ gitbook mobi ./ ./MyFirstBook.mobi
```

​        如果出现错误，说明还未安装Calibre。

​        运行安装脚本：

```
sudo -v && wget -nv -O- https://raw.githubusercontent.com/kovidgoyal/calibre/master/setup/linux-installer.py | sudo python -c "import sys; main=lambda:sys.stderr.write('Download failed\n'); exec(sys.stdin.read()); main()"
```

​        运行Calibre：

```
$ calibre
```

​        再次运行转换命令，即可生成 mobi 格式电子书。