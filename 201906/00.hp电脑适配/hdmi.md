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

### 3.1 找到注册设备节点的代码：

根据SNDRV_PCM_IOCTL_INFO　这个cmd去找：

```shell
hp-4.19/kernel/sound/core$ grep -inR "SNDRV_PCM_IOCTL_INFO"
pcm_compat.c:674:	case SNDRV_PCM_IOCTL_INFO:
pcm_native.c:2876:	case SNDRV_PCM_IOCTL_INFO:
```

这里可以找到两个地方，　通过在这两个地方加dump_stack()发现都同时被调用到了

```shell
-----------start snd_pcm_ioctl_compat 677
CPU: 0 PID: 1567 Comm: audioserver Not tainted 4.19.50-PhoenixOS-
Hardware name: HP HP Pavilion x360 Convertible 14-dh0xxx/85C4, BI
Call Trace:
 dump_stack+0x63/0x85
 snd_pcm_ioctl_compat+0x3ea/0x8f0 [snd_pcm]
 __se_compat_sys_ioctl+0x370/0x1160
 ? do_compat_writev+0x88/0xc0
 __ia32_compat_sys_ioctl+0x17/0x20
 do_fast_syscall_32+0x95/0x237
 entry_SYSENTER_compat+0x6b/0x7a
RIP: 0023:0xef3d0ec9
Code: ff ff 89 f0 74 02 89 32 5e 5f 5d c3 8b 0c 24 c3 8b 1c 24 c3
RSP: 002b:00000000ffac238c EFLAGS: 00000282 ORIG_RAX: 00000000000
RAX: ffffffffffffffda RBX: 0000000000000005 RCX: 0000000081204101
RDX: 00000000ee33ba40 RSI: 0000000000000005 RDI: 00000000ef2e394e
RBP: 00000000ffac34f8 R08: 0000000000000000 R09: 0000000000000000
R10: 0000000000000000 R11: 0000000000000000 R12: 0000000000000000
R13: 0000000000000000 R14: 0000000000000000 R15: 0000000000000000
-----------end snd_pcm_ioctl_compat 679




---------------start snd_pcm_common_ioctl 2879
CPU: 0 PID: 1567 Comm: audioserver Not tainted 4.19.50-PhoenixOS-
Hardware name: HP HP Pavilion x360 Convertible 14-dh0xxx/85C4, BI
Call Trace:
 dump_stack+0x63/0x85
 snd_pcm_common_ioctl+0x5fe/0xa50 [snd_pcm]
 snd_pcm_ioctl_compat+0x415/0x8f0 [snd_pcm]
 __se_compat_sys_ioctl+0x370/0x1160
 ? do_compat_writev+0x88/0xc0
 __ia32_compat_sys_ioctl+0x17/0x20
 do_fast_syscall_32+0x95/0x237
 entry_SYSENTER_compat+0x6b/0x7a
RIP: 0023:0xef3d0ec9
Code: ff ff 89 f0 74 02 89 32 5e 5f 5d c3 8b 0c 24 c3 8b 1c 24 c3
RSP: 002b:00000000ffac238c EFLAGS: 00000282 ORIG_RAX: 00000000000
RAX: ffffffffffffffda RBX: 0000000000000005 RCX: 0000000081204101
RDX: 00000000ee33ba40 RSI: 0000000000000005 RDI: 00000000ef2e394e
RBP: 00000000ffac34f8 R08: 0000000000000000 R09: 0000000000000000
R10: 0000000000000000 R11: 0000000000000000 R12: 0000000000000000
R13: 0000000000000000 R14: 0000000000000000 R15: 0000000000000000
---------------end snd_pcm_common_ioctl 2881
```

根据Ｌｏｇ进一步进行分析：

首先分析第一个

```c
static long snd_pcm_ioctl_compat(struct file *file, unsigned int cmd, unsigned long arg)
{
    struct snd_pcm_file *pcm_file;
    struct snd_pcm_substream *substream;
    void __user *argp = compat_ptr(arg);

    pcm_file = file->private_data;
    if (! pcm_file)
        return -ENOTTY;
    substream = pcm_file->substream;
    if (! substream)
        return -ENOTTY;

    /*  
     * When PCM is used on 32bit mode, we need to disable
     * mmap of PCM status/control records because of the size
     * incompatibility.
     */
    pcm_file->no_compat_mmap = 1;

    switch (cmd) {
    case SNDRV_PCM_IOCTL_PVERSION:
    case SNDRV_PCM_IOCTL_INFO: // 这个就是　hal层传递过来的cmd


printk(KERN_ERR"\n\n\n-----------start %s %d\n", __func__, __LINE__);
dump_stack();
printk(KERN_ERR"-----------end %s %d\n\n\n", __func__, __LINE__);
    }
    
    ...
 }
```



