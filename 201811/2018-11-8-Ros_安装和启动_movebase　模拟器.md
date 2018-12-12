# Ros 安装和启动　movebase　模拟器

### 1.1 安装模拟器

```shell
colby@colby-myntai:~$ sudo apt-cache search ros-kinetic-teb
ros-kinetic-teb-local-planner - The teb_local_planner package implements a plugin to the base_local_planner of the 2D navigation stack.
ros-kinetic-teb-local-planner-tutorials - The teb_local_planner_tutorials package

 
 sudo apt-get install ros-kinetic-teb-local-planner-tutorials
 sudo apt-get install ros-kinetic-global-planner 
```

### 1.2 启动模拟器

```shell
colby@colby-myntai:/opt/ros/kinetic/share/teb_local_planner_tutorials/launch$ 
roslaunch robot_diff_drive_in_stage_costmap_conversion.launch
```

### 1.3 观察　odom（相对坐标系的值）

```shell
rostopic echo /odom
```

