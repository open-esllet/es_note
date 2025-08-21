/*
 * Copyright (C) 2018 Fuzhou Rockchip Electronics Co.Ltd.
 *
 * Modification based on code covered by the Apache License, Version 2.0 (the "License").
 * You may not use this software except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS TO YOU ON AN "AS IS" BASIS
 * AND ANY AND ALL WARRANTIES AND REPRESENTATIONS WITH RESPECT TO SUCH SOFTWARE, WHETHER EXPRESS,
 * IMPLIED, STATUTORY OR OTHERWISE, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF TITLE,
 * NON-INFRINGEMENT, MERCHANTABILITY, SATISFACTROY QUALITY, ACCURACY OR FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.
 *
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <jni.h>
#include <sys/time.h>
#include <unistd.h>
#include "committer.h"
#include "native_adapter.h"

static float XScale = 0.106;//0.999;//0.089;//0.059;//depend on pen device
static float YScale = 0.110;//0.999;//0.089;//0.059;

extern int g_touch_screen_type;

long long startMicro = 0;

void Luma8bit_to_8bit_bw(short int *src, short int *dst, int w)
{
    int i;

    for (i = 0; i < w; i += 2) {
        *dst++ = *src++;
    }
}

void Luma8bit_to_4bit_bw(short int  *src,  char *dst, int w)
{
    int i;
    int g0, g1;
    int src_data;

    for (i = 0; i < w; i += 2) {
        src_data =  *src++;
        g0 = (src_data & 0xf0) >> 4;

        g1 = (src_data & 0xf000) >> 8;

        *dst++ = g1 | g0;
    }
}

void Luma8bit_to_4bit_gray(short int  *src,  char *dst, int w, int parity)
{
    int i;
    int g0, g1;
    int src_data;

    for (i = 0; i < w; i += 2) {
        src_data =  *src++;
        g0 = src_data & 0x0f;
        if (g0 != 0x0f) {
            if (parity)
                g0 = 0x0f;
            else
                g0 = 0x00;
        }
        g1 = ((src_data & 0x0f00) >> 4);
        if (g1 != 0xf0) {
            if (parity)
                g1 = 0x00;
            else
                g1 = 0xf0;
        }
        *dst++ = g1 | g0;
    }
}

void gray256_to_gray2_fix(uint8_t *dst, uint8_t *src, int panel_h, int panel_w,
                          int vir_width, struct Rect* rect, int pen_color, int buf_format)
{
    int w = rect->right - rect->left;
    int offset = rect->top * panel_w + rect->left;
    int offset_dst = rect->top * vir_width + rect->left;
    if (offset_dst % 2) {
        offset_dst += (2 - offset_dst % 2);
    }
    if (offset % 2) {
        offset += (2 - offset % 2);
    }
    if ((offset_dst + w) % 2) {
        w -=  (offset_dst + w) % 2;
    }
    for (int h = rect->top; h <= rect->bottom && h < panel_h; h++) {
        if (pen_color > 2) {
            Luma8bit_to_4bit_gray((short int*)(src + offset), (char*)(dst + (offset_dst >> 1)), w, h&1);
        } else {
            if (buf_format == EBC_Y4)
                Luma8bit_to_4bit_bw((short int*)(src + offset), (char*)(dst + (offset_dst >> 1)), w);
            else
                Luma8bit_to_8bit_bw((short int*)(src + offset), (short int*)(dst + offset_dst), w);
        }

        offset += panel_w;
        offset_dst += vir_width;
    }
}

int init(struct committer* c, int* o_width, int* o_height, int* o_panel_color) {
    c->ebc_fd = -1;
    c->ebc_buf_format = EBC_Y4;
    c->eventX = 0;
    c->eventY = 0;

    c->ebc_buffer_base = NULL;
    c->gray16_buffer = NULL;

#ifndef CONF_EPD_DRIVER
    c->ebc_fd = open("/dev/ebc", O_RDWR, 0);
#else
    c->ebc_fd = open("/dev/epd", O_RDWR, 0);
#endif
    if (c->ebc_fd < 0) {
        ALOGD("open /dev/ebc failed\n");
        return -1;
    }

    if(ioctl(c->ebc_fd, EBC_GET_BUFFER_INFO,&c->ebc_buf_info)!=0) {
        ALOGD("GET_EBC_BUFFER failed\n");
        return -1;
    }

    if(ioctl(c->ebc_fd, EBC_GET_BUF_FORMAT, &c->ebc_buf_format)!=0){
        ALOGD("EBC_GET_BUF_FORMAT failed\n");
        return -1;
    }

#ifndef CONF_EPD_DRIVER
        c->ebc_buffer_base = mmap(0, EINK_FB_SIZE * 5, PROT_READ | PROT_WRITE, MAP_SHARED,
                                  c->ebc_fd, 0);
#else
    c->ebc_buffer_base = mmap(0, c->ebc_buf_info.mmap_size,
                                  PROT_READ|PROT_WRITE, MAP_SHARED, c->ebc_fd, 0);
#endif
    if (c->ebc_buffer_base == MAP_FAILED) {
        ALOGD("Error mapping the ebc buffer (%s)\n", strerror(errno));
        return -1;
    }

    unsigned long vaddr_real = (long int)(c->ebc_buffer_base);
    if(ioctl(c->ebc_fd, EBC_GET_OSD_BUFFER, &c->buf_info)!=0) {
        ALOGD("EBC_GET_OSD_BUFFER failed\n");
        return -1;
    }
    c->gray16_buffer = (int *)(vaddr_real + c->buf_info.offset);
    if (c->ebc_buf_format == EBC_Y4)
        memset(c->gray16_buffer, 0xff, c->ebc_buf_info.width * c->ebc_buf_info.height >> 1);
    else
        memset(c->gray16_buffer, 0xff, c->ebc_buf_info.width * c->ebc_buf_info.height);

    if (c->ebc_buf_info.panel_color == 3) {
        init_uv_head();
    }

    ALOGD("panel_color:%d, ebc_buf_format:%d, width:%d,height:%d",
          c->ebc_buf_info.panel_color, c->ebc_buf_format, c->ebc_buf_info.width, c->ebc_buf_info.height);

    *o_width  =  c->ebc_buf_info.width;
    *o_height = c->ebc_buf_info.height;
    *o_panel_color = c->ebc_buf_info.panel_color;

    return 0;
}

int set_overlay_enabled(struct committer* c, bool enabled) {
    if(enabled) {
        if(ioctl(c->ebc_fd, EBC_ENABLE_OVERLAY, NULL) != 0) {
            ALOGD("ENABLE_EBC_OVERLAY failed\n");
            return -1;
        } else {
            ALOGD("set_overlay_enabled() EBC_ENABLE_OVERLAY");
        }
    } else {
        if(ioctl(c->ebc_fd, EBC_DISABLE_OVERLAY, NULL) != 0) {
            ALOGD("DISABLE_EBC_OVERLAY failed\n");
            return -1;
        } else {
            ALOGD("set_overlay_enabled() EBC_DISABLE_OVERLAY");
        }
    }

    return 0;
}

int get_overlay_status(struct committer* c) {
    int status;
    if (ioctl(c->ebc_fd, EBC_GET_OVERLAY_STATUS, &status)) {
        ALOGD("get_overlay_status() failed !");
        return -1;
    }
    return status;
}

void setHandwritingEnabled(struct committer* c, bool enabled)
{
    c->handwritingEnabled = enabled;
}

void refreshScreen(struct committer* c) {
    if(ioctl(c->ebc_fd, EBC_REFRESH_SCREEN, &c->buf_info)!=0) {
        ALOGD("%s EBC_REFRESH_SCREEN failed\n", __FUNCTION__);
        return;
    } else {
        ALOGD("%s EBC_REFRESH_SCREEN\n", __FUNCTION__);
    }
}

void clearOsd(struct committer* c, bool clearContent, bool sendBuff)
{
    if (clearContent) {
        ALOGD("%s memset", __FUNCTION__);
        memset(c->gray16_buffer, 0xff, c->ebc_buf_info.width * c->ebc_buf_info.height);
    }

    if (sendBuff) {
        struct ebc_buf_info_t buf_info;
        buf_info.win_x1 = 0;
        buf_info.win_y1 = 0;
        buf_info.win_y2 = c->ebc_buf_info.height;
        buf_info.win_x2 = c->ebc_buf_info.width;
        buf_info.epd_mode = EPD_OVERLAY;

        if (ioctl(c->ebc_fd, EBC_SEND_OSD_BUFFER, &buf_info) != 0) {
            ALOGD("%s EBC_SEND_OSD_BUFFER failed\n", __FUNCTION__);
            return;
        }
    }
}

__kernel_suseconds_t getMicroSeconds() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return tv.tv_sec * 1000000 + tv.tv_usec;
}

void drawBuf(struct committer* c, void* buffer, float left, float top, float right, float bottom)
{
    struct ebc_buf_info_t buf_info;

    buf_info.win_x1 = left;
    buf_info.win_y1 = top;
    buf_info.win_x2 = right;
    buf_info.win_y2 = bottom;

    if (c->ebc_buf_info.panel_color == 3) {
        buf_info.win_x1= buf_info.win_x1-( buf_info.win_x1%2);
        buf_info.win_y1= buf_info.win_y1-( buf_info.win_y1%2);
        buf_info.win_x2= buf_info.win_x2+( buf_info.win_x2%2);
        buf_info.win_y2= buf_info.win_y2+( buf_info.win_y2%2);
    }

    struct Rect rect = {buf_info.win_x1, buf_info.win_y1, buf_info.win_x2, buf_info.win_y2};
    buf_info.epd_mode = EPD_OVERLAY;

    if (c->ebc_buf_info.panel_color == 0) {
#ifndef CONF_EPD_DRIVER
        gray256_to_gray2_fix((uint8_t * )(c->gray16_buffer), (uint8_t *) buffer,
                             c->ebc_buf_info.height,
                             c->ebc_buf_info.width,
                             c->ebc_buf_info.width,
                             &rect,
                             1,
                             c->ebc_buf_format);
#else
        Rgba8888ToGrayRgba8888Window((char*)(c->gray16_buffer),
                                  (char*)buffer,
                                  buf_info.win_x1,
                                  buf_info.win_y1,
                                  buf_info.win_x2,
                                  buf_info.win_y2,
                                  c->ebc_buf_info.width);
#endif
    } else {
        Rgba8888ToGrayColorWindow((char*)(c->gray16_buffer),
                                  (char*)buffer,//RGBA 8888
                                  buf_info.win_x1,
                                  buf_info.win_y1,
                                  buf_info.win_x2,
                                  buf_info.win_y2,
                                  c->ebc_buf_info.width);
    }

    if(ioctl(c->ebc_fd, EBC_SEND_OSD_BUFFER, &buf_info)!=0) {
        ALOGD("%s EBC_SEND_OSD_BUFFER failed\n", __FUNCTION__);
        return;
    }
}

void resetEvent(struct committer *c, bool ignoreUpdateProp = false)
{
    if (!ignoreUpdateProp) {
        c->pressure = 0;
        c->eventX = 0;
        c->eventY = 0;
    }
    reportEvent(TOUCH_UP, c->eventX, c->eventY, c->pressure);
    TPLOG("%s", __FUNCTION__);
}

void once(struct committer *c)
{
    struct input_event e;

    if (get_event(&e, 500) == 0) {

        if (e.type == EV_ABS) {
            if (e.code == ABS_X) {
                if (g_touch_screen_type == TOUCH_SCREEN_WACOM) {
                    c->eventX = e.value * XScale;
                } else if (g_touch_screen_type == TOUCH_SCREEN_HUION) {
                    c->eventX = e.value;
                } else if (g_touch_screen_type == TOUCH_SCREEN_ILI) {
                    c->eventX = e.value * 0.136718f;
                } else if (g_touch_screen_type == TOUCH_SCREEN_SIS) {
                    c->eventX = e.value * 0.546875f;
                    TPLOG("e.value = %d eventX = %d",e.value,c->eventX);
                }
                else if (g_touch_screen_type == TOUCH_SCREEN_HUICHI) {
                    c->eventX = e.value * 0.1f;
                }
                else if (g_touch_screen_type == TOUCH_SCREEN_KT6739) {
#ifndef CONF_238_WITH_KASA
                    c->eventX = c->ebc_buf_info.width - e.value * 0.043902f;
#else
                    c->eventX = e.value * 0.093752861f;
                    TPLOG("%s eventX = %d, originX = %d", __FUNCTION__, c->eventX, e.value);
#endif
                }
            } else if (e.code == ABS_Y) {
#ifndef CONF_EPD_DRIVER
                if (g_touch_screen_type == TOUCH_SCREEN_WACOM) {
                    c->eventY = c->ebc_buf_info.height - e.value * YScale;
                } else if (g_touch_screen_type == TOUCH_SCREEN_HUION) {
                    c->eventY = e.value;
                } else if (g_touch_screen_type == TOUCH_SCREEN_ILI) {
                    c->eventY = e.value * 0.102539f; // MPP
                }
#else
                if (c->ebc_buf_info.flipv == 0) {
                    if (g_touch_screen_type == TOUCH_SCREEN_WACOM) {
                        c->eventY = e.value * YScale;
                    } else if (g_touch_screen_type == TOUCH_SCREEN_HUION) {
                         c->eventY = e.value;
                    } else if (g_touch_screen_type == TOUCH_SCREEN_ILI) {
                        c->eventY = e.value * 0.175f; // USI
                    } else if (g_touch_screen_type == TOUCH_SCREEN_SIS) {
                        c->eventY = e.value * 0.40625; // USI
                        TPLOG("e.value = %d event Y = %d",e.value,c->eventY);
                    }
                    else if (g_touch_screen_type == TOUCH_SCREEN_HUICHI) {
                        c->eventY = e.value;
                    } else if (g_touch_screen_type == TOUCH_SCREEN_KT6739) {
#ifdef CONF_238_WITH_KASA
                        c->eventY = e.value * 0.052735984f;
                        TPLOG("%s eventY = %d, originY = %d", __FUNCTION__,  c->eventY, e.value);
#endif
                    }
                } else if (c->ebc_buf_info.flipv == 1) {

                    if (g_touch_screen_type == TOUCH_SCREEN_WACOM) {
                        c->eventY = c->ebc_buf_info.height - e.value * YScale;
                    }  else if (g_touch_screen_type == TOUCH_SCREEN_HUION) {
                        c->eventY = c->ebc_buf_info.height - e.value;
                    } else if (g_touch_screen_type == TOUCH_SCREEN_ILI) {
                        c->eventY = c->ebc_buf_info.height - e.value * 0.102539f; // MPP
                    } else if (g_touch_screen_type == TOUCH_SCREEN_SIS) {
                        c->eventY = c->ebc_buf_info.height - e.value * 0.102539f; // MPP
                        TPLOG("c->ebc_buf_info.height");
                    } else if (g_touch_screen_type == TOUCH_SCREEN_HUICHI) {
                        c->eventY = c->ebc_buf_info.height - e.value * 0.1f;
                    } else if (g_touch_screen_type == TOUCH_SCREEN_KT6739) {
                        c->eventY = c->ebc_buf_info.height - e.value * 0.078048f;
                    }
                }
#endif
            } else if (e.code == ABS_PRESSURE) {
                if (!c->downPressed) return;

                c->pressure = e.value;
            } else if (e.code == ABS_TILT_X) {
                if (!c->downPressed) return;

            } else if (e.code == ABS_TILT_Y) {
                if (!c->downPressed) return;
            }
        } else if (e.type == EV_KEY) {
            if (e.code == BTN_TOUCH) {
                if (e.value == 1) {
                    if (c->eventX == 0 || c->eventY == 0) {
                        TPLOG("%s ignore BTN_TOUCH down (x:%d, y:%d)", __FUNCTION__, c->eventX, c->eventY);
                    } else {
                        TPLOG("%s BTN_TOUCH down (x:%d, y:%d)", __FUNCTION__, c->eventX, c->eventY);
                        c->downPressed = true;
                        reportEvent(TOUCH_DOWN, c->ebc_buf_info.width-c->eventX, c->eventY, c->pressure);
                    }
                } else if (e.value == 0) {
                    TPLOG("%s BTN_TOUCH up (x:%d, y:%d)", __FUNCTION__, c->eventX, c->eventY);
                    reportEvent(TOUCH_UP, c->ebc_buf_info.width-c->eventX, c->eventY, c->pressure);
                }
            } else if (e.code == BTN_TOOL_PEN) {
                if (e.value == 1) {
                    TPLOG("%s BTN_TOOL_PEN down (x:%d, y:%d)", __FUNCTION__, c->eventX, c->eventY);
                } else if (e.value == 0) {
                    TPLOG("%s BTN_TOOL_PEN up (x:%d, y:%d)", __FUNCTION__, c->eventX, c->eventY);
                }
            } else if (e.code == BTN_TOOL_RUBBER) {
                if (e.value == 1) {
                    TPLOG("%s BTN_TOOL_RUBBER down (x:%d, y:%d)", __FUNCTION__, c->eventX, c->eventY);
                    resetEvent(c, true);
                    c->downPressed = true;
                    reportEvent(RUBBER_DOWN, c->ebc_buf_info.width-c->eventX, c->eventY, c->pressure);
                    if (c->eventX == 0 || c->eventY == 0) {
                        TPLOG("%s BTN_TOOL_RUBBER ignore BTN_TOUCH down (x:%d, y:%d)", __FUNCTION__, c->eventX, c->eventY);
                    } else {
                        TPLOG("%s BTN_TOOL_RUBBER BTN_TOUCH down (x:%d, y:%d)", __FUNCTION__, c->eventX, c->eventY);
                        reportEvent(TOUCH_DOWN, c->ebc_buf_info.width-c->eventX, c->eventY, c->pressure);
                    }
                } else if (e.value == 0) {
                    TPLOG("%s BTN_TOOL_RUBBER up (x:%d, y:%d)", __FUNCTION__, c->eventX, c->eventY);
                    resetEvent(c, true);
                    reportEvent(RUBBER_UP, c->ebc_buf_info.width-c->eventX, c->eventY, c->pressure);
                }
            } else {
                TPLOG("%s EV_KEY code=%x, value=%d", __FUNCTION__, e.code, e.value);
            }
        } else if (e.type == EV_SYN) {
            if (c->downPressed) {
                if (c->eventX < CONF_ACTION_BAR_HEIGHT *2 + 6 || c->eventX > c->ebc_buf_info.width
                    || c->eventY < 0 || c->eventY > c->ebc_buf_info.height) {
                    resetEvent(c);
                } else {
                    if (c->eventX == 0 || c->eventY == 0 || c->pressure == 0) {
                        TPLOG("%s ignore move (x:%d, y:%d) pressure=%d", __FUNCTION__, c->eventX,
                              c->eventY, c->pressure);
                    } else {
                        TPLOG("%s move (x:%d, y:%d) pressure=%d", __FUNCTION__, c->eventX, c->eventY, c->pressure);
                        reportEvent(TOUCH_MOVE, c->ebc_buf_info.width-c->eventX, c->eventY, c->pressure);
                    }
                }
            }
            else
                TPLOG("c->downPressed null");
        } else if(e.type == EV_MSC) {
            if (e.code == MSC_SCAN) {
                TPLOG("%s ignore MSC_SCAN event", __FUNCTION__);
            }
        } else {
            TPLOG("%s other event type=%x, code=%x, value=%d", __FUNCTION__, e.type, e.code, e.value);
            resetEvent(c);
        }
    }
}

void* eventLoop(void* arg)
{
    struct committer *c = (struct committer *)arg;

    while (true) {
        if (c->exit_) {
            break;
        }

        if (!c->handwritingEnabled) {
            sleep(1);
            continue;
        }

        once(c);
    }

    uninit_getevent();
    ALOGD("%s stopped.", __FUNCTION__);

    pthread_exit(0);
}

int startEventLoop(struct committer* c)
{
    int ret = init_getevent();
    if (ret != 0) {
        ALOGD("%s init_getevent() failed !", __FUNCTION__);
        return ret;
    }

    c->exit_ = false;
    ret = pthread_create(&c->thread_, NULL, eventLoop, c);
    if (ret) {
        ALOGD("Could not create thread %d", ret);
        return ret;
    }

    return 0;
}

void stopEventLoop(struct committer* c) {
    c->exit_ = true;
}

void onAppCrash(struct committer* c) {
    if(ioctl(c->ebc_fd, EBC_CRASH,&c->ebc_buf_info)!=0) {
        ALOGD("%s failed!\n", __FUNCTION__);
    } else {
        ALOGD("%s EBC_CRASH\n", __FUNCTION__);
    }
}

void onAppEnter(struct committer* c) {
    if(ioctl(c->ebc_fd, EBC_ON_APP_ENTER,&c->ebc_buf_info)!=0) {
        ALOGD("%s failed!\n", __FUNCTION__);
    } else {
        ALOGD("%s EBC_ON_APP_ENTER!\n", __FUNCTION__);
    }
}

void onAppExit(struct committer* c) {
    if(ioctl(c->ebc_fd, EBC_ON_APP_EXIT,&c->ebc_buf_info)!=0) {
        ALOGD("%s failed!\n", __FUNCTION__);
    } else {
        ALOGD("%s EBC_ON_APP_EXIT\n", __FUNCTION__);
    }
}

struct committer* createCommitter() {
    struct committer* c = (struct committer*)malloc(sizeof(struct committer));

    c->init = init;
    c->set_overlay_enabled = set_overlay_enabled;
    c->get_overlay_status = get_overlay_status;
    c->drawBuf = drawBuf;

    c->exit_ = false;
    c->handwritingEnabled = false;
    c->setHandwritingEnabled = setHandwritingEnabled;
    c->startEventLoop = startEventLoop;
    c->stopEventLoop = stopEventLoop;
    c->clearOsd = clearOsd;
    c->refreshScreen = refreshScreen;
    c->onAppCrash = onAppCrash;
    c->onAppEnter = onAppEnter;
    c->onAppExit = onAppExit;

    resetEvent(c);
    return c;
}