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
