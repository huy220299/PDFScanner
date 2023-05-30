package com.remi.pdfscanner.customview.stickerview;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewCompat;


import com.remi.pdfscanner.R;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Sticker View
 *
 * @author wupanjie
 */
public class StickerView extends FrameLayout {
    private String lastDoing = "";
    public  final int MODE_BACKGROUND = -1;
    public  final int MODE_GRADIENT = 0;
    public  final int MODE_COLOR = 1;

    private ChangeColorEvent changeColorEvent;
    public boolean showIcons;
    public boolean showBorder;
    private boolean showGrid;
    private final boolean bringToFrontCurrentSticker;
    private Paint paintGrid, paintRound;
    private Path dashPath;
    private Bitmap bitmapBackground;
    Paint paintBitmap;
    Rect rectBackground;
    public int MODE_TEST = MODE_COLOR;
    Paint paintGradient;
    private String startColor = "#FFFFFF", endColor = "#FFFFFF";
    int angle = 30;

    public int getAngle() {
        return angle;
    }

    LinearGradient linearGradient;
    private String backgroundColor1 = "#00000000";

    public String getStartColor() {
        return startColor;
    }

    public String getEndColor() {
        return endColor;
    }

    public String getBackgroundColor1() {
        return backgroundColor1;
    }

    public void setBackgroundColor1(String backgroundColor) {
        this.backgroundColor1 = backgroundColor;
        MODE_TEST = MODE_COLOR;
        invalidate();
    }

    public void setMode(int mode) {
        MODE_TEST = mode;
        invalidate();
    }

    public void setShowGrid(boolean is) {
        this.showGrid = is;
        invalidate();
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setStartColor(String startColor) {
        this.startColor = startColor;
        MODE_TEST = MODE_GRADIENT;
        if (getWidth() > 0) {
            float range = (float) (getWidth() * Math.sqrt(2));
            linearGradient = new LinearGradient(range / 2f, 0, range / 2f, range, Color.parseColor(startColor), Color.parseColor(endColor), Shader.TileMode.REPEAT);
            paintGradient.setShader(linearGradient);
        }
        invalidate();
    }

    public void setAngle(int angle) {
        MODE_TEST = MODE_GRADIENT;
        this.angle = angle;
        invalidate();
    }

    public void setEndColor(String endColor) {
        this.endColor = endColor;
        MODE_TEST = MODE_GRADIENT;
        if (getWidth() > 0) {
            float range = (float) (getWidth() * Math.sqrt(2));
            linearGradient = new LinearGradient(range / 2f, 0, range / 2f, range, Color.parseColor(startColor), Color.parseColor(endColor), Shader.TileMode.REPEAT);
            paintGradient.setShader(linearGradient);
        }
        invalidate();
    }
    public void setGradientBackground(String startColor, String endColor, int angle){
        this.startColor = startColor;
        this.endColor = endColor;
        this.angle = angle;
        MODE_TEST = MODE_GRADIENT;
        if (getWidth() > 0) {
            float range = (float) (getWidth() * Math.sqrt(2));
            linearGradient = new LinearGradient(range / 2f, 0, range / 2f, range, Color.parseColor(startColor), Color.parseColor(endColor), Shader.TileMode.REPEAT);
            paintGradient.setShader(linearGradient);
        }
        invalidate();
    }

    public void setBitmapBackground(Bitmap bitmapBackground) {
        this.bitmapBackground = bitmapBackground;
        MODE_TEST = MODE_BACKGROUND;
        invalidate();
    }






    public void setChangeColorEvent(ChangeColorEvent changeColorEvent) {
        this.changeColorEvent = changeColorEvent;
    }

    public void onChangeColor() {
        if (changeColorEvent != null) {
            changeColorEvent.onChange();
        }


    }


    @IntDef({
            ActionMode.NONE, ActionMode.DRAG, ActionMode.ZOOM_WITH_TWO_FINGER, ActionMode.ICON,
            ActionMode.CLICK
    })
    @Retention(RetentionPolicy.SOURCE)
    protected @interface ActionMode {
        int NONE = 0;
        int DRAG = 1;
        int ZOOM_WITH_TWO_FINGER = 2;
        int ICON = 3;
        int CLICK = 4;
    }

    @IntDef(flag = true, value = {FLIP_HORIZONTALLY, FLIP_VERTICALLY})
    @Retention(RetentionPolicy.SOURCE)
    protected @interface Flip {
    }

    private static final String TAG = "StickerView";

    private static final int DEFAULT_MIN_CLICK_DELAY_TIME = 200;

    public static final int FLIP_HORIZONTALLY = 1;
    public static final int FLIP_VERTICALLY = 1 << 1;

    private List<Sticker> stickers = new ArrayList<>();
    private final List<BitmapStickerIcon> icons = new ArrayList<>(4);

    private final Paint borderPaint = new Paint();
    private final RectF stickerRect = new RectF();

    private final Matrix sizeMatrix = new Matrix();
    private final Matrix downMatrix = new Matrix();
    private final Matrix moveMatrix = new Matrix();

    // region storing variables
    private final float[] bitmapPoints = new float[8];
    private final float[] bounds = new float[8];
    private final float[] point = new float[2];
    private final PointF currentCenterPoint = new PointF();
    private final float[] tmp = new float[2];
    private PointF midPoint = new PointF();
    // endregion
    private final int touchSlop;

    private BitmapStickerIcon currentIcon;
    //the first point down position
    private float downX;
    private float downY;

    private float oldDistance = 0f;
    private float oldRotation = 0f;

    @ActionMode
    private int currentMode = ActionMode.NONE;

    private Sticker handlingSticker;

    private boolean locked;
    private boolean constrained;

    private OnStickerOperationListener onStickerOperationListener;

    private long lastClickTime = 0;
    private int minClickDelayTime = DEFAULT_MIN_CLICK_DELAY_TIME;

    void init() {
        paintGradient = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintGrid = new Paint(Paint.ANTI_ALIAS_FLAG);
        dashPath = new Path();
        paintGrid.setStyle(Paint.Style.STROKE);
        paintGrid.setStrokeWidth(getResources().getDimension(com.intuit.sdp.R.dimen._1sdp));
//        paintGrid.setPathEffect(new DashPathEffect(new float[]{10, 10}, 5));

        paintRound = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintRound.setStyle(Paint.Style.STROKE);
        paintRound.setColor(Color.BLACK);
        paintRound.setStrokeWidth(getResources().getDimension(com.intuit.sdp.R.dimen._1sdp));
    }

    public StickerView(Context context) {
        this(context, null);
        init();
    }

    public StickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public StickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        TypedArray a = null;
        try {
            a = context.obtainStyledAttributes(attrs, R.styleable.StickerView);
            showIcons = a.getBoolean(R.styleable.StickerView_showIcons, false);
            showBorder = a.getBoolean(R.styleable.StickerView_showBorder, false);
            showGrid = a.getBoolean(R.styleable.StickerView_showGrid, false);
            bringToFrontCurrentSticker =
                    a.getBoolean(R.styleable.StickerView_bringToFrontCurrentSticker, false);

            borderPaint.setAntiAlias(true);
            borderPaint.setColor(a.getColor(R.styleable.StickerView_borderColor, Color.BLACK));
            borderPaint.setAlpha(a.getInteger(R.styleable.StickerView_borderAlpha, 255));
            borderPaint.setStrokeWidth(getContext().getResources().getDimension(com.intuit.sdp.R.dimen._1sdp));

            configDefaultIcons();
        } finally {
            if (a != null) {
                a.recycle();
            }
        }
    }

