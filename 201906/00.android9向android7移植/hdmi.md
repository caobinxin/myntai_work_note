# hdmi-音频输出

## 1. hdmi问题描述：

在hp x360板子上：当我插入hdmi　屏幕上没有声音，　当我拔掉　hdmi的时候，　扬声器同样也是没有声音。

https://me.csdn.net/leesheen 原公司　lixin同学的博客

## 2. 问题分析：

### 2.1 log1

启动之后　没有插入hdmi，系统是有声音的：

```shell
audio_hw_primary: colby out_write 810
audio_hw_primary: colby start_output_stream 442
audio_hw_primary: colby select_card 279
audio_hw_primary: colby choose pcmC0D0p for 0 # 选择了0p
audio_hw_primary: colby my_pcm_open 343
audio_hw_primary: colby out_write 810
```



插入hdmi 调节声音后　，但是此时没有声音输出

```shell
E audio_hw_primary: reset_mixer_state: invalid audio_route
E audio_hw_primary: audio_route_apply_path: invalid audio_route
E audio_hw_primary: update_mixer_state: invalid audio_route
D audio_hw_primary: colby out_write 810
D audio_hw_primary: colby start_output_stream 442
D audio_hw_primary: colby select_card 279
I audio_hw_primary: colby choose pcmC0D10p for 3 # 选择了10p
D audio_hw_primary: colby my_pcm_open 343
I NuPlayerDecoder: [OMX.ffmpeg.vorbis.decoder] resubmitting CSD
I NuPlayerDecoder: [OMX.ffmpeg.vorbis.decoder] resubmitting CSD                                                                                                              
D NuPlayerDriver: notifyListener_l(0xf3987780), (6, 0, 0), loop setting(0, 0)
D audio_hw_primary: colby out_write 810

```

拔掉hdmi后　播放音乐：此时灭有声音：

```shell
D SurfaceFlinger: Set power mode=2, type=0 flinger=0x748aeba4a000
I ConsoleManagerT: type=1400 audit(0.0:1064): avc: denied { ioctl } for path="/dev/dri/card0" dev="tmpfs" ino=13446 ioctlcmd=0x641e scontext=u:r:surfaceflinger:s0 tcontext=u:object
D GRALLOC-DRM: set master
D NuPlayerDriver: start(0xf3987780), state is 6, eos is 1
D audio_hw_primary: colby out_write 810 
D audio_hw_primary: colby start_output_stream 442 
D audio_hw_primary: colby select_card 279 
I audio_hw_primary: colby choose pcmC0D10p for 3 # 看到这里我们依然选择的是　10p
D audio_hw_primary: colby my_pcm_open 343 
I NuPlayerDecoder: [OMX.ffmpeg.vorbis.decoder] resubmitting CSD 
I NuPlayerDecoder: [OMX.ffmpeg.vorbis.decoder] resubmitting CSD 
D NuPlayerDriver: notifyListener_l(0xf3987780), (6, 0, 0), loop setting(0, 0)
D audio_hw_primary: colby out_write 810 
D audio_hw_primary: colby out_write 810
```

### 2.2 log2

说明：　重启后，　外放是有声音的。然后在终端中插入　hdmi，　发现此时　电脑扬声器是有声音的

```shell
07-18 14:50:48.853  1567  1795 D audio_hw_primary: colby out_write 810
 82 07-18 14:50:48.853  1567  1795 D audio_hw_primary: colby start_output_stream 442
 83 07-18 14:50:48.853  1567  1795 D audio_hw_primary: colby select_card 279
 84 07-18 14:50:48.853  1567  1795 I audio_hw_primary: colby choose pcmC0D0p for 0　# 这里选择了　0p
 85 07-18 14:50:48.853  1567  1795 D audio_hw_primary: colby my_pcm_open 343
 86 07-18 14:50:48.856  1575  3098 I NuPlayerDecoder: [OMX.ffmpeg.vorbis.decoder] resubmitting CSD
 87 07-18 14:50:48.856  1575  3098 I NuPlayerDecoder: [OMX.ffmpeg.vorbis.decoder] resubmitting CSD
 88 07-18 14:50:48.857  1575  2304 D NuPlayerDriver: notifyListener_l(0xe752bd80), (6, 0, 0), loop setting(0, 0)
 89 07-18 14:50:48.862  1572  3100 D SoftFFmpegAudio: ffmpeg audio decoder eos
 90 07-18 14:50:48.862  1572  3100 D SoftFFmpegAudio: ffmpeg audio decoder fill eos outbuf
 91 07-18 14:50:48.862  1575  3098 I NuPlayerDecoder: [audio] saw output EOS
 92 07-18 14:50:48.894  1567  1794 D audio_hw_primary: colby out_write 810
```

