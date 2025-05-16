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
