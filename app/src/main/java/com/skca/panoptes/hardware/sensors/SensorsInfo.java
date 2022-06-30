package com.skca.panoptes.hardware.sensors;

import android.hardware.Sensor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class SensorsInfo {

    private static final ReentrantLock l = new ReentrantLock();

    private static Map<String, SensorWrapper> sensorsMap = new HashMap<>();

    public static synchronized void loadSensors(Map<String, SensorWrapper> m) {
        l.lock();
        sensorsMap = m;
        l.unlock();
    }

    public static Map<String, SensorWrapper> getSensorMap() {
        return sensorsMap;
    }

    //TODO: independent parser will apply to each sensor types.
    public static synchronized String readSensorValues(boolean readValues) {
        StringBuilder b = new StringBuilder();
        l.lock();
        Set<Map.Entry<String, SensorWrapper>> s = sensorsMap.entrySet();
        for (Map.Entry<String, SensorWrapper> e : s) {
            b.append(e.getKey()).append("\n");
            SensorWrapper sensorWrapper = e.getValue();
            if (readValues && sensorWrapper.listen) b.append(sensorWrapper.getValue());
            b.append("\n\n");
        }
        l.unlock();

        return b.toString();
    }

    public static synchronized void updateSensorValues(String key, Sensor s, String value) {
        try {
            if (l.tryLock(50, TimeUnit.MILLISECONDS)) {
                sensorsMap.get(key).updateValue(value);
            }
        }
        catch (InterruptedException ignore) { }
        finally {
            l.unlock();
        }
    }

}
