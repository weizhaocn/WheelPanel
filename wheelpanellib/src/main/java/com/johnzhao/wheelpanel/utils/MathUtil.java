package com.johnzhao.wheelpanel.utils;

import android.graphics.PointF;

public class MathUtil {


    /**
     * 把角度限制在（-180,180]范围内
     * @param angle
     * @return
     */
    public static double getIdenticalAngle(double angle){
        angle %= 360;
        if(angle <= -180){
            angle += 360;
        }else if(angle > 180){
            angle -= 360;
        }
        return angle;
    }

    public static float getIdenticalAngle(float angle){
        angle %= 360;
        if(angle <= -180){
            angle += 360;
        }else if(angle > 180){
            angle -= 360;
        }
        return angle;
    }

    public static float computeDistanceBetween2Points(PointF firstPoint, PointF secondPoint){
        float xDistance = Math.abs(firstPoint.x - secondPoint.x);
        float ydistance = Math.abs(firstPoint.y - secondPoint.y);
        return (float) Math.sqrt(Math.pow(xDistance, 2) + Math.pow(ydistance, 2));
    }

    public static float computeDistanceBetween2Points(float firstX, float firstY, float secondX, float secondY){
        float xDistance = Math.abs(firstX - secondX);
        float ydistance = Math.abs(firstY - secondY);
        return (float) Math.sqrt(Math.pow(xDistance, 2) + Math.pow(ydistance, 2));
    }
}
