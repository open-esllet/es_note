package com.es.note

import android.app.Application
import com.es.note.jni.NativeAdapter
import com.es.note.utils.Config
import com.es.note.utils.LogUtil
import com.es.note.utils.SysPropHelper.setHwcCommitDisabled

class NoteApp : Application() {
    private val TAG: String = NoteApp::class.java.getSimpleName()

    override fun onCreate() {
        super.onCreate()

        val array: IntArray? = NativeAdapter.getInstance()._init()
        NativeAdapter.getInstance()._startEventLoop()

        array?.also {
            LogUtil.d(TAG, "${it[0]}, ${it[1]}, ${it[2]}")
            Config.EPD_WIDTH = it[0]
            Config.EPD_HEIGHT = it[1]
            Config.EPD_COLOR = it[2]
        }

        Thread.setDefaultUncaughtExceptionHandler { t: Thread?, e: Throwable? ->
            LogUtil.d(TAG, "UncaughtExceptionHandler()", e!!)
            releaseResources(true)
        }
    }

    fun releaseResources(crash: Boolean = false) {
        setHwcCommitDisabled(false)
        NativeAdapter.getInstance()._setHandwritingEnabled(false)
        NativeAdapter.getInstance()._setOverlayEnabled(false)

        if (crash) {
            NativeAdapter.getInstance()._onAppCrash()
        } else {
            NativeAdapter.getInstance()._onAppExit()
        }
    }
}
