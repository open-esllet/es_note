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
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.toRect
import com.es.note.R
import com.es.note.vm.NoteVM
import com.es.note.jni.NativeAdapter
import com.es.note.utils.Config
import com.es.note.utils.CoorMapUtil
import com.es.note.widget.OsdPalette
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ToolbarDrawingInfo(
    private val palette: OsdPalette,
    bitmap: Bitmap,
    canvas: Canvas,
    paint: Paint
) : DrawingInfo(bitmap, canvas, paint) {
    private val TAG = ToolbarDrawingInfo::class.java.simpleName

    companion object {
        @JvmField
        val TOOL_TYPE_NONE = 0
        @JvmField
        val TOOL_TYPE_PEN = 1
        @JvmField
        val TOOL_TYPE_ERASER = 2
        @JvmField
        val TOOL_TYPE_UNDO = 3
        @JvmField
        val TOOL_TYPE_REDO = 4
        @JvmField
        val TOOL_TYPE_REFRESH = 5
        @JvmField
        val TOOL_TYPE_PAGE_PREV = 6
        @JvmField
        val TOOL_TYPE_PAGE_IND = 7
        @JvmField
        val TOOL_TYPE_PAGE_NEXT = 8
        @JvmField
        val TOOL_TYPE_PAGE_DEL = 9
        @JvmField
        val TOOL_TYPE_SAVE_EXIT = 10

        @JvmField
        val SUBTOOL_TYPE_PENCEIL = 11
        @JvmField
        val SUBTOOL_TYPE_PEN = 12
        @JvmField
        val SUBTOOL_TYPE_BRUSH = 13
        @JvmField
        val SUBTOOL_SMALL_POINT = 14
        @JvmField
        val SUBTOOL_MEDIUM_POINT = 15
        @JvmField
        val SUBTOOL_LARGE_POINT = 16

        @JvmField
        val SUBTOOL_TYPE_BLACK = 21
        @JvmField
        val SUBTOOL_TYPE_WHITE = 22
        @JvmField
        val SUBTOOL_TYPE_RED = 23
        @JvmField
        val SUBTOOL_TYPE_BLUE = 24
        @JvmField
        val SUBTOOL_TYPE_GREEN = 25
    }

    private val DEBUG = false
    private val VIEW_WIDTH = bitmap.height
    private val VIEW_HEIGHT = bitmap.width
    private val WIDTH_OF_TOOL: Float
    private val WIDTH_OF_SUBTOOL: Float
    private val HEIGHT_OF_TOOL: Float = Config.TOOL_BAR_HEIGHT.toFloat()

    private val aBitmap = createBitmap(VIEW_WIDTH, VIEW_HEIGHT)
    private val aCanvas = Canvas(aBitmap)

    private val penRect: ToolRect
    private val eraserRect: ToolRect
    private val undoRect: ToolRect
    private val redoRect: ToolRect
    private val refreshRect: ToolRect
    private val pagePrevRect: ToolRect
    private val pageIndRect: ToolRect
    private val pageNextRect: ToolRect
    private val pageDelRect: ToolRect
    private val saveExitRect: ToolRect

    private val penceilSubRect: SubToolRect
    private val penSubRect: SubToolRect
    private val brushSubRect: SubToolRect
    private val smallPointSubRect: SubToolRect
    private val mediumPointSubRect: SubToolRect
    private val largePointSubRect: SubToolRect
    private val blackSubRect: SubToolRect
    private val redSubRect: SubToolRect
    private val blueSubRect: SubToolRect
    private val greenSubRect: SubToolRect


    private var lastClickedType: Int = TOOL_TYPE_PEN

    private var pageIndication: String = "1/1"

    private lateinit var vm: NoteVM

    init {
        WIDTH_OF_TOOL = VIEW_WIDTH.toFloat() / 10
        WIDTH_OF_SUBTOOL = VIEW_WIDTH.toFloat() / 10

        var i = 1
        penRect = ToolRect(TOOL_TYPE_PEN,          WIDTH_OF_TOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_TOOL/2, HEIGHT_OF_TOOL/2f))
        eraserRect = ToolRect(TOOL_TYPE_ERASER,    WIDTH_OF_TOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_TOOL*i++   + WIDTH_OF_TOOL/2, HEIGHT_OF_TOOL/2))
        undoRect = ToolRect(TOOL_TYPE_UNDO,        WIDTH_OF_TOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_TOOL*i++ + WIDTH_OF_TOOL/2, HEIGHT_OF_TOOL/2))
        redoRect = ToolRect(TOOL_TYPE_REDO,        WIDTH_OF_TOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_TOOL*i++ + WIDTH_OF_TOOL/2, HEIGHT_OF_TOOL/2))
        refreshRect = ToolRect(TOOL_TYPE_REFRESH,    WIDTH_OF_TOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_TOOL*i++ + WIDTH_OF_TOOL/2, HEIGHT_OF_TOOL/2))
        pagePrevRect = ToolRect(TOOL_TYPE_PAGE_PREV, WIDTH_OF_TOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_TOOL*i++ + WIDTH_OF_TOOL/2, HEIGHT_OF_TOOL/2))
        pageIndRect = ToolRect(TOOL_TYPE_PAGE_IND, WIDTH_OF_TOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_TOOL*i++ + WIDTH_OF_TOOL/2, HEIGHT_OF_TOOL/2))
        pageNextRect = ToolRect(TOOL_TYPE_PAGE_NEXT, WIDTH_OF_TOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_TOOL*i++ + WIDTH_OF_TOOL/2, HEIGHT_OF_TOOL/2))
        pageDelRect = ToolRect(TOOL_TYPE_PAGE_DEL, WIDTH_OF_TOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_TOOL*i++ + WIDTH_OF_TOOL/2, HEIGHT_OF_TOOL/2))
        saveExitRect = ToolRect(TOOL_TYPE_SAVE_EXIT, WIDTH_OF_TOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_TOOL*i++ + WIDTH_OF_TOOL/2, HEIGHT_OF_TOOL/2))

        i = 1
        penceilSubRect = SubToolRect(SUBTOOL_TYPE_PENCEIL, WIDTH_OF_SUBTOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_SUBTOOL/2, HEIGHT_OF_TOOL/2 + HEIGHT_OF_TOOL))
        penSubRect = SubToolRect(SUBTOOL_TYPE_PEN, WIDTH_OF_SUBTOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_SUBTOOL*i++ + WIDTH_OF_SUBTOOL/2, HEIGHT_OF_TOOL/2 + HEIGHT_OF_TOOL))
        brushSubRect = SubToolRect(SUBTOOL_TYPE_BRUSH, WIDTH_OF_SUBTOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_SUBTOOL*i++ + WIDTH_OF_SUBTOOL/2, HEIGHT_OF_TOOL/2 + HEIGHT_OF_TOOL))
        smallPointSubRect = SubToolRect(SUBTOOL_SMALL_POINT, WIDTH_OF_SUBTOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_SUBTOOL*i++ + WIDTH_OF_SUBTOOL/2, HEIGHT_OF_TOOL/2 + HEIGHT_OF_TOOL))
        mediumPointSubRect = SubToolRect(SUBTOOL_MEDIUM_POINT, WIDTH_OF_SUBTOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_SUBTOOL*i++ + WIDTH_OF_SUBTOOL/2, HEIGHT_OF_TOOL/2 + HEIGHT_OF_TOOL))
        largePointSubRect = SubToolRect(SUBTOOL_LARGE_POINT, WIDTH_OF_SUBTOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_SUBTOOL*i++ + WIDTH_OF_SUBTOOL/2, HEIGHT_OF_TOOL/2 + HEIGHT_OF_TOOL))
        blackSubRect = SubToolRect(SUBTOOL_TYPE_BLACK, WIDTH_OF_SUBTOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_SUBTOOL*i++ + WIDTH_OF_SUBTOOL/2, HEIGHT_OF_TOOL/2 + HEIGHT_OF_TOOL))
        redSubRect = SubToolRect(SUBTOOL_TYPE_RED, WIDTH_OF_SUBTOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_SUBTOOL*i++ + WIDTH_OF_SUBTOOL/2, HEIGHT_OF_TOOL/2 + HEIGHT_OF_TOOL))
        blueSubRect = SubToolRect(SUBTOOL_TYPE_BLUE, WIDTH_OF_SUBTOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_SUBTOOL*i++ + WIDTH_OF_SUBTOOL/2, HEIGHT_OF_TOOL/2 + HEIGHT_OF_TOOL))
        greenSubRect = SubToolRect(SUBTOOL_TYPE_GREEN, WIDTH_OF_SUBTOOL, HEIGHT_OF_TOOL,
            PointF(WIDTH_OF_SUBTOOL*i++ + WIDTH_OF_SUBTOOL/2, HEIGHT_OF_TOOL/2 + HEIGHT_OF_TOOL))
    }

    fun init(vm: NoteVM) {
        this.vm = vm
        drawTools()
    }

    private fun drawTools() {
        updateCheckedState(true, false)

        paint.strokeWidth = 1f
        paint.color = Color.BLACK
        aCanvas.drawLine(0f, Config.TOOL_BAR_HEIGHT - paint.strokeWidth,
            VIEW_WIDTH.toFloat(), Config.TOOL_BAR_HEIGHT - paint.strokeWidth, paint)
        aCanvas.drawLine(0f, Config.TOOL_BAR_HEIGHT * 2 - paint.strokeWidth,
            VIEW_WIDTH.toFloat(), Config.TOOL_BAR_HEIGHT * 2 - paint.strokeWidth, paint)
    }

    fun updateCheckedState(forceUpdate: Boolean = false, commit: Boolean) {
        penRect.updateState(vm.mode == NoteVM.MODE_PEN, forceUpdate, commit)
        eraserRect.updateState(vm.mode == NoteVM.MODE_ERASER,  forceUpdate, commit)
        undoRect.updateState(false,  forceUpdate, commit)
        redoRect.updateState(false,  forceUpdate, commit)
        refreshRect.updateState(false,  forceUpdate, commit)
        pagePrevRect.updateState(false,  forceUpdate, commit)
        pageIndRect.updateState(false,  forceUpdate, commit)
        pageNextRect.updateState(false,  forceUpdate, commit)
        pageDelRect.updateState(false,  forceUpdate, commit)
        saveExitRect.updateState(false,  forceUpdate, commit)

        penceilSubRect.updateState(vm.penType == NoteVM.PEN_TYPE_PENCIL,  forceUpdate, commit)
        penSubRect.updateState(vm.penType == NoteVM.PEN_TYPE_PEN,  forceUpdate, commit)
        brushSubRect.updateState(vm.penType == NoteVM.PEN_TYPE_BRUSH,  forceUpdate, commit)
        smallPointSubRect.updateState(vm.penWidthLevel == NoteVM.MIN_PEN_WIDTH_LEVEL,  forceUpdate, commit)
        mediumPointSubRect.updateState(vm.penWidthLevel == NoteVM.MEDIUM_PEN_WIDTH_LEVEL,  forceUpdate, commit)
        largePointSubRect.updateState(vm.penWidthLevel == NoteVM.MAX_PEN_WIDTH_LEVEL,  forceUpdate, commit)
        if (Config.EPD_COLOR == 3) {
            blackSubRect.updateState(vm.penColor == NoteVM.PEN_COLOR_BLACK,  forceUpdate, commit)
            redSubRect.updateState(vm.penColor == NoteVM.PEN_COLOR_RED,  forceUpdate, commit)
            blueSubRect.updateState(vm.penColor == NoteVM.PEN_COLOR_BLUE,  forceUpdate, commit)
            greenSubRect.updateState(vm.penColor == NoteVM.PEN_COLOR_GREEN,  forceUpdate, commit)
        }
    }

    fun setPageIndication(pageNo:Int, pageTotal:Int, commit: Boolean = true) {
        pageIndication = "$pageNo/$pageTotal"
        pageIndRect.draw(commit)
    }

    fun handleClick(x:Int, y:Int, listener: (toolType: Int) -> Unit) {
        if (y >= Config.TOOL_BAR_HEIGHT * 2) return

        var i = 1;
        if (y <= Config.TOOL_BAR_HEIGHT) {
            if (x < WIDTH_OF_TOOL * TOOL_TYPE_PEN) {
                if (lastClickedType != TOOL_TYPE_PEN) {
                    lastClickedType = TOOL_TYPE_PEN
                    penRect.performClick(listener)
                }
            } else if (x < WIDTH_OF_TOOL * TOOL_TYPE_ERASER) {
                if (lastClickedType != TOOL_TYPE_ERASER) {
                    lastClickedType = TOOL_TYPE_ERASER
                    eraserRect.performClick(listener)
                }
            } else if (x < WIDTH_OF_TOOL * TOOL_TYPE_UNDO) {
                if (lastClickedType != TOOL_TYPE_UNDO) {
                    lastClickedType = TOOL_TYPE_UNDO
                    undoRect.performClick(listener)
                }
            } else if (x < WIDTH_OF_TOOL * TOOL_TYPE_REDO) {
                if (lastClickedType != TOOL_TYPE_REDO) {
                    lastClickedType = TOOL_TYPE_REDO
                    redoRect.performClick(listener)
                }
            } else if (x < WIDTH_OF_TOOL * TOOL_TYPE_REFRESH) {
                if (lastClickedType != TOOL_TYPE_REFRESH) {
                    lastClickedType = TOOL_TYPE_REFRESH
                    refreshRect.performClick(listener)
                }
            } else if (x < WIDTH_OF_TOOL * TOOL_TYPE_PAGE_PREV) {
                pagePrevRect.performClick(listener)
            } else if (x < WIDTH_OF_TOOL * TOOL_TYPE_PAGE_IND) {
                pageIndRect.performClick(listener)
            } else if (x < WIDTH_OF_TOOL * TOOL_TYPE_PAGE_NEXT) {
                pageNextRect.performClick(listener)
            } else if (x < WIDTH_OF_TOOL * TOOL_TYPE_PAGE_DEL) {
                pageDelRect.performClick(listener)
            } else if (x < WIDTH_OF_TOOL * TOOL_TYPE_SAVE_EXIT) {
                if (lastClickedType != TOOL_TYPE_SAVE_EXIT) {
                    lastClickedType = TOOL_TYPE_SAVE_EXIT
                    saveExitRect.performClick(listener)
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(1000)
                        lastClickedType = TOOL_TYPE_NONE
                    }
                }
            }
        } else {
            if (x < WIDTH_OF_SUBTOOL * i++) {
                penceilSubRect.performClick(listener)
            } else if (x < WIDTH_OF_SUBTOOL * i++) {
                penSubRect.performClick(listener)
            } else if (x < WIDTH_OF_SUBTOOL * i++) {
                brushSubRect.performClick(listener)
            } else if (x < WIDTH_OF_SUBTOOL * i++) {
                smallPointSubRect.performClick(listener)
            } else if (x < WIDTH_OF_SUBTOOL * i++) {
                mediumPointSubRect.performClick(listener)
            } else if (x < WIDTH_OF_SUBTOOL * i++) {
                largePointSubRect.performClick(listener)
            } else if (x < WIDTH_OF_SUBTOOL * i++) {
                if (Config.EPD_COLOR == 3) blackSubRect.performClick(listener)
            } else if (x < WIDTH_OF_SUBTOOL * i++) {
                if (Config.EPD_COLOR == 3) redSubRect.performClick(listener)
            } else if (x < WIDTH_OF_SUBTOOL * i++) {
                if (Config.EPD_COLOR == 3) blueSubRect.performClick(listener)
            } else if (x < WIDTH_OF_SUBTOOL * i++) {
                if (Config.EPD_COLOR == 3) greenSubRect.performClick(listener)
            }
        }
    }

    override fun draw(commit: Boolean) {
        CoorMapUtil.android2Osd(aBitmap,
            android.graphics.Rect(0, 0, aBitmap.width, Config.TOOL_BAR_HEIGHT * 2),
            b, true)
        drawAll(commit)
    }

    override fun drawAll(commit: Boolean) {
        if (!commit) return

        if (!DEBUG) {
            if (Config.TP_SIS) {
                NativeAdapter.getInstance().commitBitmap(
                    b,
                    Config.getWidth() - Config.TOOL_BAR_HEIGHT*2,
                    0,
                    Config.getWidth(),
                    Config.getHeight())
            } else {
                NativeAdapter.getInstance().commitBitmap(
                    b,
                    0,
                    0,
                    Config.TOOL_BAR_HEIGHT * 2,
                    Config.getHeight())
            }
        } else {
            NativeAdapter.getInstance().commitBitmap(b, 0, 0, Config.getWidth(), Config.getHeight())
        }
    }

    inner class SubToolRect(toolType: Int, width: Float, height: Float, centerPos: PointF) : ToolRect(toolType, width, height, centerPos) {
        override fun updateFields() {
            WIDTH_OF_DRAWABLE = 100f

            rectOfTool = RectF(centerPos.x - width/2,
                centerPos.y - height/2,
                centerPos.x + width/2,
                centerPos.y + height/2)

            when (toolType) {
                SUBTOOL_TYPE_PENCEIL -> {
                    normalDrawable = AppCompatResources.getDrawable(
                        palette.context,  R.drawable.ic_sub_pencil)
                }
                SUBTOOL_TYPE_PEN -> {
                        normalDrawable = AppCompatResources.getDrawable(
                            palette.context, R.drawable.ic_sub_pen)
                }
                SUBTOOL_TYPE_BRUSH -> {
                        normalDrawable = AppCompatResources.getDrawable(
                            palette.context, R.drawable.ic_sub_brush)
                }
                SUBTOOL_SMALL_POINT -> {
                        normalDrawable = AppCompatResources.getDrawable(
                            palette.context, R.drawable.ic_sub_small_point)
                }
                SUBTOOL_MEDIUM_POINT -> {
                        normalDrawable = AppCompatResources.getDrawable(
                            palette.context, R.drawable.ic_sub_medium_point)
                }
                SUBTOOL_LARGE_POINT -> {
                        normalDrawable = AppCompatResources.getDrawable(
                            palette.context, R.drawable.ic_sub_large_point)
                }
                SUBTOOL_TYPE_BLACK -> {
                    normalDrawable = AppCompatResources.getDrawable(
                        palette.context, R.drawable.ic_sub_pen_color_black
                    )
                }
                SUBTOOL_TYPE_WHITE -> {
                    normalDrawable = AppCompatResources.getDrawable(
                        palette.context, R.drawable.ic_sub_pen_color_withe)
                }
                SUBTOOL_TYPE_RED -> {
                    normalDrawable = AppCompatResources.getDrawable(
                        palette.context, R.drawable.ic_sub_pen_color_red)
                }
                SUBTOOL_TYPE_BLUE -> {
                    normalDrawable = AppCompatResources.getDrawable(
                        palette.context, R.drawable.ic_sub_pen_color_blue)
                }
                SUBTOOL_TYPE_GREEN -> {
                    normalDrawable = AppCompatResources.getDrawable(
                        palette.context, R.drawable.ic_sub_pen_color_green)
                }
            }
            checkedDrawable = normalDrawable

            RectF(centerPos.x - WIDTH_OF_DRAWABLE/2,
                centerPos.y - WIDTH_OF_DRAWABLE/2,
                centerPos.x + WIDTH_OF_DRAWABLE/2,
                centerPos.y + WIDTH_OF_DRAWABLE/2).also {
                normalDrawable!!.bounds = it.toRect()
                checkedDrawable!!.bounds = it.toRect()
            }
        }

        override fun draw(commit: Boolean) {
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.strokeWidth = 1f

            rectOfTool?.also {
                val rect = RectF(it)
                rect.bottom -= 3
                paint.color = Color.WHITE
                aCanvas.drawRect(rect, paint)
                normalDrawable!!.draw(aCanvas)

                if (checked) {
                    paint.color = Color.BLACK
                    if (Config.EPD_COLOR == 3) {
                        paint.strokeWidth = 1f
                    } else {
                        paint.strokeWidth = 2f
                    }
                    paint.style = Paint.Style.STROKE
                    if (Config.EPD_COLOR == 3) {
                        rect.inset(8f, 8f)
                    } else {
                        rect.inset(18f, 18f)
                    }
                    aCanvas.drawRect(rect, paint)
                }

                if (commit) {
                    val oRect = RectF()
                    CoorMapUtil.android2Osd(it, oRect)

                    CoorMapUtil.android2Osd(aBitmap, it.toRect(), b, true)
                    NativeAdapter.getInstance().commitBitmap(b,
                        oRect.left.toInt(), oRect.top.toInt(),
                        oRect.right.toInt(), oRect.bottom.toInt())
                }
            }
        }
    }

    open inner class ToolRect(val toolType: Int, val width: Float, val height: Float, val centerPos: PointF) {
        protected var WIDTH_OF_DRAWABLE = 70f
        protected var TEXT_SIZE = if (Config.EPD_COLOR == 3) 38 else 30
        protected var PADDING = 15f

        protected var rectOfDrawable: RectF? = null
        protected var rectOfTool: RectF? = null
        protected var normalDrawable: Drawable? = null
        protected var checkedDrawable: Drawable? = null
        public var checked = false

        init {
            updateFields()
        }

        open fun updateFields() {
            rectOfTool = RectF(centerPos.x - width/2,
                centerPos.y - height/2,
                centerPos.x + width/2,
                centerPos.y + height/2)

            when (toolType) {
                TOOL_TYPE_PEN -> {
                    normalDrawable = AppCompatResources.getDrawable(
                        palette.context,
                        R.drawable.ic_tool_pen_normal_new
                    )
                    checkedDrawable = AppCompatResources.getDrawable(
                        palette.context,
                        R.drawable.ic_tool_pen_checked_new
                    )
                }

                TOOL_TYPE_ERASER -> {
                    normalDrawable = AppCompatResources.getDrawable(
                        palette.context,
                        R.drawable.ic_tool_eraser_normal
                    )
                    checkedDrawable = AppCompatResources.getDrawable(
                        palette.context,
                        R.drawable.ic_tool_eraser_checked
                    )
                }

                TOOL_TYPE_UNDO -> {
                    normalDrawable = AppCompatResources.getDrawable(
                        palette.context,
                        R.drawable.ic_tool_undo_normal
                    )
                    checkedDrawable = AppCompatResources.getDrawable(
                        palette.context,
                        R.drawable.ic_tool_undo_checked
                    )
                }

                TOOL_TYPE_REDO -> {
                    normalDrawable = AppCompatResources.getDrawable(
                        palette.context,
                        R.drawable.ic_tool_redo_normal
                    )
                    checkedDrawable = AppCompatResources.getDrawable(
                        palette.context,
                        R.drawable.ic_tool_redo_checked
                    )
                }

                TOOL_TYPE_REFRESH -> {
                    normalDrawable = AppCompatResources.getDrawable(
                        palette.context,
                        R.drawable.ic_tool_refresh_screen_new
                    )
                    checkedDrawable = AppCompatResources.getDrawable(
                        palette.context,
                        R.drawable.ic_tool_refresh_screen_checked_new
                    )
                }

                TOOL_TYPE_PAGE_PREV -> {
                    normalDrawable = AppCompatResources.getDrawable(
                        palette.context,
                        android.R.drawable.ic_media_previous
                    )?.apply {
                        DrawableCompat.setTint(this, Color.BLACK)
                    }
                    checkedDrawable = AppCompatResources.getDrawable(
                        palette.context,
                        android.R.drawable.ic_media_previous
                    )?.apply {
                        DrawableCompat.setTint(this, Color.BLACK)
                    }
                }

                TOOL_TYPE_PAGE_IND -> {
                    normalDrawable = null
                    checkedDrawable = null
                }

                TOOL_TYPE_PAGE_NEXT -> {
                    normalDrawable = AppCompatResources.getDrawable(
                        palette.context,
                        android.R.drawable.ic_media_next
                    )?.apply {
                        DrawableCompat.setTint(this, Color.BLACK)
                    }
                    checkedDrawable = AppCompatResources.getDrawable(
                        palette.context,
                        android.R.drawable.ic_media_next
                    )?.apply {
                        DrawableCompat.setTint(this, Color.BLACK)
                    }
                }

                TOOL_TYPE_PAGE_DEL -> {
                    normalDrawable = AppCompatResources.getDrawable(
                        palette.context,
                        android.R.drawable.ic_menu_delete
                    )?.apply {
                        DrawableCompat.setTint(this, Color.BLACK)
                    }
                    checkedDrawable = AppCompatResources.getDrawable(
                        palette.context,
                        android.R.drawable.ic_menu_delete
                    )?.apply {
                        DrawableCompat.setTint(this, Color.BLACK)
                    }
                }

                TOOL_TYPE_SAVE_EXIT -> {
                    normalDrawable = AppCompatResources.getDrawable(
                        palette.context,
                        R.drawable.ic_tool_save_exit_normal_new
                    )
                    checkedDrawable = AppCompatResources.getDrawable(
                        palette.context,
                        R.drawable.ic_tool_save_exit_checked_new
                    )
                }
            }

            if (toolType != TOOL_TYPE_PAGE_IND)
                rectOfDrawable = RectF(centerPos.x - WIDTH_OF_DRAWABLE/2,
                    PADDING,
                    centerPos.x + WIDTH_OF_DRAWABLE/2,
                    PADDING + WIDTH_OF_DRAWABLE).also {
                    normalDrawable!!.bounds = it.toRect()
                    checkedDrawable!!.bounds = it.toRect()
            }
        }

        fun updateState(checked: Boolean, forceUpdate: Boolean = false, commit: Boolean = false) {
            if (this.checked != checked || forceUpdate) {
                this.checked = checked
                draw(commit)
            }
        }

        fun performClick(listener: (toolType:Int) -> Unit) {
            listener(toolType)
        }

        open fun draw(commit: Boolean = false) {
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.strokeWidth = 0.8f

            rectOfTool?.also {
                val rect = RectF(it)
                if (toolType != TOOL_TYPE_PAGE_IND) {
                    rect.bottom -= 1
                } else {
                    if (Config.EPD_COLOR == 3) {
                        rect.bottom -= 4
                    } else {
                        rect.bottom -= 5
                    }
                }
                paint.color = Color.WHITE
                aCanvas.drawRect(rect, paint)

                if (toolType != TOOL_TYPE_PAGE_IND) {
                    normalDrawable?.draw(aCanvas)

                    paint.textSize = TEXT_SIZE.toFloat()
                    paint.color = Color.BLACK
                    var text = ""
                    when (toolType) {
                        TOOL_TYPE_PEN ->
                            text = palette.context.getString(R.string.pen_type_pen)

                        TOOL_TYPE_ERASER ->
                            text = palette.context.getString(R.string.eraser_type_scope)

                        TOOL_TYPE_UNDO ->
                            text = palette.context.getString(R.string.tool_undo)

                        TOOL_TYPE_REDO ->
                            text = palette.context.getString(R.string.tool_redo)

                        TOOL_TYPE_REFRESH ->
                            text = palette.context.getString(R.string.tool_refresh_screen)

                        TOOL_TYPE_PAGE_PREV ->
                            text = palette.context.getString(R.string.tool_page_prev)

                        TOOL_TYPE_PAGE_NEXT ->
                            text = palette.context.getString(R.string.tool_page_next)

                        TOOL_TYPE_PAGE_DEL ->
                            text = palette.context.getString(R.string.tool_page_del)

                        TOOL_TYPE_SAVE_EXIT ->
                            text = palette.context.getString(R.string.tool_save_exit)
                    }
                    val textWidth = paint.measureText(text)
                    aCanvas.drawText(
                        text,
                        (rect.right - rect.width() / 2 - textWidth / 2),
                        (rectOfDrawable!!.bottom + TEXT_SIZE),
                        paint
                    )
                    if (checked) {
                        if (Config.EPD_COLOR == 3) {
                            paint.strokeWidth = 1f
                        } else {
                            paint.strokeWidth = 2f
                        }
                        paint.style = Paint.Style.STROKE
                        if (Config.EPD_COLOR == 3) {
                            rect.inset(8f, 8f)
                        } else {
                            rect.inset(18f, 18f)
                        }
                        aCanvas.drawRect(rect, paint)
                    }
                } else  {
                    paint.style = Paint.Style.FILL
                    paint.textSize = 70f
                    paint.color = Color.BLACK
                    val textWidth = paint.measureText(pageIndication)
                    aCanvas.drawText(
                        pageIndication,
                        (rect.right - rect.width() / 2 - textWidth / 2),
                        rect.top + 100,
                        paint
                    )
                }

                if (commit) {
                    val oRect = RectF()
                    CoorMapUtil.android2Osd(it, oRect)

                    CoorMapUtil.android2Osd(aBitmap, it.toRect(), b, true)
                    NativeAdapter.getInstance().commitBitmap(b,
                        oRect.left.toInt(), oRect.top.toInt(),
                        oRect.right.toInt(), oRect.bottom.toInt())
                }
            }
        }
    }
}
