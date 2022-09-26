package com.skca.panoptes.helper;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import com.skca.panoptes.MainActivity;
import com.skca.panoptes.gnss.MeasurementProvider;
import com.skca.panoptes.hardware.DataManager;
import com.skca.panoptes.hardware.sensors.SensorsInfo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Recorder implements SensorEventListener {

    // make internal file. when recording starts, stream to file.


    public Map<String, Long> counter = new HashMap<>();
    private MainActivity mainActivity;
    private String fileName;

    private Thread recorder;

    //TODO: recorderControlToken and started/paused/stopped bool may be integrated
    private volatile int recorderControlToken = 0; //0 : run, 1: pause, 2: stop

    private PipedInputStream pipeReader;
    private PipedOutputStream pipeWriter;
    private FileOutputStream fileWriter;

    private OutputStream externalListener;

    private StringBuilder stringBuilder;
    public boolean isRecording = false;

    public Recorder(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }


    public void start(Context context) {
        stringBuilder = new StringBuilder();
        counter = new HashMap<>();

        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            fileName = "SENSOR_LOG" + System.currentTimeMillis() + ".txt";
            File target = new File(dir, fileName);

            fileWriter = new FileOutputStream(target);
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
                        else Thread.sleep(50);
                    }
                }
                catch (IOException | InterruptedException ignore) {}
            });
        }
        catch (IOException ignore) {
            Log.e("IOE", ignore.getMessage(), ignore);
        }

        recorder.start();

        try {
            pipeWriter. write(DataManager.get().getDeviceInfo().toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignore) {}
        resume();
    }

    public void stop() {
        pause();
        Toast.makeText(mainActivity, "Saved as Downloads/" + fileName, Toast.LENGTH_LONG).show();

        recorderControlToken = 2;
    }

    public void pause() {
        recorderControlToken = 1;
        DataManager.get().releaseSensors(this);
        MeasurementProvider measurementProvider = MeasurementProvider.get();
        measurementProvider.unregisterLocation();
        measurementProvider.unregisterNmea();

        isRecording = false;
    }

    public void resume() {
        recorderControlToken = 0;
        DataManager.get().listenSensors(this);
        MeasurementProvider measurementProvider = MeasurementProvider.get();
        if (measurementProvider.listen) {
            measurementProvider.registerLocation();
            measurementProvider.registerNmea();
        }
        isRecording = true;
    }

    public void setExternalListener(OutputStream listener) {
        //TODO: check safe?
        externalListener = listener;
    }

    public void record(String log) {
        try {
            pipeWriter.write((log + "\n").getBytes(StandardCharsets.UTF_8));
        }
        catch (IOException ignore) {}
    }

    public String getLog() {
        return stringBuilder == null ? "" : stringBuilder.toString();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        String type = sensor.getStringType();
        String sensorKey = "[" + type.substring(15) + "] " + sensor.getName();
        counter.put(sensorKey, counter.getOrDefault(sensorKey, 0L) + 1);
        String formattedValue = DataManager.get().valueFormatter(event.values); //TODO: sensor type formatter.
        record(System.currentTimeMillis() + "/ " + sensorKey + " : " + formattedValue);

        /*

        SensorsInfo.updateSensorValues(sensorKey, sensor, formattedValue);
        try {
            pipeWriter.write(
                    (System.currentTimeMillis() + "/ " + sensorKey + " : " + formattedValue + "\n")
                            .getBytes(StandardCharsets.UTF_8)
            );
        }
        catch (IOException ignore) {}
        */
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
