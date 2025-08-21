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
