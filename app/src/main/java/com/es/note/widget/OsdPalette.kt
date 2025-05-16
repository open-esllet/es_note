package com.es.note.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import androidx.compose.ui.geometry.Offset
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.es.note.NoteActivity
import com.es.note.R
import com.es.note.drawing.AxesDrawingInfo
import com.es.note.drawing.BitmapDrawingInfo
import com.es.note.drawing.DrawingInfo
import com.es.note.drawing.PathDrawingInfo
import com.es.note.drawing.PointInfo
import com.es.note.drawing.RectDrawingInfo
import com.es.note.drawing.StrokeDrawingInfo
import com.es.note.drawing.ToolbarDrawingInfo
import com.es.note.vm.NoteVM
import com.es.note.jni.NativeAdapter
import com.es.note.room.entity.Note
import com.es.note.utils.BitmapUtils
import com.es.note.utils.BitmapUtils.rgba2GrayBitmap
import com.es.note.utils.Config
import com.es.note.utils.CoorMapUtil
import com.es.note.utils.LogUtil.Companion.d
import com.es.note.utils.OedColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.ceil

class OsdPalette : Palette {
    private val TAG: String = OsdPalette::class.java.getSimpleName()

    private var TEMP_DIR: File? = null

    private var vm: NoteVM? = null

    private var housekeeperBitmap: Bitmap? = null
    private var housekeeperCanvas: Canvas? = null
    private var housekeeperPaint: Paint? = null
    private var mBackupBitmap: Bitmap? = null
    private var mTemplateBitmap: Bitmap? = null
    private var mOverlayCanvas: Canvas? = null
    private var mOverlayPaint: Paint? = null
    private var mToolbarBitmap: Bitmap? = null
    private var mToolbarCanvas: Canvas? = null
    private var mToolbarPaint: Paint? = null
    private var mPencilShader: BitmapShader? = null

    private val drawingInfoList: MutableList<DrawingInfo?> = ArrayList()
    private val revokeInfoList: MutableList<DrawingInfo?> = ArrayList()

    private var toolBarDrawingInfo: ToolbarDrawingInfo? = null

    private val exe: ExecutorService = Executors.newSingleThreadExecutor()

    private var pageLoadDone = false
    var pageNo: Int = 0 // start from no.1
    var overlayList = arrayListOf<Bitmap>()
    private val pageLocker = ReentrantLock()

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setNoteVm(vm: NoteVM) {
        this.vm = vm
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        NativeAdapter.getInstance().setPalette(this)
        TEMP_DIR = File(context.noBackupFilesDir, "temp")
        TEMP_DIR!!.mkdirs()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        NativeAdapter.getInstance().setPalette(null)
    }

    fun initOverlay(overlay: Bitmap) {
        if (mOverlayPaint == null) {
            mOverlayPaint = Paint().apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
            }
        }

