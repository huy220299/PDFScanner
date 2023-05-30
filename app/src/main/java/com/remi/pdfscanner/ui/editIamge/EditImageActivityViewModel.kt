package com.remi.pdfscanner.ui.editIamge

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class EditImageActivityViewModel @Inject constructor() : ViewModel() {
    var isApplyForAll = false

    /**
     * current position editing : position in viewpager
     */
    var currentItemPosition = MutableLiveData<Int>()

    var currentContrast = 50
    var tempContrast =50

    var currentBrightness = 50
    var tempBrightness = 50

    var currentDetails = 50
    var tempDetails = 50

    /**
     * update default value when swipe to next image
     */
    var flagUpdateFilter = MutableLiveData<Boolean>()

    /**
     * notify fragment to update bitmap by config
     */
    var flagChangeTemp = MutableLiveData<Boolean>()
    var flagChangeCurrent = MutableLiveData<Boolean>()


    var listImageSaved = MutableLiveData<MutableList<String?>>()
    var flagStartSave = MutableLiveData<Boolean>()
    var flagEndSave = MutableLiveData<Boolean>()
    init {
        listImageSaved.postValue(ArrayList())
    }

    /**
     * bitmap to add signature
     */
    var bitmapSignature: Bitmap?=null
    var bitmapAntiCounterfeit: Bitmap?=null
}