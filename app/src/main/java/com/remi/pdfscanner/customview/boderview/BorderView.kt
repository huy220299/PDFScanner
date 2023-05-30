package com.remi.pdfscanner.customview.boderview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class BorderView: View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var progress = 60
    val rect = RectF()
    val tempRect = RectF()
    val path = Path()
    val rectText = Rect()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val stroke = width/60f
        paint.apply {
            style = Paint.Style.FILL_AND_STROKE
            color = Color.parseColor("#1A000000")

        }
        canvas.drawColor(Color.WHITE)
        rect.set(stroke,stroke,width.toFloat()-2*stroke,height.toFloat()-2*stroke)
        canvas.drawRoundRect(rect,rect.height()/5,rect.height()/5,paint)
        paint.apply {
            strokeWidth = stroke
            style = Paint.Style.STROKE
            color = Color.GREEN
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        path.reset()
//        path.moveTo(width/2f,0f)
        tempRect.set(-width/2f,-height/2f,width*3/2f,height*3/2f)
//        tempRect.set(stroke,stroke,width.toFloat()-2*stroke,height.toFloat()-2*stroke)
        path.moveTo(width/2f,height/2f)
        path.arcTo(tempRect,-90f,progress*360/100f)
        path.moveTo(width/2f,height/2f)
        path.close()
        canvas.save()
        canvas.clipPath(path)
        canvas.drawRoundRect(rect,rect.height()/5,rect.height()/5,paint)
        canvas.restore()
        paint.apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val text = "$progress%"
        rectText.set(width/4,height/4,width*3/4,height*3/4)
        drawRectTextFit(canvas,paint,Paint.Align.CENTER,text,rectText)
    }


    fun drawRectTextFit(canvas: Canvas, paint: Paint, align: Paint.Align, text: String, r: Rect) {
        paint.textSize = 100f
        paint.textAlign = align
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val s = r.width().toFloat() * 0.95f / bounds.width() * 100
        val s1 = r.height().toFloat() * 0.95f / bounds.height() * 100
        val min = Math.min(s, s1)
        paint.textSize = min
        paint.getTextBounds(text, 0, text.length, bounds)
        val y = r.top + r.height() / 2f + bounds.height() / 2f
        var x = r.exactCenterX()
        if (align == Paint.Align.LEFT) x = r.left.toFloat() else if (align == Paint.Align.RIGHT) x = r.right.toFloat()
        canvas.drawText(text, x, y, paint)
    }
}