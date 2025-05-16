package com.es.note.utils;

import android.graphics.Color;

import com.es.note.vm.NoteVM;

public class OedColor {
    public static final int COLOR_TRANSPARENT = Config.OED_DRIVER ? Color.TRANSPARENT : 0xff000000;
    public static final int COLOR_BLACK = Config.OED_DRIVER ? Color.BLACK : 0x0;
    public static final int COLOR_RED  = Config.OED_DRIVER ? Color.RED : 0x0;
    public static final int COLOR_GREEN = Config.OED_DRIVER ? Color.GREEN : 0x0;
    public static final int COLOR_BLUE = Config.OED_DRIVER ? Color.BLUE : 0x0;
    public static final int COLOR_YELLOW = Config.OED_DRIVER ? Color.BLACK : 0x0;
    public static final int COLOR_PINK = Config.OED_DRIVER ? Color.BLACK : 0x0;
    public static final int COLOR_INDIGO = Config.OED_DRIVER ? Color.BLACK : 0x0;
    public static final int COLOR_PURPLE = Config.OED_DRIVER ? Color.BLACK : 0x0;
    public static final int COLOR_WHITE = Config.OED_DRIVER ? Color.WHITE : 0xff000000;

    public static int penColor2CanvasColor(final int color) {
        if (color == NoteVM.PEN_COLOR_TRANSPARENT) {
            return COLOR_TRANSPARENT;
        } else if (color == NoteVM.PEN_COLOR_BLACK) {
            return COLOR_BLACK;
        } else if (color == NoteVM.PEN_COLOR_RED) {
            return COLOR_RED;
        } else if (color == NoteVM.PEN_COLOR_GREEN) {
            return COLOR_GREEN;
        } else if (color == NoteVM.PEN_COLOR_BLUE) {
            return COLOR_BLUE;
        } else if (color == NoteVM.PEN_COLOR_YELLOW) {
            return COLOR_YELLOW;
        } else if (color == NoteVM.PEN_COLOR_PINK) {
            return COLOR_PINK;
        } else if (color == NoteVM.PEN_COLOR_INDIGO) {
            return COLOR_INDIGO;
        } else if (color == NoteVM.PEN_COLOR_PURPLE) {
            return COLOR_PURPLE;
        } else if (color == NoteVM.PEN_COLOR_WHITE) {
            return COLOR_WHITE;
        }

        return COLOR_TRANSPARENT;
    }
}
