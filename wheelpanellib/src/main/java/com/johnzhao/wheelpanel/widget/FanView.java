package com.johnzhao.wheelpanel.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import com.johnzhao.wheelpanel.utils.Constants;
import com.johnzhao.wheelpanel.utils.MathUtil;


/**
 * Author:JohnZhao
 * Description: This is the customized View stand for a item in wheel-panel.
 * It disappears as a fan.
 */
public class FanView extends View {
    private float radius;
    private float sweepAngle;

    /**
     * the zone of every view in Android is a rectangle.
     * Field of layoutWidth and layoutHeight is the width and height of zhe zone.
     * value of the two field depends on the radius and sweepAngle.
     */
    private int layoutWidth, layoutHeight;
    private float deflection;
    private int bgColor;
    private String content;
    private Paint paint;
    private float textSize;
    private int textColor;
    private Path path;
    private RectF oval;
    FanView(Context context, float radius, float sweepAngle, float deflection, int bgColor, String content, float textSize, int textColor) {
        super(context);
        this.radius = radius;
        this.sweepAngle = sweepAngle;
        this.deflection = deflection;
        this.bgColor = bgColor;
        this.content = content;
        this.textSize = textSize;
        this.textColor = textColor;

        //compute layoutWidth and layoutHeight
        double sweepRadians = Math.toRadians(sweepAngle / 2);//angle->radians
        layoutWidth = (int) Math.round(Math.sin(sweepRadians) * radius * 2);
        layoutHeight = Math.round(radius);

        //create and set the paint
        paint = new Paint();
        paint.setAntiAlias(true);

        path = new Path();
        oval = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //define the size of FanView
        setMeasuredDimension(layoutWidth, layoutHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        path.reset();

        //move to the vertex of the fan
        path.moveTo(layoutWidth / 2, 0);

        //compute the coordinate of the arc's start point
        int arcStartPointX = layoutWidth;
        int arcStartPointY = (int) Math.round(Math.cos(Math.toRadians(sweepAngle / 2)) * radius);

        //draw the first sideline of the fan
        path.lineTo(arcStartPointX, arcStartPointY);

        //draw the arc line of the fan
        float left = layoutWidth /2 - radius;
        float top = -radius;
        float right = layoutWidth / 2 + radius;
        float bottom = radius;
        oval.set(left, top, right, bottom);
        float startAngle = (Constants.ANGLE_180 - sweepAngle) / 2;
        path.arcTo(oval, startAngle, sweepAngle);
        paint.setColor(bgColor);
        canvas.drawPath(path, paint);

        //start to draw text
        paint.setTextSize(textSize);
        paint.setColor(textColor);

        //measure text to get the width and height of text
        float textWidth = paint.measureText(content);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float textHeight = fontMetrics.bottom - fontMetrics.top;

        //draw text on the path
        float arcLength = (float) (Math.PI * radius / Constants.ANGLE_180 * sweepAngle);
        float hOffset = radius + (arcLength - textWidth) / 2;
        canvas.drawTextOnPath(content, path, hOffset, textHeight, paint);
    }


    public float getDeflection() {
        return deflection;
    }

    public void setDeflection(float deflection) {
        this.deflection = MathUtil.getIdenticalAngle(deflection);
    }

    public String getContent() {
        return content;
    }

    public float getSweepAngle() {
        return sweepAngle;
    }

    @Override
    public void setBackgroundColor(int color) {
        bgColor = color;
    }

    public void setContent(String content){
        this.content = content;
    }
}
