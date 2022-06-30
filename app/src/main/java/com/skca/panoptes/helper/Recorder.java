package com.skca.panoptes.helper;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import com.skca.panoptes.hardware.DataManager;
import com.skca.panoptes.hardware.sensors.SensorsInfo;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Recorder implements SensorEventListener {

    // make internal file. when recording starts, stream to file.

    private boolean started = false, stopped = false, paused = false;

    private String name;
    private long startDateTime;

    private PipedInputStream pipeReader;
    private PipedOutputStream pipeWriter;
    private OutputStreamWriter fileWriter;


    public void start() {
        if (started) throw new IllegalStateException("Recorder: Already started.");
        started = true;
        startDateTime = System.currentTimeMillis();

        //TODO: make file
        //TODO: pipe streams

        resume();
    }

    public void stop() {
        if (!started) throw new IllegalStateException("Recorder: Not started.");
        if (stopped) throw new IllegalStateException("Recorder: Already stopped.");
        pause();
        try {
            pipeWriter.close();
            pipeReader.close();
            fileWriter.close();
        } catch (IOException ignore) {}
    }

    public void pause() {
        if (!started) throw new IllegalStateException("Recorder: Not started.");
        if (stopped) throw new IllegalStateException("Recorder: Already stopped.");
        if (paused) return;
        DataManager.get().releaseSensors(this);
    }

    public void resume() {
        if (!started) throw new IllegalStateException("Recorder: Not started.");
        if (stopped) throw new IllegalStateException("Recorder: Already stopped.");
        if (!paused) return;
        DataManager.get().listenSensors(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        String sensorKey = "[" + sensor.getStringType() + "] " + sensor.getName();
        String formattedValue = DataManager.get().valueFormatter(event.values); //TODO: sensor type formatter.
        SensorsInfo.updateSensorValues(sensorKey, sensor, formattedValue);
        try {
            pipeWriter.write(
                    (System.currentTimeMillis() + "/ " + sensorKey + " : " + formattedValue)
                            .getBytes(StandardCharsets.UTF_8)
            );
        }
        catch (IOException ignore) {}
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
