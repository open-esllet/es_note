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

package com.es.note.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import androidx.core.graphics.get
import androidx.core.graphics.set

object CoorMapUtil {
    private val TAG = CoorMapUtil::class.java.simpleName

    fun android2Osd(aBitmap: Bitmap, aRect: Rect, oBitmap: Bitmap, copyWhite: Boolean = false) {
        var pixel = 0

        for (y in aRect.top until aRect.bottom) {
            for (x in aRect.left until aRect.right) {
                pixel = aBitmap[x, y]

                if (copyWhite || pixel != Color.WHITE) {
                    oBitmap[Config.getWidth() - 1 - y, Config.getHeight() - 1 - x] = pixel
                }
            }
        }
    }

    fun android2Osd(aRect: RectF, oRect: RectF) {
        oRect.left = Config.getHeight(true) - aRect.bottom
        oRect.top = Config.getWidth(true) - aRect.right
        oRect.right = Config.getHeight(true) - aRect.top
        oRect.bottom = Config.getWidth(true) - aRect.left
    }

    fun tpOffset2Android(tp: Offset): Offset {
        return Offset(Config.getHeight() - tp.y, Config.getWidth() - tp.x)
    }
}