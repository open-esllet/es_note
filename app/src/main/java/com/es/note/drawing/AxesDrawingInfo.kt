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