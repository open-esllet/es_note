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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.es.note.jni.NativeAdapter
import com.es.note.utils.Config
import com.es.note.widget.OsdPalette

class AxesDrawingInfo(palette: OsdPalette,
                      bitmap: Bitmap,
                      canvas: Canvas,
                      paint: Paint
) : DrawingInfo(bitmap, canvas, paint) {

    val START_X = 50f
    val START_Y = 50f
    val PEN_WIDTH = 5f
    val AXIS_LEN = 200
    val TXT_SIZE = 50f

    private fun updatePaint() {
        paint.setColor(Color.BLACK)
        paint.strokeWidth = PEN_WIDTH
        paint.textSize = TXT_SIZE
    }

    override fun draw(commit: Boolean) {
        updatePaint()
        c.drawLine(START_X, START_Y, START_X + AXIS_LEN, START_Y, paint)
        c.drawLine(START_X, START_Y, START_X, START_Y + AXIS_LEN, paint)
        c.drawText("x", START_X + AXIS_LEN + TXT_SIZE/2, START_Y, paint)
        c.drawText("y", START_X, START_Y + AXIS_LEN + TXT_SIZE/2, paint)

        NativeAdapter.getInstance().commitBitmap(b, 0, 0, Config.getWidth(), Config.getHeight())
    }

    override fun drawAll(show: Boolean) {
    }
}