### 2.3 分析

1. 方案一:  始终保持　一个声卡，不让其进行声卡切换
2. 方案二：将声音输出到hdmi接口中

这里优先查　方案二：

当我们在图形界面中插入hdmi时，此时有报错：

```shell
E audio_hw_primary: reset_mixer_state: invalid audio_route
E audio_hw_primary: audio_route_apply_path: invalid audio_route
E audio_hw_primary: update_mixer_state: invalid audio_route
D audio_hw_primary: colby out_write 810
D audio_hw_primary: colby start_output_stream 442
D audio_hw_primary: colby select_card 279
I audio_hw_primary: colby choose pcmC0D10p for 3 # 选择了10p
```

```shell
hp-4.19/hardware/libaudio$ vim audio_route.c +413
```

```c
/* this resets all mixer settings to the saved values */
void reset_mixer_state(struct audio_route *ar)                        
{
    unsigned int i;

    if (!ar) { /*这里为啥为空*/
        ALOGE("%s: invalid audio_route", __FUNCTION__);
        return;
    }   

    /* load all of the saved values */
    for (i = 0; i < ar->num_mixer_ctls; i++)
        ar->mixer_state[i].new_value = ar->mixer_state[i].reset_value;
}

```

调用关系为：

```shell
select_devices -> 
	-> reset_mixer_state ->
```



```c
static void select_devices(struct audio_device *adev)
{
    int headphone_on;
    int speaker_on;
    int docked;
    int main_mic_on;

    headphone_on = adev->out_device & (AUDIO_DEVICE_OUT_WIRED_HEADSET |
                                    AUDIO_DEVICE_OUT_WIRED_HEADPHONE);
    speaker_on = adev->out_device & AUDIO_DEVICE_OUT_SPEAKER;
    docked = adev->out_device & AUDIO_DEVICE_OUT_ANLG_DOCK_HEADSET;
    main_mic_on = adev->in_device & AUDIO_DEVICE_IN_BUILTIN_MIC;
    
    //当我们插入hdmi的时候 adev->out_device = 0x400 这个值是对的，　

    reset_mixer_state(adev->ar); // 这里居然传入的是null                                                                                                                                                                                  

    if (speaker_on)
        audio_route_apply_path(adev->ar, "speaker");
    if (headphone_on)
        audio_route_apply_path(adev->ar, "headphone");
    if (docked)
        audio_route_apply_path(adev->ar, "dock");
    if (main_mic_on) {
        if (adev->orientation == ORIENTATION_LANDSCAPE)
            audio_route_apply_path(adev->ar, "main-mic-left");
        else
            audio_route_apply_path(adev->ar, "main-mic-top");
    }    

    update_mixer_state(adev->ar);

    ALOGV("hp=%c speaker=%c dock=%c main-mic=%c", headphone_on ? 'y' : 'n', 
          speaker_on ? 'y' : 'n', docked ? 'y' : 'n', main_mic_on ? 'y' : 'n');
}

```

这里先要搞明白这几个宏定义的含义：

```shell
hp-4.19$ vim system/media/audio/include/system/audio.h +710
```

```c
enum {
    AUDIO_DEVICE_NONE                          = 0x0, 
    /* reserved bits */
    AUDIO_DEVICE_BIT_IN                        = 0x80000000,
    AUDIO_DEVICE_BIT_DEFAULT                   = 0x40000000,
    /* output devices */
    AUDIO_DEVICE_OUT_EARPIECE                  = 0x1, 
    AUDIO_DEVICE_OUT_SPEAKER                   = 0x2,                                                                                                                                                              
    AUDIO_DEVICE_OUT_WIRED_HEADSET             = 0x4, 
    AUDIO_DEVICE_OUT_WIRED_HEADPHONE           = 0x8, 
    AUDIO_DEVICE_OUT_BLUETOOTH_SCO             = 0x10,
    AUDIO_DEVICE_OUT_BLUETOOTH_SCO_HEADSET     = 0x20,
    AUDIO_DEVICE_OUT_BLUETOOTH_SCO_CARKIT      = 0x40,
    AUDIO_DEVICE_OUT_BLUETOOTH_A2DP            = 0x80,
    AUDIO_DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES = 0x100,
    AUDIO_DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER    = 0x200,
    AUDIO_DEVICE_OUT_AUX_DIGITAL               = 0x400,
    AUDIO_DEVICE_OUT_HDMI                      = AUDIO_DEVICE_OUT_AUX_DIGITAL, // hdmi
...
}
```

