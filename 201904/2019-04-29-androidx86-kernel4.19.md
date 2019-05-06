# androidx86-kernel4.19

## 1 源码下载

查看

```shell
colby@pc:~/androidos/kernel$ git remote -v show 
phoenix	ssh://caobinxin@192.168.1.112:29418/phoenix-kernel-4.4.10 (fetch)
phoenix	ssh://caobinxin@192.168.1.112:29418/phoenix-kernel-4.4.10 (push)
```

直接下载替换：

修改kernel.mk文件

```shell
colby@pc:~/androidos/build/core/tasks$ vim kernel.mk 
```

新的kernel.mk

```makefile
#
# Copyright (C) 2014-2016 The Android-x86 Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#

ifneq ($(TARGET_NO_KERNEL),true)
ifeq ($(TARGET_PREBUILT_KERNEL),)

KERNEL_DIR ?= kernel

ifneq ($(filter x86%,$(TARGET_ARCH)),)
TARGET_KERNEL_ARCH ?= $(TARGET_ARCH)
KERNEL_TARGET := bzImage
TARGET_KERNEL_CONFIG ?= android-$(TARGET_KERNEL_ARCH)_defconfig
KERNEL_CONFIG_DIR := arch/x86/configs
endif
ifeq ($(TARGET_ARCH),arm)
KERNEL_TARGET := zImage
TARGET_KERNEL_CONFIG ?= goldfish_defconfig
KERNEL_CONFIG_DIR := arch/arm/configs
endif

ifeq ($(TARGET_KERNEL_ARCH),x86_64)
CROSS_COMPILE ?= $(abspath prebuilts/gcc/linux-x86/host/x86_64-linux-glibc2.11-4.6/bin)/x86_64-linux-
else
CROSS_COMPILE ?= $(abspath $(TARGET_TOOLS_PREFIX))
endif
KBUILD_OUTPUT := $(abspath $(TARGET_OUT_INTERMEDIATES)/kernel)
mk_kernel := $(hide) $(MAKE) -C $(KERNEL_DIR) O=$(KBUILD_OUTPUT) ARCH=$(TARGET_ARCH) CROSS_COMPILE="$(abspath $(CC_WRAPPER)) $(CROSS_COMPILE)" $(if $(SHOW_COMMANDS),V=1)

KERNEL_CONFIG_FILE := $(if $(wildcard $(TARGET_KERNEL_CONFIG)),$(TARGET_KERNEL_CONFIG),$(KERNEL_DIR)/$(KERNEL_CONFIG_DIR)/$(TARGET_KERNEL_CONFIG))

MOD_ENABLED := $(shell grep ^CONFIG_MODULES=y $(KERNEL_CONFIG_FILE))
FIRMWARE_ENABLED := $(shell grep ^CONFIG_FIRMWARE_IN_KERNEL=y $(KERNEL_CONFIG_FILE))

# I understand Android build system discourage to use submake,
# but I don't want to write a complex Android.mk to build kernel.
# This is the simplest way I can think.
KERNEL_DOTCONFIG_FILE := $(KBUILD_OUTPUT)/.config
KERNEL_ARCH_CHANGED := $(if $(filter 0,$(shell grep -s ^$(if $(filter x86,$(TARGET_KERNEL_ARCH)),\#.)CONFIG_64BIT $(KERNEL_DOTCONFIG_FILE) | wc -l)),FORCE)
$(KERNEL_DOTCONFIG_FILE): $(KERNEL_CONFIG_FILE) $(wildcard $(TARGET_KERNEL_DIFFCONFIG)) $(KERNEL_ARCH_CHANGED)
	$(hide) mkdir -p $(@D) && cat $(wildcard $^) > $@
	$(hide) ln -sf ../../../../../../external $(@D)
	$(mk_kernel) oldnoconfig

# bison is needed to build kernel and external modules from source
BISON := $(HOST_OUT_EXECUTABLES)/bison$(HOST_EXECUTABLE_SUFFIX)

BUILT_KERNEL_TARGET := $(KBUILD_OUTPUT)/arch/$(TARGET_ARCH)/boot/$(KERNEL_TARGET)
$(BUILT_KERNEL_TARGET): $(KERNEL_DOTCONFIG_FILE) | $(BISON)
	$(mk_kernel) $(KERNEL_TARGET) $(if $(MOD_ENABLED),modules)
	$(if $(FIRMWARE_ENABLED),$(mk_kernel) INSTALL_MOD_PATH=$(abspath $(TARGET_OUT)) firmware_install)

ifneq ($(MOD_ENABLED),)
KERNEL_MODULES_DEP := $(firstword $(wildcard $(TARGET_OUT)/lib/modules/*/modules.dep))
KERNEL_MODULES_DEP := $(if $(KERNEL_MODULES_DEP),$(KERNEL_MODULES_DEP),$(TARGET_OUT)/lib/modules)

ALL_EXTRA_MODULES := $(patsubst %,$(TARGET_OUT_INTERMEDIATES)/kmodule/%,$(TARGET_EXTRA_KERNEL_MODULES))
$(ALL_EXTRA_MODULES): $(TARGET_OUT_INTERMEDIATES)/kmodule/%: $(BUILT_KERNEL_TARGET)
	@echo Building additional kernel module $*
	$(hide) mkdir -p $(@D) && $(ACP) -fr $(EXTRA_KERNEL_MODULE_PATH_$*) $(@D)
	$(mk_kernel) M=$(abspath $@) modules

$(KERNEL_MODULES_DEP): $(BUILT_KERNEL_TARGET) $(ALL_EXTRA_MODULES)
	$(hide) rm -rf $(TARGET_OUT)/lib/modules
	$(mk_kernel) INSTALL_MOD_PATH=$(abspath $(TARGET_OUT)) modules_install
	$(hide) for kmod in $(TARGET_EXTRA_KERNEL_MODULES) ; do \
		echo Installing additional kernel module $${kmod} ; \
		$(subst $(hide),,$(mk_kernel)) INSTALL_MOD_PATH=$(abspath $(TARGET_OUT)) M=$(abspath $(TARGET_OUT_INTERMEDIATES))/kmodule/$${kmod} modules_install ; \
	done
	$(hide) rm -f $(TARGET_OUT)/lib/modules/*/{build,source}
endif

$(BUILT_SYSTEMIMAGE): $(KERNEL_MODULES_DEP)

## rules to get source of Broadcom 802.11a/b/g/n hybrid device driver
## based on broadcomsetup.sh of Kyle Evans
#WL_PATH := $(KERNEL_DIR)/drivers/net/wireless/broadcom/wl
#ifeq ($(wildcard $(WL_PATH)/build.mk),)
#WL_PATH := $(KERNEL_DIR)/drivers/net/wireless/wl
#endif
#-include $(WL_PATH)/build.mk

installclean: FILES += $(KBUILD_OUTPUT) $(INSTALLED_KERNEL_TARGET)

TARGET_PREBUILT_KERNEL := $(BUILT_KERNEL_TARGET)

.PHONY: kernel $(if $(KERNEL_ARCH_CHANGED),$(KERNEL_HEADERS_COMMON)/linux/binder.h)
kernel: $(INSTALLED_KERNEL_TARGET)

endif # TARGET_PREBUILT_KERNEL

ifndef CM_BUILD
$(INSTALLED_KERNEL_TARGET): $(TARGET_PREBUILT_KERNEL) | $(ACP)
	$(copy-file-to-new-target)
ifdef TARGET_PREBUILT_MODULES
	mkdir -p $(TARGET_OUT)/lib
	$(hide) cp -r $(TARGET_PREBUILT_MODULES) $(TARGET_OUT)/lib
endif
endif # CM_BUILD
endif # KBUILD_OUTPUT
```

旧的kernel.mk 4.14.15

```makefile

```



## 2 对比

meld



## 3 编译

warning: left shift count >= width of type

https://blog.csdn.net/wh8_2011/article/details/49231221

Linux 64位机器上编译 1<<62，编译提示：warning: left shift count >= width of type

在64位机器上1分配Int类型（4字节）。

错误修改：

1UL << 62 指定1为8字节整数，因此可以解决该问题。

在64位机器上以下类型字的长度

 

| 类型              | 长度（字节） |
| ----------------- | ------------ |
| int               | 4            |
| unsigned int      | 4            |
| long              | 8            |
| unsigned long     | 8            |
| long unsigned int | 8            |
| double            | 8            |



## 4 下载社区源码

http://www.android-x86.org/getsourcecode

```shell
 repo init -u git://git.osdn.net/gitroot/android-x86/manifest -b nougat-x86
 
 repo sync -j8
```

现在手头只有.repo这个目录，这个目录是包含从远端服务器下载下来的代码库的。

在.repo同级目录下，输入repo sync -l

即可checkout出整套代码。对应的分支是.repo/manifests所指向的清单文件。