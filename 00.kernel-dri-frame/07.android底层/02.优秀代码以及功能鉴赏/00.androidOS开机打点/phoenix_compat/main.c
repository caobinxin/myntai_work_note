#define LOG_TAG "phoenix_compat"

#include "chaozhuo.h"

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
//#include <sys/types.h>
#include <dirent.h>
#include <cutils/properties.h>
#include <cutils/log.h>

#include "http.h"
#include "base64.h"

static int read_device_info(char *line, struct machine_data *m_data)
{
    char *temp[6];
    char const *delim = " ";
    int num = 0;
    int device_number = m_data->device_num;

    memset(temp, 0, sizeof(temp));

    /* Split data */
    temp[num++] = strtok(line, delim);
    while ((temp[num++] = strtok(NULL, delim)));

    /* Read device type */
    m_data->device[device_number].type = atoi(temp[0]);

    /* Read device id */
    strncpy(m_data->device[device_number].id, temp[1], strlen(temp[1]));

    /* Read device stat */
    m_data->device[device_number].stat = atoi(temp[2]);

    m_data->device_num++;

    return 0;
}

static int read_data(int line_num, char *line, struct machine_data *m_data)
{
    int ret = 0;

    switch (line_num) {
        /* Read UID */
        case 1:
            if (strlen(line) != UID_NAME_MAX_LEN - 1) {
                ALOGE("UID length(%d) error\n", (int)strlen(line));
                return -1;
            }
            strncpy(m_data->uid, line, UID_NAME_MAX_LEN);
            break;

        /* Read OS version */
        case 2:
            strncpy(m_data->os_version, line, UID_NAME_MAX_LEN);
            break;

        /* Read channel */
        case 3:
            strncpy(m_data->channel, line, UID_NAME_MAX_LEN);
            break;

        /* Read sub channel */
        case 4:
            strncpy(m_data->sub_channel, line, UID_NAME_MAX_LEN);
            break;

        /* Read phoenix boot */
        case 5:
            if (strstr(line, "boot")) {
                strncpy(m_data->boot, line+6, BOOT_NAME_MAX_LEN);
                break;
            } else {
                strncpy(m_data->boot, "unknown", 7);
            }

        /* Read device info */
        default:
            if (line_num > WIN_FILE_LINE_MAX) {
                ALOGW("Windows data have problem.");
                break;
            }

            ret = read_device_info(line, m_data);
            if (ret) {
                ALOGE("Read device info error!\n");
            }
            break;
    }

    return ret;
}

static char *read_windows_line(char *line)
{
    int i;

    if (line[0] == '\n')
        return line;

    /* Replace '\r\n' or '\n' to '\0' */
    for (i = 0; i < (int)strlen(line)+1; i++) {
        if (line[i] == '\n') {
            if (line[i-1] == '\r')
                line[i-1] = 0;
            else
                line[i] = 0;
        }

        /*  
        if (line[i] == '\r' && line[i+1] == '\n') {
            line[i] = 0;
            break;
        } */
    }

    return line;
}

static int analyze_data(FILE *fp, struct machine_data *m_data)
{
    char line[LOGFILE_LINE_MAX_LEN];
    int ret = 0;
    int line_num = 0;

    /* Initialize struct machine_data device number */
    m_data->device_num = 0;

    while (!feof(fp))
    {
        memset(line, 0, LOGFILE_LINE_MAX_LEN);

        line_num++;

        fgets(line, LOGFILE_LINE_MAX_LEN, fp);

        if (line[0] == 0)
            continue;

        read_windows_line(line);

        ret = read_data(line_num, line, m_data);
        if (ret) {
            ALOGE("Read data error!\n");
            return -1;
        }
    }

    return 0;
}

static int get_windows_data(struct machine_data *m_data)
{
    int ret = 0;
    FILE *fp = NULL;

    fp = fopen(WIN_LOG_ADDR, "r");
    if (!fp) {
        ALOGE("Open log file error!\n");
        return -1;
    }
    
    ret = analyze_data(fp, m_data);
    if (ret) {
        ALOGE("Analyze log data error!\n");
        goto error;
    }

error:
    fclose(fp);

    return ret ? -1 : 0;
}

