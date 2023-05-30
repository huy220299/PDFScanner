package com.remi.pdfscanner.base

import android.graphics.Bitmap
import com.remi.pdfscanner.util.BitmapUtil

data class MyBitmap(val bitmapDefault: Bitmap, var rotation: Int = 0, var listBitmap: MutableList<Bitmap?>) {
    init {
        rotation = 0
        listBitmap = mutableListOf(null, null, null)
    }

    fun getCurrentBitmap(isRotateRight: Boolean): Bitmap {
        if (isRotateRight) rotation++ else rotation--

        if (rotation == 0) return bitmapDefault
        if (rotation > 3) {
            rotation = 0
            return bitmapDefault
        }
        if (rotation < 0) rotation = 3
        if (listBitmap[rotation - 1] == null) {
            listBitmap[rotation - 1] = BitmapUtil.createRotateBitmap(bitmapDefault, rotation * 90)
        }
        return listBitmap[rotation-1] ?: bitmapDefault
    }
}