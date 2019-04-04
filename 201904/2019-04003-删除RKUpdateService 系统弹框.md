# 删除RKUpdateService 系统弹框

```shell
01-18 17:19:04.120   382   495 V sjf     : onFling
01-18 17:19:04.120  1121  1121 D NotifyDeleteActivity: click ok!
01-18 17:19:04.120  1121  1121 D RKUpdateService: try to deletePackage...
01-18 17:19:04.122  1121  1121 D RKUpdateService: path=/data/media/0/androidRomUpdate/all.zip ,file not exists!
01-18 17:19:04.124   382   685 W AudioTrack: AUDIO_OUTPUT_FLAG_FAST denied by client; transfer 4, track 48000 Hz, output 44100 Hz
01-18 17:19:04.132   255   329 D AudioHardwareTiny: 1212121 start_output_stream
01-18 17:19:04.132   255   329 E AudioHardwareTiny: vicent r 44100  c 2  ps 512 pc 3
01-18 17:19:04.133   255   329 D AudioHardwareTiny: Device     : 0x8040a
01-18 17:19:04.133   255   329 D AudioHardwareTiny: SampleRate : 44100
01-18 17:19:04.133   255   329 D AudioHardwareTiny: Channels   : 2
01-18 17:19:04.133   255   329 D AudioHardwareTiny: Formate    : 0
01-18 17:19:04.133   255   329 D AudioHardwareTiny: PreiodSize : 512
01-18 17:19:04.133   255   329 E AudioHardwareTiny: start_output_stream() >>>> PCM_CARD_HDMI:2, PCM_CARD:0, HdmiIn_snd_config.config:4<<<<<<<<<<<<
01-18 17:19:04.133   255   329 V alsa_route: route_pcm_open() route 11
01-18 17:19:04.133   255   329 V alsa_route: get_route_config() route 11
01-18 17:19:04.133   255   329 D alsa_route: route_info->sound_card 0, route_info->devices 0  
01-18 17:19:04.133   255   329 V alsa_route: route_pcm_close() route 24
01-18 17:19:04.135   255   329 D alsa_route: route_set_controls() set route 11
01-18 17:19:04.135   255   329 V alsa_route: get_route_config() route 11
01-18 17:19:04.135   255   329 V alsa_route: set_controls() ctls_count 26
01-18 17:19:04.143   255   329 V alsa_route: set_controls() set ctl RT5640 ASRC Switch to Disable
01-18 17:19:04.144   255   329 V alsa_route: set_controls() set ctl DAI select to 1:2|2:1
01-18 17:19:04.144   255   329 V alsa_route: set_controls() set ctl Mono DAC Playback Volume to 175
01-18 17:19:04.144   255   329 V alsa_route: set_controls() set ctl DAC2 Playback Switch to 1
01-18 17:19:04.144   255   329 V alsa_route: set_controls() set ctl Mono DAC MIXL DAC L2 Switch to 1
01-18 17:19:04.144   255   329 V alsa_route: set_controls() set ctl Mono DAC MIXR DAC R2 Switch to 1
01-18 17:19:04.144   255   329 V alsa_route: set_controls() set ctl OUT Playback Switch to 1
01-18 17:19:04.144   255   329 V alsa_route: set_controls() set ctl OUT Channel Switch to 1
01-18 17:19:04.144   255   329 V alsa_route: set_controls() set ctl LOUT MIX OUTVOL L Switch to 1
01-18 17:19:04.144   255   329 V alsa_route: set_controls() set ctl LOUT MIX OUTVOL R Switch to 1
01-18 17:19:04.144   255   329 V alsa_route: set_controls() set ctl OUT MIXL DAC R2 Switch to 1
01-18 17:19:04.144   255   329 V alsa_route: set_controls() set ctl OUT MIXL DAC L2 Switch to 1
01-18 17:19:04.144   255   329 V alsa_route: set_controls() set ctl OUT MIXR DAC L2 Switch to 1
01-18 17:19:04.145   255   329 V alsa_route: set_controls() set ctl OUT MIXR DAC R2 Switch to 1
01-18 17:19:04.145   255   329 V alsa_route: set_controls() set ctl Speaker Channel Switch to 1
01-18 17:19:04.145   255   329 V alsa_route: set_controls() set ctl Speaker Playback Volume to 39
01-18 17:19:04.145   255   329 V alsa_route: set_controls() set ctl SPOL MIX SPKVOL R Switch to 1
01-18 17:19:04.145   255   329 V alsa_route: set_controls() set ctl SPOL MIX SPKVOL L Switch to 1
01-18 17:19:04.145   255   329 V alsa_route: set_controls() set ctl SPOR MIX SPKVOL R Switch to 1
01-18 17:19:04.145   255   329 V alsa_route: set_controls() set ctl SPK MIXL DAC L2 Switch to 1
01-18 17:19:04.145   255   329 V alsa_route: set_controls() set ctl SPK MIXR DAC R2 Switch to 1
01-18 17:19:04.145   255   329 V alsa_route: set_controls() set ctl Speaker L Playback Switch to 1
01-18 17:19:04.146   255   329 V alsa_route: set_controls() set ctl Speaker R Playback Switch to 1
01-18 17:19:04.146   255   329 V alsa_route: set_controls() set ctl HPO MIX DAC2 Switch to 1
01-18 17:19:04.146   255   329 V alsa_route: set_controls() set ctl HP L Playback Switch to 1
01-18 17:19:04.146   255   329 V alsa_route: set_controls() set ctl HP R Playback Switch to 1
01-18 17:19:04.146   255   329 V alsa_route: route_pcm_open exit
01-18 17:19:04.292  1121  1121 D NotifyDeleteActivity: onDestory.........
01-18 17:19:04.292  1121  1121 D RKUpdateService: unLockWorkHandler...

```