static int set_device_stat(struct machine_data *m_data, int type, int *stat, int num)
{
    int i;
    int type_device_num = num;

    for (i = 0; i < m_data->device_num; i++) {
       if (m_data->device[i].type == type) {
           m_data->device[i].stat = stat[num - type_device_num];
           ALOGD("%s: type = %d, stat[%d] = %d", __func__, 
                   type, i, stat[num-type_device_num]);
           type_device_num--;
       }
    }

    return 0;
}

static int check_cpu_isa(char *arg)
{
    int i, ret = STAT_SUCCESS;
#ifndef OS_ARCH_X86_64
    char const *isa_needed[3] = {"mmx", "sse2", "pni"}; // "sse3" called "pni"
    ALOGD("32bit Phoenix OS need cpu have mmx, sse2/3 isa.");
#else
    char const *isa_needed[6] = {"mmx", "sse2", "pni", "sse4_1", "sse4_2", "popcnt"};
    ALOGD("64bit Phoenix OS need cpu have mmx, popcnt, sse2/3/4.1/4.2 isa.");
#endif

    int isa_len = sizeof(isa_needed) / sizeof(isa_needed[0]);


    for (i = 0; i < isa_len; i++) {
        if (!strstr(arg, isa_needed[i])) {
            ret = STAT_FAILURE;
            ALOGE("Your CPU don't have %s\n", isa_needed[i]);
        }
    }

    return ret;
}

static int check_cpu_stat(struct machine_data *m_data)
{
    FILE *cpuinfo = fopen("/proc/cpuinfo", "rb");
    char *arg = 0;
    size_t size = 0;
    int stat = STAT_UNKNOWN;

    while(getdelim(&arg, &size, 0, cpuinfo) != -1) {
        if (strstr(arg, "flags")) {
            stat = check_cpu_isa(arg);
            if (stat == STAT_UNKNOWN)
                ALOGE("PhoenixOS: CPU is not supported!\n");
            break;
        }
    }

    fclose(cpuinfo);

    set_device_stat(m_data, TYPE_CPU, &stat, 1);

    return 0;
}

static int check_gpu_stat(struct machine_data *m_data)
{
    int i, count = 0;
    int stat[VIDEO_NUM_MAX] = {0};
    char video_id[VIDEO_NUM_MAX][DEVICE_ID_LEN];
//    char prop_boot[PROPERTY_VALUE_MAX] = {0};
    int prop_boot = 2;

    for (i = 0; i < m_data->device_num; i++)
       if (m_data->device[i].type == TYPE_VIDEO) {
           strncpy(video_id[i], m_data->device[i].id, DEVICE_ID_LEN-1);
           count++;
       }

    if (count > VIDEO_NUM_MAX)
        count = VIDEO_NUM_MAX;

    prop_boot = property_get_int32("service.bootanim.exit", 2);

    switch (prop_boot) {
        case 0:
        case 1:
            for (i = 0; i < count; i++)
                stat[i] = STAT_SUCCESS;
            break;

        case 2:
            if (count == 1) {
                stat[0] = STAT_FAILURE;
            } else {
                //TODO get primary video and failure
                for (i = 0; i < count; i++) {
                    /* Dual Video card only check indepent card */
                    if (strstr(video_id[i], "8086"))
                        stat[i] = STAT_SUCCESS;
                    else
                        stat[i] = STAT_FAILURE;
                }
            }
            break;

        default:
            ALOGE("Get service.bootanim.exit error!\n");
            break;
    }

    set_device_stat(m_data, TYPE_VIDEO, stat, count);
    return 0;
}


