package com.remi.pdfscanner.customview.stickerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

/**
 * @author wupanjie
 */
public class DrawableSticker extends Sticker {

    private Drawable drawable;
    private Rect realBounds;
    Bitmap bitmap = null;

    public void setRealBounds(Rect realBounds) {
        this.realBounds = realBounds;
    }

    public Rect getRealBounds() {
        return realBounds;
    }
    //  private boolean canChangeColor=false;

    public DrawableSticker(Drawable drawable) {
        this.drawable = drawable;
        realBounds = new Rect(0, 0, getWidth(), getHeight());
    }


    public DrawableSticker() {
    }

    public DrawableSticker(Context context, Bitmap bitmap) {
        this.bitmap = bitmap;
        this.drawable = new BitmapDrawable(context.getResources(), bitmap);
        realBounds = new Rect(0, 0, getWidth(), getHeight());
    }

    public void setBitmapDrawable(Context context, Bitmap bitmap) {
        this.bitmap = bitmap;
        this.drawable = new BitmapDrawable(context.getResources(), bitmap);
        realBounds = new Rect(0, 0, getWidth(), getHeight());
    }

    public void setBitmapDrawable1(Context context, Bitmap bitmap) {
        this.bitmap = bitmap;
        this.drawable = new BitmapDrawable(context.getResources(), bitmap);
        realBounds = new Rect(0, 0, 1080, 1080);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    @NonNull
    @Override
    public Drawable getDrawable() {
        return drawable;
    }

    @Override
    public DrawableSticker setDrawable(@NonNull Drawable drawable) {
        this.drawable = drawable;
        return this;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.save();
        Matrix matrix = getMatrix();
        canvas.concat(matrix);
        drawable.setBounds(realBounds);
        drawable.draw(canvas);
//    canvas.drawBitmap(bitmap, null, realBounds, paint);
        canvas.restore();
    }

    Matrix tempMatrix;
    Paint paint;
    @Override public void draw(@NonNull Canvas canvas, float scale) {
        if (bitmap==null)
            return;
        if (tempMatrix==null)
            tempMatrix = new Matrix();
        tempMatrix.reset();
        Matrix matrix = getMatrix();

        tempMatrix.set(matrix);
        if (paint==null)
            paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        tempMatrix.preScale(0.5f,0.5f);
        tempMatrix.postScale(scale,scale);
        canvas.drawBitmap(bitmap, tempMatrix, paint);
        drawable.setBounds(realBounds);
    }

    @NonNull
    @Override
    public DrawableSticker setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        drawable.setAlpha(alpha);
        return this;
    }

    String currentColor = "";

    public String getCurrentColor() {
        return currentColor;
    }

    public void changeColor(String str) {
        currentColor = str;
        Drawable drawable = getDrawable();
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        try {
            drawable.setColorFilter(Color.parseColor(currentColor), PorterDuff.Mode.MULTIPLY);
        } catch (Exception ignored) {
        }

        this.drawable = drawable;
        setDrawable(drawable);

    }


    @Override
    public int getWidth() {
        return drawable.getIntrinsicWidth() / 2;
    }

    @Override
    public int getHeight() {
        return drawable.getIntrinsicHeight() / 2;
    }

    @Override
    public void release() {
        super.release();
        if (drawable != null) {
            drawable = null;
        }
    }

    @Override
    public void undo() {
        if (canUndo()) {
            currentPosition--;
            DrawableSticker s = (DrawableSticker) listSticker.get(currentPosition);
            this.getMatrix().set(s.getMatrix());
            if (s.isCanChangeColor())
                s.changeColor(s.getCurrentColor());
            setAlpha(s.getDrawable().getAlpha());
        }
    }

    @Override
    public void redo() {
        if (canRedo()) {
            currentPosition++;
            DrawableSticker s = (DrawableSticker) listSticker.get(currentPosition);
            this.getMatrix().set(s.getMatrix());
            if (s.isCanChangeColor())
                s.changeColor(s.getCurrentColor());
            setAlpha(s.getDrawable().getAlpha());
        }
    }

    @Override
    public Sticker createClone() {

        Drawable drwNewCopy = getDrawable().getConstantState().newDrawable().mutate();
        DrawableSticker drawableSticker = new DrawableSticker(drwNewCopy);
        drawableSticker.setMatrix(this.getMatrix());
        drawableSticker.setCanChangeColor(isCanChangeColor());
        drawableSticker.changeColor(currentColor);
        drawableSticker.setAlpha(getDrawable().getAlpha());
        return drawableSticker;
    }
}
