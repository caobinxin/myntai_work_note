# 内核

 make mrproper 

确保 kernel 的源代码目录下没有不真确的.o等文件，并且该目录下各文件的依赖性是完整的。

```shell
colby@colby-myntai:~/androidos/bottom_layer_work/FireNow-Nougat/kernel$ make mrproper 
  CLEAN   .
  CLEAN   arch/arm64/kernel/vdso
  CLEAN   arch/arm64/kernel
  CLEAN   drivers/video/logo
  CLEAN   drivers/video/rockchip/screen
  CLEAN   firmware
  CLEAN   lib
  CLEAN   security/selinux
  CLEAN   usr
  CLEAN   arch/arm64/boot
  CLEAN   arch/arm64/boot/dts/rockchip
  CLEAN   .tmp_versions
  CLEAN   scripts/basic
  CLEAN   scripts/dtc
  CLEAN   scripts/kconfig
  CLEAN   scripts/mod
  CLEAN   scripts/selinux/genheaders
  CLEAN   scripts/selinux/mdp
  CLEAN   scripts
  CLEAN   include/config include/generated arch/arm64/include/generated
  CLEAN   .config .config.old .version Module.symvers

```

Linux内核编译使用的是，gnuc,而不是标准的 ansic,  gunc 对标准c 做了增强和扩展：

1. 允许长度为 0 的数组

```c
struct linear_array{
    int length;
    char data[0];
};
char data[0] 就意味除了 为结构体 linear_array 的实例分配内存外，并不会为数组data[] 分配内存，因此sizeof(struct linear_array) = sizeof(int),而在程序需要的时候，可再为数组data申请内存。
```

2. 支持case x...y语句，以相应满足区间[x,y]的值的分支条件。

```c
char c;
switch(char_x){
    case '0'...'9': 
        c = '0';
        break;
}
```

3. 引入了 typeof 关键字

该关键字是一个运算符，用于求得某个变量的类型。

```c
#define min_var(x,y) ({\
const typeof(x) _x = (x);\
const typeof(y) _y = (y);\
(void)(&_x == &_y);\
_x < _y ? _x : _y;})

代码行(void)(&_x == &_y); 用来检查_x 和 _y的类型是否一致。如果一致，将在程序编译时报指针类型不一致的错误。
```

4. 扩展宏的参数可以变

   在gunc中，不仅函数参数可以变，而且宏的参数也可以变。这里的可变是指，参数的个数与类型可以不固定。标准c中的printf函数就是这种情况，其中的变量将随着format的不同而有变化。

   ```c
   int printf(const char *format,...);
   而在 gunc中，宏也可以接受可变数目的参数，
   #define pr_debug(fmt, arg...) \
   printk(fmt, ##arg);
   随着fmt的设定，这里arg表示其余的参数可以是零个或多个，例如下列代码：
   pr_debug("%s:%d", filename, line);  ==>  printk("%s:%d", filename, line);
   在上面的宏定义中使用 了## 表示， arg没有任何参数的时候，前面的逗号就变得多余了。使用 ## 之后，gunc预处理 会丢掉前面的逗号， 
   pr_debug("success\n"); ==> printk("success\n");
   而不是 printk("success\n" , ) ;
   ```

   5. 可以按照任意的顺序初始化数组：

   ```c
   unsigned char array[MAX] = { [0 ... MAX-1] = 0} ;
   
   struct file_operations ext2_file_operations = {
       llseek:generic_file_llseek,
       read:generic_file_read,
       write:generic_file_write
   };
   struct file_operations ext2_file_operations = {
       .llseek = generic_file_llseek,
       .read = generic_file_read,
       .write = generic_file_write
   };
   ```

   6. 引入两个保存当前函数名的标识符。

   ——FUNCTION—— ： 用来保存源码中所用的 函数名

    ——PRETTY_FUNCTION——：会根据函数所在的语言环境的不同有所变化。在gunc语言环境中，这两个标识符的值是一样的。

   ```c
   void test(){
       printf("this function:%s\n", __FUNCTION__) ;
   }
   ```

   7. 允许声明函数、变量和类型的特殊属性。

      这个增强有利于进行手工代码优化、定制和代码检查。通过在函数、变量和类型后面添加 ——attribute_—（（ATTRIBUTE））.其中ATTRIBUTE为属性说明，如果存在多个属性，则以逗号分隔。

      ```c
      noreturn 属性用于函数，表示函数从不返回。这会让编译器优化代码，并消除不必要的警告
      #define ATTRIB_NORET __attribute__((noreturn))
      asmlinkage NORET_TYPE void do_exit(long error_code) ATTRIB_NORET;
      
      
      format 属性也用于函数，表示该函数使用printf,scanf 或 strftime风格的参数，指定format属性可以让编译器根据格式串检查参数的类型。
      asmlinkage int printk(const char *fmt, ...) __attribute__ ((format (printf, 1, 2)));
      printk 函数的第一个参数是 格式串，从第二个参数开始都会根据第一个函数所指定的格式串规则检查参数。
      
      unused 属性作用与函数和比变量，表示该函数或变量可能不会被用到，这个属性可以避免编译器产生警告
      
      aligned 属性用于变量、结构体或联合体，指定变量、结构体或联合体的对齐方式，以字节为单位
      struct example_struct{
      	char a;
      	int b;
      	long c;
      } __attribute__((aligned(4)));
      
      这个例子的属性限定该结构体类型的变量以4字节对齐。
      
      packed属性 作用于变量和类型，用于变量或结构体成员时表示使用最小可能的内存。用于枚举、结构体或联合体时表示该类型使用最小的内存。
      ```

   8. 拓展了标准c的库函数。

      gunc 在扩展的函数的前面加上了 __builtin 以标识，

      ```c
      __builtin_return_address(LEVEL)
          用于告诉将返回当前函数或其调用者的返回地址，参数LEVEL指定调用栈的级数。如0表示当前函数的返回地址，1表示当前函数的调用者的返回地址。
          
      __builtin_constant_p(EXP)
          用于判断一个值 是否为编译时常数，如果参数EXP 的值是常数，函数返回1，否则返回0
         
      __builtin_expect(EXP, C)
          用于为编译器提供分支预测信息，其返回值是整数表达式EXP的值，C的值必须是编译时常数
      ```
