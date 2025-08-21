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

package com.es.note.utils;

import android.graphics.Color;

import com.es.note.vm.NoteVM;

public class OedColor {
    public static final int COLOR_TRANSPARENT = Config.OED_DRIVER ? Color.TRANSPARENT : 0xff000000;
    public static final int COLOR_BLACK = Config.OED_DRIVER ? Color.BLACK : 0x0;
    public static final int COLOR_RED  = Config.OED_DRIVER ? Color.RED : 0x0;
    public static final int COLOR_GREEN = Config.OED_DRIVER ? Color.GREEN : 0x0;
    public static final int COLOR_BLUE = Config.OED_DRIVER ? Color.BLUE : 0x0;
    public static final int COLOR_YELLOW = Config.OED_DRIVER ? Color.BLACK : 0x0;
    public static final int COLOR_PINK = Config.OED_DRIVER ? Color.BLACK : 0x0;
    public static final int COLOR_INDIGO = Config.OED_DRIVER ? Color.BLACK : 0x0;
    public static final int COLOR_PURPLE = Config.OED_DRIVER ? Color.BLACK : 0x0;
    public static final int COLOR_WHITE = Config.OED_DRIVER ? Color.WHITE : 0xff000000;

    public static int penColor2CanvasColor(final int color) {
        if (color == NoteVM.PEN_COLOR_TRANSPARENT) {
            return COLOR_TRANSPARENT;
        } else if (color == NoteVM.PEN_COLOR_BLACK) {
            return COLOR_BLACK;
        } else if (color == NoteVM.PEN_COLOR_RED) {
            return COLOR_RED;
        } else if (color == NoteVM.PEN_COLOR_GREEN) {
            return COLOR_GREEN;
        } else if (color == NoteVM.PEN_COLOR_BLUE) {
            return COLOR_BLUE;
        } else if (color == NoteVM.PEN_COLOR_YELLOW) {
            return COLOR_YELLOW;
        } else if (color == NoteVM.PEN_COLOR_PINK) {
            return COLOR_PINK;
        } else if (color == NoteVM.PEN_COLOR_INDIGO) {
            return COLOR_INDIGO;
        } else if (color == NoteVM.PEN_COLOR_PURPLE) {
            return COLOR_PURPLE;
        } else if (color == NoteVM.PEN_COLOR_WHITE) {
            return COLOR_WHITE;
        }

        return COLOR_TRANSPARENT;
    }
}
