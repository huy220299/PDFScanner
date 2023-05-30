package com.remi.pdfscanner.ui.main

import androidx.lifecycle.ViewModel
import com.remi.pdfscanner.repository.DbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(val repository: DbRepository):ViewModel() {

}