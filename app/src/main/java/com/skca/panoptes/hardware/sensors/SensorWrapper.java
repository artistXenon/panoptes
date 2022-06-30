package com.skca.panoptes.hardware.sensors;

import android.hardware.Sensor;

public class SensorWrapper {

    final private Sensor sensorObject;

    public boolean listen = false;
    private String newValue = "";

    public SensorWrapper(Sensor s) {
        sensorObject = s;
    }

    public synchronized void updateValue(String s) {
        newValue = s;
    }

    public synchronized String getValue() {
        return newValue;
    }

    public Sensor getBaseSensor() {
        return sensorObject;
    }
}
