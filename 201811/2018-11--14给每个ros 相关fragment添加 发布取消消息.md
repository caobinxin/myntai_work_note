# 给每个ros 相关fragment添加 发布取消消息



上下班　主要给他取消就可以（这样就可以　不乱跑了）

```java
public static final String ROBOT_CANCEL_ACTION = "/move_base/cancel";　//机器人导航目标取消
moveBaseActionCancel（）
MoveBaseAction.getInstance().moveBaseActionCancel();
```