    public void configDefaultIcons() {
        BitmapStickerIcon deleteIcon = new BitmapStickerIcon(
                ContextCompat.getDrawable(getContext(), R.drawable.sticker_ic_delete),
                BitmapStickerIcon.LEFT_TOP);
        deleteIcon.setIconEvent(new DeleteIconEvent());
        BitmapStickerIcon zoomIcon = new BitmapStickerIcon(
                ContextCompat.getDrawable(getContext(), R.drawable.sticker_ic_scale),
                BitmapStickerIcon.RIGHT_BOTOM);
        zoomIcon.setIconEvent(new ZoomIconEvent());
        BitmapStickerIcon flipIcon = new BitmapStickerIcon(
                ContextCompat.getDrawable(getContext(), R.drawable.sticker_ic_flip_horizontal),
                BitmapStickerIcon.RIGHT_TOP);
        flipIcon.setIconEvent(new FlipHorizontallyEvent());

        BitmapStickerIcon changeColorIcon = new BitmapStickerIcon(
                ContextCompat.getDrawable(getContext(), R.drawable.sticker_ic_flip_horizontal),
                BitmapStickerIcon.LEFT_BOTTOM);
        changeColorIcon.setIconEvent(new onChangeColorStickerEvent());

        icons.clear();
        icons.add(deleteIcon);
        icons.add(zoomIcon);
        icons.add(flipIcon);
//        icons.add(changeColorIcon);
    }

    public void setStickers(List<Sticker> list) {
        this.stickers = list;
        invalidate();
    }

    /**
     * Swaps sticker at layer [[oldPos]] with the one at layer [[newPos]].
     * Does nothing if either of the specified layers doesn't exist.
     */
    public void swapLayers(int oldPos, int newPos) {
        try {
            if (stickers.size() >= oldPos && stickers.size() >= newPos) {
                Collections.swap(stickers, oldPos, newPos);
                invalidate();
            }
        } catch (Exception e) {
        }
    }

    public void moveToBottom(Sticker sticker) {
        stickers.remove(sticker);
        stickers.add(sticker);
        invalidate();
    }

    /**
     * Sends sticker from layer [[oldPos]] to layer [[newPos]].
     * Does nothing if either of the specified layers doesn't exist.
     */
    public void sendToLayer(int oldPos, int newPos) {
        if (stickers.size() >= oldPos && stickers.size() >= newPos) {
            Sticker s = stickers.get(oldPos);
            stickers.remove(oldPos);
            stickers.add(newPos, s);
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            stickerRect.left = left;
            stickerRect.top = top;
            stickerRect.right = right;
            stickerRect.bottom = bottom;
        }
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (!isRemoveBackground) {
            if (MODE_TEST == MODE_GRADIENT  && paintGradient != null) {
                if (linearGradient==null){
                        float range = (float) (getWidth() * Math.sqrt(2));
                        linearGradient = new LinearGradient(range / 2f, 0, range / 2f, range, Color.parseColor(startColor), Color.parseColor(endColor), Shader.TileMode.REPEAT);
                        paintGradient.setShader(linearGradient);
                    }
                canvas.save();
                float temp = (float) (getWidth() * (Math.sqrt(2) - 1) / 2f);
                canvas.rotate(angle, getWidth() / 2f, getHeight() / 2f);
                canvas.translate(-temp, -temp);

                canvas.drawPaint(paintGradient);
                canvas.restore();

            } else if (MODE_TEST == MODE_COLOR) {
                canvas.drawColor(Color.parseColor(backgroundColor1));
            } else {
                if (bitmapBackground != null) {
                    if (paintBitmap == null)
                        paintBitmap = new Paint(Paint.FILTER_BITMAP_FLAG);
                    if (rectBackground == null)
                        rectBackground = new Rect();
                    if (bitmapBackground.getWidth() < bitmapBackground.getHeight())
                        rectBackground.set(0, (bitmapBackground.getHeight() - bitmapBackground.getWidth()) / 2, bitmapBackground.getWidth(), (bitmapBackground.getHeight() + bitmapBackground.getWidth()) / 2);
                    else
                        rectBackground.set((-bitmapBackground.getHeight() + bitmapBackground.getWidth()) / 2, 0, (bitmapBackground.getHeight() + bitmapBackground.getWidth()) / 2, bitmapBackground.getHeight());

                    canvas.drawBitmap(bitmapBackground, rectBackground, canvas.getClipBounds(), paintBitmap);
                }

            }
        }

        drawStickers(canvas);
        if (isGetBitmap)
            return;
        if (showGrid) {
            drawGrid(canvas);
        } else {
            drawFrameRound(canvas);
        }
        isRemoveBackground = false;
        isGetBitmap = false;
    }