static int check_driver_insmod(char *uid, char *pid)
{
    int stat = STAT_UNKNOWN;
    char line[1024] = {0};
    FILE *fp_lspci;

    /* Check lspci -mk, look for device driver */
    fp_lspci = popen(CMD_LSPCI, "r");
    if (!fp_lspci) {
        ALOGE("%s: lspci error!", __func__);
        return stat;
    }

    while (1) { 
        fgets(line, sizeof(line), fp_lspci);
        if (feof(fp_lspci))
            break;

        /* Check driver is insmod? */
        if (strcasestr(line, uid) && strcasestr(line, pid)) {
            /* lspci -mk print >50 when driver is insmod */
            if (strlen(line) <= 50)
                stat = STAT_FAILURE;
        }
    }

    pclose(fp_lspci);

    return stat;
}

static int check_net_eth_stat(char *net_id)
{
    int stat = STAT_UNKNOWN;
    int i, count = 0;
    DIR *dir_eth[ETH_NUM_MAX];
    char path[256];
    char uid[5] = {0}, pid[5] = {0};

#ifdef USE_MODALIAS
    FILE *fp_modalias;
    char modalias[ETH_NUM_MAX][1024];

    memset(modalias, 0, sizeof(uid));
#else
    FILE *fp_uid, *fp_pid;

    char uids[ETH_NUM_MAX][8], pids[ETH_NUM_MAX][8];

    memset(uids, 0, sizeof(uids));
    memset(pids, 0, sizeof(pids));
#endif /* USE_MODALIAS */

    strncpy(uid, net_id, 4);
    strncpy(pid, net_id + 4, 4);

    stat = check_driver_insmod(uid, pid);
    if (stat == STAT_FAILURE)
        return STAT_FAILURE;

    for (i = 0; i < ETH_NUM_MAX; i++) {
        memset(path, 0, 256);
        sprintf(path, "%s%d", SYS_NET_ETH, i);

        if ((dir_eth[i] = opendir(path)) == NULL)
            continue;
        closedir(dir_eth[i]);

        count++;
    }

#ifdef USE_MODALIAS
    for (i = 0; i < count; i++) {
        memset(path, 0, 256);
        sprintf(path, "%s%d%s", SYS_NET_ETH, i, SYS_NET_ETH_MODALIAS);

        fp_modalias = fopen(path, "r");
        if (!fp_modalias) {
            ALOGE("%s: open SYS_NET_ETH_UID file error", __func__);
            return STAT_UNKNOWN;
        }

        fgets(modalias[i], 1024, fp_modalias);

        fclose(fp_modalias);
    }

    for (i = 0; i < count; i++)
        if (strcasestr(modalias[i], uid) && strcasestr(net_id, pid))
            return STAT_SUCCESS;

#else
    for (i = 0; i < count; i++) {
        memset(path, 0, 256);
        sprintf(path, "%s%d%s", SYS_NET_ETH, i, SYS_NET_ETH_UID);

        fp_uid = fopen(path, "r");
        if (!fp_uid) {
            ALOGE("%s: open SYS_NET_ETH_UID file error", __func__);
            return STAT_UNKNOWN;
        }

        memset(path, 0, 256);
        sprintf(path, "%s%d%s", SYS_NET_ETH, i, SYS_NET_ETH_PID);
        fp_pid = fopen(path, "r");
        if (!fp_pid) {
            fclose(fp_uid);

            ALOGE("%s: open SYS_NET_ETH_UID file error", __func__);
            return STAT_UNKNOWN;
        }

        fgets(uids[i], 7, fp_uid);
        fgets(pids[i], 7, fp_pid);

        fclose(fp_pid);
        fclose(fp_uid);
    }

    for (i = 0; i < count; i++) {
        if (strcasestr(net_id, uids[i]+2) && strcasestr(net_id, pids[i]+2)) {
            return STAT_SUCCESS;
        }
    }
#endif /* USE_MODALIAS */

    return stat;
}

