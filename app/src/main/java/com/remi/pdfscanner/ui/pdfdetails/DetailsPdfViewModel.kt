package com.remi.pdfscanner.ui.pdfdetails

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailsPdfViewModel @Inject constructor():ViewModel() {
    var listBitmap = MutableLiveData<MutableList<ItemPage>>()
    init {
        listBitmap.value =  ArrayList()
    }
    var flagBack = MutableLiveData<Boolean>()

    var nameFile  = MutableLiveData<String>()
    init {
        nameFile.postValue("Sample")
    }
}