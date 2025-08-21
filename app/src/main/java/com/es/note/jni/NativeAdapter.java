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

package com.es.note.jni;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.es.note.utils.Config;
import com.es.note.widget.Palette;

public class NativeAdapter {
    private static final String TAG = NativeAdapter.class.getSimpleName();
    private static final int TOUCH_DOWN = 1;
    private static final int TOUCH_MOVE = 2;
    private static final int TOUCH_UP = 3;
    private static final int RUBBER_DOWN = 4;
    private static final int RUBBER_UP = 5;

    private static NativeAdapter sInstance;
    public static NativeAdapter getInstance() {
        if (sInstance == null) {
            sInstance = new NativeAdapter();
        }
        return sInstance;
    }

    private Palette mPalette;
    private Rect mPaletteRect;

    private NativeAdapter() {
        System.loadLibrary("nativeadapter");
    }

    public void setPalette(Palette palette) {
        mPalette = palette;
    }

    public void refreshScreen() {
        if (Config.OED_DRIVER) {
            _refreshScreen();
        }
    }

    public void commitBitmap(Bitmap bitmap, int l, int t, int r, int b) {
        _commitBitmap(bitmap, l, t, r, b);
    }

    public native int[] _init();
    public native void _startEventLoop();
    public native void _stopEventLoop();
    public native void _setOverlayEnabled(boolean enabled);
    public native int _getOverlayStatus();
    public native void _setHandwritingEnabled(boolean enabled);
    public native void _refreshOSD(boolean clearContent, boolean sendBuff);
    public native void _refreshScreen();
    public native void _commitBitmap(Bitmap bitmap, int l, int t, int r, int b);

    public native void _onAppEnter();
    public native void _onAppExit();
    public native void _onAppCrash();


    public void onTouchEvent(int type, int x, int y, int pressure) {
        if (mPalette != null) {
            if (type == TOUCH_DOWN) {
                mPalette.actionDown(x, y);
            } else if (type == TOUCH_MOVE) {
                mPalette.actionMove(x, y, pressure);
            } else if (type == TOUCH_UP) {
                mPalette.actionUp(x, y);
            } else if (type == RUBBER_DOWN) {
                mPalette.setRubberModeEnabled(true);
            } else if (type == RUBBER_UP) {
                mPalette.setRubberModeEnabled(false);
            }
        }
    }
}
