package com.johnzhao.wheelpanel.utils.speed;

import java.io.Serializable;

public class Speed implements Serializable{

    private long time;
    private float angleIncrement;
    public Speed(long time, float angleIncrement){
        this.time = time;
        this.angleIncrement = angleIncrement;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public float getAngleIncrement() {
        return angleIncrement;
    }

    public void setAngleIncrement(float angleIncrement) {
        this.angleIncrement = angleIncrement;
    }
}
