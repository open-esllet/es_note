package com.es.note.drawing

import com.es.note.vm.NoteVM

class PointInfo(x: Int, y: Int, penType: Int, penWidth: Float, penColor: Int) {
    @JvmField
    var x: Int = 0
    @JvmField
    var y: Int = 0
    @JvmField
    var penType: Int = NoteVM.PEN_TYPE_PEN
    @JvmField
    var penWidth: Float = 0f
    @JvmField
    var penColor: Int = 0

    init {
        this.x = x
        this.y = y
        this.penType = penType
        this.penWidth = penWidth
        this.penColor = penColor
    }
}
