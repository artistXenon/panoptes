package com.skca.panoptes.hardware;

/*
* Class to handle information collected by hardware components including device specification and sensor details.
*/

import android.content.Context;
import android.hardware.*;
import com.skca.panoptes.hardware.sensors.SensorsInfo;
import com.skca.panoptes.hardware.specification.DeviceInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataManager {

    private static DataManager _instance = new DataManager();

    public static synchronized DataManager get() { // Thread safe singleton class.
        if (_instance == null) _instance = new DataManager();
        return _instance;
    }

    private DeviceInfo deviceInfo;

    private SensorManager sensorManager;

    private DataManager() {}

    public void init(Context context) {
        loadDeviceInfo(context);
        loadSensorManager(context);
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public SensorManager getSensorManager() {
        return sensorManager;
    }

    public void loadSensors() {
        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for (Sensor s : deviceSensors) {
            // The name is guaranteed to be unique for a particular sensor type.

            sensorManager.registerListener(new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    SensorsInfo.updateSensorValues("[" + event.sensor.getStringType() + "] " + event.sensor.getName(), valueFormatter(event.values));
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {}
            }, s, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    private void loadDeviceInfo(Context context) {
        deviceInfo = new DeviceInfo(context);
    }

    private void loadSensorManager(Context s) {
        this.sensorManager = (SensorManager) s.getSystemService(Context.SENSOR_SERVICE);
    }

    private String valueFormatter(float[] fs) {
        StringBuilder b = new StringBuilder();
        for (float f : fs) {
            b.append(f + ", ");
        }
        b.setLength(b.length() - 2);
        return b.toString();
    }
}
