package com.es.note.drawing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint

abstract class DrawingInfo(
    val b: Bitmap,
    val c: Canvas,
    val paint: Paint
) {
    abstract fun draw(commit: Boolean = true)

    abstract fun drawAll(commit: Boolean = true)
}
