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
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.es.note.vm.NoteVM
import com.es.note.jni.NativeAdapter
import com.es.note.utils.Config
import java.util.Timer
import java.util.TimerTask
import kotlin.math.max
import kotlin.math.min

class StrokeDrawingInfo(
    bitmap: Bitmap,
    canvas: Canvas,
    paint: Paint,
    private val mPencilShader: BitmapShader?
) : DrawingInfo(bitmap, canvas, paint) {
    private val TAG: String = StrokeDrawingInfo::class.java.getSimpleName()

    private val list: MutableList<PointInfo> = ArrayList<PointInfo>()
    private val strokeRect = Rect()

    private var drawIndex = 0
    private var timer: Timer? = null
    private var task: TimerTask? = null

    init {
        if (Config.PERIODIC_DRAW_POINTS) {
            timer = Timer()
            task = object : TimerTask() {
                override fun run() {
                    draw()
                }
            }
            timer!!.schedule(task, 5, 2)
        }
    }

    fun cancelTimer() {
        if (timer != null) timer!!.cancel()
    }

    fun clearPointInfoList() {
        list.clear()
    }

    fun addPointInfo(pointInfo: PointInfo?) {
        list.add(pointInfo!!)
    }

    private fun updatePaint(penType: Int, width: Float, color: Int) {
        paint.setPathEffect(null)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.setColor(color)
        paint.strokeWidth = width

        if (penType == NoteVM.PEN_TYPE_PEN) {
            paint.setShader(null)
        } else if (penType == NoteVM.PEN_TYPE_PENCIL) {
            paint.setShader(null)
        } else if (penType == NoteVM.PEN_TYPE_BRUSH) {
            paint.setShader(null)
        } else if (penType == NoteVM.ERASER_PATH) {
            paint.setShader(null)
        }
    }

    override fun draw(commit: Boolean) {
        var p1: PointInfo? = null
        var p2: PointInfo? = null

        if (!Config.PERIODIC_DRAW_POINTS) {
            if (list.size < 2) return
            if (!Config.POINT_TEST) {
                p1 = list[list.size - 2]
            }
            p2 = list[list.size - 1]
        } else {
            if (drawIndex + 2 > list.size - 1) return
            p1 = list[drawIndex++]
            p2 = list[drawIndex + 1]
        }

        val penWidth = p2.penWidth

        updatePaint(p2.penType, penWidth, p2.penColor)

        if (!Config.POINT_TEST) {
            c.drawLine(p1!!.x.toFloat(), p1.y.toFloat(), p2.x.toFloat(), p2.y.toFloat(), paint)
        } else {
            c.drawPoint(p2.x.toFloat(), p2.y.toFloat(), paint)
        }

        val r = strokeRect
        if (!Config.POINT_TEST) {
            r.left = (min((p1!!.x - penWidth).toDouble(), (p2.x - penWidth).toDouble())).toInt()
            r.right = (max((p1.x + penWidth).toDouble(), (p2.x + penWidth).toDouble())).toInt()
            r.top = (min((p1.y - penWidth).toDouble(), (p2.y - penWidth).toDouble())).toInt()
            r.bottom = (max((p1.y + penWidth).toDouble(), (p2.y + penWidth).toDouble())).toInt()
        } else {
            r.left = (p2.x - penWidth).toInt()
            r.right = (p2.x + penWidth).toInt()
            r.top = (p2.y - penWidth).toInt()
            r.bottom = (p2.y + penWidth).toInt()
        }

        r.left = max(r.left.toDouble(), 0.0).toInt()
        r.right = min(r.right.toDouble(), Config.getWidth().toDouble()).toInt()
        r.top = max(r.top.toDouble(), 0.0).toInt()
        r.bottom = min(r.bottom.toDouble(), Config.getHeight().toDouble()).toInt()
        NativeAdapter.getInstance().commitBitmap(b, r.left, r.top, r.right, r.bottom)
    }

    override fun drawAll(show: Boolean) {
        var minX = 0
        var minY = 0
        var maxX = 0
        var maxY = 0
        var penWidth = 0f

        for (i in list.indices) {
            val p1 = list[i]
            minX = min(p1.x.toDouble(), minX.toDouble()).toInt()
            minY = min(p1.y.toDouble(), minY.toDouble()).toInt()
            maxX = max(p1.x.toDouble(), maxX.toDouble()).toInt()
            maxY = max(p1.y.toDouble(), maxY.toDouble()).toInt()

            if (i + 1 == list.size) break

            val p2 = list[i + 1]
            minX = min(p2.x.toDouble(), minX.toDouble()).toInt()
            minY = min(p2.y.toDouble(), minY.toDouble()).toInt()
            maxX = max(p2.x.toDouble(), maxX.toDouble()).toInt()
            maxY = max(p2.y.toDouble(), maxY.toDouble()).toInt()

            penWidth = p2.penWidth
            updatePaint(p2.penType, penWidth, p2.penColor)
            c.drawLine(p1.x.toFloat(), p1.y.toFloat(), p2.x.toFloat(), p2.y.toFloat(), paint)
        }

        if (show) {
            val r = strokeRect
            r.left = max((minX - penWidth).toDouble(), 0.0).toInt()
            r.right = min((maxX + penWidth).toDouble(), Config.getWidth().toDouble()).toInt()
            r.top = max((minY - penWidth).toDouble(), 0.0).toInt()
            r.bottom = min(maxY.toDouble(), Config.getHeight().toDouble()).toInt()
            NativeAdapter.getInstance().commitBitmap(b, r.left, r.top, r.right, r.bottom)
        }
    }
}