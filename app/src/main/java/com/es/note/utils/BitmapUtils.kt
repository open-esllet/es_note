package com.es.note.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

object BitmapUtils {
    private val TAG = BitmapUtils.javaClass.simpleName

    fun saveBitmap(bitmap: Bitmap, toFile: File, quality: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            FileOutputStream(toFile).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, quality, it)
                LogUtil.d(TAG, "saveBitmap to ${toFile.path}")
            }
        }
    }

    fun pixel2Gray(pixel: Int): Int {
        if (pixel > 0xffa0a0a0) {
            return Color.WHITE
        } else {
            return Color.BLACK
        }
    }

    fun rgba2GrayBitmap(src: Bitmap, width: Int, height: Int): Bitmap {
        val dst = createBitmap(width, height, Bitmap.Config.ALPHA_8)

        for (i in 0 until height) {
            for (j in 0 until width) {
                val pixel = src.getPixel(j, i)
                dst[j, i] =
                    if (pixel2Gray(pixel) == Color.WHITE) OedColor.COLOR_TRANSPARENT else OedColor.COLOR_BLACK
            }
        }

        return dst
    }

    fun vectorDrawable2Bitmap(drawable: VectorDrawable) : Bitmap {
        val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun drawable2Bitmap(drawable: Drawable, width: Int, height: Int) : Bitmap {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }
}