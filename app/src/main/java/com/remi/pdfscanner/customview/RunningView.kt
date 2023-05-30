package com.remi.pdfscanner.customview

import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.animation.ValueAnimator.RESTART
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class RunningView : View {
    val paint = Paint()
    val rect = Rect()
    val text = "Running Text"

    var animator: ValueAnimator? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        paint.textSize = 20f
        paint.getTextBounds(text, 0, text.length, rect)
        paint.color = Color.BLACK
    }

    fun setTime(time: Int) {
        animator?.run { removeAllUpdateListeners() }
        animator = ValueAnimator.ofFloat(-rect.width().toFloat(), width.toFloat())
        animator?.run {
            duration = time * 1000L
            addUpdateListener { animate->
                currentX = animate.animatedValue as Float
                invalidate()
            }
            repeatCount = INFINITE
            repeatMode = RESTART
            start()
        }
    }

    var currentX = 0f
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawText(text, currentX, height / 2f - rect.height() / 2f, paint)
    }
}