### 2.4 进一步看log分析：

下面的代码是我已经调通了的code: 插入hdmi电视有声音、拔掉hdmi后扬声器是有声音的。

```c
struct snd_pcm_info *select_card(unsigned int device, unsigned int flags)
{
	static struct snd_pcm_info *cached_info[4];
	struct snd_pcm_info *info;
	ALOGD("colby %s %d\n", __func__, __LINE__);
	int d = !!(flags & PCM_IN);
	char e = d ? 'c' : 'p';
	ALOGD("colby e = %c", e);
	if (!cached_info[d] && !cached_info[d + 2]) {
		ALOGD("colby %s %d\n", __func__, __LINE__);
		struct dirent **namelist;
		char path[PATH_MAX] = "/dev/snd/";
		int n = scandir(path, &namelist, NULL, alphasort);
		if (n >= 0) {
			int i, fd;
			for (i = 0; i < n; i++) {
				ALOGD("\n\n\ncolby %s %d for start...", __func__, __LINE__);
				struct dirent *de = namelist[i];
				ALOGD("%s %d de->d_name=%s", __func__, __LINE__, de->d_name);


				if(strstr((const char *)de->d_name, "C0D10p")){
					ALOGD(" ignore pcmC0D10p dir...");
					continue;
				}


				if (!strncmp(de->d_name, "pcmC", 4) && de->d_name[strlen(de->d_name) - 1] == e) {
					ALOGD("%s %d de->d_name=%s", __func__, __LINE__, de->d_name);
					strcpy(path + 9, de->d_name);
					if ((fd = open(path, O_RDWR)) >= 0) {
						info = malloc(sizeof(*info));
						if (!ioctl(fd, SNDRV_PCM_IOCTL_INFO, info)) {
							if (info->stream == d && /* ignore IntelHDMI */
									!strstr((const char *)info->id, "IntelHDMI")) {
								ALOGD("colby found audio %s at %s\ncard: %d/%d id: %s\nname: %s\nsubname: %s\nstream: %d",
										d ? "in" : "out", path,
										info->card, info->device, info->id,
										info->name, info->subname, info->stream);


								int hdmi = (!!strstr((const char *)info->id, "HDMI")) * 2;
								ALOGD("colby %s %d hdmi=%d\n", __func__, __LINE__, hdmi);
								if (is_nuc()){
									ALOGD("colby %s %d\n", __func__, __LINE__);
									if (cached_info[d + hdmi] && hdmi && !is_hdmi_connected(de->d_name)) {
										ALOGD("colby %s %d\n", __func__, __LINE__);
										ALOGD("ignore %s", de->d_name);
										free(info);
									} else {
										ALOGD("colby %s %d\n", __func__, __LINE__);
										cached_info[d + hdmi] = info;
									}
								}else{

									ALOGD("colby %s %d is_hdmi_connected=%d\n", __func__, __LINE__, is_hdmi_connected(de->d_name));
									if (cached_info[d + hdmi]) {
										ALOGD("colby %s %d\n", __func__, __LINE__);
										ALOGD("ignore %s", de->d_name);
										free(info);
									} else {
										ALOGD("colby %s %d\n", __func__, __LINE__);
										cached_info[d + hdmi] = info;
									}
								}
							}else{
								ALOGD("colby %s %d\n", __func__, __LINE__);
								ALOGD("colby ignore -> found audio %s at %s\ncard: %d/%d id: %s\nname: %s\nsubname: %s\nstream: %d",
										d ? "in" : "out", path,
										info->card, info->device, info->id,
										info->name, info->subname, info->stream);
							}
						} else {
							ALOGD("colby %s %d\n", __func__, __LINE__);
							ALOGV("can't get info of %s", path);
							free(info);
						}
						close(fd);
					}
				}
				free(de);
				ALOGD("colby %s %d for end---\n\n\n", __func__, __LINE__);
			}
			free(namelist);
		}
	}
	if ((is_nuc() ? property_get_bool("persist.sys.audio.hdmi", device == PCM_DEVICE_HDMI) :
				property_get_bool("hal.audio.primary.hdmi", device == PCM_DEVICE_HDMI)) && cached_info[d + 2]) {
		ALOGD("colby %s %d\n", __func__, __LINE__);

		if(is_hdmi_connected("pcmC0D3p")){

			info = cached_info[d + 2];
		}else{
			info = cached_info[d];
		}
	} else {
		ALOGD("colby %s %d\n", __func__, __LINE__);
		info = cached_info[d] ? cached_info[d] : cached_info[d + 2];
	}
	ALOGI_IF(info, "colby choose pcmC%dD%d%c for %d", info->card, info->device, d ? 'c' : 'p', device);
	return info;
}
```

