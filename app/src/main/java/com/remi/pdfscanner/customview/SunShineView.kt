package com.remi.pdfscanner.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class SunShineView : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var progress = 0
    val path = Path()
    val paint = Paint()
    val range = 2* PI
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.apply {
            flags = Paint.ANTI_ALIAS_FLAG
            style = Paint.Style.STROKE
            strokeWidth = 20f
            color = Color.BLACK
        }


        path.reset()
        path.moveTo(0f, height/2f)

        var i=0
        while(i<width){
            val x  =(i*1f/width)*range
            val y  = height/2 - sin(x).toFloat()*height/4
            path.lineTo(i.toFloat(),y)
            i+=1
        }
        path.moveTo(width.toFloat(),height/2 - sin(range).toFloat()*height/4)

        path.close()

        canvas.drawPath(path, paint)
//        val sunX = progress*width.toFloat()/100
        val sunX =(progress*1f/100)*range.toFloat()
        val sunY =  height/2 - sin(sunX)*height/4
        canvas.drawCircle(progress*width/100f,sunY,30f,paint)
    }


}