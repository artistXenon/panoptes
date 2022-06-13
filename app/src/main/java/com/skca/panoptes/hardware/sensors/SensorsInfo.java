package com.skca.panoptes.hardware.sensors;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class SensorsInfo {

    private static final ReentrantLock l = new ReentrantLock();

    private static Map<String, String> sensorValues = new HashMap<>();


    //TODO: independent parser will apply to each sensor types.
    public static synchronized String readSensorValues(boolean readValues) {
        StringBuilder b = new StringBuilder();
        l.lock();

        Set<Map.Entry<String, String>> s = sensorValues.entrySet();
        for (Map.Entry<String, String> e : s) {
            b.append(e.getKey()).append("\n");
            if (readValues) b.append(e.getValue());
            b.append("\n\n");
        }
        l.unlock();

        return b.toString();
    }

    public static synchronized void updateSensorValues(String key, String value) {
        try {
            if (l.tryLock(50, TimeUnit.MILLISECONDS))
                sensorValues.put(key, value);
//                Log.i("SensorMonitor", key + value);
        }
        catch (InterruptedException ignore) { }
        finally {
            l.unlock();
        }
    }

}
