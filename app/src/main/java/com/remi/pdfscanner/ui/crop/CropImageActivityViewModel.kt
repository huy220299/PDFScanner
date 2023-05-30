package com.remi.pdfscanner.ui.crop

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class CropImageActivityViewModel : ViewModel() {
    var listImageSaved = MutableLiveData<MutableList<String?>>()

    var flagStartSave = MutableLiveData<Boolean>()
    var flagEndSave = MutableLiveData<Boolean>()

    /**
     * to handle task rotate image
     */
    var flagReloadImage = MutableLiveData<Boolean>()
    var flagRotateImage = MutableLiveData<Boolean>()
    var positionCurrentImage = 0//auto update in viewpager scrolled
    var isRotateImageRight = true
    fun rotateImage(isRotateRight: Boolean) {
        this.isRotateImageRight = isRotateRight
        flagRotateImage.postValue(true)
    }

    /**
     * to handle task auto scan 4 point in image
     */
    var flagAutoScan = MutableLiveData<Boolean>()
    fun enableAutoScanImage(enableScan: Boolean) {
        flagAutoScan.postValue(enableScan)
    }

    /**
     * undo redo crop imageview
     */
    var flagUndoRedo = MutableLiveData<Boolean>()
    fun actionDo(isUndo: Boolean) {
        flagUndoRedo.postValue(isUndo)
    }

    var isCanNext = false
    var isCanPrevious = false
    var flagUpdateUI = MutableLiveData<Boolean>()
    var flagGetUIInformation = MutableLiveData<Boolean>() //activity send this to fragment->fragment listen to send by flagUpdateUI
    fun updateVisibilityUndo(previous: Boolean, next: Boolean) {
        isCanPrevious = previous
        isCanNext = next
        flagUpdateUI.postValue(true)
    }


    var isLoadingFirstImage = MutableLiveData<Boolean>()
    var listBitmap = MutableLiveData<MutableList<Bitmap?>>()
    var listUrlImage: MutableList<String> = ArrayList()

    init {
        listImageSaved.postValue(ArrayList())
    }
}