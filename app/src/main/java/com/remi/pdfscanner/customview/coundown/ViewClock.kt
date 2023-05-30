package com.remi.pdfscanner.customview.coundown

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

class ViewClock: View {
    var targetProgress = 360f
    var progress = 360f
    var hehe = 0f
    val rect = RectF()
    val paint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = Color.BLUE
    }
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        val animator = ValueAnimator.ofFloat(1000f).apply { }
        animator.addUpdateListener { animation: ValueAnimator ->
           if (progress>targetProgress)
               progress-=360f/hehe
            invalidate()
        }
        animator.apply {
            duration = 100
            interpolator = DecelerateInterpolator(1f)
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
//            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = width/20f
        paint.strokeWidth = size
        rect.set(size,size,width.toFloat()-size,height.toFloat()-size)
        canvas.drawArc(rect,-90f,progress,false,paint)
    }
}