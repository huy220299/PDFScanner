package com.remi.pdfscanner.customview.anim

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator

class ViewAnim : View {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var paint: Paint
    private var p1: Path? = null
    private var p2: Path? = null
    private var p3: Path? = null

    private var bm: Bitmap? = null
    private val rectBm: Rect
    private var text: String? = null
    private var size = 0f

    private var animator: ValueAnimator

    private var tranY = 0f
    private var a1 = 255
    private var a2 = 155
    private var a3 = 20
    private var or1 = 1
    private var or2 = 1
    private var or3 = 1
    private var step = 0


    init {
        size = dpToPx(13)
        paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = size
            textAlign = Paint.Align.CENTER
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = size / 8
        }
        rectBm = Rect()
        text = "abc abc abc abc"
        animator = ValueAnimator.ofFloat(1f)
        animator.addUpdateListener { animation: ValueAnimator ->
            val p = animation.animatedValue as Float
            tranY = size / 2 * p
            step++
            if (step % 3 == 0) {
                a1 += or1 * 10
                if (a1 > 255) {
                    a1 = 255
                    or1 = -1
                } else if (a1 < 20) {
                    a1 = 20
                    or1 = 1
                }
                a2 += or2 * 10
                if (a2 > 255) {
                    a2 = 255
                    or2 = -1
                } else if (a2 < 20) {
                    a2 = 20
                    or2 = 1
                }
                a3 += or3 * 10
                if (a3 > 255) {
                    a3 = 255
                    or3 = -1
                } else if (a3 < 20) {
                    a3 = 20
                    or3 = 1
                }
            }
            invalidate()
        }
        animator.apply {
            duration = 1600
            interpolator = DecelerateInterpolator(1f)
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == VISIBLE) {
            if (!animator.isRunning) animator.start()
        } else {
            if (animator.isRunning) animator.cancel()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (p1 == null) {
            val s = size * 5 / 10
            var fist = dpToPx(45)
            p1 = Path().apply {
                moveTo(width / 2f - s, fist - s)
                lineTo(width / 2f, fist)
                lineTo(width / 2f + s, fist - s)
            }
            fist += dpToPx(20)
            p2 = Path().apply {
                moveTo(width / 2f - s, fist - s)
                lineTo(width / 2f, fist)
                lineTo(width / 2f + s, fist - s)
            }
            fist += dpToPx(20)
            p3 = Path().apply {
                moveTo(width / 2f - s, fist - s)
                lineTo(width / 2f, fist)
                lineTo(width / 2f + s, fist - s)
            }
        }
        canvas.save()
        canvas.translate(0f, tranY)
        paint.apply {
            style = Paint.Style.FILL
            alpha = 255
        }

        bm?.let {
            rectBm.set(
                width / 2 - it.width / 2,
                height / 2 - it.height / 2,
                width / 2 + it.width / 2,
                height / 2 + it.height / 2,
            )
            canvas.drawBitmap(it, null, rectBm, paint)
        }
//        canvas.drawText(text!!, width / 2f, dpToPx(18), paint)

//        paint.apply {
//            style = Paint.Style.STROKE
//            alpha = a1
//        }
//        canvas.drawPath(p1!!, paint)
//        paint.alpha = a2
//        canvas.drawPath(p2!!, paint)
//        paint.alpha = a3
//        canvas.drawPath(p3!!, paint)
//        canvas.restore()
    }

    private fun dpToPx(dp: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        )
    }

    fun setColor(color: Int) {
        paint.color = color
        invalidate()
    }

    fun setText(text: String?) {
        this.text = text
        invalidate()
    }

    fun setBitmap(bm: Bitmap) {
        this.bm = bm
        invalidate()
    }
}