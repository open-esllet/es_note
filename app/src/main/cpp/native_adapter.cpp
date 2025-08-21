/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2025, pat733
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
#include "util.h"
#include "native_adapter.h"
#include "committer.h"
#include <jni.h>
#include <android/bitmap.h>

static JavaVM* gJavaVM;
static struct committer* gCmt = NULL;

static jobject gAdapterObj;
static jclass gAdapterCls;
static jmethodID gOnTouchEventMId;

void reportEvent(int eventType, int x, int y, int pressure)
{
    if (gAdapterObj == NULL || gOnTouchEventMId == NULL) {
        return;
    }

    JNIEnv* env = NULL;
    int status = gJavaVM->GetEnv((void**) &env, JNI_VERSION_1_6);
    if (status < 0) {
        JavaVMAttachArgs args = {JNI_VERSION_1_6, NULL, NULL};

        status = gJavaVM->AttachCurrentThread(&env, (void*) &args);
        if (status != JNI_OK) {
            ALOGD("%s thread attach failed: %#x", __FUNCTION__, status);
            goto error;
        }
    }

    env->CallVoidMethod(gAdapterObj, gOnTouchEventMId, eventType, x, y, pressure);
    return;

error:
    if(gJavaVM->DetachCurrentThread() != JNI_OK) {
        ALOGD("%s: DetachCurrentThread() failed", __FUNCTION__);
    }
    return;
}

jintArray init(JNIEnv* env, jobject jthis) {
    ALOGD("%s()", __FUNCTION__);

    gAdapterObj = env->NewGlobalRef(jthis);
    gAdapterCls = env->GetObjectClass(jthis);
    gOnTouchEventMId = env->GetMethodID(gAdapterCls, "onTouchEvent", "(IIII)V");

    const int SIZE = 3;
    jint buffer[SIZE] = {0, 0, 0 };

    jintArray result = env->NewIntArray(SIZE);
    if (result == NULL) {
        ALOGD("%s() failed mem not enough !", __FUNCTION__);
        return NULL;
    }

    if (0 == gCmt->init(gCmt, &buffer[0], &buffer[1], &buffer[2])) {
        env->SetIntArrayRegion(result, 0, SIZE, buffer);
        return result;
    }

    ALOGD("%s() committer init failed !", __FUNCTION__);
    return NULL;
}

static void startEventLoop(JNIEnv* env, jobject jthis) {
    ALOGD("startEventLoop()");
    gCmt->startEventLoop(gCmt);
}

static void stopEventLoop(JNIEnv* env, jobject jthis) {
    ALOGD("stopEventLoop()");
    gCmt->stopEventLoop(gCmt);
}

static int _getOverlayStatus(JNIEnv* env, jobject jthis) {
    return gCmt->get_overlay_status(gCmt);
}

static void setOverlayEnabled(JNIEnv* env, jobject jthis, jboolean enabled) {
    gCmt->set_overlay_enabled(gCmt, enabled);
}

static void setHandwritingEnabled(JNIEnv* env, jobject jthis, jboolean enabled) {
    ALOGD("%s(%d)", __FUNCTION__, enabled);
    gCmt->setHandwritingEnabled(gCmt, enabled);
}

static void refreshOSD(JNIEnv* env, jobject jthis, jboolean clearContent, jboolean sendBuff) {
    ALOGD("%s() clearContent=%d, sendBuff=%d", __FUNCTION__, clearContent, sendBuff);
    gCmt->clearOsd(gCmt, clearContent, sendBuff);
}

static void refreshScreen(JNIEnv* env, jobject jthis) {
    ALOGD("%s()", __FUNCTION__);
    gCmt->refreshScreen(gCmt);
}

static void onAppEnter(JNIEnv* env, jobject jthis) {
    gCmt->onAppEnter(gCmt);
}

static void onAppExit(JNIEnv* env, jobject jthis) {
    gCmt->onAppExit(gCmt);
}

static void onAppCrash(JNIEnv* env, jobject jthis) {
    gCmt->onAppCrash(gCmt);
}

static void commitBitmap(JNIEnv* env, jobject jthis, jobject jbitmap, int l, int t, int r, int b) {

    AndroidBitmapInfo info;
    AndroidBitmap_getInfo(env, jbitmap, &info);

    void* addrPtr = NULL;
    AndroidBitmap_lockPixels(env, jbitmap, &addrPtr);
    gCmt->drawBuf(gCmt, addrPtr, l, t, r, b);
    AndroidBitmap_unlockPixels(env, jbitmap);
}

static const JNINativeMethod nativeMethods[] = {
    {"_init", "()[I", (void*)init},
    {"_startEventLoop", "()V", (void*)startEventLoop},
    {"_stopEventLoop", "()V", (void*)stopEventLoop},
    {"_setHandwritingEnabled", "(Z)V", (void*)setHandwritingEnabled},
    {"_setOverlayEnabled", "(Z)V", (void*)setOverlayEnabled},
    {"_getOverlayStatus", "()I", (void*)_getOverlayStatus},
    {"_refreshOSD", "(ZZ)V", (void*)refreshOSD},
    {"_refreshScreen", "()V", (void*)refreshScreen},
    {"_commitBitmap", "(Landroid/graphics/Bitmap;IIII)V", (void*)commitBitmap},
    {"_onAppEnter", "()V", (void*)onAppEnter},
    {"_onAppExit", "()V", (void*)onAppExit},
    {"_onAppCrash", "()V", (void*)onAppCrash},
};

static int registerNativeMethods(JNIEnv* env) {
    jclass clz = env->FindClass("com/es/note/jni/NativeAdapter");
    if (NULL == clz) {
        ALOGD("%s FindClass failed !", __FUNCTION__);
        return JNI_ERR;
    }

    if (env->RegisterNatives(clz, nativeMethods,
            sizeof(nativeMethods) / sizeof(nativeMethods[0])) != JNI_OK) {
        ALOGD("%s RegisterNatives failed !", __FUNCTION__);
        return JNI_ERR;
    }
    
    return JNI_OK;
}

jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    ALOGD("%s()", __FUNCTION__);

    JNIEnv* env = NULL;
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    if (NULL == env || registerNativeMethods(env) != 0) {
        return JNI_ERR;
    }

    gJavaVM = vm;
    gCmt = createCommitter();

    return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM* vm, void* reserved)
{
    ALOGD("%s", __FUNCTION__);

    JNIEnv* env = NULL;
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK)
        return;

    env->DeleteGlobalRef(gAdapterObj);
}