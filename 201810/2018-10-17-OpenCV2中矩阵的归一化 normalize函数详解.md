# OpenCV2中矩阵的归一化 normalize函数详解

1. 归一化定义与作用

    归一化就是要把需要处理的数据经过处理后（通过某种算法）限制在你需要的一定范围内。

首先归一化是为了后面数据处理的方便，其次是保证程序运行时收敛加快。归一化的具体作用是归纳统一样本的统计分布性。归一化在0-1之间是统计的概率分布，归一化在某个区间上是统计的坐标分布。归一化有同一、统一和合一的意思。

    归一化的目的，是使得没有可比性的数据变得具有可比性，同时又保持相比较的两个数据之间的相对关系，如大小关系；或是为了作图，原来很难在一张图上作出来，归一化后就可以很方便的给出图上的相对位置等。
    
    在使用机器学习算法的数据预处理阶段，归一化也是非常重要的一个步骤。例如在应用SVM之前，缩放是非常重要的。Sarle的神经网络FAQ的第二部分（1997）阐述了缩放的重要性，大多数注意事项也适用于SVM。缩放的最主要优点是能够避免大数值区间的属性过分支配了小数值区间的属性。另一个优点能避免计算过程中数值复杂度。因为关键值通常依赖特征向量的内积（inner products），例如，线性核和多项式核力，属性的大数值可能会导致数值问题。我们推荐将每个属性线性缩放到区间[-1,+1]或者[0, 1]。
    
    当然，我们必须使用同样的方法缩放训练数据和测试数据。例如，假设我们把训练数据的第一个属性从[-10,+10]缩放到[-1, +1]，那么如果测试数据的第一个属性属于区间[-11, +8]，我们必须将测试数据转变成[-1.1, +0.8]。

:  参考：“SVM：从理论到OpenCV实践” 4.2 归一化数据：

http://blog.csdn.net/zhazhiqiang/article/details/20146243
2. normalize 函数介绍

函数原型：

    void normalize(InputArray src,OutputArray dst, double alpha=1, doublebeta=0, int norm_type=NORM_L2, int dtype=-1, InputArray mask=noArray() )
    
    该函数归一化输入数组使它的范数或者数值范围在一定的范围内。

Parameters:

src  输入数组

dst 输出数组，支持原地运算

alpha

    range normalization模式的最小值

beta

    range normalization模式的最大值，不用于norm normalization(范数归一化)模式。

normType

    归一化的类型，可以有以下的取值：
    
    NORM_MINMAX:数组的数值被平移或缩放到一个指定的范围，线性归一化，一般较常用。
    
    NORM_INF: 此类型的定义没有查到，根据OpenCV 1的对应项，可能是归一化数组的C-范数(绝对值的最大值)
    
    NORM_L1 :  归一化数组的L1-范数(绝对值的和)
    
    NORM_L2: 归一化数组的(欧几里德)L2-范数

dtype

    dtype为负数时，输出数组的type与输入数组的type相同；

否则，输出数组与输入数组只是通道数相同，而tpye=CV_MAT_DEPTH(dtype).

mask

    操作掩膜，用于指示函数是否仅仅对指定的元素进行操作。
3.归一化公式：

1、线性函数转换，表达式如下：（对应NORM_MINMAX）

ifmask(i,j)!=0

    dst(i,j)=(src(i,j)-min(src))*(b‘-a‘)/(max(src)-min(src))+ a‘

else

     dst(i,j)=src(i,j)
    
    其中b‘=MAX(a,b), a‘=MIN(a,b);

2. 当norm_type!=CV_MINMAX:

ifmask(i,j)!=0

    dst(i,j)=src(i,j)*a/norm (src,norm_type,mask)

else

    dst(i,j)=src(i,j)
    
    其中，函数norm的功能是计算norm（范数）的绝对值

Thefunctions norm calculate an absolute norm of src1 (when there is no src2 ):

技术分享

---------------------
作者：lanmeng_smile 
来源：CSDN 
原文：https://blog.csdn.net/lanmeng_smile/article/details/49903865 
版权声明：本文为博主原创文章，转载请附上博文链接！