static int check_net_wifi_stat(char *net_id)
{
    int stat = STAT_UNKNOWN;
    int i, count = 0;
    DIR *dir_wlan[ETH_NUM_MAX];
    char path[256];
    char uid[5] = {0}, pid[5] = {0};

    FILE *fp_modalias;
    char modalias[ETH_NUM_MAX][1024];

    memset(modalias, 0, sizeof(uid));

    strncpy(uid, net_id, 4);
    strncpy(pid, net_id + 4, 4);

    for (i = 0; i < WLAN_NUM_MAX; i++) {
        memset(path, 0, 256);
        sprintf(path, "%s%d", SYS_NET_WLAN, i);

        if ((dir_wlan[i] = opendir(path)) == NULL)
            continue;
        closedir(dir_wlan[i]);

        count++;
    }

    for (i = 0; i < count; i++) {
        memset(path, 0, 256);
        sprintf(path, "%s%d%s", SYS_NET_WLAN, i, SYS_NET_WLAN_MODALIAS);

        fp_modalias = fopen(path, "r");
        if (!fp_modalias) {
            ALOGE("%s: open SYS_NET_WLAN_UID file error", __func__);
            return STAT_UNKNOWN;
        }

        fgets(modalias[i], 1024, fp_modalias);

        fclose(fp_modalias);
    }

    for (i = 0; i < count; i++)
        if (strcasestr(modalias[i], uid) && strcasestr(net_id, pid)) {
            return STAT_SUCCESS;
        }

    return STAT_FAILURE;
}

static int check_net_stat(struct machine_data *m_data)
{
    char net_id[NET_NUM_MAX][DEVICE_ID_LEN]; 
    int stat[NET_NUM_MAX];
    int i, count = 0;

    for (i = 0; i < m_data->device_num; i++) {
       if (m_data->device[i].type == TYPE_NET) {
           strncpy(net_id[count], m_data->device[i].id, DEVICE_ID_LEN);
           count++;
       }
    }

    for (i = 0; i < count; i++) {
        /* Check net id is eth? */
        stat[i] = check_net_eth_stat(net_id[i]);
        if (stat[i] != STAT_UNKNOWN) 
            continue;

        /* Check net id is wifi? */
        stat[i] = check_net_wifi_stat(net_id[i]);

        /* if neither eth0 nor wifi, this is STAT_UNKNOWN */
    }

    for (i = 0; i < count; i++)
        ALOGD("net%d stat is: %d\n", i, stat[i]);

    set_device_stat(m_data, TYPE_NET, stat, count);

    return 0;
}

static int check_sound_stat(struct machine_data *m_data)
{
    //TODO
    int i, count = 0;
    int stat[SOUND_NUM_MAX] = {0};

    for (i = 0; i < m_data->device_num; i++)
       if (m_data->device[i].type == TYPE_SOUND)
           count++;

    if (count > SOUND_NUM_MAX)
        count = SOUND_NUM_MAX;

    for (i = 0; i < count; i++)
        stat[i] = 1;

    set_device_stat(m_data, TYPE_SOUND, stat, count);

    return 0;
}

static void check_version(struct machine_data *m_data)
{
    char version[OS_VERSION_NAME_MAX_LEN] = {0}; 

    property_get("ro.phoenix.version.codename", version, m_data->os_version);
    strncpy(m_data->os_version, version, strlen(version));

    return ;
}

static int write_to_new_log(struct machine_data *m_data)
{
    int i, ret = 0;
    FILE *fp_phoenix_log = NULL;
    char lines[LOGFILE_LINE_MAX_LEN * WIN_FILE_LINE_MAX] = {0};

    fp_phoenix_log = fopen(PHOENIX_LOG_ADDR, "w+");
    if (fp_phoenix_log)
    if (!fp_phoenix_log) {
        ALOGE("Open/Create Phoenix log file error!\n");
        return -1;
    }

    /* Splice Phoenix info */
    sprintf(lines, "%s\r\n%s\r\n%s\r\n%s\r\nboot: %s\r\n",
            m_data->uid,
            m_data->os_version,
            m_data->channel,
            m_data->sub_channel,
            m_data->boot);

    printf("lines = %s" ,lines);

    for (i = 0; i < m_data->device_num; i++) {
        sprintf(lines, "%s%d %s %d\r\n",
                lines,
                m_data->device[i].type,
                m_data->device[i].id,
                m_data->device[i].stat);
    }

    printf("lines = %s" ,lines);

    fputs(lines, fp_phoenix_log);

    fclose(fp_phoenix_log);

    return 0;
}

