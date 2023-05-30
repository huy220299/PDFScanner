package com.remi.pdfscanner.util

import java.text.DecimalFormat

object ConvertData {
    fun floatForm(d: Double): String {
        return DecimalFormat("#.##").format(d)
    }

    fun floatForm2(d: Double): Float {
        return  (DecimalFormat("#.##").format(d)).toFloat()
    }

    fun bytesToHuman(size: Long): String {
        val kb = (1 * 1024).toLong()
        val mb = kb * 1024
        val gb = mb * 1024
        val tb = gb * 1024
        val pb = tb * 1024
        val eb = pb * 1024
        if (size < kb) return floatForm(size.toDouble()) + " byte"
        if (size < mb) return floatForm(size.toDouble() / kb) + " Kb"
        if (size < gb) return floatForm(size.toDouble() / mb) + " Mb"
        if (size < tb) return floatForm(size.toDouble() / gb) + " Gb"
        if (size < pb) return floatForm(size.toDouble() / tb) + " Tb"
        return if (size < eb) floatForm(size.toDouble() / pb) + " Pb" else floatForm(size.toDouble() / eb) + " Eb"
    }
}