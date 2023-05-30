package com.remi.pdfscanner.customview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.remi.pdfscanner.util.DrawHelper

class OverlayCardID : View {
    val paintClear = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    val rect = RectF()
    val path = Path()
    val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
    val rectText = Rect()
    var text = ""
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val rectW = width*5/6
        val rectH = rectW*2/3
        rect.set((width-rectW)/2f,(height-rectH)/2f,(width+rectW)/2f,(height+rectH)/2f)
        path.addRect(rect,Path.Direction.CCW)

        canvas.drawColor(Color.parseColor("#66000000"))
        canvas.drawPath(path,paintClear)
        if (text.isNotBlank()){
            rectText.set(width/3,rect.top.toInt()-40-height/10,width*2/3,rect.top.toInt()-40)
            rect.set(width/6f,rectText.top-0f,width*5f/6,rectText.bottom+10f)
            paintText.color = Color.parseColor("#66000000")
            canvas.drawRoundRect(rect,rect.height()/10f,rect.height()/10f,paintText)
            paintText.color = Color.WHITE
            DrawHelper.drawRectTextFit(canvas,paintText,Paint.Align.CENTER,text,rectText)
        }

    }

}