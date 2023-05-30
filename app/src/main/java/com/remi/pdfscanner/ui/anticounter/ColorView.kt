package com.remi.pdfscanner.ui.anticounter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.remi.pdfscanner.R
import kotlin.math.min

class ColorView:View {
    var isSelect = false
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var myColor  = "#FFFFFF"
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val stroke  = width/20f
        paint.apply {
            style = Paint.Style.STROKE
            color = ContextCompat.getColor(context, R.color.main_color)
            strokeWidth  =stroke
        }
        if (isSelect)
            canvas.drawCircle(width/2f,height/2f, min(width/2f,height/2f)-stroke,paint)
        paint.apply {
            style = Paint.Style.FILL
            color = Color.parseColor(myColor)
        }
        canvas.drawCircle(width/2f,height/2f,  min(width/2f,height/2f)-5*stroke/2,paint)
    }
}