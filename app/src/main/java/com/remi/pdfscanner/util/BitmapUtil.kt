package com.remi.pdfscanner.util

import android.graphics.Bitmap
import android.graphics.Matrix

object BitmapUtil {
    fun createRotateBitmap(bitmapIn: Bitmap?, degree: Int): Bitmap? {
        if (bitmapIn == null) return null
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())

        return Bitmap.createBitmap(bitmapIn, 0, 0, bitmapIn.width, bitmapIn.height, matrix, true)
    }
}