这部分的代码的log如下：

#### 2.4.1 开机后点击音量调节log

正常开机的log

```shell
audio_hw_primary: colby select_card 279
audio_hw_primary: colby e = p
audio_hw_primary: colby select_card 284
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: colby select_card 291 for start...
audio_hw_primary: select_card 293 de->d_name=.
audio_hw_primary: colby select_card 356 for end---
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: colby select_card 291 for start...
audio_hw_primary: select_card 293 de->d_name=..
audio_hw_primary: colby select_card 356 for end---
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: colby select_card 291 for start...
audio_hw_primary: select_card 293 de->d_name=controlC0
audio_hw_primary: colby select_card 356 for end---
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: colby select_card 291 for start...
audio_hw_primary: select_card 293 de->d_name=hwC0D0
audio_hw_primary: colby select_card 356 for end---
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: colby select_card 291 for start...
audio_hw_primary: select_card 293 de->d_name=hwC0D2
audio_hw_primary: colby select_card 356 for end---
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: colby select_card 291 for start...
audio_hw_primary: select_card 293 de->d_name=pcmC0D0c
audio_hw_primary: colby select_card 356 for end---
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: colby select_card 291 for start...
audio_hw_primary: select_card 293 de->d_name=pcmC0D0p
audio_hw_primary: select_card 303 de->d_name=pcmC0D0p
audio_hw_primary: colby found audio out at /dev/snd/pcmC0D0p
audio_hw_primary: card: 0/0 id: ALC295 Analog
audio_hw_primary: name: ALC295 Analog
audio_hw_primary: subname: subdevice #0
audio_hw_primary: stream: 0
audio_hw_primary: colby select_card 317 hdmi=0
audio_hw_primary: colby is_hdmi_connected 255
audio_hw_primary: colby select_card 330 is_hdmi_connected=0
audio_hw_primary: colby select_card 336
audio_hw_primary: colby select_card 356 for end---
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: colby select_card 291 for start...
audio_hw_primary: select_card 293 de->d_name=pcmC0D10p
audio_hw_primary:  ignore pcmC0D10p dir... # 这里忽略了pcmC0D10p 节点
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: colby select_card 291 for start...
audio_hw_primary: select_card 293 de->d_name=pcmC0D3p
audio_hw_primary: select_card 303 de->d_name=pcmC0D3p
audio_hw_primary: colby found audio out at /dev/snd/pcmC0D3p
audio_hw_primary: card: 0/3 id: HDMI 0
audio_hw_primary: name: HDMI 0
audio_hw_primary: subname: subdevice #0
audio_hw_primary: stream: 0
audio_hw_primary: colby select_card 317 hdmi=2
audio_hw_primary: colby is_hdmi_connected 255
audio_hw_primary: colby check_connected 227
audio_hw_primary: leesheen arr[] = 0, index = 0
audio_hw_primary: leesheen in pcmC0D3p, is = 0
audio_hw_primary: colby select_card 330 is_hdmi_connected=0
audio_hw_primary: colby select_card 336
audio_hw_primary: colby select_card 356 for end---
audio_hw_primary: # 这里我们在hdmi的选择中只加入了　pcmC0D3p，　后面和hdmi相关的全部忽略
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: colby select_card 291 for start...
audio_hw_primary: select_card 293 de->d_name=pcmC0D7p
audio_hw_primary: select_card 303 de->d_name=pcmC0D7p
audio_hw_primary: colby found audio out at /dev/snd/pcmC0D7p
audio_hw_primary: card: 0/7 id: HDMI 1
audio_hw_primary: name: HDMI 1
audio_hw_primary: subname: subdevice #0
audio_hw_primary: stream: 0
audio_hw_primary: colby select_card 317 hdmi=2
audio_hw_primary: colby is_hdmi_connected 255
audio_hw_primary: colby check_connected 227
audio_hw_primary: leesheen arr[] = 1, index = 1
audio_hw_primary: leesheen in pcmC0D7p, is = 1
audio_hw_primary: colby select_card 330 is_hdmi_connected=1
audio_hw_primary: colby select_card 332
audio_hw_primary: ignore pcmC0D7p　# 忽略
audio_hw_primary: colby select_card 356 for end---
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: colby select_card 291 for start...
audio_hw_primary: select_card 293 de->d_name=pcmC0D8p
audio_hw_primary: select_card 303 de->d_name=pcmC0D8p
audio_hw_primary: colby found audio out at /dev/snd/pcmC0D8p
audio_hw_primary: card: 0/8 id: HDMI 2
audio_hw_primary: name: HDMI 2
audio_hw_primary: subname: subdevice #0
audio_hw_primary: stream: 0
audio_hw_primary: colby select_card 317 hdmi=2
audio_hw_primary: colby is_hdmi_connected 255
audio_hw_primary: colby check_connected 227
audio_hw_primary: leesheen arr[] = 0, index = 2
audio_hw_primary: leesheen in pcmC0D8p, is = 0
audio_hw_primary: colby select_card 330 is_hdmi_connected=0
audio_hw_primary: colby select_card 332
audio_hw_primary: ignore pcmC0D8p　# 忽略
audio_hw_primary: colby select_card 356 for end---
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: colby select_card 291 for start...
audio_hw_primary: select_card 293 de->d_name=pcmC0D9p
audio_hw_primary: select_card 303 de->d_name=pcmC0D9p
audio_hw_primary: colby found audio out at /dev/snd/pcmC0D9p
audio_hw_primary: card: 0/9 id: HDMI 3
audio_hw_primary: name: HDMI 3
audio_hw_primary: subname: subdevice #0
audio_hw_primary: stream: 0
audio_hw_primary: colby select_card 317 hdmi=2
audio_hw_primary: colby is_hdmi_connected 255
audio_hw_primary: colby check_connected 227
audio_hw_primary: leesheen arr[] = 0, index = 3
audio_hw_primary: leesheen in pcmC0D9p, is = 0
audio_hw_primary: colby select_card 330 is_hdmi_connected=0
audio_hw_primary: colby select_card 332
audio_hw_primary: ignore pcmC0D9p　# 忽略
audio_hw_primary: colby select_card 356 for end---
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: colby select_card 291 for start...
audio_hw_primary: select_card 293 de->d_name=timer
audio_hw_primary: colby select_card 356 for end---
audio_hw_primary: 
audio_hw_primary: 
audio_hw_primary: colby select_card 372
audio_hw_primary: colby choose pcmC0D0p for 0　# 由于此时没有插入hdmi所以这里选择默认的
audio_hw_primary: Failed to open /system/etc/mixer_paths.xml
AudioFlinger: loadHwModule() Loaded primary audio interface from Grouper audio HW HA
AudioFlinger: openOutput(), module 10 Device 2, SamplingRate 48000, Format 0x000001,
audio_hw_primary: colby adev_open_output_stream 1223
audio_hw_primary: colby select_card 279
audio_hw_primary: colby e = p
audio_hw_primary: colby select_card 372
audio_hw_primary: colby choose pcmC0D0p for 0
AudioFlinger: HAL output buffer size 512 frames, normal sink buffer size 1024 frames
```

