package com.skca.panoptes.hardware.sensors;

import android.hardware.Sensor;

public class SensorWrapper {

    final private Sensor sensorObject;

    public boolean listen = false;
    public int delay = 0;
    public long recordCount = 0;
    private String newValue = "";

    public SensorWrapper(Sensor s) {
        sensorObject = s;
        delay = sensorObject.getMinDelay();
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

    public String getSensorDetails() {
        int report = sensorObject.getReportingMode();
        StringBuilder b = new StringBuilder();
        if (report == Sensor.REPORTING_MODE_CONTINUOUS || report == Sensor.REPORTING_MODE_ON_CHANGE) {
            int
                min = Math.max(sensorObject.getMinDelay(), 0),
                max = Math.max(sensorObject.getMaxDelay(), 0);
            b.append("Delay range: " + (min == 0 ? "undefined" : (min + "μs")) + " ~ " + (max == 0 ? "undefined" : (max + "μs")) + "\n" +
                    "Delay now: " + this.delay + "μs\n");
        }
        b.append("Max Range: " + sensorObject.getMaximumRange() + "\n" +
                "Resolution: " + sensorObject.getResolution());
        return b.toString();

    }
}
