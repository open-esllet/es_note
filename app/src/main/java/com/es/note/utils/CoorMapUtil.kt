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