调节音量：

```shell
audio_hw_primary: colby out_write 851
audio_hw_primary: colby start_output_stream 483
audio_hw_primary: colby select_card 279
audio_hw_primary: colby e = p
audio_hw_primary: colby select_card 372
audio_hw_primary: colby choose pcmC0D0p for 0　# 由于此时没有插入hdmi所以这里选择默认的
audio_hw_primary: colby my_pcm_open 382
NuPlayerDriver: notifyListener_l(0xed507840), (6, 0, 0), loop setting(0, 0)
SoftFFmpegAudio: ffmpeg audio decoder eos
SoftFFmpegAudio: ffmpeg audio decoder fill eos outbuf
NuPlayerDecoder: [audio] saw output EOS
audio_hw_primary: colby out_write 851　# 从这里我们也可以看出来，只要有音乐播放，　在hal层就会掉它
audio_hw_primary: colby out_write 851
audio_hw_primary: colby out_write 851
audio_hw_primary: colby out_write 851
audio_hw_primary: colby out_write 851
audio_hw_primary: colby out_write 851
audio_hw_primary: colby out_write 851
audio_hw_primary: colby out_write 851
audio_hw_primary: colby out_write 851
audio_hw_primary: colby out_write 851
audio_hw_primary: colby out_write 851
audio_hw_primary: colby out_write 851
com.chaozhuo.launcher.home.HomeFragment: onSharedPreferenceChanged: today_used_minute
```

