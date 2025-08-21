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
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import com.es.note.jni.NativeAdapter
import com.es.note.utils.Config

class BitmapDrawingInfo(bitmap: Bitmap, canvas: Canvas, paint: Paint) :
    DrawingInfo(bitmap, canvas, paint) {
    private val TAG: String = BitmapDrawingInfo::class.java.getSimpleName()

    private var picture: Bitmap? = null
    private var startX = 0
    private var startY = 0
    fun setBitmap(b: Bitmap, x: Int, y: Int) {
        this.picture = b
        this.startX = x
        this.startY = y
    }

    override fun draw(commit: Boolean) {
        if (!Config.OED_DRIVER) {
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
        }
        c.drawBitmap(picture!!, startX.toFloat(), startY.toFloat(), paint)
        NativeAdapter.getInstance()
            .commitBitmap(b, startX, startY, picture!!.getWidth(), picture!!.getHeight())
    }

    override fun drawAll(commit: Boolean) {
        c.drawBitmap(picture!!, startX.toFloat(), startY.toFloat(), paint)
        if (commit) {
            NativeAdapter.getInstance()
                .commitBitmap(b, startX, startY, picture!!.getWidth(), picture!!.getHeight())
        }
    }
}
