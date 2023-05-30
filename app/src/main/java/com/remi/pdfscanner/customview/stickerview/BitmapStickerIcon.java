package com.remi.pdfscanner.customview.stickerview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import androidx.annotation.IntDef;


import com.remi.pdfscanner.util.Common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author wupanjie
 */
public class BitmapStickerIcon extends DrawableSticker implements StickerIconEvent {
    public static final float DEFAULT_ICON_RADIUS = 30f;
    public static final float DEFAULT_ICON_EXTRA_RADIUS = 150f;

    @IntDef({LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Gravity {

    }

    public static final int LEFT_TOP = 0;
    public static final int RIGHT_TOP = 1;
    public static final int LEFT_BOTTOM = 2;
    public static final int RIGHT_BOTOM = 3;

    private float iconRadius = DEFAULT_ICON_RADIUS;
    private float iconExtraRadius = DEFAULT_ICON_EXTRA_RADIUS;
    private float x;
    private float y;
    boolean isFlipVertical, isFlipHorizontal;
    @Gravity
    private int position = LEFT_TOP;

    private StickerIconEvent iconEvent;
    private Paint pWhite;

    public BitmapStickerIcon(Drawable drawable, @Gravity int gravity) {
        super(drawable);
        this.position = gravity;
        pWhite = new Paint(Paint.ANTI_ALIAS_FLAG);
        pWhite.setStrokeWidth(2);
        if (Common.INSTANCE.getScreenWidth()<1080)
            iconRadius=18f;
    }

    public void draw(Canvas canvas, Paint paint) {
//        pWhite.setColor(Color.WHITE);
//        pWhite.setStyle(Paint.Style.FILL);
//        canvas.drawCircle(x, y, iconRadius, pWhite);
//        pWhite.setStyle(Paint.Style.STROKE);
//        pWhite.setColor(Color.BLACK);
//        canvas.drawCircle(x, y, iconRadius, pWhite);
        getDrawable().setBounds((int)(x-iconRadius),(int)(y-iconRadius),(int)(x+iconRadius),(int)(y+iconRadius));
        getDrawable().draw(canvas);
//        super.draw(canvas);
    }

    float angle;
    int marginX, marginY;

    public void draw(Canvas canvas, Paint paint, Sticker sticker, int marginX, int marginY, float x, float y) {
        this.marginX = marginX;
        this.marginY = marginY;
        angle = sticker.getCurrentAngle();
        if (!sticker.isFlippedVertically() && !sticker.isFlippedHorizontally()) {
            isFlipHorizontal = false;
            isFlipVertical = false;
        } else if (!sticker.isFlippedVertically() && sticker.isFlippedHorizontally()) {
            if (angle < 0)
                angle = -180 - angle;
            else angle = 180 - angle;
            isFlipHorizontal = false;
            isFlipVertical = true;
        } else if (sticker.isFlippedVertically() && !sticker.isFlippedHorizontally()) {
            angle = -angle;
            isFlipHorizontal = true;
            isFlipVertical = false;
        } else if (sticker.isFlippedVertically() && sticker.isFlippedHorizontally()) {
            if (angle < 0)
                angle = -180 - angle;
            else angle = 180 - angle;
            angle = -angle;
            isFlipHorizontal = true;
            isFlipVertical = true;
        }
        this.x = x;
        this.y = y;


        canvas.save();
        canvas.translate(x, y);
        canvas.rotate(angle);
        //draw small
        pWhite.setColor(Color.WHITE);
        pWhite.setStyle(Paint.Style.FILL);
        pWhite.setAlpha(255);
        canvas.drawCircle(-marginX, -marginY, iconRadius, pWhite);
        pWhite.setStyle(Paint.Style.STROKE);
        pWhite.setColor(Color.BLACK);
        canvas.drawCircle(-marginX, -marginY, iconRadius, pWhite);

        if (marginX > 0 && marginY > 0) {
            getDrawable().setBounds(-marginX * 3 / 2, -marginY * 3 / 2, -marginX / 2, -marginY / 2);
        } else if (marginX > 0 && marginY < 0)
            getDrawable().setBounds(-marginX * 3 / 2, -marginY / 2, -marginX / 2, -marginY * 3 / 2);
        else if (marginX < 0 && marginY < 0)
            getDrawable().setBounds(-marginX / 2, -marginY / 2, -marginX * 3 / 2, -marginY * 3 / 2);
        else
            getDrawable().setBounds(-marginX / 2, -marginY * 3 / 2, -marginX * 3 / 2, -marginY / 2);


//        getDrawable().setBounds(-marginX,-marginY,marginX,marginY);
        getDrawable().draw(canvas);
        canvas.restore();
//        pWhite.setColor(Color.RED);
//        pWhite.setStyle(Paint.Style.FILL);
//        pWhite.setAlpha(100);
//        float tempX=0;
//        float tempY=0;
//
//        if (marginX > 0 && marginY > 0) {
//            angle = Math.abs(angle);
//            tempX = (float) (x - marginX * Math.cos(angle));
//            tempY = (float) (y - marginX * Math.cos(angle));
//        } else if (marginX > 0 && marginY < 0) {
//
//        } else if (marginX < 0 && marginY < 0) {
//
//        } else {
//
//        }
//        canvas.drawCircle(tempX, tempY, iconRadius, pWhite);


    }

    int getPosInLayout() {
        if (marginX > 0 && marginY > 0)
            return 0;//top_left
        else if (marginX > 0 && marginY < 0)
            return 1;//bottom_left
        else if (marginX < 0 && marginY > 0)
            return 2;//top_right
        else
            return 3;//bottom_right
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;

    }

    public void setY(float y) {
        this.y = y;
    }

    public float getIconRadius() {
        return iconRadius;
    }

    public void setIconRadius(float iconRadius) {
        this.iconRadius = iconRadius;
    }

    public float getIconExtraRadius() {
        return iconExtraRadius;
    }

    public void setIconExtraRadius(float iconExtraRadius) {
        this.iconExtraRadius = iconExtraRadius;
    }

    @Override
    public void onActionDown(StickerView stickerView, MotionEvent event) {
        if (iconEvent != null) {
            iconEvent.onActionDown(stickerView, event);
        }
    }

    @Override
    public void onActionMove(StickerView stickerView, MotionEvent event) {
        if (iconEvent != null) {
            iconEvent.onActionMove(stickerView, event);
        }
    }

    @Override
    public void onActionUp(StickerView stickerView, MotionEvent event) {
        if (iconEvent != null) {
            iconEvent.onActionUp(stickerView, event);
        }
    }

    public StickerIconEvent getIconEvent() {
        return iconEvent;
    }

    public void setIconEvent(StickerIconEvent iconEvent) {
        this.iconEvent = iconEvent;
    }

    @Gravity
    public int getPosition() {
        return position;
    }

    public void setPosition(@Gravity int position) {
        this.position = position;
    }
}
