package com.es.note

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.es.note.databinding.ActivityMainBinding
import com.es.note.vm.NoteVM
import com.es.note.jni.NativeAdapter
import com.es.note.repo.OfflineRepo
import com.es.note.room.NoteDb
import com.es.note.room.entity.Note
import com.es.note.utils.Config
import com.es.note.utils.LogUtil
import com.es.note.utils.SysPropHelper.setHwcCommitDisabled
import com.es.note.widget.OsdPalette
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean

class NoteActivity : AppCompatActivity() {
    private val TAG: String = NoteActivity::class.java.simpleName

    private var binding: ActivityMainBinding? = null

    private val noteRepo by lazy {
        OfflineRepo(NoteDb.getDatabase(this).folderDao(), NoteDb.getDatabase(this).noteDao())
    }

    private val ID_PALETTE = 0x949311
    private var currentNote: Note? = null

    private val atomicSaving = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        atomicSaving.set(false)

        setHwcCommitDisabled(true)
        setOverlayEnabled(true)
        fullscreen()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        if (!Config.OSD_UI) {
            if (savedInstanceState == null) {
                return
            }
        } else {
            val vm = ViewModelProvider(this)[NoteVM::class.java]
            val lp = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            val palette = OsdPalette(this)
            palette.id = ID_PALETTE
            palette.setNoteVm(vm)
            palette.setBackgroundColor(Color.WHITE)
            binding!!.root.addView(palette, lp)
        }

        NativeAdapter.getInstance()._onAppEnter()

        CoroutineScope(Dispatchers.IO).launch {
            initNote()
        }
    }

    private fun fullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        window.decorView.setOnApplyWindowInsetsListener { view: View, windowInsets: WindowInsets ->
            if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
                || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())
            ) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            }
            view.onApplyWindowInsets(windowInsets)
            windowInsets
        }
    }

    override fun onPause() {
        super.onPause()

        setHandwritingEnabled(false)
        setOverlayEnabled(false)

        saveExit()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setOverlayEnabled(enabled: Boolean) {
        NativeAdapter.getInstance()._setOverlayEnabled(enabled)
    }

    private fun setHandwritingEnabled(enabled: Boolean) {
        NativeAdapter.getInstance()._setHandwritingEnabled(enabled)
    }

    private suspend fun initNote() {
        if (!Config.OSD_UI) return

        var status = NativeAdapter.getInstance()._getOverlayStatus()
        if (status < 0) {
            return
        }
        while (status == 1) {
            delay(10)
            status = NativeAdapter.getInstance()._getOverlayStatus()
        }

        NativeAdapter.getInstance()._refreshOSD(true, false)
        NativeAdapter.getInstance().refreshScreen()

        val palette = binding!!.root.getViewById(ID_PALETTE) as OsdPalette
        val id = intent.getLongExtra("id", -1L)

        if (id == -1L) {
            currentNote = Note(Config.NOTE_ROOT_FOLDER_ID, getString(R.string.default_note_name))
            currentNote?.also {
                it.id = noteRepo.insertNote(it)
                palette.createNewPageLock()
                delay(Config.LOAD_HANDWRITING_MILLIS)
                setHandwritingEnabled(true)
            }
        } else {
            currentNote = noteRepo.getNote(id)
            val palette = binding!!.root.getViewById(ID_PALETTE) as OsdPalette
            palette.restorePalette(currentNote!!)
        }
    }

    fun saveExit() {
        if (!Config.OSD_UI) {
            TODO("NEED IMPL..")
            return
        }

        if (atomicSaving.get()) {
            return
        }
        atomicSaving.set(true)

        val palette = binding!!.root.getViewById(ID_PALETTE) as OsdPalette

        currentNote?.also {
            runBlocking {
                it.saving = 1
                noteRepo.updateNote(it)
            }
            setResult(RESULT_OK, Intent().apply {
                putExtra("saving_id", it.id)
            })
            LogUtil.d(TAG, "saveExit() setResult() OK id = ${it.id}")
        }
        finish()

        CoroutineScope(Dispatchers.IO).launch {
            LogUtil.d(TAG, "saveExit() start: currentNote = $currentNote")

            val matrix = Matrix().apply {
                preScale(-1f, 1f)

                if (Config.TP_SIS) {
                    postRotate(90f)
                } else {
                    postRotate(270f)
                }
            }

            var outBitmap: Bitmap? = null
            val imgPaths = StringBuffer()
            for ((i, osd) in palette.overlayList.withIndex()) {
                if (Config.TP_SIS) {
                    outBitmap = Bitmap.createBitmap(
                        osd,
                        0,
                        0,
                        palette.overlayList.last().width - Config.TOOL_BAR_HEIGHT * 2,
                        palette.overlayList.last().height,
                        matrix,
                        true
                    )
                } else {
                    outBitmap = Bitmap.createBitmap(
                        osd,
                        Config.TOOL_BAR_HEIGHT,
                        0,
                        palette.overlayList.last().width - Config.TOOL_BAR_HEIGHT,
                        palette.overlayList.last().height,
                        matrix,
                        true
                    )
                }

                val noteFile = Config.getNoteFile(currentNote!!.id!!, i + 1)
                FileOutputStream(noteFile).use {
                    outBitmap.compress(Bitmap.CompressFormat.PNG, 80, it)
                }

                imgPaths.append(noteFile.absolutePath).append(";")
            }

            currentNote?.also {
                it.pageNo = palette.pageNo
                it.imgPaths = imgPaths.toString()
                it.accessTime = System.currentTimeMillis()
                it.updatedTime = System.currentTimeMillis()
                it.saving = 0
                noteRepo.updateNote(it)
                LogUtil.d(TAG, "saveExit() end: pageNo = ${it.pageNo}, save note image to ${it.imgPaths}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d(TAG, "onDestroy()")

        if (Config.OSD_UI) {
            NativeAdapter.getInstance()._refreshOSD(true, true)
            (application as NoteApp).releaseResources(false)
        }
    }
}