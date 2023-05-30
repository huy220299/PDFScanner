package com.remi.pdfscanner.util

import android.graphics.*
import android.graphics.Paint.Align
import kotlin.math.max
import kotlin.math.min

object DrawHelper {
    fun drawRectTextFit(canvas: Canvas, paint: Paint, align: Align, text: String, left: Float, top: Float, right: Float, bottom: Float) {
        drawRectTextFit(canvas, paint, align, text, Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt()))
    }

    fun drawRectTextFit(canvas: Canvas, paint: Paint, text: String, left: Float, top: Float, right: Float, bottom: Float) {
        drawRectTextFit(canvas, paint, text, Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt()))
    }

    fun drawRectTextFit(canvas: Canvas, paint: Paint, align: Align, text: String, r: Rect) {
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
        if (align == Align.LEFT) x = r.left.toFloat() else if (align == Align.RIGHT) x = r.right.toFloat()
        canvas.drawText(text, x, y, paint)
    }

    fun drawRectTextSpecial(canvas: Canvas, paint: Paint, align: Align, textInput: String, rectInput: Rect) {
        val w = rectInput.width()
        val h = rectInput.height()
        val textHour = textInput.split("\n")[0]
        val textMinus = textInput.split("\n")[1]
        //draw Hour
        val rectHour =  Rect(0, 0, w - w * 1 / 3, h)


        paint.textSize = 100f
        paint.textAlign = align
        val boundHour = Rect()
        if (textHour.length == 2)
            paint.getTextBounds(textHour, 0, textHour.length, boundHour)
        else
            paint.getTextBounds("1$textHour",0,"1$textHour".length,boundHour)
        val sHour = rectHour.width().toFloat() * 0.95f / boundHour.width() * 100
        val s1H = rectHour.height().toFloat() * 0.95f / boundHour.height() * 100
        val minHour = min(sHour, s1H)
        paint.textSize = minHour
        paint.getTextBounds(textHour, 0, textHour.length, boundHour)
        val yH = rectHour.top + rectHour.height() / 2f + boundHour.height() / 2f
        var xH = rectHour.exactCenterX()
//        if (align == Align.LEFT) xH = rectHour.left.toFloat() else if (align == Align.RIGHT) xH = rectHour.right.toFloat()
        xH = rectHour.right.toFloat() //auto draw Right
        canvas.drawText(textHour, xH, yH, paint)
        //draw minus
        val topMinus = yH.toInt()-boundHour.height()
        val rectMinus =   Rect(w * 2 / 3, topMinus, w, topMinus+w/3)

        paint.textSize = 100f
        paint.textAlign = align
        val bounds = Rect()
        paint.getTextBounds(textMinus, 0, textMinus.length, bounds)
        val s = rectMinus.width().toFloat() * 0.95f / bounds.width() * 100
        val s1 = rectMinus.height().toFloat() * 0.95f / bounds.height() * 100
        val min = Math.min(s, s1)
        paint.textSize = min
        paint.getTextBounds(textMinus, 0, textMinus.length, bounds)
        val y = rectMinus.top + rectMinus.height() / 2f + bounds.height() / 2f
        var x = rectMinus.exactCenterX()
        if (align == Align.LEFT) x = rectMinus.left.toFloat() else if (align == Align.RIGHT) x = rectMinus.right.toFloat()
        canvas.drawText(textMinus, x, yH-boundHour.height()+bounds.height(), paint)

    }
    fun drawRectTextSpecial1(canvas: Canvas, paint: Paint, align: Align, textInput: String, rectInput: Rect) {
        val w = rectInput.width()
        val h = rectInput.height()
        val textHour = textInput.split("\n")[0]
        val textMinus = textInput.split("\n")[1]
        //draw Hour
        val rectHour = if (textHour.length == 2) Rect(0, 0, w - w * 1 / 3, h)
        else Rect(w*34/150,0, w*80/150, h)

        paint.textSize = 100f
        paint.textAlign = align
        val boundHour = Rect()
        paint.getTextBounds(textHour, 0, textHour.length, boundHour)
        val sHour = rectHour.width().toFloat() * 0.95f / boundHour.width() * 100
        val s1H = rectHour.height().toFloat() * 0.95f / boundHour.height() * 100
        val minHour = min(sHour, s1H)
        paint.textSize = minHour
        paint.getTextBounds(textHour, 0, textHour.length, boundHour)
        val yH = rectHour.top + rectHour.height() / 2f + boundHour.height() / 2f
        var xH = rectHour.exactCenterX()
        if (align == Align.LEFT) xH = rectHour.left.toFloat() else if (align == Align.RIGHT) xH = rectHour.right.toFloat()
        canvas.drawText(textHour, xH, yH, paint)
    //draw minus
        val topMinus = yH.toInt()-boundHour.height()
        val rectMinus = if (textHour.length == 2) Rect(w * 2 / 3, topMinus, w, topMinus+w/3)
        else Rect(w * 85 / 150, topMinus, w * 120 / 150, topMinus+rectInput.width()/3)
        paint.textSize = 100f
        paint.textAlign = align
        val bounds = Rect()
        paint.getTextBounds(textMinus, 0, textMinus.length, bounds)
        val s = rectMinus.width().toFloat() * 0.95f / bounds.width() * 100
        val s1 = rectMinus.height().toFloat() * 0.95f / bounds.height() * 100
        val min = Math.min(s, s1)
        paint.textSize = min
        paint.getTextBounds(textMinus, 0, textMinus.length, bounds)
        val y = rectMinus.top + rectMinus.height() / 2f + bounds.height() / 2f
        var x = rectMinus.exactCenterX()
        if (align == Align.LEFT) x = rectMinus.left.toFloat() else if (align == Align.RIGHT) x = rectMinus.right.toFloat()
        canvas.drawText(textMinus, x, yH-boundHour.height()+bounds.height(), paint)

    }

    fun drawRect2LineTextFit(canvas: Canvas, paint: Paint, align: Align, input: String, r: Rect) {
        paint.textSize = 100f
        paint.textAlign = align
        val bounds = Rect()
        val text = input.split("\n")[0]
        val text2 = input.split("\n")[1]
        r.set(r.left, r.top, r.right, r.bottom - r.height() / 2)
        paint.getTextBounds(text, 0, text.length, bounds)
        val s = r.width().toFloat() * 0.95f / bounds.width() * 100
        val s1 = r.height().toFloat() * 0.95f / bounds.height() * 100
        val min = Math.min(s, s1)
        paint.textSize = min*0.9f
        paint.getTextBounds(text, 0, text.length, bounds)
        val y = r.top + r.height() / 2f + bounds.height() / 2f
        var x = r.exactCenterX()
        if (align == Align.LEFT) x = r.left.toFloat() else if (align == Align.RIGHT) x = r.right.toFloat()
        canvas.drawText(text, x, y, paint)
        val bounds2 = Rect()
        paint.getTextBounds(text2, 0, text2.length, bounds2)
        if (align==Align.LEFT)
            canvas.drawText(text2, 0f, y + bounds.height()*1.1f, paint)
        else
            canvas.drawText(text2, x + (bounds2.width() - bounds.width()) / 2f, y + bounds.height()*1.1f, paint)
    }

    fun drawRectMultiLineTextFit(canvas: Canvas, paint: Paint, align: Align, input: String, r: Rect) {
        paint.textSize = 100f
        paint.textAlign = align
        val bounds = Rect()
        val listText = input.split("\n")
        var maxString = ""
        listText.forEach {
            if (it.length > maxString.length)
                maxString = it
        }
        val r1 = Rect(r.left, r.top, r.right, r.height() / listText.size)

        paint.getTextBounds(maxString, 0, maxString.length, bounds)
        val s = r1.width().toFloat() * 0.95f / bounds.width() * 100
        val s1 = r1.height().toFloat() * 0.95f / bounds.height() * 100
        val min = min(s, s1)
        paint.textSize = min //finish set text size
        paint.getTextBounds(maxString, 0, maxString.length, bounds)
        val h = bounds.height() * 1.1
        val y = r.height() / 2f - h * listText.size / 2
//        val y =  0
        for (i in listText.indices) {
            val text = listText[i]
            paint.getTextBounds(text, 0, text.length, bounds)
            var x = r.exactCenterX()
            if (align == Align.LEFT) x = r.left.toFloat() else if (align == Align.RIGHT) x = r.right.toFloat()
            canvas.drawText(text, x, (y + (i + 1) * h).toFloat(), paint)
        }
    }
    fun drawRectMultiLineTextFitColor(canvas: Canvas, paint: Paint, align: Align, input: String, r: Rect, paintSub:Paint,createShader:Boolean=false,startColor:Int,endColor:Int) {
        paint.textSize = 100f
        paint.textAlign = align
        val bounds = Rect()
        val listText = input.split("\n")
        var maxString = ""
        listText.forEach {
            if (it.length > maxString.length)
                maxString = it
        }
        val r1 = Rect(r.left, r.top, r.right, r.height() / listText.size)

        paint.getTextBounds(maxString, 0, maxString.length, bounds)
        val s = r1.width().toFloat() * 0.95f / bounds.width() * 100
        val s1 = r1.height().toFloat() * 0.95f / bounds.height() * 100
        val min = min(s, s1)
        paint.textSize = min //finish set text size
        paintSub.textSize = min
        paint.getTextBounds(maxString, 0, maxString.length, bounds)
        val h = bounds.height() * 1.1
        val y = r.height() / 2f - h * listText.size / 2
//        val y =  0
        for (i in listText.indices) {
            val text = listText[i]
            paint.getTextBounds(text, 0, text.length, bounds)
            var x = r.exactCenterX()
            if (align == Align.LEFT) x = r.left.toFloat() else if (align == Align.RIGHT) x = r.right.toFloat()
            if (i==listText.size-1){
                if (createShader)
                    paint.shader = LinearGradient(bounds.left.toFloat(), bounds.top.toFloat(), bounds.right.toFloat(), bounds.bottom.toFloat(), startColor, endColor, Shader.TileMode.MIRROR)
                canvas.drawText(text, x, (y + (i + 1) * h).toFloat(), paint)
            }

            else
                canvas.drawText(text, x, (y + (i + 1) * h).toFloat(), paintSub)
        }
    }

    fun drawRectTextFit(canvas: Canvas, paint: Paint, text: String, r: Rect) {
        drawRectTextFit(canvas, paint, Align.CENTER, text, r)
    }

    fun drawIconWithPath(canvas: Canvas, path: Path, paint: Paint, size: Float, x: Int, y: Int) {
        val rectF = RectF()
        path.computeBounds(rectF, true)
        val scale = size / rectF.width()
        canvas.save()
        canvas.translate(x.toFloat(), y.toFloat())
        canvas.scale(scale, scale)
        canvas.drawPath(path, paint)
        canvas.restore()
    }

    fun createRect(source: Bitmap, canvas: Canvas):Rect {
        val dst = Rect()
        val src = Rect()

        val sourceWidth = source.width
        val sourceHeight = source.height

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger of these two.
        val xScale = canvas.width / sourceWidth.toFloat()
        val yScale = canvas.height / sourceHeight.toFloat()
        val scale = max(xScale, yScale)
        val screenHeight = canvas.height
        val screenWidth = canvas.width

        // Now get the size of the source bitmap when scaled
        val scaledWidth = scale * sourceWidth
        val scaledHeight = scale * sourceHeight
        if (scale == xScale) {
            dst.set(-1, -(scaledHeight - screenHeight).toInt() / 2-1, screenWidth.toInt()+1, (scaledHeight + screenHeight).toInt() / 2+1)
        } else {
            dst.set(-(scaledWidth - screenWidth).toInt() / 2-1, -1, (screenWidth + scaledWidth).toInt() / 2+1, screenHeight.toInt()+1)
        }
        return dst
    }

}