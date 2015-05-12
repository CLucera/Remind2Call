package com.crea3d.remind2call.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

/**
 * Created by clucera on 21/03/15.
 */
public class VerticalTextView extends TextView{


    public VerticalTextView(Context context)
    {
        super(context);
        init();
    }

    public VerticalTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public VerticalTextView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        setGravity(Gravity.CENTER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }


    @Override
    protected void onDraw(Canvas canvas) {

        TextPaint textPaint = getPaint();
        textPaint.drawableState = getDrawableState();
        textPaint.setColor(getCurrentTextColor());

        canvas.save();

        canvas.translate(0,getHeight());
        canvas.rotate(-90);

        canvas.translate(getCompoundPaddingLeft(), getExtendedPaddingTop());
        getLayout().draw(canvas);
        canvas.restore();

    }
}