    boolean isGetBitmap = false;



    public void setGetBitmap(boolean getBitmap) {
        isGetBitmap = getBitmap;
        isRemoveBackground = false;
    }

    Path pathCover;

    int margin = 0;

    protected void drawStickers(Canvas canvas) {

        for (int i = 0; i < stickers.size(); i++) {
            Sticker sticker = stickers.get(i);
            if (sticker != null && sticker.getVisibility()) {
//                if (sticker.getListSticker()!=null&& sticker.getListSticker().get(sticker.currentPosition) != null)
//                    sticker.getListSticker().get(sticker.currentPosition).draw(canvas);
//                else
                sticker.draw(canvas);
            }
        }

        if (handlingSticker != null && !locked && (showBorder || showIcons)) {

            getStickerPoints(handlingSticker, bitmapPoints);
            if (pathCover == null)
                pathCover = new Path();

//            if (handlingSticker instanceof DrawableSticker)
//                drawBoundDrawableSticker(canvas);
//            else if (handlingSticker instanceof TextSticker)
//                drawBoundTextSticker(canvas);
            drawBoundTextSticker(canvas);

        }
    }


    void drawFrameRound(Canvas canvas) {



//        canvas.drawRect(canvas.getClipBounds(), paintRound);
    }

    void drawGrid(Canvas canvas) {

        paintGrid.setStyle(Paint.Style.STROKE);
        paintGrid.setStrokeWidth(getResources().getDimension(com.intuit.sdp.R.dimen._1sdp));
        paintGrid.setColor(Color.BLACK);

        for (int i = 0; i <= 9; i++) {
            dashPath.reset();
            dashPath.moveTo(stickerRect.width() * i / 9f, 0);
            dashPath.lineTo(stickerRect.width() * i / 9f, stickerRect.height());
            canvas.drawPath(dashPath, paintGrid);
        }
        for (int i = 0; i <= 9; i++) {
            dashPath.reset();
            dashPath.moveTo(0, stickerRect.height() * i / 9f);
            dashPath.lineTo(stickerRect.width(), stickerRect.height() * i / 9f);
            canvas.drawPath(dashPath, paintGrid);
        }
    }

    void drawBoundDrawableSticker(Canvas canvas) {

        margin = icons.get(0).getWidth();
        float angle = handlingSticker.getCurrentAngle();


        float x1 = bitmapPoints[0];
        float y1 = bitmapPoints[1];
        float x2 = bitmapPoints[2];
        float y2 = bitmapPoints[3];
        float x3 = bitmapPoints[4];
        float y3 = bitmapPoints[5];
        float x4 = bitmapPoints[6];
        float y4 = bitmapPoints[7];


        if (showBorder) {
            borderPaint.setColor(Color.parseColor("#979797"));
            borderPaint.setStyle(Paint.Style.STROKE);
            canvas.save();
            RectF rectF = new RectF(-margin, -margin, handlingSticker.getCurrentWidth() + margin, handlingSticker.getCurrentHeight() + margin);
            canvas.translate(x1, y1);
            if (handlingSticker.isFlippedVertically())
                angle = -handlingSticker.getCurrentAngle();
            if (handlingSticker.isFlippedHorizontally()) {
                if (angle > 0)
                    angle = 180 - angle;
                else
                    angle = -180 - angle;
            }

            canvas.rotate(angle);
            canvas.drawRect(rectF, borderPaint);
            canvas.restore();
        }

        //draw icons
        if (showIcons) {
            float rotation = calculateRotation(x4, y4, x3, y3);
            for (int i = 0; i < icons.size(); i++) {
                BitmapStickerIcon icon = icons.get(i);
                switch (icon.getPosition()) {
                    case BitmapStickerIcon.LEFT_TOP:
//                            configIconMatrix(icon, x1, y1, rotation, margin);
//                            icon.draw(canvas, borderPaint);
                        icon.draw(canvas, borderPaint, handlingSticker, margin, margin, x1, y1);
                        break;

                    case BitmapStickerIcon.RIGHT_TOP:
//                            configIconMatrix(icon, x2, y2, rotation);
//                            icon.draw(canvas, borderPaint);
                        icon.draw(canvas, borderPaint, handlingSticker, -margin, margin, x2, y2);
                        break;

                    case BitmapStickerIcon.LEFT_BOTTOM:
//                            configIconMatrix(icon, x3, y3, rotation);
                        if (handlingSticker.isCanChangeColor()) {
//                                icon.draw(canvas, borderPaint);
                            icon.draw(canvas, borderPaint, handlingSticker, margin, -margin, x3, y3);
                        }
                        break;
                    case BitmapStickerIcon.RIGHT_BOTOM:
//                            configIconMatrix(icon, x4, y4, rotation);
//                            icon.draw(canvas, borderPaint);
                        icon.draw(canvas, borderPaint, handlingSticker, -margin, -margin, x4, y4);
                        break;
                }


            }
        }
    }