#### 2.4.2 插入hdmi 调节音量

在2.4.1的基础上，插入hdmi　调节音量：

```shell
audio_hw_primary: colby select_devices 411 adev->outdevice=0x400
audio_hw_primary: reset_mixer_state: invalid audio_route
audio_hw_primary: audio_route_apply_path: invalid audio_route  name=main-mi
audio_hw_primary: update_mixer_state: invalid audio_route
audio_hw_primary: hp=n speaker=n dock=n main-mic=y
audio_hw_primary: colby out_write 851
audio_hw_primary: colby start_output_stream 483
audio_hw_primary: colby select_card 279
audio_hw_primary: colby e = p
audio_hw_primary: colby select_card 363
audio_hw_primary: colby is_hdmi_connected 255
audio_hw_primary: colby check_connected 227
audio_hw_primary: leesheen arr[] = 1, index = 0
audio_hw_primary: leesheen in pcmC0D3p, is = 1
audio_hw_primary: colby choose pcmC0D3p for 3　#由于此时插入了hdmi所以这里要选择　pcmC0D3p
audio_hw_primary: colby my_pcm_open 382
NuPlayerDecoder: [OMX.ffmpeg.vorbis.decoder] resubmitting CSD
NuPlayerDecoder: [OMX.ffmpeg.vorbis.decoder] resubmitting CSD
NuPlayerDriver: notifyListener_l(0xed507840), (6, 0, 0), loop setting(0, 0)

audio_hw_primary: colby out_write 851
audio_hw_primary: colby out_write 851
audio_hw_primary: colby out_write 851
audio_hw_primary: colby out_write 851
```

#### 2.4.3 问题分析记录

taidu上面的log是调通之后的log. 之前错误的log, 我在这里说明一下：

错误的log:

```shell
# 开机后在初始化hal层的时候，　在选择hdmi声卡的时候选择了　pcmC0D10p
# 然后将　其他有关hdmi的全部忽略了
```

导致这个问题出现的原因其实在：scandir函数

```c
struct snd_pcm_info *select_card(unsigned int device, unsigned int flags)
{
    char path[PATH_MAX] = "/dev/snd/";
    int n = scandir(path, &namelist, NULL, alphasort);
    ...
}
```

scandir函数在扫描目录的时候，alphasort　参数是按照字母顺序进行排序的。

所以在排序的时候会将顺序排为一下：

```shell
pcmC0D10p
pcmC0D3p
pcmC0D7p
pcmC0D8p
pcmC0D9p
```

现在的问题是　当我们选择pcmC0D3p的时候，hdmi是正常工作的。

如果将pcmC0D3p　变为pcmC0D03p其实这个问题就能解决。hal层就不用改了。这个是最好的解决方式：

如果能改变成功，效果如下：

```shell
pcmC0D03p
pcmC0D07p
pcmC0D08p
pcmC0D09p
pcmC0D10p
```

如果kernel可以改成这样，那么hal层就不用修改了(忽略pcmC0D10p)

我草草的在hal层改了，其实这样是不解决根本问题的。

既然作为kernel工程师，就应该追踪问源。在kernel层去解决这个bug,而不是在hal层中。这是一种工作的态度。

这里我将整个 hardware/libaudio 中的代码在　当前的目录中做了备份。

其中我认为　一下几个函数还是挺有用的。

