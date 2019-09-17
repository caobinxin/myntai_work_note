/*
 * =====================================================================================
 *
 *       Filename:  chaozhuo.h
 *
 *    Description:  
 *
 *        Version:  1.0
 *        Created:  2017年03月02日 11时37分53秒
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  YOUR NAME (), 
 *   Organization:  
 *
 * =====================================================================================
 */


#ifndef _CHAOZHUO_H_
#define _CHAOZHUO_H_

//#define TEST_VERSION


/* ***************************************************** */
// LOG FILE
#define LOGFILE_LINE_MAX_LEN   1024
/* ***************************************************** */

/* ***************************************************** */
#ifdef TEST_VERSION
#define HTTPADDR_CZ_API           "http://api.test.chaozhuo.org"
#else
//TODO
#define HTTPADDR_CZ_API           "http://api.phenixos.com"
#endif

#define WIN_LOG_ADDR              "/data/system/phoenixos.log"
#define PHOENIX_LOG_ADDR          "/data/system/phoenixos1.log"

#define HTTPADDR_CZ_BOOT_COMPAT        "/v1/compatibility/mark/"
#define HTTPADDR_CZ_RUN_COMPAT         "/v1/compatibility/running/"

#define CHANNEL_PROPERTY           "persist.sys.phoenix.channel"

#define QUERY_LEN_MAX        4096        
#define BASE64_LEN           4096        
#define HTTP_RET_LEN         1024        
/* ***************************************************** */


/* ***************************************************** */
#define NAME_UID            "did"
#define NAME_OS_VERSION     "os_version"
#define NAME_CHANNEL_ID     "channel"
#define NAME_SUB_CHANNEL_ID "channel_sub"

char *device_info_type[32] = {"cpu", "video", "sound", "net"};

#define TYPE_CPU            1
#define TYPE_VIDEO          2
#define TYPE_SOUND          3
#define TYPE_NET            4

#if 0
#define NAME_CPU            "cpu"

#define NAME_NET1           "net"
#define NAME_NET2           "net2"
#define NAME_NET3           "net3"

#define NAME_VIDEO1         "video"
#define NAME_VIDEO2         "video2"

#define NAME_SOUND1         "sound"
#define NAME_SOUND2         "sound2"

#define NAME_CPU_STAT       "cpu_stat"

#define NAME_NET1_STAT      "net_stat"
#define NAME_NET2_STAT      "net2_stat"
#define NAME_NET3_STAT      "net3_stat"

#define NAME_VIDEO1_STAT    "video_stat"
#define NAME_VIDEO2_STAT    "video2_stat"

#define NAME_SOUND1_STAT    "sound_stat"
#define NAME_SOUND2_STAT    "sound2_stat"
#endif
/* ***************************************************** */


/* ***************************************************** */
#define STAT_UNKNOWN        0
#define STAT_SUCCESS        1
#define STAT_FAILURE        2
/* ***************************************************** */


/* ***************************************************** */
/* UID name max length */
#define UID_NAME_MAX_LEN        (32 + 1)
/* PhoenixOS version max length */
#define OS_VERSION_NAME_MAX_LEN (8 + 1)
/* Channel name max length */
#define CHANNEL_NAME_MAX_LEN    (16 + 1)
/* Subchannel name max length */
#define SUBCHANNEL_NAME_MAX_LEN (16 + 1)
/* Boot name max length */
#define BOOT_NAME_MAX_LEN       (16 + 1)

/* Device data ID length */
#define DEVICE_ID_LEN           (8 + 1)

/* Max device number, cpu=1, video=2, net=3, sound=3 */
#define CPU_NUM_MAX             1
#define VIDEO_NUM_MAX           2
#define NET_NUM_MAX             3
#define SOUND_NUM_MAX           3

#define DEVICE_NUM_MAX          (CPU_NUM_MAX + VIDEO_NUM_MAX + NET_NUM_MAX + SOUND_NUM_MAX)
#define WIN_FILE_LINE_MAX       (DEVICE_NUM_MAX + 5)
/* ***************************************************** */


/* ***************************************************** */
#define USE_MODALIAS            1

#define SYS_NET_ETH             "/sys/class/net/eth"
#define SYS_NET_WLAN            "/sys/class/net/wlan"

#define SYS_NET_WLAN_MODALIAS    "/device/modalias"

#ifdef USE_MODALIAS
#define SYS_NET_ETH_MODALIAS    "/device/modalias"
#else
#define SYS_NET_ETH_UID         "/device/vendor"
#define SYS_NET_ETH_PID         "/device/device"
#endif



#define ETH_NUM_MAX             2
#define WLAN_NUM_MAX             2

#define CMD_LSPCI               "/system/xbin/lspci -mk"
/* ***************************************************** */

struct device_data {
    int type;
    int stat;
    char id[DEVICE_ID_LEN];
};

struct machine_data {
    char uid[UID_NAME_MAX_LEN];
    char os_version[OS_VERSION_NAME_MAX_LEN];
    char channel[CHANNEL_NAME_MAX_LEN];
    char sub_channel[SUBCHANNEL_NAME_MAX_LEN];
    char boot[BOOT_NAME_MAX_LEN];

    int device_num;
    struct device_data device[DEVICE_NUM_MAX];
};

#endif
