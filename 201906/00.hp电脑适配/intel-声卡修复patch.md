# intel-声卡启动

## 1. 

在每次从Windows下去切换到android平台时。喇叭没有声音：

## 2. 问题分析

在windows下有省电模式，对声卡设备做了处理。配置了相关的寄存器。

## 3. patch包

kernel 版本为： 4.19.50

```c
From 1915532282393b1105fe407011fc632c4d566a35 Mon Sep 17 00:00:00 2001
From: "he, bo" <bo.he@intel.com>
Date: Sat, 6 Jul 2019 08:55:50 +0800
Subject: [PATCH] ALSA: hda/realtek: fix the audio lost with restore the
 default coeff registers

restore the default coeff register to fix HP Pavlilon x360 audio lost
during reboot from windows as the registers will not reset event with
double function reset, study show the 0xd register restore from 0xa02f
to 0xa023, here restore all the chagned registers for safety.

Tracked-On:
Signed-off-by: he, bo <bo.he@intel.com>
---
 sound/pci/hda/patch_realtek.c | 12 ++++++++++++
 1 file changed, 12 insertions(+)

diff --git a/sound/pci/hda/patch_realtek.c b/sound/pci/hda/patch_realtek.c
index 05b4eb85a092..86cb831fea18 100644
--- a/sound/pci/hda/patch_realtek.c
+++ b/sound/pci/hda/patch_realtek.c
@@ -7738,6 +7738,18 @@ static int patch_alc269(struct hda_codec *codec)
 		spec->shutup = alc225_shutup;
 		spec->init_hook = alc225_init;
 		spec->gen.mixer_nid = 0; /* no loopback on ALC225, ALC295 and ALC299 */
+
+		/* restore the default coeff register to fix HP Pavlilon x360 audio lost
+		 * during reboot from windows as the registers will not reset event with
+		 * double function reset, study show the 0xd register restore from 0xa02f
+		 * to 0xa023, here restore all the chagned registers for safety. 
+		 * */
+		alc_write_coefex_idx(codec, 0x20, 0x8, 0x6a8c);
+		alc_write_coefex_idx(codec, 0x20, 0xb, 0x7770);
+		alc_write_coefex_idx(codec, 0x20, 0xd, 0xa023);
+		alc_write_coefex_idx(codec, 0x20, 0x31, 0xd200);
+		alc_write_coefex_idx(codec, 0x20, 0x37, 0xfe05);
+		alc_write_coefex_idx(codec, 0x20, 0x51, 0x6f6f);
 		break;
 	case 0x10ec0234:
 	case 0x10ec0274:
-- 
2.20.1
```

从patch中我们发现，配置了相关的寄存器。