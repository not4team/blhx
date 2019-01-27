#include <jni.h>
#include <string>
#include <unistd.h>
#include <errno.h>
#include "screenshot.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_notfour_blhx_jni_ScreenShot_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_notfour_blhx_jni_ScreenShot_switchUid(
        JNIEnv *env,
        jobject /* this */, jint uid) {
    int state;
    state = switch_euid((uid_t) uid);
    if (!state) {
        LOGE("switch_euid fail");
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_notfour_blhx_jni_ScreenShot_takeScreenshot(
        JNIEnv *env,
        jobject /* this */, jstring out) {
    FILE *png = NULL;
    FILE *fb_in = NULL;
    fb_in = fopen("/dev/graphics/fb0", "r");
    if (!fb_in) {
        LOGE("error: could not read framebuffer\n");
        return 1;
    }
    const char *outfile = env->GetStringUTFChars(out, 0);
    png = fopen(outfile, "w");
    if (!png) {
        LOGE("error: writing file %s: %s\n",
             outfile, strerror(errno));
        env->ReleaseStringUTFChars(out, outfile);
        return 1;
    }
    take_screenshot(fb_in, png);
    env->ReleaseStringUTFChars(out, outfile);
    return 0;
}
