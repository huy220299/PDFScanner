package com.remi.pdfscanner.ui.splash;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.remi.pdfscanner.R;


public class SeekbarSplash extends View {
    Paint paint ;
    RectF rectF;
    Rect rectText;
    int progress =10;
    float stroke;
    void init(){
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(getResources().getDimension(com.intuit.ssp.R.dimen._12ssp));
        paint.setTypeface( ResourcesCompat.getFont(getContext(), R.font.bold) );
        rectF = new RectF();
        rectText = new Rect();
        stroke = getResources().getDimension(com.intuit.sdp.R.dimen._1sdp);
    }
    public SeekbarSplash(Context context) {
        super(context);
        init();
    }

    public SeekbarSplash(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SeekbarSplash(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    public void setProgress(int p){
        this.progress =p;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth()==0)
            return;
        String txt = String.valueOf(progress)+"%";
        paint.getTextBounds(txt,0,txt.length(),rectText);
        rectF.set(stroke,getHeight()/5f ,getWidth()-stroke,getHeight()*4/5f );
        paint.setColor(Color.parseColor("#FFFFFF"));
        paint.setStrokeWidth(stroke);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(rectF,rectF.height()/4f,rectF.height()/4f,paint);
        paint.setStyle(Paint.Style.FILL);
        float currentX = (getWidth()-stroke*6)*progress*1f/100;
        rectF.set(stroke*3,getHeight()/5f +stroke*2,stroke*3+currentX,getHeight()*4/5f-stroke*2 );
        canvas.drawRoundRect(rectF,rectF.height()/4f,rectF.height()/4f,paint);
        paint.setColor(Color.parseColor("#333333"));
        canvas.drawText(txt,getWidth()/2f-rectText.width()/2f,getHeight()/2f+rectText.height()/2f,paint);
    }
}
