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