```c
const struct file_operations snd_pcm_f_ops[2] = {
    {    
        .owner =        THIS_MODULE,
        .write =        snd_pcm_write,
        .write_iter =       snd_pcm_writev,
        .open =         snd_pcm_playback_open,
        .release =      snd_pcm_release,
        .llseek =       no_llseek,
        .poll =         snd_pcm_poll,
        .unlocked_ioctl =   snd_pcm_ioctl,
        .compat_ioctl =     snd_pcm_ioctl_compat,// 在这里被赋值了
        .mmap =         snd_pcm_mmap,
        .fasync =       snd_pcm_fasync,
        .get_unmapped_area =    snd_pcm_get_unmapped_area,
    },   
    {    
        .owner =        THIS_MODULE,
        .read =         snd_pcm_read,
        .read_iter =        snd_pcm_readv,
        .open =         snd_pcm_capture_open,
        .release =      snd_pcm_release,
        .llseek =       no_llseek,
        .poll =         snd_pcm_poll,
        .unlocked_ioctl =   snd_pcm_ioctl,
        .compat_ioctl =     snd_pcm_ioctl_compat,//　在这里被赋值了
        .mmap =         snd_pcm_mmap,
        .fasync =       snd_pcm_fasync,
        .get_unmapped_area =    snd_pcm_get_unmapped_area,
    }    
};
```

```c
static int snd_pcm_dev_register(struct snd_device *device)                                                                                                                                                         
{
    int cidx, err;
    struct snd_pcm_substream *substream;
    struct snd_pcm *pcm;

    if (snd_BUG_ON(!device || !device->device_data))
        return -ENXIO;
    pcm = device->device_data;

    mutex_lock(&register_mutex);
    err = snd_pcm_add(pcm);
    if (err)
        goto unlock;
    for (cidx = 0; cidx < 2; cidx++) {
        int devtype = -1;
        if (pcm->streams[cidx].substream == NULL)
            continue;
        switch (cidx) {
        case SNDRV_PCM_STREAM_PLAYBACK:
            devtype = SNDRV_DEVICE_TYPE_PCM_PLAYBACK;
            break;
        case SNDRV_PCM_STREAM_CAPTURE:
            devtype = SNDRV_DEVICE_TYPE_PCM_CAPTURE;
            break;
        }
        /* register pcm */
        err = snd_register_device(devtype, pcm->card, pcm->device,
                      &snd_pcm_f_ops[cidx], pcm,  // 在这里使用到了
                      &pcm->streams[cidx].dev);
        if (err < 0) { 
            list_del_init(&pcm->list);
            goto unlock;
        }

        for (substream = pcm->streams[cidx].substream; substream; substream = substream->next)
            snd_pcm_timer_init(substream);
    }    

    pcm_call_notify(pcm, n_register);

 unlock:
    mutex_unlock(&register_mutex);
    return err; 
}
```

```c
static int _snd_pcm_new(struct snd_card *card, const char *id, int device,
        int playback_count, int capture_count, bool internal,
        struct snd_pcm **rpcm)
{
    struct snd_pcm *pcm;
    int err; 
    static struct snd_device_ops ops = {
        .dev_free = snd_pcm_dev_free,
        .dev_register = snd_pcm_dev_register, // 这里被放置到了　ops中
        .dev_disconnect = snd_pcm_dev_disconnect,
    };   
    static struct snd_device_ops internal_ops = {
        .dev_free = snd_pcm_dev_free,
    };   

    if (snd_BUG_ON(!card))
        return -ENXIO;
    if (rpcm)
        *rpcm = NULL;
    pcm = kzalloc(sizeof(*pcm), GFP_KERNEL);
    if (!pcm)
        return -ENOMEM;
    pcm->card = card;
    pcm->device = device;
    pcm->internal = internal;
    mutex_init(&pcm->open_mutex);
    init_waitqueue_head(&pcm->open_wait);
    INIT_LIST_HEAD(&pcm->list);
    if (id) 
        strlcpy(pcm->id, id, sizeof(pcm->id));

    err = snd_pcm_new_stream(pcm, SNDRV_PCM_STREAM_PLAYBACK,
                 playback_count);
    if (err < 0) 
        goto free_pcm;

    err = snd_pcm_new_stream(pcm, SNDRV_PCM_STREAM_CAPTURE, capture_count);
    if (err < 0) 
        goto free_pcm;

    err = snd_device_new(card, SNDRV_DEV_PCM, pcm, 
                 internal ? &internal_ops : &ops);　// 这里使用了os　snd_device_new　函数也要看看
    if (err < 0) 
        goto free_pcm;

    if (rpcm)
        *rpcm = pcm; 
    return 0;                                                                                                                                                                                                      

free_pcm:
    snd_pcm_free(pcm);
    return err; 
}
```