        if (mPencilShader == null) {
            val vd: VectorDrawable? = checkNotNull(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_pencil_shader,
                    null
                ) as VectorDrawable?
            )
            mPencilShader = BitmapShader(
                BitmapUtils.vectorDrawable2Bitmap(vd!!),
                Shader.TileMode.CLAMP,
                Shader.TileMode.REPEAT
            )
        }

        mOverlayCanvas = Canvas(overlay).apply {
            setDensity(0)
        }
    }

    fun commitOverlay(overlay: Bitmap, cleanOsd: Boolean = true) {
        if (cleanOsd)
            cleanOverlay(true, true, true, null)


        if (Config.TP_SIS) {
            NativeAdapter.getInstance()
                .commitBitmap(
                    overlay, 0, 0,
                    overlay.width - Config.TOOL_BAR_HEIGHT*2, overlay.height
                )
        }

        if (Config.SHOW_OSD_AXES) AxesDrawingInfo(
            this,
            overlay, mOverlayCanvas!!, mOverlayPaint!!
        ).draw()
    }

    fun updateToolbar(pageNo: Int, totalPages: Int) {
        d(TAG, "updateToolbar() pageNo = $pageNo, size= $totalPages")
        if (mToolbarBitmap == null) {
            mToolbarBitmap =  createBitmap(Config.getWidth(), Config.getHeight()).apply {
                mToolbarPaint = Paint().apply {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
                }
                mToolbarCanvas = Canvas(this)

                toolBarDrawingInfo = ToolbarDrawingInfo(
                    this@OsdPalette,
                    this, mToolbarCanvas!!, mToolbarPaint!!
                ).apply {
                    init(vm!!)
                    setPageIndication(pageNo, totalPages, false)
                    draw(true)
                }
            }
        } else  {
            toolBarDrawingInfo?.setPageIndication(pageNo, totalPages, true)
        }
    }

    fun setPage(pageNo: Int) {
        assert(pageNo >= 1)

        this.pageNo = pageNo
        val pageIndex = pageNo - 1

        d(TAG, "setPage() pageNo = $pageNo, size=${overlayList.size}")
        updateToolbar(pageNo, overlayList.size)
        overlayList[pageIndex].also {
            initOverlay(it)
            commitOverlay(it)
        }
    }

    fun setPageLock(pageNo: Int) {
        if (pageLocker.isLocked) return

        pageLocker.lock()
        try {
            setPage(pageNo)
        } finally {
            pageLocker.unlock()
        }
    }

    fun deletePageLock(pageNo: Int) {
        if (pageLocker.isLocked) return

        pageLocker.lock()
        try {
            assert(pageNo >= 1)
            overlayList.removeAt(this.pageNo - 1)

            this.pageNo = pageNo - 1
            if (this.pageNo < 1) {
                this.pageNo = 1
            }

            d(TAG, "deletePageLock() pageNo = $pageNo, this.pageNo = ${this.pageNo}")
            setPage(this.pageNo)
        } finally {
            pageLocker.unlock()
        }
    }

    fun createNewPageLock() {
        if (pageLocker.isLocked) return

        pageLocker.lock()
        d(TAG, "createNewPageLock()")
        try {
            val width = Config.getWidth()
            val height = Config.getHeight()

            if (Config.OED_DRIVER) {
                overlayList.add(createBitmap(width, height).also {
                    it.eraseColor(Color.WHITE)
                })
            } else {
                overlayList.add(createBitmap(width, height, Bitmap.Config.ALPHA_8))
            }

            pageNo = overlayList.size

            setPage(pageNo)
        } finally {
            pageLocker.unlock()
            pageLoadDone  = true
        }
    }

    suspend fun restorePalette(note: Note) {
        val width = Config.getWidth()
        val height = Config.getHeight()
        overlayList.clear()

        var totalPages = 0
        note.imgPaths?.apply {
            totalPages = split(";").size - 1
        }

        d(TAG, "restorePalette() totalPage == $totalPages")
        if (totalPages == 0) {
            (context as NoteActivity).finish()
            return
        }

        val m1 = Matrix().apply {
            preScale(-1f, 1f)
            postRotate(90f)
        }

        fun createOverlay(pageNo: Int, commit: Boolean = false): Bitmap {
            return if (Config.OED_DRIVER) {
                createBitmap(width, height)
            } else {
                createBitmap(width, height, Bitmap.Config.ALPHA_8)
            }.let { overlay ->
                overlay.eraseColor(Color.WHITE)

                val noteFile = Config.getNoteFile(note.id!!, pageNo)
                BitmapFactory.decodeFile(noteFile.absolutePath).apply {
                    val noteBitmap = Bitmap.createBitmap(
                        this,
                        0, 0, this.width, this.height, m1, true)

                    drawBitmap2Overlay(noteBitmap, overlay, commit)

                    d(TAG, "restorePalette() drawBitmap ${noteFile.absolutePath}")
                }
                overlay
            }
        }

        fun enableHandwriting() {
            NativeAdapter.getInstance()._setHandwritingEnabled(true)
        }

        this.pageNo = note.pageNo
        updateToolbar(note.pageNo, totalPages)
        createOverlay(note.pageNo).apply {
            overlayList.add(this)
            initOverlay(this)
            commitOverlay(this, true)
            delay(Config.LOAD_HANDWRITING_MILLIS)
            enableHandwriting()
        }

        CoroutineScope(Dispatchers.IO).launch {
            repeat(totalPages) { i ->
                val pageNo = i + 1
                if (pageNo == this@OsdPalette.pageNo) {
                    return@repeat
                }

                createOverlay(pageNo).apply {
                    overlayList.add(pageNo - 1,this)
                }
            }

            d(TAG, "restorePalette() done")
            pageLoadDone = true
        }
    }

    fun drawBitmap2Overlay(from: Bitmap, overlay: Bitmap, commit: Boolean = false) {
        BitmapDrawingInfo(overlay, Canvas(overlay), Paint()).apply {
            setBitmap(from, 0, 0)
            drawAll(commit)
        }
    }

    fun cleanOverlay(clearHistory: Boolean, excludeToolbar: Boolean, useHousekeeper: Boolean = false, cb: Runnable?) {
        val ri = if (useHousekeeper) {
            if (housekeeperBitmap == null) {
                housekeeperBitmap = createBitmap(Config.getWidth(), Config.getHeight()).apply {
                    housekeeperPaint = Paint()
                    housekeeperCanvas = Canvas(this)
                }
            }

            RectDrawingInfo(housekeeperBitmap!!,
                housekeeperCanvas!!, housekeeperPaint!!)
        } else {
            RectDrawingInfo(overlayList[pageNo - 1],
                mOverlayCanvas!!, mOverlayPaint!!)
        }

        ri.setExcludeToolbar(excludeToolbar)
        ri.draw()
        if (clearHistory) {
            drawingInfoList.clear()
            revokeInfoList.clear()
        }
    }

    fun undo() {
        if (drawingInfoList.isEmpty()) return
        revokeInfoList.add(drawingInfoList.removeAt(drawingInfoList.size - 1))
        val ri = RectDrawingInfo(overlayList[pageNo - 1], mOverlayCanvas!!, mOverlayPaint!!)

        if (drawingInfoList.isEmpty()) {
            ri.clearDrawingArea(true)
        } else {
            ri.clearDrawingArea(false)
            for (i in drawingInfoList.indices) {
                drawingInfoList[i]!!.drawAll(false)
                if (i + 1 == drawingInfoList.size) ri.updateBitmap(
                    0,
                    0,
                    Config.getWidth() - Config.TOOL_BAR_HEIGHT * 2,
                    Config.getHeight()
                )
            }
        }
    }

    fun redo() {
        if (revokeInfoList.isEmpty()) return

        drawingInfoList.add(revokeInfoList.removeAt(revokeInfoList.size - 1))
        for (i in drawingInfoList.indices) {
            drawingInfoList[i]!!.drawAll(true)
        }
    }

    fun drawLastFrame() {
        if (drawingInfoList.isEmpty()) return
        for (i in drawingInfoList.indices) {
            drawingInfoList[i]!!.drawAll(true)
        }
        toolBarDrawingInfo = ToolbarDrawingInfo(
            this,
            overlayList[pageNo - 1], mOverlayCanvas!!, mOverlayPaint!!
        )
        toolBarDrawingInfo!!.draw()
    }

    private fun getPenWidthByPressure(pressure: Float): Float {
        val penWidthLevel = vm!!.penWidthLevel

        if (vm!!.penType == NoteVM.PEN_TYPE_PEN) {
            if (Config.STYLUS_WACOM) {
                return if (pressure <= 2000) {
                    (NoteVM.MAX_PEN_WIDTH_LEVEL - 3).toFloat()
                } else {
                    ceil(((pressure - 2000) / Config.PRESSURE_MAX * NoteVM.MAX_PEN_WIDTH_LEVEL).toDouble()).toFloat()
                }
            } else if (Config.STYLUS_HUION) {
                return if (pressure <= 1500) {
                    NoteVM.MIN_PEN_WIDTH_LEVEL.toFloat()
                } else {
                    (NoteVM.MIN_PEN_WIDTH_LEVEL
                            + ceil(((pressure - 1500) / Config.PRESSURE_MAX * NoteVM.MAX_PEN_WIDTH_LEVEL).toDouble())).toFloat()
                }
            } else if (Config.STYLUS_ILI_HUION) {
                val penWidth =
                    (pressure - 1) * (NoteVM.PEN_STANDARD_RANGE[1] - NoteVM.PEN_STANDARD_RANGE[0]) / (Config.PRESSURE_MAX - 1) + NoteVM.PEN_STANDARD_RANGE[0]
                return if (penWidth <= NoteVM.PEN_STANDARD_RANGE[NoteVM.PEN_STANDARD_RANGE.size / 2]) {
                    penWidth + vm!!.penWidthLevel * 1f
                } else {
                    penWidth * 1.5f + vm!!.penWidthLevel * 1.5f
                }
            } else if (Config.STYLUS_ILI_XINWEI) {
                return if (pressure <= 1500) {
                    NoteVM.MIN_PEN_WIDTH_LEVEL.toFloat()
                } else {
                    (NoteVM.MIN_PEN_WIDTH_LEVEL
                            + ceil(((pressure - 1500) / Config.PRESSURE_MAX * NoteVM.MAX_PEN_WIDTH_LEVEL).toDouble())).toFloat()
                }
            } else if (Config.STYLUS_SIS_XINWEI) {
                return when (vm!!.penWidthLevel) {
                    NoteVM.MIN_PEN_WIDTH_LEVEL -> {
                        pressure / Config.PRESSURE_MAX * NoteVM.MAX_PEN_WIDTH_LEVEL
                    }
                    NoteVM.MEDIUM_PEN_WIDTH_LEVEL -> {
                        pressure / Config.PRESSURE_MAX * NoteVM.MAX_PEN_WIDTH_LEVEL * 2
                    }
                    else -> {
                        pressure / Config.PRESSURE_MAX * NoteVM.MAX_PEN_WIDTH_LEVEL * 3
                    }
                }
            } else if (Config.STYLUS_KT6739) {
                return 1f
            }

            return NoteVM.MIN_PEN_WIDTH_LEVEL.toFloat()
        } else if (vm!!.penType == NoteVM.PEN_TYPE_PENCIL) {
            if (Config.STYLUS_ILI_HUION) {
                val penWidth = (NoteVM.PENCIL_MIN_WIDTH + penWidthLevel).toFloat()
                return penWidth * 2
            } else if (Config.STYLUS_SIS_XINWEI) {
                return (penWidthLevel * 2).toFloat()
            }
        } else if (vm!!.penType == NoteVM.PEN_TYPE_BRUSH) {
            if (Config.STYLUS_SIS_XINWEI) {
                return if (vm!!.penWidthLevel == NoteVM.MIN_PEN_WIDTH_LEVEL) {
                    pressure / Config.PRESSURE_MAX * NoteVM.MAX_PEN_WIDTH_LEVEL * 3
                } else if (vm!!.penWidthLevel == NoteVM.MEDIUM_PEN_WIDTH_LEVEL) {
                    pressure / Config.PRESSURE_MAX * NoteVM.MAX_PEN_WIDTH_LEVEL * 6
                } else {
                    pressure / Config.PRESSURE_MAX * NoteVM.MAX_PEN_WIDTH_LEVEL * 12
                }
            }
        }

        return penWidthLevel.toFloat()
    }

    override fun setRubberModeEnabled(enabled: Boolean) {
        if (enabled) {
            vm!!.mode = NoteVM.MODE_ERASER
            vm!!.eraserType = NoteVM.ERASER_SCOPE
        } else {
            vm!!.mode = NoteVM.MODE_PEN
            vm!!.penType = NoteVM.PEN_TYPE_PEN
        }
    }

    private val toolClickListener: (Int) -> Unit = listener@{ toolType: Int ->
        d(TAG, "toolClickListener() toolType=$toolType")

        if (toolType == ToolbarDrawingInfo.TOOL_TYPE_PEN) {
            setRubberModeEnabled(false)
            toolBarDrawingInfo?.updateCheckedState(false, true)
        } else if (toolType == ToolbarDrawingInfo.TOOL_TYPE_ERASER) {
            setRubberModeEnabled(true)
            toolBarDrawingInfo?.updateCheckedState(false, true)
        } else if (toolType == ToolbarDrawingInfo.TOOL_TYPE_UNDO) {
            undo()
        } else if (toolType == ToolbarDrawingInfo.TOOL_TYPE_REDO) {
            redo()
        } else if (toolType == ToolbarDrawingInfo.TOOL_TYPE_REFRESH) {
            NativeAdapter.getInstance().refreshScreen()
        } else if (toolType == ToolbarDrawingInfo.TOOL_TYPE_PAGE_PREV) {
            if (!pageLoadDone) return@listener
            if (pageNo >= 2) setPageLock(pageNo - 1)
        } else if (toolType == ToolbarDrawingInfo.TOOL_TYPE_PAGE_NEXT) {
            if (!pageLoadDone) return@listener
            if (pageNo >= overlayList.size) {
                if (overlayList.size < 9) createNewPageLock()
            } else {
                setPageLock(pageNo + 1)
            }
        } else if (toolType == ToolbarDrawingInfo.TOOL_TYPE_PAGE_DEL) {
            if (!pageLoadDone) return@listener
            if (overlayList.size >= 2) {
                deletePageLock(pageNo)
            }
        } else if (toolType == ToolbarDrawingInfo.TOOL_TYPE_PAGE_IND) {
        } else if (toolType == ToolbarDrawingInfo.TOOL_TYPE_SAVE_EXIT) {
            if (!pageLoadDone) return@listener
            (context as NoteActivity).saveExit()
        } else if (toolType == ToolbarDrawingInfo.SUBTOOL_TYPE_PENCEIL) {
            vm!!.penType = NoteVM.PEN_TYPE_PENCIL
            toolBarDrawingInfo?.updateCheckedState(false, true)
        } else if (toolType == ToolbarDrawingInfo.SUBTOOL_TYPE_PEN) {
            vm!!.penType = NoteVM.PEN_TYPE_PEN
            toolBarDrawingInfo?.updateCheckedState(false, true)
        } else if (toolType == ToolbarDrawingInfo.SUBTOOL_TYPE_BRUSH) {
            vm!!.penType = NoteVM.PEN_TYPE_BRUSH
            toolBarDrawingInfo?.updateCheckedState(false, true)
        } else if (toolType == ToolbarDrawingInfo.SUBTOOL_SMALL_POINT) {
            vm!!.penWidthLevel = NoteVM.MIN_PEN_WIDTH_LEVEL
            toolBarDrawingInfo?.updateCheckedState(false, true)
        } else if (toolType == ToolbarDrawingInfo.SUBTOOL_MEDIUM_POINT) {
            vm!!.penWidthLevel = NoteVM.MEDIUM_PEN_WIDTH_LEVEL
            toolBarDrawingInfo?.updateCheckedState(false, true)
        } else if (toolType == ToolbarDrawingInfo.SUBTOOL_LARGE_POINT) {
            vm!!.penWidthLevel = NoteVM.MAX_PEN_WIDTH_LEVEL
            toolBarDrawingInfo?.updateCheckedState(false, true)
        } else if (toolType == ToolbarDrawingInfo.SUBTOOL_TYPE_BLACK) {
            vm!!.penColor = NoteVM.PEN_COLOR_BLACK
            toolBarDrawingInfo?.updateCheckedState(false, true)
        } else if (toolType == ToolbarDrawingInfo.SUBTOOL_TYPE_WHITE) {
            vm!!.penColor = NoteVM.PEN_COLOR_WHITE
            toolBarDrawingInfo?.updateCheckedState(false, true)
        } else if (toolType == ToolbarDrawingInfo.SUBTOOL_TYPE_RED) {
            vm!!.penColor = NoteVM.PEN_COLOR_RED
            toolBarDrawingInfo?.updateCheckedState(false, true)
        } else if (toolType == ToolbarDrawingInfo.SUBTOOL_TYPE_GREEN) {
            vm!!.penColor = NoteVM.PEN_COLOR_GREEN
            toolBarDrawingInfo?.updateCheckedState(false, true)
        } else if (toolType == ToolbarDrawingInfo.SUBTOOL_TYPE_BLUE) {
            vm!!.penColor = NoteVM.PEN_COLOR_BLUE
            toolBarDrawingInfo?.updateCheckedState(false, true)
        }
    }

    override fun actionDown(x: Int, y: Int) {
        if (toolBarDrawingInfo == null) return

        val aOffset = CoorMapUtil.tpOffset2Android(Offset(x.toFloat(), y.toFloat()))
        if (aOffset.y <= Config.TOOL_BAR_HEIGHT*2) {
            toolBarDrawingInfo!!.handleClick(aOffset.x.toInt(), aOffset.y.toInt(), toolClickListener)
            return;
        }

        if (pageNo - 1 < 0 || pageNo - 1 >= overlayList.size) {
            return
        }

        if (vm!!.mode == NoteVM.MODE_PEN) {
            if (x > Config.TOOL_BAR_HEIGHT * 2 + 1) {
                val si = StrokeDrawingInfo(
                    overlayList[pageNo - 1],
                    mOverlayCanvas!!,
                    mOverlayPaint!!,
                    mPencilShader
                )
                drawingInfoList.add(si)
                revokeInfoList.clear()
            }
        } else if (vm!!.mode == NoteVM.MODE_ERASER) {
            if (vm!!.eraserType == NoteVM.ERASER_PATH) {
                val si =
                    StrokeDrawingInfo(overlayList[pageNo - 1], mOverlayCanvas!!, mOverlayPaint!!, null)
                drawingInfoList.add(si)
            } else if (vm!!.eraserType == NoteVM.ERASER_SCOPE) {
                val pi = PathDrawingInfo(overlayList[pageNo - 1], mOverlayCanvas!!, mOverlayPaint!!)
                drawingInfoList.add(pi)
                pi.updatePaint(NoteVM.ERASER_SCOPE)
            }
        }
    }

    override fun actionMove(x: Int, y: Int, pressure: Int) {
        val aOffset = CoorMapUtil.tpOffset2Android(Offset(x.toFloat(), y.toFloat()))
        if (aOffset.y <= Config.TOOL_BAR_HEIGHT*2) {
            return;
        }


        if (vm!!.mode == NoteVM.MODE_PEN) {
            var penWidth = getPenWidthByPressure(pressure.toFloat())
            if (penWidth == Float.Companion.NEGATIVE_INFINITY || penWidth == Float.Companion.POSITIVE_INFINITY) {
                penWidth = NoteVM.PEN_STANDARD_RANGE[1].toFloat()
            }
            if (Config.POINT_TEST) {
                penWidth = 3f
            }
            actionHandlePenMove(x, y, penWidth)
        } else if (vm!!.mode == NoteVM.MODE_ERASER) {
            actionHandleEraserMove(x, y)
        }
    }

    override fun actionUp(x: Int, y: Int) {
        val aOffset = CoorMapUtil.tpOffset2Android(Offset(x.toFloat(), y.toFloat()))
        if (aOffset.y <= Config.TOOL_BAR_HEIGHT*2) {
            return;
        }

        if (vm!!.mode == NoteVM.MODE_ERASER && vm!!.eraserType == NoteVM.ERASER_SCOPE) {
            if (drawingInfoList.isEmpty()) return
            val di = drawingInfoList[drawingInfoList.size - 1]
            if (di !is PathDrawingInfo) return

            val pi = di
            pi.closePath()
            pi.draw()
            pi.drawAll(true)
        } else if (vm!!.mode == NoteVM.MODE_PEN) {
            if (drawingInfoList.isEmpty()) return
            val di = drawingInfoList[drawingInfoList.size - 1]
            if (di !is StrokeDrawingInfo) return

            val si = di
            si.cancelTimer()
        }
    }

    private fun actionHandlePenMove(x: Int, y: Int, penWidth: Float) {
        if (drawingInfoList.isEmpty() || vm!!.mode == NoteVM.MODE_ERASER) return
        val info = drawingInfoList[drawingInfoList.size - 1]
        if (info is StrokeDrawingInfo) {
            val si = drawingInfoList[drawingInfoList.size - 1] as StrokeDrawingInfo
            si.addPointInfo(
                PointInfo(
                    x, y, vm!!.penType, penWidth, OedColor.penColor2CanvasColor(
                        vm!!.penColor
                    )
                )
            )
            if (!Config.PERIODIC_DRAW_POINTS) si.draw()
        }
    }

    private fun actionHandleEraserMove(x: Int, y: Int) {
        if (drawingInfoList.isEmpty()) return
        val di = drawingInfoList[drawingInfoList.size - 1]

        if (vm!!.eraserType == NoteVM.ERASER_PATH) {
            if (di is StrokeDrawingInfo) {
                val si = di
                si.addPointInfo(
                    PointInfo(
                        x,
                        y,
                        NoteVM.ERASER_PATH,
                        80f,
                        OedColor.COLOR_TRANSPARENT
                    )
                )
                si.draw()
            }
        } else if (vm!!.eraserType == NoteVM.ERASER_SCOPE) {
            if (di is PathDrawingInfo) {
                val pi = di
                pi.quadTo(x, y)
                pi.draw()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}