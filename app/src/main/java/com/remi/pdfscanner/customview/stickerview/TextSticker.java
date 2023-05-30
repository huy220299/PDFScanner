//package com.remi.pdfscanner.customview.stickerview;
//
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Matrix;
//import android.graphics.Paint;
//import android.graphics.Path;
//import android.graphics.Rect;
//import android.graphics.RectF;
//import android.graphics.Typeface;
//import android.graphics.drawable.Drawable;
//import android.graphics.drawable.GradientDrawable;
//import android.text.Layout;
//import android.text.StaticLayout;
//import android.text.TextPaint;
//
//import androidx.annotation.Dimension;
//import androidx.annotation.IntRange;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.content.ContextCompat;
//
//import com.remi.pdfscanner.R;
//import com.remi.pdfscanner.util.Common;
//
//
///**
// * Customize your sticker with text and image background.
// * You can place some text into a given region, however,
// * you can also add a plain text sticker. To support text
// * auto resizing , I take most of the code from AutoResizeTextView.
// * See https://adilatwork.blogspot.com/2014/08/android-textview-which-resizes-its-text.html
// * Notice: It's not efficient to add long text due to too much of
// * StaticLayout object allocation.
// * Created by liutao on 30/11/2016.
// */
//
//public class TextSticker extends Sticker {
//
//    /**
//     * Our ellipsis string.
//     */
//    private static final String mEllipsis = "\u2026";
//    private final Context context;
//    private Rect realBounds;
//    private final Rect textRect;
//    private final TextPaint textPaint;
//    private Drawable drawable;
//    private StaticLayout staticLayout;
//    private Layout.Alignment alignment;
//    private String text;
//    private StaticLayout staticLayoutStroke; //to draw Stroke text color
//    private String strokeColor = "";
//    private TextPaint paintStroke;
//    private String style = "fill";
//
//
//    Typeface currentTypeface;
//    String stringTypeface = "";
//
//    String colorShadow = "#000000";
//    int shadowOpacity = 100;
//
//    /**
//     * Upper bounds for text size.
//     * This acts as a starting point for resizing.
//     */
//    private float maxTextSizePixels;
//
//    /**
//     * Lower bounds for text size.
//     */
//    private float minTextSizePixels;
//
//    /**
//     * Line spacing multiplier.
//     */
//    private float lineSpacingMultiplier = 1f;
//
//    /**
//     * Additional line spacing.
//     */
//    private float lineSpacingExtra = 0.0f;
//
//    String currentTextColor = "#000000";
//    float letterSpacing = 0f;
//    int shadowX = 0, shadowY = 0, shadowRadius = 0;
//
//    public TextSticker(@NonNull Context context) {
//        this(context, null);
//        setCanChangeColor(true);
//    }
//
//    public TextSticker(@NonNull Context context, @Nullable Drawable drawable) {
//        this.context = context;
//        this.drawable = drawable;
//        if (drawable == null) {
//            this.drawable = ContextCompat.getDrawable(context, R.drawable.sticker_transparent_background);
//        }
//        textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
//        textPaint.setStrokeWidth(context.getResources().getDimension(com.intuit.sdp.R.dimen._2sdp));
//        paintStroke = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
//        paintStroke.setStyle(Paint.Style.STROKE);
//        paintStroke.setStrokeWidth(context.getResources().getDimension(com.intuit.sdp.R.dimen._1sdp));
//        paintStroke.setColor(Color.BLACK);
//        realBounds = new Rect(0, 0, getWidth(), getHeight());
//        textRect = new Rect(0, 0, getWidth(), getHeight());
//        minTextSizePixels = convertSpToPx(6);
//        maxTextSizePixels = convertSpToPx(32);
//        alignment = Layout.Alignment.ALIGN_CENTER;
//        setCanChangeColor(true);
////    textPaint.setTextSize(maxTextSizePixels);
//    }
//
//    public void setRealBounds(Rect rect) {
//        this.realBounds = rect;
//    }
//
//    public TextPaint getTextPaint() {
//        return textPaint;
//    }
//
//    @Override
//    public void draw(@NonNull Canvas canvas) {
//        if (isArcText) {
//            drawArcText(canvas);
//            return;
//        }
//        Matrix matrix = getMatrix();
//        canvas.save();
//        canvas.concat(matrix);
//        if (drawable != null) {
//            drawable.setBounds(realBounds);
//            drawable.draw(canvas);
//        }
//        canvas.restore();
//
//        canvas.save();
//        canvas.concat(matrix);
//        if (textRect.width() == getWidth()) {
//            int dy = getHeight() / 2 - staticLayout.getHeight() / 2;
//            // center vertical
//            canvas.translate(0, dy);
//        } else {
//            int dx = textRect.left;
//            int dy = textRect.top + textRect.height() / 2 - staticLayout.getHeight() / 2;
//            canvas.translate(dx, dy);
//        }
//        if (staticLayoutStroke != null)
//            staticLayoutStroke.draw(canvas);
//        staticLayout.draw(canvas);
//        canvas.restore();
//
//    }
//
//    Matrix tempMatrix;
//    @Override
//    public void draw(@NonNull Canvas canvas, float scale) {
//        if (isArcText) {
//            drawArcText(canvas);
//            return;
//        }
//        if (tempMatrix==null)
//            tempMatrix = new Matrix();
//        tempMatrix.reset();
//
//        Matrix matrix = getMatrix();
//        tempMatrix.set(matrix);
////        tempMatrix.preScale(1.5f,1.5f);
//        tempMatrix.postScale(scale,scale);
//        canvas.save();
//        canvas.concat(tempMatrix);
//
//        if (drawable != null) {
//            drawable.setBounds(realBounds);
//            drawable.draw(canvas);
//        }
//        canvas.restore();
//
//        canvas.save();
//        canvas.concat(tempMatrix);
//        if (textRect.width() == getWidth()) {
//            int dy = getHeight() / 2 - staticLayout.getHeight() / 2;
//            // center vertical
//            canvas.translate(0, dy);
//        } else {
//            int dx = textRect.left;
//            int dy = textRect.top + textRect.height() / 2 - staticLayout.getHeight() / 2;
//            canvas.translate(dx, dy);
//        }
//        staticLayout.draw(canvas);
//        if (staticLayoutStroke != null)
//            staticLayoutStroke.draw(canvas);
//
//        canvas.restore();
//
//    }
//
//    boolean isArcText = false;
//    Path path;
//    float configArc = 0f;
//
//    public float getConfigArc() {
//        return configArc;
//    }
//
//    public boolean isArcText() {
//        return isArcText;
//    }
//
//    Rect textRect1;
//
//    void drawArcText(Canvas canvas) {
//        if (textRect1 == null)
//            textRect1 = new Rect();
//        Matrix matrix = getMatrix();
//        canvas.save();
//        canvas.concat(matrix);
//        if (drawable != null) {
//            drawable.setBounds(realBounds);
//            drawable.draw(canvas);
//        }
//        canvas.restore();
//
//        canvas.save();
//        canvas.concat(matrix);
//
//        textPaint.getTextBounds(getText(), 0, getText().length(), textRect1);
//
//        if (path == null) {
//            path = new Path();
//        }
//        path.reset();
////        float r = (float) (textRect1.width() / (2 * Math.PI));
////        float bonus1 = configArc;
////        if (configArc < 50) {
////            path.addArc(createRectF(r + bonus1, realBounds.centerX(), (float) (realBounds.centerY() + textRect1.height() / 2f + bonus1)), 270 - calculateAngle(textRect1.width(), bonus1) / 2, calculateAngle(textRect1.width(), bonus1));
////            if (staticLayoutStroke != null)
////                canvas.drawTextOnPath(getText(), path, 0f, 0f, paintStroke);
////            canvas.drawTextOnPath(getText(), path, 0f, 0f, textPaint);
////
////        } else {
////            path.addArc(
////                    createRectF(r + (100 - bonus1), realBounds.centerX(), (realBounds.centerY() - textRect1.height()  - (100 - bonus1))),
////                    90 + calculateAngle(textRect1.width(), (100 - bonus1)) / 2,
////                    -calculateAngle(textRect1.width(), (100 - bonus1)));
////            if (staticLayoutStroke != null)
////                canvas.drawTextOnPath(getText(), path, 0f, textRect1.height(), paintStroke);
////            canvas.drawTextOnPath(getText(), path, 0f, textRect1.height(), textPaint);
////
////        }
//        float bonus1;
//        if (configArc > 0)
//            bonus1 = configArc;
//        else bonus1 = -configArc;
//        float r = (float) (180 * textRect1.width() / (2 * Math.PI * bonus1));
//
//
//        if (configArc > 0) {
//            path.addArc(createRectF(r, realBounds.centerX(), realBounds.centerY() + textRect1.height() / 2f + r), 270 - configArc, configArc * 2);
//            if (staticLayoutStroke != null)
//                canvas.drawTextOnPath(getText(), path, 0f, 0f, paintStroke);
//            canvas.drawTextOnPath(getText(), path, 0f, 0f, textPaint);
//
//        } else {
////            path.addArc(
////                    createRectF(r + (100 - bonus1), realBounds.centerX(), (realBounds.centerY() - textRect1.height() - (100 - bonus1))),
////                    90 + calculateAngle(textRect1.width(), (100 - bonus1)) / 2,
////                    -calculateAngle(textRect1.width(), (100 - bonus1)));
//            path.addArc(createRectF(r, realBounds.centerX(), realBounds.centerY() - textRect1.height() / 2f - r), 90 - configArc, configArc * 2);
//            if (staticLayoutStroke != null)
//                canvas.drawTextOnPath(getText(), path, 0f, textRect1.height(), paintStroke);
//            canvas.drawTextOnPath(getText(), path, 0f, textRect1.height(), textPaint);
//        }
//
//        canvas.restore();
//    }
//
//    public void setArcText(boolean arcText) {
//        isArcText = arcText;
//    }
//
//    public void setConfigArc(float configArc) {
//        isArcText = true;
//        if (configArc == 0f)
//            isArcText = false;
//        else
//            this.configArc = configArc;
//    }
//
//    RectF createRectF(float r, float x, float y) {
//
//        return new RectF(x - r, y - r, x + r, y + r);
//    }
//
//    /**
//     * @param length : length of circle arc
//     * @param bonus: increase of radius
//     * @return angle
//     */
//    float calculateAngle(float length, float bonus) {
//        return (float) (length * 360 / (length + 2 * Math.PI * bonus));
//    }
//
//    @Override
//    public int getWidth() {
//        return drawable.getIntrinsicWidth();
//    }
//
//    @Override
//    public int getHeight() {
//        return drawable.getIntrinsicHeight();
//    }
//
//    @Override
//    public void release() {
//        super.release();
//        if (drawable != null) {
//            drawable = null;
//        }
//    }
//
//
//    @NonNull
//    @Override
//    public TextSticker setAlpha(@IntRange(from = 0, to = 255) int alpha) {
//        textPaint.setAlpha(alpha);
//        paintStroke.setAlpha(alpha);
//        return this;
//    }
//
//    @NonNull
//    @Override
//    public Drawable getDrawable() {
//        return drawable;
//    }
//
//    @Override
//    public TextSticker setDrawable(@NonNull Drawable drawable) {
//        this.drawable = drawable;
//        realBounds.set(0, 0, getWidth(), getHeight());
//        textRect.set(0, 0, getWidth(), getHeight());
//        return this;
//    }
//
//    public TextSticker setDrawable1(@NonNull Drawable drawable) {
//        this.drawable = drawable;
//        realBounds.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
//        textRect.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
//        return this;
//    }
//
//
//    @NonNull
//    public TextSticker setDrawable(@NonNull Drawable drawable, @Nullable Rect region) {
//        this.drawable = drawable;
//        realBounds.set(0, 0, getWidth(), getHeight());
//        if (region == null) {
//            textRect.set(0, 0, getWidth(), getHeight());
//        } else {
//            textRect.set(region.left, region.top, region.right, region.bottom);
//        }
//        return this;
//    }
//
//
//    public TextSticker setTypeface(@Nullable Typeface typeface, String stringTypeface) {
//        currentTypeface = typeface;
//        this.stringTypeface = stringTypeface;
//        textPaint.setTypeface(typeface);
//        paintStroke.setTypeface(typeface);
//        return this;
//    }
//
//    public void setTypeface(@Nullable Typeface typeface) {
//        currentTypeface = typeface;
//        textPaint.setTypeface(typeface);
//        paintStroke.setTypeface(typeface);
//    }
//
//    public String getStringTypeface() {
//        return stringTypeface;
//    }
//
//    public Typeface getCurrentTypeface() {
//        return currentTypeface;
//    }
//
//
//    public String getTextColor() {
//        return currentTextColor;
//    }
//
//    @NonNull
//    public TextSticker setTextColor(String color) {
//        currentTextColor = color;
//        textPaint.setColor(Color.parseColor(currentTextColor));
////    resizeText();
//        return this;
//    }
//
//
//    @NonNull
//    public TextSticker setTextAlign(@NonNull Layout.Alignment alignment) {
//        this.alignment = alignment;
//        createDrawable();
//        return this;
//    }
//
//    @NonNull
//    public TextSticker setMaxTextSize(@Dimension(unit = Dimension.SP) float size) {
//        textPaint.setTextSize(convertSpToPx(size));
//        maxTextSizePixels = textPaint.getTextSize();
//        return this;
//    }
//
//    /**
//     * Sets the lower text size limit
//     *
//     * @param minTextSizeScaledPixels the minimum size to use for text in this view,
//     *                                in scaled pixels.
//     */
//    @NonNull
//    public TextSticker setMinTextSize(float minTextSizeScaledPixels) {
////    minTextSizePixels = convertSpToPx(minTextSizeScaledPixels);
//        minTextSizePixels = (minTextSizeScaledPixels);
//        return this;
//    }
//
//    @NonNull
//    public TextSticker setLineSpacing(float add, float multiplier) {
//        lineSpacingMultiplier = multiplier;
//        lineSpacingExtra = add;
//        return this;
//    }
//
//    public void setLineSpacing(float space) {
//        lineSpacingMultiplier = space;
//        createDrawable();
//    }
//
//    public float getLineSpacingMultiplier() {
//        return lineSpacingMultiplier;
//    }
//
//
//    public float getLetterSpacing() {
//        return letterSpacing;
//    }
//
//    public void setLetterSpace(float space) {
//        letterSpacing = space;
//        textPaint.setLetterSpacing(space);
//        paintStroke.setLetterSpacing(space);
//        createDrawable();
//    }
//
//
//    public int getShadowOpacity() {
//        return shadowOpacity;
//    }
//
//    public void setShadowOpacity(int shadowOpacity) {
//        this.shadowOpacity = shadowOpacity;
//
//        if (shadowOpacity != 100)
//            textPaint.setShadowLayer(shadowRadius, shadowX, shadowY, Color.parseColor(Common.INSTANCE.convertOpacityToStringColor(shadowOpacity, colorShadow)));
//        else
//            textPaint.setShadowLayer(shadowRadius, shadowX, shadowY, Color.parseColor(colorShadow));
//    }
//
//    public String getColorShadow() {
//        return colorShadow;
//    }
//
//    public void setColorShadow(String colorShadow) {
//        this.colorShadow = colorShadow;
//        shadowOpacity = 100;
//        textPaint.setShadowLayer(shadowRadius, shadowX, shadowY, Color.parseColor(colorShadow));
//    }
//
//    public void setShadow(int x, int y, int radius) {
//        shadowX = x;
//        shadowY = y;
//        shadowRadius = radius;
//        if (shadowOpacity != 100)
//            textPaint.setShadowLayer(shadowRadius, shadowX, shadowY, Color.parseColor(Common.INSTANCE.convertOpacityToStringColor(shadowOpacity, colorShadow)));
//        else
//            textPaint.setShadowLayer(shadowRadius, shadowX, shadowY, Color.parseColor(colorShadow));
//    }
//
//    public void setShadowRadius(int shadowRadius) {
//        this.shadowRadius = shadowRadius;
//        if (shadowOpacity != 100)
//            textPaint.setShadowLayer(shadowRadius, shadowX, shadowY, Color.parseColor(Common.INSTANCE.convertOpacityToStringColor(shadowOpacity, colorShadow)));
//        else
//            textPaint.setShadowLayer(shadowRadius, shadowX, shadowY, Color.parseColor(colorShadow));
//    }
//
//    public void setShadowX(int shadowX) {
//        this.shadowX = shadowX;
//        if (shadowOpacity != 100)
//            textPaint.setShadowLayer(shadowRadius, shadowX, shadowY, Color.parseColor(Common.INSTANCE.convertOpacityToStringColor(shadowOpacity, colorShadow)));
//        else
//            textPaint.setShadowLayer(shadowRadius, shadowX, shadowY, Color.parseColor(colorShadow));
//    }
//
//    public void setShadowY(int shadowY) {
//        this.shadowY = shadowY;
//        if (shadowOpacity != 100)
//            textPaint.setShadowLayer(shadowRadius, shadowX, shadowY, Color.parseColor(Common.INSTANCE.convertOpacityToStringColor(shadowOpacity, colorShadow)));
//        else
//            textPaint.setShadowLayer(shadowRadius, shadowX, shadowY, Color.parseColor(colorShadow));
//    }
//
//    public int getShadowX() {
//        return shadowX;
//    }
//
//    public int getShadowY() {
//        return shadowY;
//    }
//
//    public int getShadowRadius() {
//        return shadowRadius;
//    }
//
//    @NonNull
//    public TextSticker setText(@Nullable String text) {
//        this.text = text;
//        createDrawable();
//        return this;
//    }
//
//    public void editText(String text) {
//        this.text = text;
//        createDrawable();
//    }
//
//    @Nullable
//    public String getText() {
//        return text;
//    }
//
//    /**
//     * Resize this view's text size with respect to its width and height
//     * (minus padding). You should always call this method after the initialization.
//     */
//    @NonNull
//    public TextSticker resizeText() {
//        final int availableHeightPixels = textRect.height();
//
//        final int availableWidthPixels = textRect.width();
//
//        final CharSequence text = getText();
//
//        // Safety check
//        // (Do not resize if the view does not have dimensions or if there is no text)
//        if (text == null
//                || text.length() <= 0
//                || availableHeightPixels <= 0
//                || availableWidthPixels <= 0
//                || maxTextSizePixels <= 0) {
//            return this;
//        }
//
//        float targetTextSizePixels = maxTextSizePixels;
//        int targetTextHeightPixels =
//                getTextHeightPixels(text, availableWidthPixels, targetTextSizePixels);
//
//        // Until we either fit within our TextView
//        // or we have reached our minimum text size,
//        // incrementally try smaller sizes
//        while (targetTextHeightPixels > availableHeightPixels
//                && targetTextSizePixels > minTextSizePixels) {
//            targetTextSizePixels = Math.max(targetTextSizePixels - 2, minTextSizePixels);
//
//            targetTextHeightPixels =
//                    getTextHeightPixels(text, availableWidthPixels, targetTextSizePixels);
//        }
//
//        // If we have reached our minimum text size and the text still doesn't fit,
//        // append an ellipsis
//        // (NOTE: Auto-ellipsize doesn't work hence why we have to do it here)
//        if (targetTextSizePixels == minTextSizePixels
//                && targetTextHeightPixels > availableHeightPixels) {
//            // Make a copy of the original TextPaint object for measuring
//            TextPaint textPaintCopy = new TextPaint(textPaint);
//            textPaintCopy.setTextSize(targetTextSizePixels);
//
//            // Measure using a StaticLayout instance
//            StaticLayout staticLayout =
//                    new StaticLayout(text, textPaintCopy, availableWidthPixels, Layout.Alignment.ALIGN_NORMAL,
//                            lineSpacingMultiplier, lineSpacingExtra, false);
//
//            // Check that we have a least one line of rendered text
//            if (staticLayout.getLineCount() > 0) {
//                // Since the line at the specific vertical position would be cut off,
//                // we must trim up to the previous line and add an ellipsis
//                int lastLine = staticLayout.getLineForVertical(availableHeightPixels) - 1;
//
//                if (lastLine >= 0) {
//                    int startOffset = staticLayout.getLineStart(lastLine);
//                    int endOffset = staticLayout.getLineEnd(lastLine);
//                    float lineWidthPixels = staticLayout.getLineWidth(lastLine);
//                    float ellipseWidth = textPaintCopy.measureText(mEllipsis);
//
//                    // Trim characters off until we have enough room to draw the ellipsis
//                    while (availableWidthPixels < lineWidthPixels + ellipseWidth) {
//                        endOffset--;
//                        lineWidthPixels =
//                                textPaintCopy.measureText(text.subSequence(startOffset, endOffset + 1).toString());
//                    }
//
//                    setText(text.subSequence(0, endOffset) + mEllipsis);
//                }
//            }
//        }
//        textPaint.setTextSize(targetTextSizePixels);
//        paintStroke.setTextSize(targetTextSizePixels);
//        staticLayout = new StaticLayout(this.text, textPaint, textRect.width(), alignment, lineSpacingMultiplier, lineSpacingExtra, true);
//        if (!strokeColor.equalsIgnoreCase(""))
//            staticLayoutStroke = new StaticLayout(this.text, paintStroke, textRect.width(), alignment, lineSpacingMultiplier, lineSpacingExtra, true);
//        return this;
//    }
//
//    public TextSticker setupText() {
//        staticLayout = new StaticLayout(this.text, textPaint, textRect.width(), alignment, lineSpacingMultiplier, lineSpacingExtra, true);
//        if (!strokeColor.equalsIgnoreCase(""))
//            staticLayoutStroke = new StaticLayout(this.text, paintStroke, textRect.width(), alignment, lineSpacingMultiplier, lineSpacingExtra, true);
//        return this;
//    }
//
//    float currentTextSize = minTextSizePixels;
//
//    public void setTextSize(float size) {
//        currentTextSize = size;
//        textPaint.setTextSize(size);
//        paintStroke.setTextSize(size);
//        createDrawable();
//    }
//
//    public float getCurrentTextSize() {
//        return currentTextSize;
//    }
//
//    /**
//     * @return lower text size limit, in pixels.
//     */
//    public float getMinTextSizePixels() {
//        return minTextSizePixels;
//    }
//
//    public float getMaxTextSizePixels() {
//        return maxTextSizePixels;
//    }
//
//
//    /**
//     * Sets the text size of a clone of the view's {@link TextPaint} object
//     * and uses a {@link StaticLayout} instance to measure the height of the text.
//     *
//     * @return the height of the text when placed in a view
//     * with the specified width
//     * and when the text has the specified size.
//     */
//    protected int getTextHeightPixels(@NonNull CharSequence source, int availableWidthPixels,
//                                      float textSizePixels) {
//        textPaint.setTextSize(textSizePixels);
//        // It's not efficient to create a StaticLayout instance
//        // every time when measuring, we can use StaticLayout.Builder
//        // since api 23.
//        StaticLayout staticLayout = new StaticLayout(source, textPaint, availableWidthPixels, Layout.Alignment.ALIGN_NORMAL, lineSpacingMultiplier, lineSpacingExtra, true);
//        return staticLayout.getHeight();
//    }
//
//    /**
//     * @return the number of pixels which scaledPixels corresponds to on the device.
//     */
//    private float convertSpToPx(float scaledPixels) {
//        return scaledPixels * context.getResources().getDisplayMetrics().scaledDensity;
//    }
//
//    public String getAlignment() {
//        return alignment.name();
//    }
//
//    void createDrawable() {
//        String str = getText();
//        int line = 1;
//        int max = 0;
//
//        if (str != null) {
//            String[] temp = getText().split("\n");
//            line = temp.length;
//            if (temp.length == 1)
//                max = str.length();
//            for (String s : temp) {
//                if (s.length() > max)
//                    max = s.length();
//            }
//        }
//        textPaint.getTextBounds(str, 0, str.length(), textRect);
//
//        float w;
//
//        if (line > 1)
//            w = (textRect.width() * 1.1f * (max + 1) / str.length());
//        else
//            w = (textRect.width() * 1.1f * (max) / str.length());
//
//        float h = (textRect.height() * 1.2f * line);
//
//        if (h < 50)
//            h = 50;
//
//        if (line == 1 && text.length() > 32 && !isOneLine) {
//            w = 300;
//            h=300;
//
//            GradientDrawable drawable = new GradientDrawable();
//            drawable.setSize((int) w, (int) h);
//            drawable.setColor(Color.TRANSPARENT);
//            setDrawable1(drawable);
//
//        } else {
//            GradientDrawable drawable = new GradientDrawable();
//            drawable.setSize((int) w, (int) h);
//            drawable.setColor(Color.TRANSPARENT);
//            setDrawable(drawable);
//
//        }
//        if (lineSpacingMultiplier==0f)
//            lineSpacingMultiplier=1f;
//        staticLayout =
//                new StaticLayout(this.text, textPaint, textRect.width(), alignment, lineSpacingMultiplier,
//                        lineSpacingExtra, true);
//
//        if (!strokeColor.equalsIgnoreCase("")) {
//            staticLayoutStroke = new StaticLayout(this.text, paintStroke, textRect.width(),
//                    alignment, lineSpacingMultiplier, lineSpacingExtra, true);
//        } else
//            staticLayoutStroke = null;
//
//    }
//
//    boolean isOneLine = false;
//
//    public void setOneLine(boolean oneLine) {
//        isOneLine = oneLine;
//    }
//
//    public void setStrokeColor(String strokeColor) {
//        this.strokeColor = strokeColor;
//        if (!strokeColor.equalsIgnoreCase("")) {
//            paintStroke.setColor(Color.parseColor(strokeColor));
//            createDrawable();
//        }
//
//    }
//
//    public String getStrokeColor() {
//        return strokeColor;
//    }
//
//    String handleText(String text) {
//        String result = text;
//        return result;
//    }
//
//
//    private boolean isBold = false, isItalic = false, isUnder = false;
//
//    public boolean isUnder() {
//        return isUnder;
//    }
//
//    public void setUnder(boolean under) {
//        isUnder = under;
//        if (isUnder)
//            textPaint.setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
//        else
//            textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
//    }
//
//    public boolean isBold() {
//        return isBold;
//    }
//
//    public void setBold(boolean bold) {
//        isBold = bold;
//    }
//
//    public boolean isItalic() {
//        return isItalic;
//    }
//
//    public void setItalic(boolean italic) {
//        isItalic = italic;
//    }
//
//    public void setStyle(String style) {
//        this.style = style;
//        if (style.equalsIgnoreCase("stroke"))
//            textPaint.setStyle(Paint.Style.STROKE);
//        else
//            textPaint.setStyle(Paint.Style.FILL);
//    }
//
//    public String getStyle() {
//        return style;
//    }
//
//    @Override
//    public void undo() {
//        if (canUndo()) {
//            currentPosition--;
//            TextSticker s = (TextSticker) listSticker.get(currentPosition);
//            setUpSticker(s);
//        }
//    }
//
//    @Override
//    public void redo() {
//        if (canRedo()) {
//            currentPosition++;
//            TextSticker s = (TextSticker) listSticker.get(currentPosition);
//            setUpSticker(s);
//        }
//    }
//
//    void setUpSticker(TextSticker s) {
//        setText(s.getText());
//        getMatrix().set(s.getMatrix());
//        setTypeface(s.getCurrentTypeface(), s.getStringTypeface());
//        setTextColor(s.getTextColor());
//        if (!s.getStrokeColor().equalsIgnoreCase(""))
//            setStrokeColor(s.getStrokeColor());
//        setAlpha(s.getTextPaint().getAlpha());
////        setTextAlign(s.getAlignment());
//        setLineSpacing(s.getLineSpacingMultiplier());
//        setLetterSpace(s.getLetterSpacing());
//        setShadowOpacity(s.getShadowOpacity());
//        setShadow(s.getShadowX(), s.getShadowY(), s.getShadowRadius());
//        setConfigArc(s.getConfigArc());
//        setArcText(s.isArcText());
//        setUnder(s.isUnder());
//        setBold(s.isBold());
//        setItalic(s.isItalic());
//    }
//
//    @Override
//    public Sticker createClone() {
//        TextSticker sticker = new TextSticker(context);
//        sticker.setText(getText());
//        sticker.setTextSize(textPaint.getTextSize());
//        sticker.setAlpha(textPaint.getAlpha());
//        sticker.setTextAlign(alignment);
//        sticker.setLineSpacing(lineSpacingMultiplier);
//        sticker.setLetterSpace(letterSpacing);
//        sticker.setMatrix(getMatrix());
//        sticker.setTextColor(currentTextColor);
//        sticker.setShadow(shadowX, shadowY, shadowRadius);
//        sticker.setShadowOpacity(shadowOpacity);
//        sticker.setTypeface(currentTypeface, stringTypeface);
//        sticker.setStrokeColor(strokeColor);
//        sticker.setConfigArc(configArc);
//        sticker.setArcText(isArcText);
//        sticker.setUnder(isUnder);
//        sticker.setBold(isBold);
//        sticker.setItalic(isItalic);
//        return sticker;
//    }
//}
