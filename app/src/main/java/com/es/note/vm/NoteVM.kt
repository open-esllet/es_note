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

package com.es.note.vm

import androidx.lifecycle.ViewModel

class NoteVM : ViewModel() {
    val TAG: String = NoteVM::class.java.getSimpleName()

    @JvmField
    var mode: Int = MODE_PEN

    @JvmField
    var checkedButton: Int = 0

    @JvmField
    var penType: Int = PEN_TYPE_PEN
    @JvmField
    var penWidthLevel: Int = MEDIUM_PEN_WIDTH_LEVEL
    @JvmField
    var penColor: Int = PEN_COLOR_BLACK

    @JvmField
    var eraserType: Int = ERASER_PATH
    @JvmField
    var shapeType: Int = SHAPE_LINE

    @JvmField
    var template: String? = null

    companion object {
        const val MODE_PEN: Int = 1
        const val MODE_ERASER: Int = 2
        const val MODE_SHAPE: Int = 3

        const val PEN_TYPE_PEN: Int = 1
        const val PEN_TYPE_PENCIL: Int = 2
        const val PEN_TYPE_BRUSH: Int = 3

        const val PEN_COLOR_TRANSPARENT: Int = 0
        const val PEN_COLOR_BLACK: Int = 1
        const val PEN_COLOR_RED: Int = 2
        const val PEN_COLOR_GREEN: Int = 3
        const val PEN_COLOR_BLUE: Int = 4
        const val PEN_COLOR_YELLOW: Int = 5
        const val PEN_COLOR_PINK: Int = 6
        const val PEN_COLOR_INDIGO: Int = 7
        const val PEN_COLOR_PURPLE: Int = 8
        const val PEN_COLOR_WHITE: Int = 9

        @JvmField
        val PEN_STANDARD_RANGE: IntArray = intArrayOf(1, 5)
        const val PENCIL_MIN_WIDTH: Int = 8
        const val MIN_PEN_WIDTH_LEVEL: Int = 1
        const val MEDIUM_PEN_WIDTH_LEVEL: Int = 3
        const val MAX_PEN_WIDTH_LEVEL: Int = 5

        const val ERASER_PATH: Int = 11
        const val ERASER_SCOPE: Int = 12

        const val SHAPE_LINE: Int = 21
        const val SHAPE_CIRCLE: Int = 22
        const val SHAPE_RECT: Int = 23
        const val SHAPE_TRIANGLE: Int = 24
    }
}