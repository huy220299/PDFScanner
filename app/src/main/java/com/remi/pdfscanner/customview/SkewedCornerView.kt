package com.remi.pdfscanner.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class SkewedCornerView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val path = Path()
    val paint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        color = Color.WHITE
        style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas?) {
//        super.onDraw(canvas)
        val padding = height/50f
        paint.strokeWidth = padding
        canvas?.drawColor(Color.BLACK)
        canvas?.apply {
            path.reset()
            val cornerSize = width / 15f
            val width = width.toFloat()
            val height = height.toFloat()
//            path.moveTo(padding, height-padding)
            path.moveTo(padding, height/2)
            path.lineTo(cornerSize, padding)
            path.lineTo(width - cornerSize-padding, padding)
            path.lineTo(width-padding, height/2)
//            path.lineTo(width-padding, height-padding)
//            path.lineTo(padding, height-padding)
//            path.close()
            drawPath(path, paint)
        }
    }
}