# wpa_supplicant

## 1 编译

device/generic/common/BoardConfig.mk

板子相关的配置 编译，在这里都可以找到。 例如kernel命令行传参

```makefile
BOARD_WPA_SUPPLICANT_DRIVER ?= NL80211
BOARD_WPA_SUPPLICANT_PRIVATE_LIB ?= private_lib_driver_cmd                                                            
WPA_SUPPLICANT_VERSION ?= VER_2_1_DEVEL
WIFI_DRIVER_MODULE_PATH ?= auto
```

