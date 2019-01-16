//
// Created by shixq on 19-1-14.
//

#ifndef BLHX_SCREENSHOT_H
#define BLHX_SCREENSHOT_H

#include <stdio.h>
#include <android/log.h>

#define AID_ROOT             0  /* traditional unix root user */
#define AID_SYSTEM        1000  /* system server */
#define AID_GRAPHICS      1003  /* graphics devices */
#define AID_SDCARD_RW     1015  /* external storage write access */
#define AID_SHELL         2000  /* adb and debug shell user */
#define AID_LOG           1007  /* log devices */
#define LOG_TAG "screenshot"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#ifdef __cplusplus
extern "C" {
#endif

int switch_euid(uid_t uid);

int take_screenshot(FILE *fb_in, FILE *fb_out);

#ifdef __cplusplus
}
#endif
#endif //BLHX_SCREENSHOT_H