```c
From 0a5993dc2bce95eec0709706e57d535cf6be0229 Mon Sep 17 00:00:00 2001
From: qinshaohui <qinshaohui@phoenixos.com>
Date: Mon, 10 Dec 2018 15:53:14 +0800
Subject: [PATCH] fix audio-in anr

---
 audio_hw.c | 107 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++--
 1 file changed, 105 insertions(+), 2 deletions(-)

diff --git a/audio_hw.c b/audio_hw.c
index 7c2406f..b1b56c7 100644
--- a/hardware/libaudio/audio_hw.c
+++ b/hardware/libaudio/audio_hw.c
@@ -23,6 +23,7 @@
 #include <pthread.h>
 #include <stdint.h>
 #include <stdlib.h>
+#include <string.h>
 #include <sys/time.h>
 
 #include <cutils/log.h>
@@ -178,11 +179,103 @@ static void release_buffer(struct resampler_buffer_provider *buffer_provider,
 
 /* Helper functions */
 
+int hdmi_is_connected(const char *path_hdmi_name)
+{
+	int r = -1;
+	char *result = NULL;
+	size_t result_len = 0;
+	ssize_t n;
+
+	const char tag_connected[] = "connected";
+	const char tag_disconnected[] = "disconnected";
+	const char filename_status[] = "/status";
+	char path[256] = "/sys/class/drm/";
+
+	strcat(path, path_hdmi_name);
+	strcat(path, filename_status);
+
+	FILE *fp = fopen(path, "r");
+	if (!fp) {
+
+		return r;
+	}
+
+	n = getline(&result, &result_len, fp);
+	if (n < 6) {
+		return r;
+	}
+
+	if (!strncmp(result, tag_disconnected, strlen(tag_disconnected))) {
+		r = 0;
+	}
+	if (!strncmp(result, tag_connected, strlen(tag_connected))) {
+		r = 1;
+	}
+
+	free(result);
+
+	return r;
+}
+
+int check_connected(int index)
+{
+	int i, count = 0;
+	char hdmi_sys_path[PATH_MAX] = "/sys/class/drm/";
+	struct dirent **namelist = NULL;
+	int hdmi_audio_num_arr[64] = {0};
+
+
+	int len = scandir(hdmi_sys_path, &namelist, NULL, alphasort);
+
+	for (i = 0; i < len; i++) {
+		int c = -1;
+		struct dirent *de = namelist[i]; 
+
+		if (strncmp(de->d_name, "card0-", 6))
+			continue;
+
+		c = hdmi_is_connected(de->d_name);
+		hdmi_audio_num_arr[count] = (c == 1 ? 1 : 0);
+		count++;
+	}
+
+	free(namelist);
+//	ALOGD("leesheen arr[] = %d, index = %d", hdmi_audio_num_arr[index], index);
+	return hdmi_audio_num_arr[index];
+}
+
+int is_hdmi_connected(const char *hdmi_name)
+{
+	int i;
+	int is_connected = 0;
+	const int name_list_len = 5;
+	const char name_list[name_list_len][16] = {"pcmC0D3p","pcmC0D7p", "pcmC0D8p", "pcmC0D9p", "pcmC0D10p"};
+
+	for (i = 0; i < name_list_len; i++) {
+		if (!strcmp(hdmi_name, name_list[i])) {
+			is_connected = check_connected(i);
+//			ALOGD("leesheen in %s, is = %d", hdmi_name, is_connected);
+		}
+	}
+
+	return is_connected;
+}
+
+bool is_nuc() {
+    char value[PROPERTY_VALUE_MAX];
+    char ChannelProp[PROPERTY_KEY_MAX]= "persist.sys.phoenix.channel";
+    bool have_property = property_get(ChannelProp, value, "") > 0;
+    if (!have_property) {
+        return false;
+    }
+    return strcmp(value, "IntelNUC") == 0;
+}
 struct snd_pcm_info *select_card(unsigned int device, unsigned int flags)
 {
     static struct snd_pcm_info *cached_info[4];
     struct snd_pcm_info *info;
     int d = !!(flags & PCM_IN);
+    char e = d ? 'c' : 'p';
     if (!cached_info[d] && !cached_info[d + 2]) {
         struct dirent **namelist;
         char path[PATH_MAX] = "/dev/snd/";
@@ -191,7 +284,7 @@ struct snd_pcm_info *select_card(unsigned int device, unsigned int flags)
             int i, fd;
             for (i = 0; i < n; i++) {
                 struct dirent *de = namelist[i];
-                if (!strncmp(de->d_name, "pcmC", 4)) {
+                if (!strncmp(de->d_name, "pcmC", 4) && de->d_name[strlen(de->d_name) - 1] == e) {
                     strcpy(path + 9, de->d_name);
                     if ((fd = open(path, O_RDWR)) >= 0) {
                         info = malloc(sizeof(*info));
@@ -203,11 +296,20 @@ struct snd_pcm_info *select_card(unsigned int device, unsigned int flags)
                                         info->card, info->device, info->id,
                                         info->name, info->subname, info->stream);
                                 int hdmi = (!!strstr((const char *)info->id, "HDMI")) * 2;
+                                if (is_nuc()){
+                                    if (cached_info[d + hdmi] && hdmi && !is_hdmi_connected(de->d_name)) {
+                                        ALOGD("ignore %s", de->d_name);
+                                        free(info);
+                                    } else {
+                                        cached_info[d + hdmi] = info;
+                                    }
+                                }else{
                                 if (cached_info[d + hdmi]) {
                                     ALOGD("ignore %s", de->d_name);
                                     free(info);
                                 } else {
                                     cached_info[d + hdmi] = info;
+                                    }
                                 }
                             }
                         } else {
@@ -222,7 +324,8 @@ struct snd_pcm_info *select_card(unsigned int device, unsigned int flags)
             free(namelist);
         }
     }
-    if (property_get_bool("hal.audio.primary.hdmi", device == PCM_DEVICE_HDMI) && cached_info[d + 2]) {
+    if ((is_nuc() ? property_get_bool("persist.sys.audio.hdmi", device == PCM_DEVICE_HDMI) :
+            property_get_bool("hal.audio.primary.hdmi", device == PCM_DEVICE_HDMI)) && cached_info[d + 2]) {
         info = cached_info[d + 2];
     } else {
         info = cached_info[d] ? cached_info[d] : cached_info[d + 2];
-- 
2.7.4



```

