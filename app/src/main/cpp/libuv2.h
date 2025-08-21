/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2025, Hancunchou@OED
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

#ifndef LIBUV32_H
#define LIBUV32_H
#include <stdio.h>
#include <stdlib.h>


#if defined(__cplusplus)
extern "C" {
#endif

struct UVHead{
    uint8_t r;
    uint8_t g;
    uint8_t y;
    uint8_t b;
};


int init_uv_head(void);
int Rgba8888ToGrayColorWindow(char *dst_buf,char *src_buf,int rgba_buff_left,int rgba_buff_top,int rgba_buff_right,int rgba_buff_bottom,int buff_width);
int Rgba8888ToGrayRgba8888Window(char *dst_buf,char *src_buf,int rgba_buff_left,int rgba_buff_top,int rgba_buff_right,int rgba_buff_bottom,int buff_width);

#if defined(__cplusplus)
}
#endif
#endif /* LIBUV_H */