```c
/**
 * snd_pcm_new - create a new PCM instance
 * @card: the card instance
 * @id: the id string
 * @device: the device index (zero based)
 * @playback_count: the number of substreams for playback
 * @capture_count: the number of substreams for capture
 * @rpcm: the pointer to store the new pcm instance
 *
 * Creates a new PCM instance.
 *
 * The pcm operators have to be set afterwards to the new instance
 * via snd_pcm_set_ops().
 *
 * Return: Zero if successful, or a negative error code on failure.
 */

int snd_pcm_new(struct snd_card *card, const char *id, int device,
        int playback_count, int capture_count, struct snd_pcm **rpcm)
{
    printk(KERN_ERR"colby %s %d id=%s device=%d playback_count=%d capture_count=%d", __func__, __LINE__, id, device, playback_count, capture_count);
    
    return _snd_pcm_new(card, id, device, playback_count, capture_count,        
            false, rpcm);
}
EXPORT_SYMBOL(snd_pcm_new);
```

**snd_pcm_new 函数很重要的**

------

回头看看　snd_device_new函数

```c

// snd_device_new(card, SNDRV_DEV_PCM, pcm, internal ? &internal_ops : &ops);

int snd_device_new(struct snd_card *card, enum snd_device_type type,
           void *device_data, struct snd_device_ops *ops)                                                                                                                                                          
{
    struct snd_device *dev;
    struct list_head *p; 

    if (snd_BUG_ON(!card || !device_data || !ops))
        return -ENXIO;
    dev = kzalloc(sizeof(*dev), GFP_KERNEL); // 在这里才分配了　snd_device
    if (!dev)
        return -ENOMEM;
    INIT_LIST_HEAD(&dev->list);
    dev->card = card;
    dev->type = type;
    dev->state = SNDRV_DEV_BUILD;
    dev->device_data = device_data;
    dev->ops = ops; // 这里也仅仅是赋值，并没有调用。

    /* insert the entry in an incrementally sorted list */
    list_for_each_prev(p, &card->devices) {
        struct snd_device *pdev = list_entry(p, struct snd_device, list);
        if ((unsigned int)pdev->type <= (unsigned int)type)
            break;
    }   

    list_add(&dev->list, p); 
    return 0;
}
EXPORT_SYMBOL(snd_device_new);
```



-------

snd_register_device函数分析：

```c
int snd_register_device(int type, struct snd_card *card, int dev,
            const struct file_operations *f_ops,
            void *private_data, struct device *device)
{
    int minor;
    int err = 0;
    struct snd_minor *preg;

    if (snd_BUG_ON(!device))
        return -EINVAL;

    preg = kmalloc(sizeof *preg, GFP_KERNEL);
    if (preg == NULL)
        return -ENOMEM;
    preg->type = type;
    preg->card = card ? card->number : -1;
    preg->device = dev;
    preg->f_ops = f_ops;
    preg->private_data = private_data;
    preg->card_ptr = card;
    mutex_lock(&sound_mutex);
    minor = snd_find_free_minor(type, card, dev);
    if (minor < 0) {
        err = minor;
        goto error;
    }

    preg->dev = device;
    device->devt = MKDEV(major, minor);
    err = device_add(device);
    if (err < 0)
        goto error;

    snd_minors[minor] = preg;
 error:
    mutex_unlock(&sound_mutex);
    if (err < 0)
        kfree(preg);
    return err;
}
EXPORT_SYMBOL(snd_register_device);
```

```c
#ifdef CONFIG_SND_DEBUG
void snd_pcm_debug_name(struct snd_pcm_substream *substream,
               char *name, size_t len) 
{
    snprintf(name, len, "pcmC%dD%d%c:%d",
         substream->pcm->card->number,
         substream->pcm->device,
         substream->stream ? 'c' : 'p', 
         substream->number);
}                                                                                                                                                                                                                  
EXPORT_SYMBOL(snd_pcm_debug_name);
#endif
```







修改：

```c
int snd_pcm_new_stream(struct snd_pcm *pcm, int stream, int substream_count)
{
    int idx, err; 
    struct snd_pcm_str *pstr = &pcm->streams[stream];
    struct snd_pcm_substream *substream, *prev;

#if IS_ENABLED(CONFIG_SND_PCM_OSS)
    mutex_init(&pstr->oss.setup_mutex);
#endif
    pstr->stream = stream;
    pstr->pcm = pcm; 
    pstr->substream_count = substream_count;
    if (!substream_count)
        return 0;                                                                                                                                                                                                  

    snd_device_initialize(&pstr->dev, pcm->card);
    pstr->dev.groups = pcm_dev_attr_groups;
    printk(KERN_ERR"colby %s %d  \n", __func__, __LINE__);
    if( (pcm->device) >= 10){ 
        dev_set_name(&pstr->dev, "pcmC%iD%i%c", pcm->card->number, pcm->device,
                stream == SNDRV_PCM_STREAM_PLAYBACK ? 'p' : 'c');
    
    }else{
        dev_set_name(&pstr->dev, "pcmC%iD0%i%c", pcm->card->number, pcm->device,
             stream == SNDRV_PCM_STREAM_PLAYBACK ? 'p' : 'c');
    }
    

...
}
```

通过追，发现要想　多加一个零，确实就是在这里加的。但此时　hal层会报错：failed: cannot open device '/dev/snd/pcmC0D0p': No such file or directory

所以我们这里不能添加0.



所以这里还是采用在hal层修改的方案。