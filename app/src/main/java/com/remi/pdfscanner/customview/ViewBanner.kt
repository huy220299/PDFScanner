package com.remi.pdfscanner.customview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.graphics.toRectF
import com.remi.pdfscanner.R

class ViewBanner : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var bitmap:Bitmap?=null
    val paintClear = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
    }
    var progress = 0f
    val paintText = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        bitmap = BitmapFactory.decodeResource(resources, R.drawable.test_led_banner)
        val animator = ValueAnimator.ofFloat(3000f).apply { }
        animator.addUpdateListener { animation: ValueAnimator ->
            progress+=width*10/2000
            if (progress>width*3/2f) progress=-width/4f
            invalidate()
        }
        animator.apply {
            duration = 500
            interpolator = DecelerateInterpolator(1f)
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = 4f
        canvas.drawColor(Color.parseColor("#707070"))
        paintText.color = Color.RED

        canvas.save()
        canvas.translate(progress, 0f)
        canvas.drawCircle(width / 2f, height / 2f, width / 4f, paintText)
        bitmap?.run {
            canvas.drawBitmap(this,0f,50f,paintText)
        }
        canvas.restore()


        canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), paintText)
        canvas.drawColor(Color.BLACK)

        var tempX = 0f
        var tempY = 0f
        val padding = width / 400f
        val size = width / 200f
        while (tempX < width + size * 2) {
            while (tempY < height + size * 2) {
                canvas.drawCircle(tempX, tempY, size, paintClear)
//                canvas.drawRect(tempX-size,tempY-size,tempX+size,tempY+size,paintClear)
                tempY += size * 2 + padding
            }
            tempY = 0f
            tempX += size * 2 + padding
        }
        canvas.restore()
    }
}