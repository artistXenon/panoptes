package com.skca.panoptes.hardware.specification;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import androidx.annotation.NonNull;
import com.skca.panoptes.helper.Terminal;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/*
* NOTE: cpu information is only collected through reading /proc fs with cat cmd with format of multiple lines of  <key : value>.
* device and system info are obtained separately by get action but are temporarily formatted in the same way cpu info is collected.
*
* */
public class DeviceInfo {

    private String rawCpuInformation = "";
    private String rawDeviceInformation;
    private String rawSystemInformation;
    private String rawBatteryInformation;

    public DeviceInfo(Context context) {
        collectRawCpuInfo();
        collectRawDeviceInfo(context);
        collectRawSystemInfo();
//        collectBatteryInfo(context);
    }

    private void collectRawCpuInfo() {
        try {
            rawCpuInformation =  Terminal.exec("cat /proc/cpuinfo");
        } catch (IOException ignore) {}
    }

    private void collectRawDeviceInfo(Context context) {
        StringBuilder device = new StringBuilder();
        device.append("Model : " + Build.MODEL + "\n");
        device.append("Manufacturer : " + Build.MANUFACTURER + "\n");
        device.append("Board : " + Build.BOARD + "\n");
        device.append("Hardware : " + Build.HARDWARE + "\n");

        ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        if (actManager != null) {
            actManager.getMemoryInfo(memInfo);
            device.append("RAM : " + memInfo.totalMem + " B\n");
        }
        else device.append("RAM : ? B\n");

        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        device.append("Storage : " + stat.getBlockSizeLong() * stat.getBlockCountLong() + " B\n");

        rawDeviceInformation = device.toString();
    }

    private void collectRawSystemInfo() {
        StringBuilder system = new StringBuilder();
        system.append("OS Version : " + Build.VERSION.SDK_INT + "\n");
        system.append("Security Patch : " + android.os.Build.VERSION.SECURITY_PATCH + "\n");
        system.append("Build ID : " + Build.FINGERPRINT + "\n");
        system.append("ART Version : " + System.getProperty("java.vm.version") + "\n");
        system.append("ABIs : ");
        for (String abi : Build.SUPPORTED_ABIS) {
            system.append(abi + ", ");
        }
        system.setLength(system.length() - 2);
        system.append("\n");
        system.append("Kernel Architecture : " + System.getProperty("os.arch") + "\n");
        system.append("Kernel Version : " + System.getProperty("os.version") + "\n");
        
        rawSystemInformation = system.toString();
    }

    // TODO: WARNING not safe way to collect info. independent class required for asynchronous intent calls.
    private void collectBatteryInfo(Context context) {
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                StringBuilder battery = new StringBuilder();
                int h = intent.getIntExtra(BatteryManager.EXTRA_HEALTH,0);

                battery.append("Battery Status : ");
                battery.append(
                        h == BatteryManager.BATTERY_HEALTH_COLD ? "Cold" :
                        h == BatteryManager.BATTERY_HEALTH_DEAD ? "Dead" :
                        h == BatteryManager.BATTERY_HEALTH_GOOD ? "Good" :
                        h == BatteryManager.BATTERY_HEALTH_OVERHEAT ? "Overheat" :
                        h == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE ? "Over Voltage" :
                        "Unknown");

                battery.append("\n");


                rawBatteryInformation = battery.toString();
            }
        }, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    public String getRawCpuInformation() {
        return rawCpuInformation;
    }

    public String getRawDeviceInformation() {
        return rawDeviceInformation;
    }

    public String getRawSystemInformation() {
        return rawSystemInformation;
    }

    @NonNull
    @NotNull
    @Override
    public String toString() {
        return "== CPU INFORMATION == \n" +
            getRawCpuInformation() + "\n\n" +
            "== DEVICE INFORMATION == \n" +
            getRawDeviceInformation() + "\n\n" +
            "== SYSTEM INFORMATION == \n" +
            getRawSystemInformation() + "\n";
    }
}
