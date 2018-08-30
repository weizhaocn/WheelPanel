package com.johnzhao.wheelpanel.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;


import com.johnzhao.wheelpanel.R;
import com.johnzhao.wheelpanel.utils.Constants;
import com.johnzhao.wheelpanel.utils.MathUtil;
import com.johnzhao.wheelpanel.utils.speed.Speed;
import com.johnzhao.wheelpanel.utils.speed.SpeedRecorder;
import com.nineoldandroids.view.ViewHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class WheelPanel extends FrameLayout {

    private static final String TAG = "WheelPanel";
    private static final boolean DEBUG = false;

    //after the finger up, the wheel turns around with inertance
    private static final int FLAG_RUN_BY_SELF = 1;

    //the wheel should rebounce to the position with one item's midline vertical
    private static final int FLAG_REBOUNCE = 2;

    //how many angles rebounce back each time
    private static final float REBOUNCE_BACK_ANGLE = 1;

    //value of acceleration should be minors
    private static final float ACCELERATION = -0.003f;

    //indicate whether the finger moved its position before up or cancel
    private boolean fingerMoved = false;
    private int itemCount;
    private float anchorAngle;
    private float radius;
    private List<FanView> children;

    //center of the wheel
    private PointF centerPoint = new PointF();

    //the coordinate is original from center of the wheel
    private PointF downPointByCenter = new PointF();
    private PointF currentPointByCenter = new PointF();

    //for test
    //private int[] colors = {Color.RED, Color.CYAN, Color.YELLOW, Color.BLACK, Color.LTGRAY, Color.BLUE};
    //private String colorStrs[] = {"红", "浅蓝", "黄", "黑", "灰", "深蓝"};

    private SpeedRecorder speedRecorder;
    private WheelPanelHandler wheelPanelHandler;

    public WheelPanel(Context context, float radius, int itemCount, float textSize, int textColor) {
        super(context);
        init(radius, itemCount, textSize, textColor);
    }

    public WheelPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WheelPanel);
        float radius = typedArray.getDimension(R.styleable.WheelPanel_radius, 0);
        int itemCount = typedArray.getInteger(R.styleable.WheelPanel_itemCount, 2);//TODO 默认数目的设置
        float textSize = typedArray.getDimension(R.styleable.WheelPanel_textSize, 12);//TODO 设置默认
        int textColor = typedArray.getColor(R.styleable.WheelPanel_textColor, Color.BLACK);
        typedArray.recycle();
        init(radius, itemCount, textSize, textColor);
    }

    private void init(float radius, int itemCount, float textSize, int textColor){
        this.radius = radius;
        this.itemCount = itemCount;
        children = new ArrayList<FanView>();
        speedRecorder = new SpeedRecorder();
        wheelPanelHandler = new WheelPanelHandler(new WeakReference<WheelPanel>(this));

        //add children
        int fanSweepDegree = Constants.ANGLE_360 / itemCount;
        FanView child;
        for(int i=0; i<itemCount; i++){
            float deflection = i * fanSweepDegree;
            child = new FanView(getContext(),radius, fanSweepDegree, deflection, Color.TRANSPARENT, "", textSize, textColor);
            children.add(child);
            addView(child);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heiMode = MeasureSpec.getMode(heightMeasureSpec);
        if(widthMode == MeasureSpec.AT_MOST){
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(Math.round(radius * 2 + getPaddingLeft() + getPaddingRight()), MeasureSpec.EXACTLY);
        }
        if(heiMode == MeasureSpec.AT_MOST){
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.round(radius * 2 + getPaddingTop() + getPaddingBottom()), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    //@Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
////        MeasureSpec.AT_MOST->2
////        MeasureSpec.EXACTLY->1
////        MeasureSpec.UNSPECIFIED->0
//        int widthSpace = MeasureSpec.getSize(widthMeasureSpec);
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int heightSpace = MeasureSpec.getSize(heightMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        Log.d(TAG, "widthMode:"+widthMode+" widthSpace:"+widthMeasureSpec+" heightMode:"+heightMode+" heightSpace:"+heightSpace);
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int layoutWidth = getLayoutParams().width;
//        int layoutHeight = getLayoutParams().height;
//
//
//        Log.d(TAG, "layoutWidth:"+layoutWidth+" layoutHeight:"+layoutHeight);
//        //setMeasuredDimension(Math.round(radius * 2), Math.round(radius * 2));
//        //measureChildren(widthMeasureSpec, heightMeasureSpec);
//
//    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        centerPoint.set((l + r) / 2, (t + b) / 2);
        int wheelPanelWidth = r - l;
        int wheelPanelHeight = b - t;
        if(DEBUG){
            Log.d(TAG, "WheelPanel layout -> l: "+ l +"; t: "+ t + "; r: " + r +"; b: "+b );
        }

        for(FanView child : children){
            child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
            child.offsetLeftAndRight((int) Math.round(wheelPanelWidth / 2 - Math.sin(Math.toRadians(child.getSweepAngle() / 2)) * radius));
            child.offsetTopAndBottom(wheelPanelHeight / 2);
            child.setPivotX(child.getMeasuredWidth() / 2);
            child.setPivotY(0);
            ViewHelper.setRotation(child, child.getDeflection());
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                //outside of the circle
                if(MathUtil.computeDistanceBetween2Points(centerPoint.x, centerPoint.y, ev.getX(), ev.getY()) > radius){
                    return false;
                }
                break;
        }
        return true;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //outside of the circle
                if(MathUtil.computeDistanceBetween2Points(centerPoint.x, centerPoint.y, event.getX(), event.getY()) > radius){
                    return false;
                }
                fingerMoved = false;

                //remove message from queue to stop the wheel
                wheelPanelHandler.removeCallbacksAndMessages(null);

                //set coordinate of the down pointer
                downPointByCenter.set(event.getX() - radius, event.getY() - radius);

                //reset the speed recorder
                speedRecorder.reset();
                break;

            case MotionEvent.ACTION_MOVE:
                fingerMoved = true;
                //set coordinate of current pointer
                currentPointByCenter.set(event.getX() - radius, event.getY() - radius);

                //re-layout
                float deflectAngle = computeIncludedAngle(downPointByCenter, currentPointByCenter);
                for(FanView child : children){
                    child.setDeflection(child.getDeflection()+deflectAngle);
                }
                requestLayout();

                //refresh coordinate of down pointer
                downPointByCenter.set(currentPointByCenter.x, currentPointByCenter.y);

                //record speed
                speedRecorder.add(new Speed(System.currentTimeMillis(), deflectAngle));
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(fingerMoved){
                    float currentSpeed = speedRecorder.getSpeed();
                    if(currentSpeed != 0){
                        //turn around by self
                        Message msg = Message.obtain();
                        msg.what = FLAG_RUN_BY_SELF;
                        msg.obj = currentSpeed;
                        wheelPanelHandler.sendMessage(msg);
                        break;
                    }
                }
                float deflationOffset = getDeflationOffset(children);
                if(deflationOffset!=0){
                    rebounce(children, deflationOffset);
                }
                break;
        }
        return true;
    }


    public void setItemBackgroundColor(int index, int color){
        if(index < 0 || index >= itemCount){
            throw new ArrayIndexOutOfBoundsException("the index is "+index+", the children count is "+itemCount);
        }
        children.get(index).setBackgroundColor(color);
    }

    public void setItemBackgroundColors(List<Integer> colors){
        if(colors.size() < itemCount){
            throw new IllegalArgumentException("require a list of length of "+itemCount+", found a list of length of "+colors.size());
        }
        for(int i=0; i<itemCount; i++){
            FanView child = children.get(i);
            child.setBackgroundColor(colors.get(i));
        }
    }


    public void setItemContent(int index, String content){
        if(index < 0 || index >= itemCount){
            throw new ArrayIndexOutOfBoundsException("the index is "+index+", the children count is "+itemCount);
        }
        children.get(index).setContent(content);
    }

    public void setItemContents(List<String> contents){
        if(contents.size() < itemCount){
            throw new IllegalArgumentException("require a list of length of "+itemCount+", found a list of length of "+contents.size());
        }
        for(int i=0; i<itemCount; i++){
            FanView child = children.get(i);
            child.setContent(contents.get(i));
        }
    }

    /**
     * get the included angle of two lines.
     * the first line connects the point(0, 0) and firstPoint,
     * the second line connects the point(0, 0) and secondPoint.
     * the value of result limits in (-180, 180]
     * @param firstPoint first pointer in coordinate
     * @param secondPoint second pointer in coordinate
     * @return the included angle value of line (first pointer and original pointer) and line (second pointer and original pointer)
     */
    public float computeIncludedAngle(PointF firstPoint, PointF secondPoint){
        double firstRadians = Math.atan2(firstPoint.y, firstPoint.x);//(-180, 180]
        double firstAngle = Math.toDegrees(firstRadians);
        double secondRadians = Math.atan2(secondPoint.y, secondPoint.x);//(-180, 180]
        double secondAngle = Math.toDegrees(secondRadians);
        return (float) differAngles(firstAngle, secondAngle);
    }

    /**
     * get the difference between two angles.
     * the result limits in (-180, 180]
     * @param firstAngle first angle
     * @param secondAngle second angle
     * @return the difference between the two angles
     */
    public static double differAngles(double firstAngle, double secondAngle){
        firstAngle %= Constants.ANGLE_360;
        secondAngle %= Constants.ANGLE_360;
        double deltAngle = (secondAngle - firstAngle) % Constants.ANGLE_360;
        return MathUtil.getIdenticalAngle(deltAngle);
    }

    /**
     * get the deflation offset to rebounce.
     * the result is the value of angle to rebounce back.
     * @param children collection of FanViews in the WheelPanel
     * @return the minimal angle offset of the FanViews
     */
    private float getDeflationOffset(List<FanView> children) {
        float minDeflation = Constants.ANGLE_180;
        for(FanView child: children){
            if(Math.abs(child.getDeflection()) < Math.abs(minDeflation)){
                minDeflation = child.getDeflection();
            }
        }
        return minDeflation;
    }

    /**
     * when the finger is up, the current state of wheel panel is offset from the initial state,
     * it should rebounce back to the initial state(with one FanView's midline vertical)
     * @param children collection of FanViews in the WheelPanel
     * @param deflationOffset the minimal angle offset of the FanViews
     */
    private void rebounce(List<FanView> children, float deflationOffset) {
        if(deflationOffset != 0){
            float remainOffset = 0;
            float realRebounceBackAngle = 0;
            if(deflationOffset > 0){
                remainOffset = deflationOffset - REBOUNCE_BACK_ANGLE;
                if(remainOffset > 0){
                    realRebounceBackAngle = REBOUNCE_BACK_ANGLE;
                }else{
                    realRebounceBackAngle = deflationOffset;
                    remainOffset = 0;
                }
            }else if(deflationOffset < 0){
                remainOffset = deflationOffset + REBOUNCE_BACK_ANGLE;
                if(remainOffset < 0){
                    realRebounceBackAngle = -REBOUNCE_BACK_ANGLE;
                }else{
                    realRebounceBackAngle = -deflationOffset;
                    remainOffset = 0;
                }
            }
            for(FanView child : children){
                child.setDeflection(child.getDeflection() - realRebounceBackAngle);
            }
            requestLayout();
            if(remainOffset!= 0){
                Message msgRebounce = Message.obtain();
                msgRebounce.what = FLAG_REBOUNCE;
                msgRebounce.obj = remainOffset;
                wheelPanelHandler.sendMessage(msgRebounce);
            }
        }
    }

    public int getItemCount() {
        return itemCount;
    }


    public float getAnchorAngle() {
        return anchorAngle;
    }

    public void setAnchorAngle(float anchorAngle) {
        this.anchorAngle = anchorAngle;
    }

    public float getRadius() {
        return radius;
    }




    private static class WheelPanelHandler extends Handler{
        private WeakReference<WheelPanel> wheelPanelWeakReference;
        WheelPanelHandler(WeakReference<WheelPanel> wheelPanelWeakReference){
            this.wheelPanelWeakReference = wheelPanelWeakReference;
        }
        @Override
        public void handleMessage(Message msgReceived) {
            WheelPanel wheelPanel = wheelPanelWeakReference.get();
            if(wheelPanel==null){
                return;
            }
            switch (msgReceived.what){
                case FLAG_RUN_BY_SELF:
                    //assert currentSpeed is not 0
                    float currentSpeed = (float) msgReceived.obj;
                    if(DEBUG){
                        Log.d(TAG, "RUN_BY_SELF-> currentSpeed ："+currentSpeed);
                    }

                    //compute passed time
                    long when = msgReceived.getWhen();
                    long currentTime = SystemClock.uptimeMillis();
                    long deltTime = currentTime - when;
                    if(DEBUG){
                        Log.d(TAG, "RUN_BY_SELF-> deltTime ： "+deltTime);
                    }

                    float deltAngle = 0;
                    if(currentSpeed > 0){
                        deltAngle = currentSpeed * deltTime + ACCELERATION * deltTime * deltTime * 0.5f;
                        if(deltAngle < 0){
                            //deltAngle and currentSpeed are in opposite directions, stop run_by_self, start rebounce
                            float deflationOffset = wheelPanel.getDeflationOffset(wheelPanel.children);
                            if(deflationOffset!=0){
                                wheelPanel.rebounce(wheelPanel.children, deflationOffset);
                            }
                            return;
                        }
                    }else if(currentSpeed < 0){
                        deltAngle = currentSpeed * deltTime - ACCELERATION * deltTime * deltTime * 0.5f;
                        if(deltAngle > 0){
                            //deltAngle and currentSpeed are in opposite directions, stop run_by_self, start rebounce
                            float deflationOffset = wheelPanel.getDeflationOffset(wheelPanel.children);
                            if(deflationOffset!=0){
                                wheelPanel.rebounce(wheelPanel.children, deflationOffset);
                            }
                            return;
                        }
                    }
                    if(DEBUG){
                        Log.d(TAG, "RUN_BY_SELF-> deltAngle ："+deltAngle);
                    }

                    for(FanView child : wheelPanel.children){
                        child.setDeflection(child.getDeflection() + deltAngle);
                    }
                    wheelPanel.requestLayout();
                    //send message and prepare data for next run_by_self
                    Message msgRunBySelf = Message.obtain();
                    msgRunBySelf.what = FLAG_RUN_BY_SELF;
                    float tempSpeed = 0;
                    if(currentSpeed > 0){
                        tempSpeed = currentSpeed + ACCELERATION * deltTime;
                    }else if(currentSpeed < 0){
                        tempSpeed = currentSpeed - ACCELERATION * deltTime;
                    }
                    if(DEBUG){
                        Log.d(TAG, "RUN_BY_SELF-> decayed speed ："+tempSpeed);
                    }

                    if((currentSpeed > 0 && tempSpeed <= 0)||(currentSpeed < 0 && tempSpeed >= 0)){
                        //rebounce
                        float deflationOffset = wheelPanel.getDeflationOffset(wheelPanel.children);
                        if(deflationOffset!=0){
                            wheelPanel.rebounce(wheelPanel.children, deflationOffset);
                        }
                        if(DEBUG){
                            Log.d(TAG, "RUN_BY_SELF-> decayed speed is opposite to current speed, stop run_by_self, start rebounce.");
                        }
                        return;
                    }
                    msgRunBySelf.obj = tempSpeed;
                    sendMessage(msgRunBySelf);
                    break;

                case FLAG_REBOUNCE:
                    float deflationOffset = (float) msgReceived.obj;
                    if(deflationOffset!=0){
                        wheelPanel.rebounce(wheelPanel.children, deflationOffset);
                    }
                    break;
            }
        }
    }
}
