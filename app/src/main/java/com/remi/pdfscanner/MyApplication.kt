package com.remi.pdfscanner

import android.app.Application
import com.remi.pdfscanner.repository.model.GalleryModel
import com.remi.pdfscanner.util.Common
import dagger.hilt.android.HiltAndroidApp
import me.pqpo.smartcropperlib.SmartCropper

@HiltAndroidApp
class MyApplication : Application() {
    companion object {
        var galleryModelList: List<GalleryModel> = ArrayList()
    }

    override fun onCreate() {
        super.onCreate()
        SmartCropper.buildImageDetector(this)

        try {
            galleryModelList = Common.getAllFolder(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}