package com.remi.pdfscanner.repository.model

data class GalleryModel(var nameFolder: String?, var allImageInFolder: ArrayList<String>?, var listOrientation: List<Int>?,var isSelect:Boolean = false)