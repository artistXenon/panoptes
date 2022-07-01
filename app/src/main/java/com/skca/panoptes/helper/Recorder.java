package com.skca.panoptes.helper;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import com.skca.panoptes.hardware.DataManager;
import com.skca.panoptes.hardware.sensors.SensorsInfo;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Recorder implements SensorEventListener {

    // make internal file. when recording starts, stream to file.

    private boolean started = false, stopped = false, paused = true;

    private String name;
    private long startDateTime;

    private Thread recorder;

    //TODO: recorderControlToken and started/paused/stopped bool may be integrated
    private volatile int recorderControlToken = 0; //0 : run, 1: pause, 2: stop

    private PipedInputStream pipeReader;
    private PipedOutputStream pipeWriter;
    private FileOutputStream fileWriter;

    private OutputStream externalListener;


    public void start(Context context) {
        if (started) throw new IllegalStateException("Recorder: Already started.");
        started = true;
        startDateTime = System.currentTimeMillis();

        String filename = startDateTime + ".txt";
        try {
            fileWriter = context.openFileOutput(filename, Context.MODE_PRIVATE);
            pipeWriter = new PipedOutputStream();
            pipeReader = new PipedInputStream();
            pipeWriter.connect(pipeReader);
            recorderControlToken = 0;

            recorder = new Thread(() -> {
                byte[] buf = new byte[8192];
                int length;
                try {
                    while (true) {
                        if (recorderControlToken == 0 && (length = pipeReader.read(buf)) > 0) {
                            fileWriter.write(buf, 0, length);
                            if (externalListener != null)
                                externalListener.write(buf, 0, length);
                            Log.i("Recorder", new String(buf, 0, length, StandardCharsets.UTF_8)); //TODO: REMOVE
                        }
                        else if (recorderControlToken == 2) {
                            pipeWriter.close();
                            pipeReader.close();
                            fileWriter.close();
                            return;
                        }
                        else Thread.sleep(100);
                    }
                }
                catch (IOException | InterruptedException ignore) {}
            });
        }
        catch (IOException ignore) {}

        recorder.start();
        resume();
    }

    public void stop() {
        if (!started) throw new IllegalStateException("Recorder: Not started.");
        if (stopped) throw new IllegalStateException("Recorder: Already stopped.");
        pause();
        stopped = true;
        recorderControlToken = 2;
    }

    public void pause() {
        if (!started) throw new IllegalStateException("Recorder: Not started.");
        if (stopped) throw new IllegalStateException("Recorder: Already stopped.");
        if (paused) return;
        paused = true;
        recorderControlToken = 1;
        DataManager.get().releaseSensors(this);
    }

    public void resume() {
        if (!started) throw new IllegalStateException("Recorder: Not started.");
        if (stopped) throw new IllegalStateException("Recorder: Already stopped.");
        if (!paused) return;
        paused = false;
        recorderControlToken = 0;
        DataManager.get().listenSensors(this);
    }

    public void setExternalListener(OutputStream listener) {
        //TODO: check safe?
        externalListener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        String sensorKey = "[" + sensor.getStringType() + "] " + sensor.getName();
        String formattedValue = DataManager.get().valueFormatter(event.values); //TODO: sensor type formatter.
        SensorsInfo.updateSensorValues(sensorKey, sensor, formattedValue);
        try {
            pipeWriter.write(
                    (System.currentTimeMillis() + "/ " + sensorKey + " : " + formattedValue + "\n")
                            .getBytes(StandardCharsets.UTF_8)
            );
        }
        catch (IOException ignore) {}
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
