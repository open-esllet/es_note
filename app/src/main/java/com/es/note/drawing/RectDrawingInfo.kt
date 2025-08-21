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

package com.es.note.drawing

import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.es.note.jni.NativeAdapter
import com.es.note.utils.Config

class RectDrawingInfo(bitmap: Bitmap, canvas: Canvas, paint: Paint) :
    DrawingInfo(bitmap, canvas, paint) {
    private val TAG = this::class.java.simpleName
    private var excludeToolbar = false

    fun setExcludeToolbar(excludeToolbar: Boolean) {
        this.excludeToolbar = excludeToolbar
    }

    fun clearDrawingArea(commit: Boolean) {
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.setColor(Color.WHITE)
        if (Config.TP_SIS) {
            c.drawRect(
                0f,
                0f,
                (Config.getWidth() - Config.TOOL_BAR_HEIGHT*2).toFloat(),
                Config.getHeight().toFloat(),
                paint
            )
            if (commit) {
                NativeAdapter.getInstance()
                    .commitBitmap(b, 0, 0, Config.getWidth() - Config.TOOL_BAR_HEIGHT*2, Config.getHeight())
            }
        } else {
            c.drawRect(
                Config.TOOL_BAR_HEIGHT.toFloat(),
                0f,
                Config.getWidth().toFloat(),
                Config.getHeight().toFloat(),
                paint
            )
            if (commit) {
                NativeAdapter.getInstance()
                    .commitBitmap(b, Config.TOOL_BAR_HEIGHT, 0, Config.getWidth(), Config.getHeight())
            }
        }
    }

    fun updateBitmap(left: Int, top: Int, right: Int, bottom: Int) {
        NativeAdapter.getInstance().commitBitmap(b, left, top, right, bottom)
    }

    override fun draw(commit: Boolean) {
        if (!excludeToolbar) {
            c.drawColor(Color.WHITE, BlendMode.SRC)
            NativeAdapter.getInstance().commitBitmap(b, 0, 0, Config.getWidth(), Config.getHeight())
        } else {
            clearDrawingArea(commit)
        }
    }

    override fun drawAll(commit: Boolean) {
        draw(commit)
    }
}
