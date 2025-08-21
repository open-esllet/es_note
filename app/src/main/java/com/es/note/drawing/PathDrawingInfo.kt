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
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import com.es.note.vm.NoteVM
import com.es.note.jni.NativeAdapter
import com.es.note.utils.Config
import com.es.note.utils.OedColor
import kotlin.math.max
import kotlin.math.min

class PathDrawingInfo(bitmap: Bitmap, canvas: Canvas, paint: Paint) :
    DrawingInfo(bitmap, canvas, paint) {
    private val TAG = PathDrawingInfo::class.java.getSimpleName()

    private val path = Path()

    private var penType = 0
    private val penWidth = 5
    private val listPoint: MutableList<PointF> = ArrayList<PointF>()
    private val pathRect = RectF()

    fun updatePaint(penType: Int) {
        this.penType = penType

        if (penType == NoteVM.ERASER_SCOPE) {
            paint.setPathEffect(DashPathEffect(floatArrayOf(30f, 50f), 2f))
            paint.strokeWidth = penWidth.toFloat()
            paint.setColor(OedColor.COLOR_BLACK)
            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeJoin = Paint.Join.ROUND
        }
    }

    fun firstMoveTo(x: Int, y: Int) {
        path.moveTo(x.toFloat(), y.toFloat())
        listPoint.add(PointF(x.toFloat(), y.toFloat()))
    }

    fun quadTo(x: Int, y: Int) {
        if (listPoint.isEmpty()) {
            firstMoveTo(x, y)
            return
        }

        val last = listPoint[listPoint.size - 1]
        path.quadTo(last.x, last.y, x.toFloat(), y.toFloat())
        listPoint.add(PointF(x.toFloat(), y.toFloat()))
    }

    fun closePath() {
        if (listPoint.isEmpty()) return
        val last = listPoint[listPoint.size - 1]
        val first = listPoint[0]
        path.quadTo(last.x, last.y, first.x, first.y)
        listPoint.add(PointF(first.x, first.y))
    }

    override fun draw(commit: Boolean) {
        if (listPoint.size < 2) return

        c.drawPath(path, paint)

        val r = pathRect
        val last = listPoint[listPoint.size - 2]
        val cur = listPoint[listPoint.size - 1]
        r.left = if (last.x < cur.x) last.x - penWidth else cur.x - penWidth
        r.right = if (last.x > cur.x) last.x + penWidth else cur.x + penWidth
        r.top = if (last.y < cur.y) last.y - penWidth else cur.y - penWidth
        r.bottom = if (last.y > cur.y) last.y + penWidth else cur.y + penWidth

        r.left = max(r.left.toDouble(), 0.0).toFloat()
        r.right = min(r.right.toDouble(), Config.getWidth().toDouble()).toFloat()
        r.top = max(r.top.toDouble(), 0.0).toFloat()
        r.bottom = min(r.bottom.toDouble(), Config.getHeight().toDouble()).toFloat()

        NativeAdapter.getInstance().commitBitmap(
            b, r.left.toInt(), r.top.toInt(),
            r.right.toInt(), r.bottom.toInt()
        )
    }

    override fun drawAll(commit: Boolean) {
        if (penType == NoteVM.ERASER_SCOPE) {
            val r = pathRect
            path.computeBounds(r, true)

            r.left = max((r.left - penWidth).toDouble(), 0.0).toFloat()
            r.right = min((r.right + penWidth).toDouble(), Config.getWidth().toDouble()).toFloat()
            r.top = max((r.top - penWidth).toDouble(), 0.0).toFloat()
            r.bottom = min((r.bottom + penWidth).toDouble(), Config.getHeight().toDouble()).toFloat()

            paint.setColor(OedColor.COLOR_WHITE)
            paint.style = Paint.Style.FILL_AND_STROKE
            c.drawRect(r, paint)
            NativeAdapter.getInstance().commitBitmap(
                b, r.left.toInt(), r.top.toInt(),
                r.right.toInt(), r.bottom.toInt()
            )
        }
    }
}