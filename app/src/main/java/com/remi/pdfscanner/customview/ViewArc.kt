package com.remi.pdfscanner.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class ViewArc: View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    val paint = Paint()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawArc(0f,0f,width.toFloat(),height.toFloat(),-90f,20f,false,paint)
    }
}