static int set_channel(struct machine_data *m_data)
{
    int ret = 0;
    char sys_channel[CHANNEL_NAME_MAX_LEN+SUBCHANNEL_NAME_MAX_LEN+1] = {0};
    char file_channel[CHANNEL_NAME_MAX_LEN+SUBCHANNEL_NAME_MAX_LEN+1] = {0};

    sprintf(file_channel, "%s_%s", m_data->channel, m_data->sub_channel);

    property_get(CHANNEL_PROPERTY, sys_channel, "unknown");
    //strncpy(m_data->os_version, version, strlen(version));

    if (!strncmp(sys_channel, "unknown", 7)) {
        ALOGD("PhoenixOS: no channel, set %s is %s", CHANNEL_PROPERTY, file_channel);
        ret = property_set(CHANNEL_PROPERTY, file_channel);
        if (ret)
            ALOGE("PhoenixOS: set %s error!", CHANNEL_PROPERTY);
    } else {
        if (strncmp(file_channel, sys_channel, strlen(sys_channel))) {
            ALOGW("PhoenixOS: read channel(%s) is different from %s(%s), keep %s",
                sys_channel, CHANNEL_PROPERTY, file_channel, sys_channel);
        } else {
            ALOGD("PhoenixOS: channel is %s", sys_channel);
        }
    }

    return ret;
}

static int set_stat(struct machine_data *m_data)
{
    int ret = 0;

    check_version(m_data);

    ret = set_channel(m_data);
    
    check_cpu_stat(m_data);

    ret = check_gpu_stat(m_data);

    ret = check_net_stat(m_data);

    check_sound_stat(m_data);

    ret = write_to_new_log(m_data);

    return ret;
}

static void splice_device_info(struct machine_data *m_data, int type, char *query)
{
    char tmp[QUERY_LEN_MAX] = {0};
    int i, type_device_num = 1;

    for (i = 0; i < m_data->device_num; i++) {
       if (m_data->device[i].type == type) {
           if (type_device_num > 1)
               sprintf(tmp, "&%s%d=%s", device_info_type[type-1], type_device_num, m_data->device[i].id);
           else
               sprintf(tmp, "&%s=%s", device_info_type[type-1], m_data->device[i].id);

           strcat(query, tmp);
           type_device_num++;
       } 
    }

    return;
}

static void splice_stat_info(struct machine_data *m_data, int type, char *query)
{
    char tmp[QUERY_LEN_MAX] = {0};
    int i, type_device_num = 1;

    for (i = 0; i < m_data->device_num; i++) {
       if (m_data->device[i].type == type) {
           if (type_device_num > 1)
               sprintf(tmp, "&%s%d_stat=%d", device_info_type[type-1], type_device_num, m_data->device[i].stat);
           else
               sprintf(tmp, "&%s_stat=%d", device_info_type[type-1], m_data->device[i].stat);

           strcat(query, tmp);
           type_device_num++;
       } 
    }

    return;
}

static int splice_query(struct machine_data *m_data, char *query)
{
    char tmp[QUERY_LEN_MAX] = {0};
    int i, len = 0;

    /* Splice Phoenix info */
    sprintf(tmp, "%s=%s&%s=%s&%s=%s&%s=%s",
            NAME_UID, m_data->uid,
            NAME_OS_VERSION, m_data->os_version,
            NAME_CHANNEL_ID, m_data->channel,
            NAME_SUB_CHANNEL_ID, m_data->sub_channel);

    len = sizeof(device_info_type) / sizeof(device_info_type[0]);

    /* Splice CPU, video, sound, network info */
    for (i = 1; i <= len; i++)
        splice_device_info(m_data, i, tmp);

    /* Splice CPU, video, sound, network stat info */
    for (i = 1; i <= len; i++)
        splice_stat_info(m_data, i, tmp);

    if (strncmp(m_data->boot, "unknown", 7))
        sprintf(tmp, "%s&boot=%s", tmp, m_data->boot);

    len = strlen(tmp);
    strncpy(query, tmp, len);

    return len;
}

