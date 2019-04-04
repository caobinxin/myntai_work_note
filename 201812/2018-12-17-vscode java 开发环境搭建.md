# vscode java 开发环境搭建

> 出自微软的Visual Studio Code  并不是一个 IDE，它是个有理想，有前途的编辑器，通过相应语言的插件，可以将其包装成一个 轻量级的功能完善的IDE。
>
>  自从遇见了她，真的是对她一见钟情不能自拔~ 
> 在IDEA之余最爱用的就是它了，并且它是免费，免费，免费的，插件生态圈很活跃，vs code本身版本更新迭代也基本两周左右更新一次，真心无比贴心、强大~

------

> 本文内容基于 V1.27.2 版本:

> 首先下载最新版本 V1.27.2：[点我去下载](https://code.visualstudio.com/Download)
>  然后依次安装以下插件：

------

> 1、Chinese (Simplified) Language Pack for Visual Studio Code
>  中文包，虽然认识几个字母，但还是感觉中文好用点… 这个是汉化整个编辑器菜单内容的

------

> 2、Atom One Dark Theme
>  这个是一个主题包，个人比较喜欢它的风格，不喜欢的可以不装

------

> 3、Atom One Dark Theme
>  这个是一个目录图标，当你打开目录时，文件树显示各种彩色图标看着舒服

------

> 前面3步骤的操作动图如下：
>  ![在这里插入图片描述](https://img-blog.csdn.net/20180927221001210?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3hpYW9jeTY2/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

------

> 4、Java Extension Pack
>  敲黑板！！ 这个就是我们搭建java开发环境核心的一个插件了，它还自带其它三个插件：
>
> - Language Support for Java™ by Red Hat java语法
> - Debugger for Java java 调试
> - Java Test Runner  java测试
> - Maven Project Explorer   支持maven

------

> 第4步的操作动图如下：
>  ![在这里插入图片描述](https://img-blog.csdn.net/20180927220919658?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3hpYW9jeTY2/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

------

> 安装好这些扩展插件后，记得要重启VS Code 才能生效

------

> 在java代码中print输出中文时，会显示乱码，这个时候先在vs 打开.java文件到编辑器界面，然后在vs code 界面右下角，点以下那个默认的 UTF-8 ，然后在弹出的选项选择第二个：以编码方式保存，然后在接下来弹出的下拉选项输入框输入GBK，选择gbk编码回车（或者直接鼠标点以下GBK这一行），等待左下角compile完成后，再次运行就能输出无乱码的中文了。

------

> debug调试功能的动图如下：
>  ![在这里插入图片描述](https://img-blog.csdn.net/20180927220747546?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3hpYW9jeTY2/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

VScode Icon  根据提示安装这个 主题的图标