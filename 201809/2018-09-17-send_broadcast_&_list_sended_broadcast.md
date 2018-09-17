# send broadcast & list sended broadcast

am命令发送广播：

am broadcast -a android.intent.action.BOOT_COMPLETED

就发送了一条开机广播，其他广播类似，其基本格式为：

am broadcast -a 你的广播



​    同样，在开发过程中，有时候想知道我们自己定义的广播是否成功发送，或者是某些情况触发了系统的什么广播，那么下文告诉各位一个小技巧：

命令行执行：

dumpsys | grep BroadcastRecord

结果如下所示：

   BroadcastRecord{114315e u0 com.taobao.accs.intent.action.COMMAND} to user 0
​    BroadcastRecord{452e20c u0 android.content.jobscheduler.JOB_DELAY_EXPIRED} to user 0
​    BroadcastRecord{9721755 u0 android.content.jobscheduler.JOB_DEADLINE_EXPIRED} to user 0