static int encrypt(char *query, int len)
{
    int i;
    char *tmp = query;

    for (i = 0; i < len; i++) {
        tmp[i] ^= ((i + 9) % 13 + 'B');
    }

    return len;
}

static void base64_urlization(char *query, int len)
{
    int i;

    for (i = 0; i < len; i++) {
        if (query[i] == '+')
            query[i] = '-';

        if (query[i] == '/')
            query[i] = '_';

        if (query[i] == '=')
            query[i] = 0;
    }

    return;
}

static int check_http_get_result(const char *http_get_ret)
{
    char temp[HTTP_RET_LEN] = {0};
    char *ret_value[2];
    char const *delim = "\r\n";
    int i, len = (int)strlen(http_get_ret);

    memcpy(temp, http_get_ret, (len > HTTP_RET_LEN) ? HTTP_RET_LEN : len);

    /* Split data */
    ret_value[0] = strtok(temp, delim);
    ret_value[1] = strtok(NULL, delim);

    for (i=0; i<len; i++)
        ALOGD("%d\t", http_get_ret[i]);

    ALOGD("\nret = %d\n", atoi(read_windows_line(ret_value[0])));
    ALOGD("ret = %s\n", ret_value[0]);
    ALOGD("ret = %s\n", ret_value[1]);

    return 0;
}

static int send_data(struct machine_data *m_data, int boot)
{
    int ret = 0;
    int len = 0;
    char query[QUERY_LEN_MAX] = {0};
    char query_base64[QUERY_LEN_MAX] = {0};
    char *ret_http_get = NULL;

    if (strncmp(m_data->boot, "phoenixos", 9) == 0)
        boot = 1;

    len = splice_query(m_data, query);
    if (!len) {
        ALOGE("splice query error\n");
        return -1;
    }
    ALOGD("len = %d, splice query: \n%s\n", len, query);

    len = encrypt(query, len);
    //ALOGD("len = %d, encrypt query: %s\n", len, query);
    // encrypt print all ascii

    len = Base64encode(query_base64, query, len);
    ALOGD("len = %d, base64 query: \n%s\n", len, query_base64);

    /* Convert '+' to '-', '/' to '_', '=' to '\0' */
    base64_urlization(query_base64, len);
    ALOGD("len = %d, base64 urlization query: \n%s\n", len, query_base64);

    /* Gen URL */
    memset(query, 0, QUERY_LEN_MAX);
    strcat(query, HTTPADDR_CZ_API);

    if (boot == 1)
        strcat(query, HTTPADDR_CZ_RUN_COMPAT);
    else
        strcat(query, HTTPADDR_CZ_BOOT_COMPAT);

    strcat(query, query_base64);

    ALOGD("url: \n%s\n", query);
    ret_http_get = http_get(query);
    //ret = check_http_get_result(ret_http_get);

    return ret;
}


int main(int argc, char **argv)
{
    int ret = 0;
    int boot = 0;
    struct machine_data m_data;

    memset(&m_data, 0, sizeof(struct machine_data));
    if (argc > 1)
        if (!strncmp(argv[1], "phoenixos", 9))
            boot = 1;

    ret = get_windows_data(&m_data);
    if (ret) {
        ALOGE("Get windows data error!\n");
        exit(-2);
    }

    ret = set_stat(&m_data);
    if (ret) {
        ALOGE("Set data stat error!\n");
        //exit(-3);
    }

    ret = send_data(&m_data, boot);
    if (ret) {
        ALOGE("Send data error!\n");
        exit(-4);
    }

    exit(0);
}