    void drawBoundTextSticker(Canvas canvas) {
        float x1 = bitmapPoints[0];
        float y1 = bitmapPoints[1];
        float x2 = bitmapPoints[2];
        float y2 = bitmapPoints[3];
        float x3 = bitmapPoints[4];
        float y3 = bitmapPoints[5];
        float x4 = bitmapPoints[6];
        float y4 = bitmapPoints[7];

        if (showBorder) {
//            canvas.drawLine(x1, y1, x2, y2, borderPaint);
//            canvas.drawLine(x1, y1, x3, y3, borderPaint);
//            canvas.drawLine(x2, y2, x4, y4, borderPaint);
//            canvas.drawLine(x4, y4, x3, y3, borderPaint);

//            canvas.drawLine(x1, y1, x4, y4, borderPaint);
//            canvas.drawLine(x2, y2, x3, y3, borderPaint);
            dashPath.reset();
            paintGrid.setColor(Color.BLACK);
            dashPath.moveTo(x1,y1);
            dashPath.lineTo(x2,y2);
            dashPath.lineTo(x4,y4);
            dashPath.lineTo(x3,y3);
            dashPath.lineTo(x1,y1);
            canvas.drawPath(dashPath,paintGrid);

        }

        //draw icons
        if (showIcons) {
            float rotation = calculateRotation(x4, y4, x3, y3);
            for (int i = 0; i < icons.size(); i++) {
                BitmapStickerIcon icon = icons.get(i);
                switch (icon.getPosition()) {
                    case BitmapStickerIcon.LEFT_TOP:
                        configIconMatrix(icon, x1, y1, rotation);
                        icon.draw(canvas, borderPaint);
                        break;

                    case BitmapStickerIcon.RIGHT_TOP:
                        configIconMatrix(icon, x2, y2, rotation);
                        icon.draw(canvas, borderPaint);
                        break;

                    case BitmapStickerIcon.LEFT_BOTTOM:
                        configIconMatrix(icon, x3, y3, rotation);
                        if (handlingSticker.isCanChangeColor()) {
                            icon.draw(canvas, borderPaint);
                        }
                        break;
                    case BitmapStickerIcon.RIGHT_BOTOM:

                        configIconMatrix(icon, x4, y4, rotation);
                        icon.draw(canvas, borderPaint);
                        break;
                }


            }
        }
    }

    protected void configIconMatrix(@NonNull BitmapStickerIcon icon, float x, float y,
                                    float rotation) {
        icon.setX(x);
        icon.setY(y);
        icon.getMatrix().reset();

        icon.getMatrix().postRotate(rotation, icon.getWidth() / 2, icon.getHeight() / 2);
        icon.getMatrix().postTranslate(x - icon.getWidth() / 2, y - icon.getHeight() / 2);
    }

    protected void configIconMatrix(@NonNull BitmapStickerIcon icon, float x, float y,
                                    float rotation, int margin) {
        icon.setX(x);
        icon.setY(y);
        icon.getMatrix().reset();

        icon.getMatrix().postRotate(rotation, icon.getWidth() / 2, icon.getHeight() / 2);
        icon.getMatrix().postTranslate(x - icon.getWidth() / 2 - margin, y - icon.getHeight() / 2 - margin);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (locked) return super.onInterceptTouchEvent(ev);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();

                return findCurrentIconTouched() != null || findHandlingSticker() != null;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (locked) {
            return super.onTouchEvent(event);
        }
        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!onTouchDown(event)) {
                    return false;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDistance = calculateDistance(event);
                oldRotation = calculateRotation(event);

                midPoint = calculateMidPoint(event);

                if (handlingSticker != null && isInStickerArea(handlingSticker, event.getX(1),
                        event.getY(1)) && findCurrentIconTouched() == null) {
                    currentMode = ActionMode.ZOOM_WITH_TWO_FINGER;
                }

                break;

            case MotionEvent.ACTION_MOVE:
                handleCurrentMode(event);
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                onTouchUp(event);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if (currentMode == ActionMode.ZOOM_WITH_TWO_FINGER && handlingSticker != null) {
                    if (onStickerOperationListener != null) {
                        onStickerOperationListener.onStickerZoomFinished(handlingSticker);
                    }
                }
                currentMode = ActionMode.NONE;
                break;
        }

