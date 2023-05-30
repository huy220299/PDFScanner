package com.remi.pdfscanner.ui.anticounter

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.remi.pdfscanner.ui.setting.OptionSize

class AntiCounterView : View {
    var text = "hahahaha"
    var size = 10
    var myColor = "#000000"
    var transparent = 20
    var isShuffle = true
    var tempShuffle = false
    var angle = -45f
    var bitmap1: Bitmap? = null
    var bitmap2: Bitmap? = null
    var isPassport = false
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1A000000") }
    private val rectText = Rect()

    private val paintBitmap = Paint(Paint.FILTER_BITMAP_FLAG).apply { }
    private val rectF = RectF()
    private val rectTemp = RectF()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setTextSize(int: Int) {
        size = int
        isCreateRect = true
        invalidate()
    }

    var isCreateRect = true
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawTextUnder(canvas)

        if (isPassport) {
            drawBitmapPassport(canvas)
        } else {
            if (bitmap2 == null)
                drawBitmapSingleCard(canvas)
            else drawBitmapBothSizeCard(canvas)
        }

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

    private fun drawBitmapPassport(canvas: Canvas) {
        bitmap1?.let {
//             val newH = (width*0.6f)*it.height/it.width
            val newH = (width * 0.6f) * 256 / 179
            rectF.set(width * 0.2f, (height - newH) / 2f, width * 0.8f, (height + newH) / 2)
//             canvas.drawBitmap(it,null,rectF,paintBitmap)
            drawBitmapFitRect(canvas, it, rectF)
        }
    }

    /**
     * draw bitmap in center size w = 60% Width
     */
    private fun drawBitmapSingleCard(canvas: Canvas) {
        bitmap1?.let {
//             val newH = (width*0.6f)*it.height/it.width
            val newH = (canvas.width * 0.6f) * 9 / 14
            rectF.set(canvas.width * 0.2f, (canvas.height - newH) / 2f, canvas.width * 0.8f, (canvas.height + newH) / 2)
//             canvas.drawBitmap(it,null,rectF,paintBitmap)
            drawBitmapFitRect(canvas, it, rectF)
        }
    }

    private fun drawBitmapBothSizeCard(canvas: Canvas) {
        bitmap1?.let {
            val newH = (canvas.width * 0.6f) * 9 / 14
            rectF.set(canvas.width * 0.2f, canvas.height * .2f, canvas.width * 0.8f, canvas.height * .2f + newH)
//            canvas.drawBitmap(it,null,rectF,paintBitmap)
            drawBitmapFitRect(canvas, it, rectF)
        }
        bitmap2?.let {
            val newH = (canvas.width * 0.6f) * 9 / 14
            rectF.set(canvas.width * 0.2f, canvas.height * .55f, canvas.width * 0.8f, canvas.height * .55f + newH)
            drawBitmapFitRect(canvas, it, rectF)
        }
    }

    private fun drawBitmapFitRect(canvas: Canvas, bitmap: Bitmap, rectF: RectF) {
        canvas.drawRect(rectF, paintBackground)
        canvas.save()
        canvas.translate(rectF.left, rectF.top)
        if (bitmap.width / bitmap.height < rectF.width() / rectF.height()) {
            rectTemp.set((rectF.width() - (rectF.height() * bitmap.width / bitmap.height)) / 2, 0f, (rectF.width() + (rectF.height() * bitmap.width / bitmap.height)) / 2, rectF.height())
        } else {
            rectTemp.set(0f, (rectF.height() - (rectF.width() * bitmap.height / bitmap.width)) / 2f, rectF.width(), (rectF.height() + (rectF.width() * bitmap.height / bitmap.width)) / 2f)
        }
        canvas.drawBitmap(bitmap, null, rectTemp, paintBitmap)
        canvas.restore()
    }

    fun getBitmapBySize(size:OptionSize):Bitmap{
        val bitmapResult =when(size){
            OptionSize.A3->Bitmap.createBitmap(1123,1512,Bitmap.Config.ARGB_8888)
            OptionSize.A5->Bitmap.createBitmap(559,793,Bitmap.Config.ARGB_8888)
            OptionSize.B4->Bitmap.createBitmap(945,1334,Bitmap.Config.ARGB_8888)
            OptionSize.B5->Bitmap.createBitmap(665,945,Bitmap.Config.ARGB_8888)
            else->Bitmap.createBitmap(793,1123,Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(bitmapResult)
        draw(canvas)
        return bitmapResult
    }
}