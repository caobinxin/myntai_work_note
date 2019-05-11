# Linux（Ubuntu18.04）安装 VS Code

1. 下载地址：VSCode https://code.visualstudio.com/docs?start=true

2. 在安装包文件夹，

sudo dpkg -i code_1.24.1-1528912196_amd64.deb

或者

双击安装包，再点击Install即可。

3.在安装好 VS code 之后，你可以安装官方插件，方便你的使用：在VsCode的”扩展”（Ctrl+Shift+X）中搜索需要的插件,点击安装按钮即可(关于C++的插件)

4.修改成中文

按 ctrl+shift+p  ，在上方输入框输入“language”选择Configure Display Language,


中文设置成英文输入 “配置语言”  打开locale.json 设置 "locale":"en"

英文设置成中文输入 "Configure Language"  打开locale.json  "locale":"zh-CN"

也就是注释掉原来的，把en换成zh-CN即可！