        return true;
    }

    /**
     * @param event MotionEvent received from {@link #onTouchEvent)
     * @return true if has touch something
     */
    protected boolean onTouchDown(@NonNull MotionEvent event) {

        currentMode = ActionMode.DRAG;

        downX = event.getX();
        downY = event.getY();

        midPoint = calculateMidPoint();
        oldDistance = calculateDistance(midPoint.x, midPoint.y, downX, downY);
        oldRotation = calculateRotation(midPoint.x, midPoint.y, downX, downY);

        currentIcon = findCurrentIconTouched();
        if (currentIcon != null) {
            currentMode = ActionMode.ICON;
            currentIcon.onActionDown(this, event);
        } else {
            handlingSticker = findHandlingSticker();
        }

        if (handlingSticker != null) {
            downMatrix.set(handlingSticker.getMatrix());
//            showIcons = true;
//            showBorder = true;
            if (bringToFrontCurrentSticker) {
                stickers.remove(handlingSticker);
                stickers.add(handlingSticker);
            }
            if (onStickerOperationListener != null) {
//        onStickerOperationListener.onStickerTouchedDown(handlingSticker);
            }
        }

        if (currentIcon == null && handlingSticker == null) {
            return false;
        }
        invalidate();
        return true;
    }

    protected void onTouchUp(@NonNull MotionEvent event) {
        long currentTime = SystemClock.uptimeMillis();

        if (currentMode == ActionMode.ICON && currentIcon != null && handlingSticker != null) {
            if (onStickerOperationListener != null) {
                onStickerOperationListener.onStickerZoomFinished(handlingSticker);
            }
            currentIcon.onActionUp(this, event);
        }

        if (currentMode == ActionMode.DRAG
                && Math.abs(event.getX() - downX) < touchSlop
                && Math.abs(event.getY() - downY) < touchSlop
                && handlingSticker != null) {
            currentMode = ActionMode.CLICK;
            if (onStickerOperationListener != null) {
                onStickerOperationListener.onStickerClicked(handlingSticker);
            }
            if (currentTime - lastClickTime < minClickDelayTime) {
                if (onStickerOperationListener != null) {
                    try {
                        onStickerOperationListener.onStickerDoubleTapped(handlingSticker);
//            onStickerOperationListener.onTextStickerDoubleTapped((TextSticker) handlingSticker);
                    } catch (Exception e) {

                    }

                }
            }
        }

        if (currentMode == ActionMode.DRAG && handlingSticker != null) {
            if (onStickerOperationListener != null) {
                lastDoing = "";
                downMatrix.set(moveMatrix);
                onStickerOperationListener.onStickerDragFinished(handlingSticker);
            }
        }

        calculateMidPoint();
        currentMode = ActionMode.NONE;
        lastClickTime = currentTime;
    }

    public void translateCurrentSticker(int x, int y) {
        if (handlingSticker != null) {
            downMatrix.set(handlingSticker.getMatrix());
            moveMatrix.set(downMatrix);
            moveMatrix.postTranslate(x, y);
            handlingSticker.setMatrix(moveMatrix);
            if (constrained) {
                constrainSticker(handlingSticker);
            }
            invalidate();
        }
    }

    protected void handleCurrentMode(@NonNull MotionEvent event) {
        switch (currentMode) {
            case ActionMode.NONE:
            case ActionMode.CLICK:
                break;
            case ActionMode.DRAG:
                if (handlingSticker != null) {
                    moveMatrix.set(downMatrix);
                    moveMatrix.postTranslate(event.getX() - downX, event.getY() - downY);
                    handlingSticker.setMatrix(moveMatrix);
                    if (constrained) {
                        constrainSticker(handlingSticker);
                    }
                }
                break;
            case ActionMode.ZOOM_WITH_TWO_FINGER:
                if (handlingSticker != null) {
                    float newDistance = calculateDistance(event);
                    float newRotation = calculateRotation(event);

                    moveMatrix.set(downMatrix);
                    moveMatrix.postScale(newDistance / oldDistance, newDistance / oldDistance, midPoint.x,
                            midPoint.y);
                    moveMatrix.postRotate(newRotation - oldRotation, midPoint.x, midPoint.y);
                    handlingSticker.setMatrix(moveMatrix);
                }

                break;

            case ActionMode.ICON:
                if (handlingSticker != null && currentIcon != null) {
                    currentIcon.onActionMove(this, event);
                }
                break;
        }
    }

    public void zoomAndRotateCurrentSticker(@NonNull MotionEvent event) {
        zoomAndRotateSticker(handlingSticker, event);
    }

    public void zoomAndRotateSticker(@Nullable Sticker sticker, @NonNull MotionEvent event) {
        if (sticker != null) {
            float newDistance = calculateDistance(midPoint.x, midPoint.y, event.getX(), event.getY());
            float newRotation = calculateRotation(midPoint.x, midPoint.y, event.getX(), event.getY());

            moveMatrix.set(downMatrix);
            moveMatrix.postScale(newDistance / oldDistance, newDistance / oldDistance, midPoint.x,
                    midPoint.y);
            moveMatrix.postRotate(newRotation - oldRotation, midPoint.x, midPoint.y);
            handlingSticker.setMatrix(moveMatrix);
        }
    }

    protected void constrainSticker(@NonNull Sticker sticker) {
        float moveX = 0;
        float moveY = 0;
        int width = getWidth();
        int height = getHeight();
        sticker.getMappedCenterPoint(currentCenterPoint, point, tmp);
        if (currentCenterPoint.x < 0) {
            moveX = -currentCenterPoint.x;
        }

        if (currentCenterPoint.x > width) {
            moveX = width - currentCenterPoint.x;
        }

        if (currentCenterPoint.y < 0) {
            moveY = -currentCenterPoint.y;
        }

        if (currentCenterPoint.y > height) {
            moveY = height - currentCenterPoint.y;
        }

        sticker.getMatrix().postTranslate(moveX, moveY);
    }

    @Nullable
    protected BitmapStickerIcon findCurrentIconTouched() {
        for (BitmapStickerIcon icon : icons) {
            float x = icon.getX() - downX;
            float y = icon.getY() - downY;
            float distance_pow_2 = x * x + y * y;
            int temp = 0;
            if (handlingSticker != null && handlingSticker instanceof DrawableSticker)
                temp = margin;
            if (distance_pow_2 <= Math.pow(icon.getIconRadius() + icon.getIconRadius(), 2) + temp) {
                return icon;
            }
        }

        return null;
    }

    /**
     * find the touched Sticker
     **/
    @Nullable
    protected Sticker findHandlingSticker() {
        for (int i = stickers.size() - 1; i >= 0; i--) {
            if (isInStickerArea(stickers.get(i), downX, downY) && !stickers.get(i).isLock()) {
//                Sticker sticker = stickers.get(i);
//                if (sticker.getListSticker()!=null&& sticker.getListSticker().get(sticker.currentPosition) != null)
//                   return sticker.getListSticker().get(sticker.currentPosition);
                return stickers.get(i);
            }
        }
        return null;
    }

    protected boolean isInStickerArea(@NonNull Sticker sticker, float downX, float downY) {
        tmp[0] = downX;
        tmp[1] = downY;
        return sticker.contains(tmp);
    }

    @NonNull
    protected PointF calculateMidPoint(@Nullable MotionEvent event) {
        if (event == null || event.getPointerCount() < 2) {
            midPoint.set(0, 0);
            return midPoint;
        }
        float x = (event.getX(0) + event.getX(1)) / 2;
        float y = (event.getY(0) + event.getY(1)) / 2;
        midPoint.set(x, y);
        return midPoint;
    }

    @NonNull
    protected PointF calculateMidPoint() {
        if (handlingSticker == null) {
            midPoint.set(0, 0);
            return midPoint;
        }
        handlingSticker.getMappedCenterPoint(midPoint, point, tmp);
        return midPoint;
    }

    /**
     * calculate rotation in line with two fingers and x-axis
     **/
    protected float calculateRotation(@Nullable MotionEvent event) {
        if (event == null || event.getPointerCount() < 2) {
            return 0f;
        }
        return calculateRotation(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
    }

    protected float calculateRotation(float x1, float y1, float x2, float y2) {
        double x = x1 - x2;
        double y = y1 - y2;
        double radians = Math.atan2(y, x);
        return (float) Math.toDegrees(radians);
    }

    /**
     * calculate Distance in two fingers
     **/
    protected float calculateDistance(@Nullable MotionEvent event) {
        if (event == null || event.getPointerCount() < 2) {
            return 0f;
        }
        return calculateDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
    }

    protected float calculateDistance(float x1, float y1, float x2, float y2) {
        double x = x1 - x2;
        double y = y1 - y2;

        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        for (int i = 0; i < stickers.size(); i++) {
            Sticker sticker = stickers.get(i);
            if (sticker != null) {
//        transformSticker(sticker);
            }
        }
    }

    /**
     * Sticker's drawable will be too bigger or smaller
     * This method is to transform it to fit
     * step 1：let the center of the sticker image is coincident with the center of the View.
     * step 2：Calculate the zoom and zoom
     **/
    protected void transformSticker(@Nullable Sticker sticker) {
        if (sticker == null) {
            Log.e(TAG, "transformSticker: the bitmapSticker is null or the bitmapSticker bitmap is null");
            return;
        }

        sizeMatrix.reset();

        float width = getWidth();
        float height = getHeight();
        float stickerWidth = sticker.getWidth();
        float stickerHeight = sticker.getHeight();
        //step 1
        float offsetX = (width - stickerWidth) / 2;
        float offsetY = (height - stickerHeight) / 2;

        sizeMatrix.postTranslate(offsetX, offsetY);

        //step 2
        float scaleFactor;
        if (width < height) {
            scaleFactor = width / stickerWidth;
        } else {
            scaleFactor = height / stickerHeight;
        }

        sizeMatrix.postScale(scaleFactor / 2f, scaleFactor / 2f, width / 2f, height / 2f);

        sticker.getMatrix().reset();
        sticker.setMatrix(sizeMatrix);

        invalidate();
    }

    public void flipCurrentSticker(int direction) {
        if (!lastDoing.equalsIgnoreCase("flip"+direction)){
            downMatrix.set(handlingSticker.getMatrix());
            lastDoing = "flip"+direction;
        }
        flip(handlingSticker, direction);
    }


    boolean x  = false;
    public void rotateCurrentSticker(int angle) {
        if (handlingSticker != null) {
            if (!lastDoing.equalsIgnoreCase("rotate")){
                downMatrix.set(handlingSticker.getMatrix());
                lastDoing = "rotate";
                calculateMidPoint();
            }

            moveMatrix.set(downMatrix);
            moveMatrix.postRotate(angle, midPoint.x, midPoint.y);
            handlingSticker.setMatrix(moveMatrix);
            invalidate();

        }
    }


    public void zoomCurrentSticker(float scale) {
        if (handlingSticker != null) {
            if (!lastDoing.equalsIgnoreCase("zoom")){
                downMatrix.set(handlingSticker.getMatrix());
            }
//            Matrix matrix = new Matrix();
//            matrix.set(downMatrix);
//            matrix.setScale(scale, scale, handlingSticker.getWidth() / 2f, handlingSticker.getHeight() / 2f);
//            handlingSticker.setMatrix(matrix);
//            invalidate();
            calculateMidPoint();
            moveMatrix.set(downMatrix);
            moveMatrix.postScale(scale, scale, midPoint.x, midPoint.y);
            handlingSticker.setMatrix(moveMatrix);
            invalidate();
            lastDoing = "zoom";
//            getStickerPoints(handlingSticker, bitmapPoints);
        }
    }

    public void flip(@Nullable Sticker sticker, @Flip int direction) {
        if (sticker != null) {
            sticker.getCenterPoint(midPoint);
            if ((direction & FLIP_HORIZONTALLY) > 0) {
                sticker.getMatrix().preScale(-1, 1, midPoint.x, midPoint.y);
                sticker.setFlippedHorizontally(!sticker.isFlippedHorizontally());
            }
            if ((direction & FLIP_VERTICALLY) > 0) {
                sticker.getMatrix().preScale(1, -1, midPoint.x, midPoint.y);
                sticker.setFlippedVertically(!sticker.isFlippedVertically());
            }

            if (onStickerOperationListener != null) {
//        onStickerOperationListener.onStickerFlipped(sticker);
            }

            invalidate();
        }
    }

    public boolean replace(@Nullable Sticker sticker) {
        return replace(sticker, true);
    }

    public boolean replace(@Nullable Sticker sticker, boolean needStayState) {
        if (handlingSticker != null && sticker != null) {
            float width = getWidth();
            float height = getHeight();
            if (needStayState) {
                sticker.setMatrix(handlingSticker.getMatrix());
                sticker.setFlippedVertically(handlingSticker.isFlippedVertically());
                sticker.setFlippedHorizontally(handlingSticker.isFlippedHorizontally());
            } else {
                handlingSticker.getMatrix().reset();
                // reset scale, angle, and put it in center
                float offsetX = (width - handlingSticker.getWidth()) / 2f;
                float offsetY = (height - handlingSticker.getHeight()) / 2f;
                sticker.getMatrix().postTranslate(offsetX, offsetY);

                float scaleFactor;
                if (width < height) {
                    scaleFactor = width / handlingSticker.getDrawable().getIntrinsicWidth();
                } else {
                    scaleFactor = height / handlingSticker.getDrawable().getIntrinsicHeight();
                }
                sticker.getMatrix().postScale(scaleFactor / 2f, scaleFactor / 2f, width / 2f, height / 2f);
            }
            int index = stickers.indexOf(handlingSticker);
            stickers.set(index, sticker);
            handlingSticker = sticker;

            invalidate();
            return true;
        } else {
            return false;
        }
    }

    public boolean remove(@Nullable Sticker sticker) {
        if (stickers.contains(sticker)) {
            stickers.remove(sticker);
            if (onStickerOperationListener != null) {
//        onStickerOperationListener.onStickerDeleted(sticker);
            }
            if (handlingSticker == sticker) {
                handlingSticker = null;
            }
            invalidate();

            return true;
        } else {
            Log.d(TAG, "remove: the sticker is not in this StickerView");

            return false;
        }
    }

    public boolean removeCurrentSticker() {
        return remove(handlingSticker);
    }

    public void removeAllStickers() {
        stickers.clear();
        if (handlingSticker != null) {
            handlingSticker.release();
            handlingSticker = null;
        }
        invalidate();
    }

    @NonNull
    public StickerView addSticker(@NonNull Sticker sticker) {
        return addSticker(sticker, Sticker.Position.CENTER);
    }


    public StickerView addSticker(@NonNull final Sticker sticker,
                                  final @Sticker.Position int position) {
        if (ViewCompat.isLaidOut(this)) {
            addStickerImmediately(sticker, position);
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    addStickerImmediately(sticker, position);
                }
            });
        }
        return this;
    }

    protected void addStickerImmediately(@NonNull Sticker sticker, @Sticker.Position int position) {
        setStickerPosition(sticker, position);


        float scaleFactor, widthScaleFactor, heightScaleFactor;

        widthScaleFactor = (float) getWidth() / sticker.getDrawable().getIntrinsicWidth();
        heightScaleFactor = (float) getHeight() / sticker.getDrawable().getIntrinsicHeight();
        scaleFactor = widthScaleFactor > heightScaleFactor ? heightScaleFactor : widthScaleFactor;

        sticker.getMatrix()
                .postScale(scaleFactor / 2, scaleFactor / 2, getWidth() / 2, getHeight() / 2);

        handlingSticker = sticker;
        stickers.add(sticker);
        if (onStickerOperationListener != null) {
            onStickerOperationListener.onStickerAdded(sticker);
        }
        invalidate();
    }


    protected void setStickerPosition(@NonNull Sticker sticker, @Sticker.Position int position) {
        float width = getWidth();
        float height = getHeight();
        float offsetX = width - sticker.getWidth();
        float offsetY = height - sticker.getHeight();
        if ((position & Sticker.Position.TOP) > 0) {
            offsetY /= 4f;
        } else if ((position & Sticker.Position.BOTTOM) > 0) {
            offsetY *= 3f / 4f;
        } else {
            offsetY /= 2f;
        }
        if ((position & Sticker.Position.LEFT) > 0) {
            offsetX /= 4f;
        } else if ((position & Sticker.Position.RIGHT) > 0) {
            offsetX *= 3f / 4f;
        } else {
            offsetX /= 2f;
        }
        sticker.getMatrix().postTranslate(offsetX, offsetY);
    }

    @NonNull
    public float[] getStickerPoints(@Nullable Sticker sticker) {
        float[] points = new float[8];
        getStickerPoints(sticker, points);
        return points;
    }

    public void getStickerPoints(@Nullable Sticker sticker, @NonNull float[] dst) {
        if (sticker == null) {
            Arrays.fill(dst, 0);
            return;
        }
        sticker.getBoundPoints(bounds);
        sticker.getMappedPoints(dst, bounds);
    }

    public File save(@NonNull File file, int width, int height) {
        File f = null;
        try {
            f = StickerUtils.saveImageToGallery(file, createBitmap(width, height));
            StickerUtils.notifySystemGallery(getContext(), file);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
            //
        }

        return f;
    }

    public Bitmap getBitmap(int width, int height) {
        return createBitmap(width, height);
    }

    /**
     * get bitmap original ratio
     * @param width :to width of bitmap out
     * @return
     */
    public Bitmap getBitmap(int width) {
        return createBitmap(width, width*getHeight()/getWidth());
    }

    public Bitmap getBitmap() {
        return createBitmap();
    }

    boolean isRemoveBackground = false;

    public Bitmap getBitmapPNG() {
        isRemoveBackground = true;
        return createBitmap();
    }
    public Bitmap getCustomBitmap(int currentSize, int outSize, int outHeight){
        handlingSticker = null;
        isRemoveBackground = true;
        Bitmap bitmap = Bitmap.createBitmap(outSize, outHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        for (int i = 0; i < stickers.size(); i++) {
            Sticker sticker = stickers.get(i);
            if (sticker != null) {
                sticker.draw(canvas,outSize*1f/currentSize);
            }
        }

        return bitmap;
    }


    public Bitmap getBitmapBackground(int w, int h) {
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (MODE_TEST == MODE_GRADIENT && linearGradient != null && paintGradient != null) {
            canvas.save();
            float temp = (float) (getWidth() * (Math.sqrt(2) - 1) / 2f);
            canvas.rotate(angle, getWidth() / 2f, getHeight() / 2f);
            canvas.translate(-temp, -temp);

            canvas.drawPaint(paintGradient);
            canvas.restore();

        } else if (MODE_TEST == MODE_COLOR) {
            canvas.drawColor(Color.parseColor(backgroundColor1));
        } else {
            if (bitmapBackground != null) {
                if (paintBitmap == null)
                    paintBitmap = new Paint(Paint.FILTER_BITMAP_FLAG);
                if (rectBackground == null)
                    rectBackground = new Rect();
                if (bitmapBackground.getWidth() < bitmapBackground.getHeight())
                    rectBackground.set(0, (bitmapBackground.getHeight() - bitmapBackground.getWidth()) / 2, bitmapBackground.getWidth(), (bitmapBackground.getHeight() + bitmapBackground.getWidth()) / 2);
                else
                    rectBackground.set((-bitmapBackground.getHeight() + bitmapBackground.getWidth()) / 2, 0, (bitmapBackground.getHeight() + bitmapBackground.getWidth()) / 2, bitmapBackground.getHeight());

                canvas.drawBitmap(bitmapBackground, rectBackground, canvas.getClipBounds(), paintBitmap);
            }

        }
        return bitmap;
    }

    public void saveBitmap(@NonNull File file, Bitmap bm) {
        try {
            StickerUtils.saveImageToGallery(file, bm);
            StickerUtils.notifySystemGallery(getContext(), file);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
            //
        }
    }

    @NonNull
    public Bitmap createBitmap() throws OutOfMemoryError {
        isGetBitmap = true;
        handlingSticker = null;
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        this.draw(canvas);
        return bitmap;
    }

    public Bitmap createBitmap(int width, int height) throws OutOfMemoryError {
        isGetBitmap = true;
        handlingSticker = null;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        this.draw(canvas);
        return bitmap;
    }

    public int getPosLayerSticker(Sticker sticker) {
        if (stickers == null || stickers.size() == 0)
            return -1;
        for (int i = 0; i < stickers.size(); i++) {
            if (sticker == stickers.get(i))
                return i;
        }
        return -1;
    }

    public int getStickerCount() {
        return stickers.size();
    }

    public boolean isNoneSticker() {
        return getStickerCount() == 0;
    }

    public boolean isLocked() {
        return locked;
    }

    @NonNull
    public StickerView setLocked(boolean locked) {
        this.locked = locked;
        invalidate();
        return this;
    }

    @NonNull
    public StickerView setMinClickDelayTime(int minClickDelayTime) {
        this.minClickDelayTime = minClickDelayTime;
        return this;
    }

    public int getMinClickDelayTime() {
        return minClickDelayTime;
    }

    public boolean isConstrained() {
        return constrained;
    }

    @NonNull
    public StickerView setConstrained(boolean constrained) {
        this.constrained = constrained;
        postInvalidate();
        return this;
    }

    @NonNull
    public StickerView setOnStickerOperationListener(
            @Nullable OnStickerOperationListener onStickerOperationListener) {
        this.onStickerOperationListener = onStickerOperationListener;
        return this;
    }

    @Nullable
    public OnStickerOperationListener getOnStickerOperationListener() {
        return onStickerOperationListener;
    }

    @Nullable
    public Sticker getCurrentSticker() {
        return handlingSticker;
    }

    public void setCurrentSticker(Sticker sticker) {
        handlingSticker = sticker;
        invalidate();
    }

    public List<Sticker> getStickers() {
        return stickers;
    }

    @NonNull
    public List<BitmapStickerIcon> getIcons() {
        return icons;
    }

    public void setIcons(@NonNull List<BitmapStickerIcon> icons) {
        this.icons.clear();
        this.icons.addAll(icons);
        invalidate();
    }

    public void sortByPosition() {
        if (stickers == null)
            return;
        Collections.sort(stickers, new Comparator<Sticker>() {
            @Override
            public int compare(Sticker o1, Sticker o2) {
                return o1.getPosition() - o2.getPosition();
            }
        });
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        super.setOnTouchListener(l);
    }



}
