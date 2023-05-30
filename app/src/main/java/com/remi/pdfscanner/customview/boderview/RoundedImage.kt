package com.remi.pdfscanner.customview.boderview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class RoundedImage:AppCompatImageView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    val rect = RectF()
    val path = Path()
    override fun onDraw(canvas: Canvas) {
        rect.set(0f,0f,width.toFloat(),height.toFloat())
        path.addRoundRect(rect,20f,20f,Path.Direction.CW)
        canvas.clipPath(path)
        super.onDraw(canvas)
    }
}