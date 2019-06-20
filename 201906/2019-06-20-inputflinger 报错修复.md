# inputflinger 报错修复

报错的内容：

```shell
06-20 10:54:46.996  3975  3975 I InputFlinger: InputFlinger is starting                                   
06-20 10:54:46.996  1967  1998 I chatty  : uid=1000(system) InputDispatcher expire 3600 lines                         
06-20 10:54:46.996  3975  3975 F InputDriver: Input module evdev not found                                    
06-20 10:54:46.996  3975  3975 F libc    : Fatal signal 6 (SIGABRT), code -6 in tid 3975 (inputflinger)  
06-20 10:54:46.996  1646  1646 W         : debuggerd: handling request: pid=3975 uid=1000 gid=1004 tid=3975           
06-20 10:54:47.048  3976  3976 F DEBUG   : *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***     
06-20 10:54:47.048  3976  3976 F DEBUG   : Build fingerprint: 'Android-x86/android_x86_64/x86_64:7.1.1/NMF26O/colby051
06-20 10:54:47.048  3976  3976 F DEBUG   : Revision: '0'                                                              
06-20 10:54:47.048  3976  3976 F DEBUG   : ABI: 'x86_64'                                                              
06-20 10:54:47.048  3976  3976 F DEBUG   : pid: 3975, tid: 3975, name: inputflinger  >>> /system/bin/inputflinger <<< 
06-20 10:54:47.048  3976  3976 F DEBUG   : signal 6 (SIGABRT), code -6 (SI_TKILL), fault addr --------                
06-20 10:54:47.048  3976  3976 F DEBUG   : Abort message: 'Input module evdev not found'                  
06-20 10:54:47.048  3976  3976 F DEBUG   :     rax 0000000000000000  rbx 0000749f99b40be8  rcx 0000749f9948dd87  rdx 0
06-20 10:54:47.048  3976  3976 F DEBUG   :     rsi 0000000000000f87  rdi 0000000000000f87                      
06-20 10:54:47.048  3976  3976 F DEBUG   :     r8  0000000000000000  r9  0000749f992d2090  r10 0000000000000008  r11 0
06-20 10:54:47.048  3976  3976 F DEBUG   :     r12 0000000000000f87  r13 0000000000000006  r14 0000749f993fa9f1  r15 0
06-20 10:54:47.048  3976  3976 F DEBUG   :     cs  0000000000000033  ss  000000000000002b                             
06-20 10:54:47.048  3976  3976 F DEBUG   :     rip 0000749f9948dd87  rbp 0000000000000002  rsp 00007ffd4e49cd48  eflag
06-20 10:54:47.049  3976  3976 F DEBUG   :                                                                            
06-20 10:54:47.049  3976  3976 F DEBUG   : backtrace:                                                                 
06-20 10:54:47.049  3976  3976 F DEBUG   :     #00 pc 000000000008dd87  /system/lib64/libc.so (tgkill+7)              
06-20 10:54:47.049  3976  3976 F DEBUG   :     #01 pc 000000000008a7e1  /system/lib64/libc.so (pthread_kill+65)       
06-20 10:54:47.049  3976  3976 F DEBUG   :     #02 pc 0000000000030301  /system/lib64/libc.so (raise+17)
06-20 10:54:47.049  3976  3976 F DEBUG   :     #03 pc 000000000002883d  /system/lib64/libc.so (abort+77)              
06-20 10:54:47.049  3976  3976 F DEBUG   :     #04 pc 000000000000575f  /system/lib64/liblog.so (__android_log_assert+
06-20 10:54:47.049  3976  3976 F DEBUG   :     #05 pc 0000000000008f5f  /system/lib64/libinputflingerhost.so (_ZN7andr
06-20 10:54:47.049  3976  3976 F DEBUG   :     #06 pc 00000000000084f7  /system/lib64/libinputflingerhost.so (_ZN7andr
06-20 10:54:47.049  3976  3976 F DEBUG   :     #07 pc 0000000000000c4b  /system/bin/inputflinger                      
06-20 10:54:47.049  3976  3976 F DEBUG   :     #08 pc 0000000000000bbf  /system/bin/inputflinger                      
06-20 10:54:47.049  3976  3976 F DEBUG   :     #09 pc 000000000001c994  /system/lib64/libc.so (__libc_init+84)        
06-20 10:54:47.049  3976  3976 F DEBUG   :     #10 pc 0000000000000ae7  /system/bin/inputflinger
```

![](2019-06-20-inputflinger 报错修复.assets/2019-06-20 11-04-32 的屏幕截图.png)

