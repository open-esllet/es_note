LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= \
    native_adapter.cpp \
    committer.cpp \
    get_event.cpp \
    libuv2.c

LOCAL_CFLAGS := -O2 -g -W -Wall -Wno-unused-parameter -Wno-unused-variable -Wno-constant-conversion -Wno-sign-compare -frtti
LOCAL_LDLIBS := -llog -ljnigraphics

LOCAL_MODULE:= libnativeadapter

LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)