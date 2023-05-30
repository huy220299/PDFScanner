package com.remi.pdfscanner.ui.anticounter

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class AntiCounterMaskView: View {
    var text = "hahahaha"
    var size = 10
    var myColor = "#000000"
    var transparent = 0
    var isShuffle = true
    var tempShuffle = false
    var angle = -45f
    var bitmap1: Bitmap? = null
    var bitmap2: Bitmap? = null
    var isPassport = false
    var isCreateRect = true
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1A000000") }
    private val rectText = Rect()

    private val paintBitmap = Paint(Paint.FILTER_BITMAP_FLAG).apply { }
    private val rectF = RectF()
    private val rectTemp = RectF()
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawTextUnder(canvas)
    }
    fun setTextSize(int: Int) {
        size = int
        isCreateRect = true
        invalidate()
    }
    private fun drawTextUnder(canvas: Canvas) {
        val marginW = canvas.width / 10
        val marginH = canvas.width / 10
        paint.apply {
            textSize = canvas.width / 400f * size
            color = Color.parseColor(myColor)
            alpha = 255 * transparent / 100
        }
        if (isCreateRect) {
            isCreateRect = false
            paint.getTextBounds(text, 0, text.length, rectText)
        }
        canvas.save()
        canvas.rotate(angle, canvas.width / 2f, canvas.height / 2f)

        var startX = -canvas.width.toFloat()
        var startY = -canvas.height.toFloat()
        while (startY < canvas.height * 2) {
            while (startX < canvas.width * 2) {
                canvas.drawText(text, startX, startY, paint)
                startX += marginW + rectText.width()
            }
            tempShuffle = !tempShuffle
            if (isShuffle && tempShuffle)
                startX = -canvas.width.toFloat()
            else
                startX = -canvas.width.toFloat() + rectText.width() / 2
            startY += marginH + rectText.height()
        }
        canvas.restore()
    }

    fun getBitmap(widthOut: Int): Bitmap? {
        val bitmapResult = Bitmap.createBitmap(widthOut,widthOut*height/width,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapResult)
        draw(canvas)
        return bitmapResult
    }
}