暂时记录下，　留着之后使用。

## 3. 从kernel从中去fix bug:

kernel中相关的打印如下：

```shell
[    5.340809] snd_hda_codec_realtek hdaudioC0D0: autoconfig for ALC295: line_outs=1 (0x14/0x0/0x0/0x0/0x0) type:speaker
[    5.340810] snd_hda_codec_realtek hdaudioC0D0:    speaker_outs=0 (0x0/0x0/0x0/0x0/0x0)
[    5.340811] snd_hda_codec_realtek hdaudioC0D0:    hp_outs=1 (0x21/0x0/0x0/0x0/0x0)
[    5.340811] snd_hda_codec_realtek hdaudioC0D0:    mono: mono_out=0x0                                                                                                                                        
[    5.340812] snd_hda_codec_realtek hdaudioC0D0:    inputs:
[    5.340812] snd_hda_codec_realtek hdaudioC0D0:      Mic=0x19
[    5.340813] snd_hda_codec_realtek hdaudioC0D0:      Internal Mic=0x12
[    5.378272] modprobe: /sbin/modprobe hdaudio:v8086280Br00100000a01
[    5.378272] 
[    5.387145] input: HDA Intel PCH Mic as /devices/pci0000:00/0000:00:1f.3/sound/card0/input28
[    5.387186] input: HDA Intel PCH Headphone as /devices/pci0000:00/0000:00:1f.3/sound/card0/input29
[    5.387221] input: HDA Intel PCH HDMI/DP,pcm=3 as /devices/pci0000:00/0000:00:1f.3/sound/card0/input30 # 这个就是那个３

[    5.387260] input: HDA Intel PCH HDMI/DP,pcm=7 as /devices/pci0000:00/0000:00:1f.3/sound/card0/input31

[    5.387299] input: HDA Intel PCH HDMI/DP,pcm=8 as /devices/pci0000:00/0000:00:1f.3/sound/card0/input32

[    5.387342] input: HDA Intel PCH HDMI/DP,pcm=9 as /devices/pci0000:00/0000:00:1f.3/sound/card0/input33

[    5.387384] input: HDA Intel PCH HDMI/DP,pcm=10 as /devices/pci0000:00/0000:00:1f.3/sound/card0/input34
```



暂时记录下　我下内核中找到的信息，当并不是最终的，需要继续去追：

```shell
hp-4.19/kernel/sound/pci/hda$ vim patch_hdmi.c +2136
```

```c
static int generic_hdmi_build_jack(struct hda_codec *codec, int pcm_idx)
{
    char hdmi_str[32] = "HDMI/DP";
    struct hdmi_spec *spec = codec->spec;
    struct hdmi_spec_per_pin *per_pin;
    struct hda_jack_tbl *jack;
    int pcmdev = get_pcm_rec(spec, pcm_idx)->device;
    bool phantom_jack;
    int ret; 

    if (pcmdev > 0) 
        sprintf(hdmi_str + strlen(hdmi_str), ",pcm=%d", pcmdev); //　有可能在这
...
    
}
```

我们这里要找到，　向上注册　/dev/snd/pcmC0D3p　的地方。

```shell
drivers/usb/gadget/function/u_uac1_legacy.h:21:#define FILE_PCM_PLAYBACK	"/dev/snd/pcmC0D0p"

Documentation/media/uapi/mediactl/media-types.rst:324:       -  typically, /dev/snd/pcmC?D?p

```

感觉这两个都不是：

这里暂时记录一下：在kernel中，我们可以根据　ioctrl　去找：SNDRV_PCM_IOCTL_INFO

是可以找到的：　这里暂时不做记录了，　下周来做。