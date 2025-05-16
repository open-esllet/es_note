package com.es.note.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public abstract class Palette extends View {
    public Palette(Context context) {
        super(context);
    }

    public Palette(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Palette(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Palette(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public abstract void actionDown(final int x, final int y);

    public abstract void actionMove(final int x, final int y, final int pressure);

    public void actionUp(int x, int y) {}

    public void setRubberModeEnabled(boolean enabled) {}
}
