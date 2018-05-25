package com.dudu.fatidog.map.bean;

/**
 * Created by Robert on 10/07/2017.
 */

public class SpeedChangeEvent {
    private float speedKmPH; //速度： 公里每时

    public SpeedChangeEvent(float speedKmPH) {
        this.speedKmPH = speedKmPH;
    }

    public float getSpeedKmPH() {
        return speedKmPH;
    }

    public void setSpeedKmPH(float speedKmPH) {
        this.speedKmPH = speedKmPH;
    }

    @Override
    public String toString() {
        return "SpeedChangeEvent{" +
                "speedKmPH=" + speedKmPH +
                '}';
    }
}
