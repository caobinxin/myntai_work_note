# Copyright (C) 2011 The Android-x86 Open Source Project

LOCAL_PATH := $(my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := main.c http.c base64.c
#LOCAL_CFLAGS := -Werror

ifeq ($(TARGET_ARCH),x86_64)
LOCAL_CFLAGS += -DOS_ARCH_X86_64
else
LOCAL_CFLAGS += -DOS_ARCH_X86
endif

LOCAL_MODULE := phoenix_compat
LOCAL_SHARED_LIBRARIES := libcutils #liblog
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)

include $(BUILD_EXECUTABLE)
