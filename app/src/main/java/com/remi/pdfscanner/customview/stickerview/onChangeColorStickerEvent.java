package com.remi.pdfscanner.customview.stickerview;

import android.view.MotionEvent;

public class onChangeColorStickerEvent implements StickerIconEvent {
    @Override
    public void onActionDown(StickerView stickerView, MotionEvent event) {

    }

    @Override
    public void onActionMove(StickerView stickerView, MotionEvent event) {

    }

    @Override
    public void onActionUp(StickerView stickerView, MotionEvent event) {
        stickerView.onChangeColor();
    }
}
