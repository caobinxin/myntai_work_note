# TensorFlow学习笔记（UTF-8 问题解决 UnicodeDecodeError: 'utf-8' codec can't decode byte 0xff in position 0: in



  UnicodeDecodeError: 'utf-8' codec can't decode byte 0xff in position 0: invalid start byte

 在是使用Tensorflow读取图片文件的情况下，会出现这个报错

 代码如下

```python
import matplotlib.pyplot as plt 
import tensorflow as tf 

image_raw_data = tf.gfile.FastGFile('./read_img/res/img1/timg.jpeg', 'r').read()
```

报错：

UnicodeDecodeError: 'utf-8' codec can't decode byte 0xff in position 0: invalid start byte

修改后：

```python
import matplotlib.pyplot as plt 
import tensorflow as tf 

image_raw_data = tf.gfile.FastGFile('./read_img/res/img1/timg.jpeg', 'rb').read()
```

