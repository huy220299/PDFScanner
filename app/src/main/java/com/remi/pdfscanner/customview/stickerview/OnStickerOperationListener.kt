package com.remi.pdfscanner.customview.stickerview

import com.remi.pdfscanner.customview.stickerview.Sticker

interface OnStickerOperationListener {
    fun onStickerAdded(sticker: Sticker){}


     fun onStickerClicked(sticker: Sticker){}


    //    void onStickerDeleted(@NonNull Sticker sticker);

      fun onStickerDragFinished(sticker: Sticker){}


    //    void onStickerTouchedDown(@NonNull Sticker sticker);

      fun onStickerZoomFinished(sticker: Sticker){}


    //    void onStickerFlipped(@NonNull Sticker sticker);
     fun onStickerDoubleTapped(sticker: Sticker){}
      fun onStickerNotClicked() {}
//    void onTextStickerDoubleTapped(@NonNull TextSticker textSticker);
}