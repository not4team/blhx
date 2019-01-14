//
// Created by shixq on 19-1-14.
//

#ifndef BLHX_SCREENSHOT_H
#define BLHX_SCREENSHOT_H

#include <stdio.h>
#include <android/log.h>

#define LOG_TAG "screenshot"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#ifdef __cplusplus
extern "C" {
#endif

int take_screenshot(FILE *fb_in, FILE *fb_out);

#ifdef __cplusplus
}
#endif
#endif //BLHX_SCREENSHOT_H
