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

package com.es.note.utils

import android.os.Environment
import java.io.File

class Config {
    companion object {
        @JvmField
        val NOTE_BASE_FOLDER = File(Environment.getExternalStorageDirectory(), "EsNote")

        fun getNoteFile(noteId: Long, pageNo: Int): File {
            val pngName = "${noteId}_$pageNo.png"
            val noteFolder = File(NOTE_BASE_FOLDER, noteId.toString())
            if (!noteFolder.exists()) {
                noteFolder.mkdirs()
            }

            return File(noteFolder, pngName)
        }

        @JvmField
        val NOTE_ROOT_FOLDER_ID = 0L

        @JvmField
        val SHOW_OSD_AXES = false

        @JvmField
        val POINT_TEST = false

        @JvmField
        val TP_SIS = true

        @JvmField
        val OSD_UI = true

        @JvmField
        val PERIODIC_DRAW_POINTS = false

        @JvmField
        val SHOW_PLAYER = false

        @JvmField
        val SHOW_IMAGE_VIEWER = false

        @JvmField
        val SHOW_TEST_TOOL = false

        const val OED_DRIVER = true

        @JvmField
        var EPD_WIDTH = 2240

        @JvmField
        var EPD_HEIGHT = 1680

        @JvmField
        var EPD_COLOR = 0

        @JvmField
        val LOAD_HANDWRITING_MILLIS = 50L

        @JvmStatic
        fun getWidth(aWidth: Boolean = false): Int {
            return if (aWidth) {
                EPD_HEIGHT
            } else {
                EPD_WIDTH
            }
        }

        @JvmStatic
        fun getHeight(aHeight: Boolean = false): Int {
            return if(aHeight) {
                EPD_WIDTH
            } else {
                EPD_HEIGHT
            }
        }

        @JvmField
        val TOOL_BAR_HEIGHT = 148

        @JvmField
        val DEBUG_BITMAP = false

        @JvmField
        val STYLUS_WACOM = false

        @JvmField
        val STYLUS_HUION = false

        @JvmField
        val STYLUS_ILI_HUION = false

        @JvmField
        val STYLUS_ILI_XINWEI = false

        @JvmField
        val STYLUS_SIS_XINWEI = true

        @JvmField
        val STYLUS_KT6739 = false

        @JvmField
        var PRESSURE_MAX = 0;

        init {
            if (STYLUS_WACOM) {
                PRESSURE_MAX = 2096;
            } else if (STYLUS_HUION) {
                PRESSURE_MAX = 3900;
            } else if (STYLUS_ILI_HUION || STYLUS_ILI_XINWEI || STYLUS_SIS_XINWEI) {
                PRESSURE_MAX = 4095;
            }
        }
    }
}