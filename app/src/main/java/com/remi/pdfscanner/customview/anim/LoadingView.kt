package com.remi.pdfscanner.customview.anim

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.remi.pdfscanner.R

class LoadingView:View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    var index = 0
    val pos = floatArrayOf(0f,0f,0f)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var animator: ValueAnimator

    init {
        paint.color = ContextCompat.getColor(context, R.color.main_color)
        animator = ValueAnimator.ofFloat(3000f).apply {  }
        animator.addUpdateListener { animation: ValueAnimator ->
            val p = animation.animatedValue as Float
            index = ((p-1)/1000).toInt()
            invalidate()
        }
        animator.apply {
            duration = 500
            interpolator = DecelerateInterpolator(1f)
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            start()
        }
    }

    fun onFinish(){
        animator.cancel()
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = width*.14f
        val padding = width*.07f
        for (i in pos.indices){
            if (i==index)
                canvas.drawCircle(radius*(i*2+1)+padding*i,radius,radius,paint)
            else canvas.drawCircle(radius*(i*2+1)+padding*i,height-radius,radius,paint)
        }
    }
}