package com.johnzhao.wheelpanel.utils.speed;

public class SpeedRecorder {

    private static final int LENGTH = 15;
    private LimitQueue<Speed> records;

    public SpeedRecorder(){
        records = new LimitQueue<>(LENGTH);
    }

    public void add(Speed speed){
        records.offer(speed);
    }

    public float getSpeed(){
        if(records.size() == 0){
            return 0;
        }

        long deltTime = records.getLast().getTime() - records.getFirst().getTime();
        if(deltTime == 0){
            return 0;
        }
        float sumAngle = 0;
        for(int i=0; i<records.size(); i++){
            sumAngle += records.get(i).getAngleIncrement();
        }

        return sumAngle / deltTime;
    }

    public void reset(){
        records.clear();
    }
}
