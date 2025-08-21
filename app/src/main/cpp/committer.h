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

#ifndef COMMITER_H
#define COMMITER_H

#include "config.h"
#include "util.h"
#include "get_event.h"
#include "libuv2.h"
#include <stdbool.h>
#include <pthread.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <sys/mman.h>
#include <linux/input.h>
#include <fcntl.h>
#include <string.h>
#include <errno.h>
#include <pthread.h>

#define HAL_PRIORITY_URGENT_DISPLAY     (-8)

#define EINK_FB_SIZE		0x400000 /* 4M */

/*
* ebc buf format
*/
#define EBC_Y4 (0)
#define EBC_Y8 (1)


/*
 * ebc system ioctl command
 */
#define EBC_GET_BUFFER			(0x7000)
#define EBC_SEND_BUFFER			(0x7001)
#define EBC_GET_BUFFER_INFO		(0x7002)
#define EBC_SET_FULL_MODE_NUM	(0x7003)
#define EBC_ENABLE_OVERLAY		(0x7004)
#define EBC_DISABLE_OVERLAY		(0x7005)
#define EBC_GET_OSD_BUFFER		(0x7006)
#define EBC_SEND_OSD_BUFFER		(0x7007)
#define EBC_NEW_BUF_PREPARE		(0x7008)
#define EBC_SET_DIFF_PERCENT		(0x7009)
#define EBC_WAIT_NEW_BUF_TIME	(0x700a)
#define EBC_GET_OVERLAY_STATUS	(0x700b)
#define EBC_ENABLE_BG_CONTROL	(0x700c)
#define EBC_DISABLE_BG_CONTROL	(0x700d)
#define EBC_ENABLE_RESUME_COUNT	(0x700e)
#define EBC_DISABLE_RESUME_COUNT	(0x700f)
#define EBC_GET_BUF_FORMAT		(0x7010)
#define EBC_DROP_PREV_BUFFER		(0x7011)
#define EBC_SET_PANEL_INDEX		(0x7012)
#define EBC_SET_VCOM		(0x7013)
#define EBC_REFRESH_SCREEN		(0x7014)
#define EBC_ENABLE_VIDEO_MODE		(0x7015)
#define EBC_DISABLE_VIDEO_MODE		(0x7016)
#define EBC_OSD_AS_BACKGROUND (0x7019)
#define EBC_OSD_AS_FOREGROUND (0x700A)
#define EBC_CRASH (0x701B)
#define EBC_ON_APP_ENTER (0x701C)
#define EBC_ON_APP_EXIT (0x701D)

/*q
 * IMPORTANT: Those values is corresponding to android hardware program,
 * so *FORBID* to changes bellow values, unless you know what you're doing.
 * And if you want to add new refresh modes, please appended to the tail.
 */
enum panel_refresh_mode {
    EPD_AUTO		= 0,
    EPD_OVERLAY		= 1,
    EPD_FULL_GC16		= 2,
    EPD_FULL_GL16		= 3,
    EPD_FULL_GLR16		= 4,
    EPD_FULL_GLD16		= 5,
    EPD_FULL_GCC16		= 6,
    EPD_PART_GC16		= 7,
    EPD_PART_GL16		= 8,
    EPD_PART_GLR16		= 9,
    EPD_PART_GLD16		= 10,
    EPD_PART_GCC16		= 11,
    EPD_A2			= 12,
    EPD_DU			= 13,
    EPD_DU4			= 14,
    EPD_A2_ENTER	= 15,
    EPD_RESET		= 16,
};

/*
 * IMPORTANT: android hardware use struct, so *FORBID* to changes this, unless you know what you're doing.
 */
#ifndef CONF_EPD_DRIVER
struct ebc_buf_info_t {
    int offset;
    int epd_mode;
    int height;
    int width;
    int panel_color;
    int win_x1;
    int win_y1;
    int win_x2;
    int win_y2;
    int width_mm;
    int height_mm;
    int needpic;
    char tid_name[16];
};
#else
struct ebc_buf_info_t {
    int offset;
    int epd_mode;
    int height;
    int width;
    int panel_color;
    int rgba_format;
    int win_x1;
    int win_y1;
    int win_x2;
    int win_y2;
    int width_mm;
    int height_mm;
    int mmap_size;
    int flipv;
    int fliph;
    int panel_index;
    int needpic;
    char tid_name[16];
    int feature_width;
    int feature_height;
    int feature_diff_pix_count;
    int rgby_feature[4];
    int gray_totals[16];
};

#endif

struct Rect {
    int left;
    int top;
    int right;
    int bottom;
};

struct committer {
    bool downPressed;
    int eventX;
    int eventY;
    int pressure;
    pthread_t thread_;
    bool exit_;
    bool handwritingEnabled;

    int ebc_fd;
    int ebc_buf_format;
    void *ebc_buffer_base;
    void *overlayBuffer;
    int *gray16_buffer;

    struct ebc_buf_info_t buf_info;
    struct ebc_buf_info_t ebc_buf_info;
    struct Rect win_info;
    struct Rect win_info_restricted;

    int (*init)(struct committer* c, int* o_width, int* o_height, int* o_panel_color);
    int (*set_overlay_enabled)(struct committer* c, bool enabled);
    int (*get_overlay_status)(struct committer* c);
    void (*drawBuf)(struct committer* c, void* buffer, float left, float top, float right, float bottom);
    void (*setHandwritingEnabled)(struct committer*, bool enabled);
    int (*startEventLoop)(struct committer* c);
    void (*stopEventLoop)(struct committer* c);
    void (*clearOsd)(struct committer* c, bool clearContent, bool sendBuff);
    void (*refreshScreen)(struct committer* c);
    void (*onAppCrash)(struct committer* c);
    void (*onAppEnter)(struct committer* c);
    void (*onAppExit)(struct committer* c);
};

struct committer* createCommitter();

#endif