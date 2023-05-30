package com.remi.pdfscanner.ui.anticounter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AntiCounterViewModel:ViewModel() {
    var currentText = "PDF Scanner"
    var currentSize = 10
    var currentTransparent = 10
    var currentColor = "#000000"
    val tempText = MutableLiveData<String>()
    val tempSize = MutableLiveData<Int>()
    val tempTransparent = MutableLiveData<Int>()
    val tempColor = MutableLiveData<String>()
    init {
        tempText.value = currentText
        tempSize.value = currentSize
        tempTransparent.value = currentTransparent
        tempColor.value = currentColor
    }
}