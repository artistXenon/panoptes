package com.skca.panoptes.hardware;

/*
* Class to handle information collected by hardware components including device specification and sensor details.
*/

import android.content.Context;
import android.hardware.*;
import com.skca.panoptes.hardware.sensors.SensorWrapper;
import com.skca.panoptes.hardware.sensors.SensorsInfo;
import com.skca.panoptes.hardware.specification.DeviceInfo;
import com.skca.panoptes.helper.Recorder;

import java.util.*;

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
        Map<String, SensorWrapper> sensorMap = new HashMap<>();

        for (Sensor s : deviceSensors) {
            // The name is guaranteed to be unique for a particular sensor type.
            sensorMap.put("[" + s.getStringType().substring(15) + "] " + s.getName(), new SensorWrapper(s));
        }

        SensorsInfo.loadSensors(sensorMap);
    }

    public void listenSensors(Recorder r) {
        for (SensorWrapper e : SensorsInfo.getSensorMap().values()) {
            if (!e.listen) continue;
            e.recordCount = 0;
            sensorManager.registerListener(r, e.getBaseSensor(), e.delay > 0 ? e.delay : SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void releaseSensors(Recorder r) {
        for (SensorWrapper e : SensorsInfo.getSensorMap().values()) {
            sensorManager.unregisterListener(r, e.getBaseSensor());
        }
    }

    private void loadDeviceInfo(Context context) {
        deviceInfo = new DeviceInfo(context);
    }

    private void loadSensorManager(Context s) {
        this.sensorManager = (SensorManager) s.getSystemService(Context.SENSOR_SERVICE);
    }

    public String valueFormatter(float[] fs) {
        StringBuilder b = new StringBuilder();
        for (float f : fs) {
            b.append(f + ", ");
        }
        b.setLength(b.length() - 2);
        return b.toString();
    }
}
