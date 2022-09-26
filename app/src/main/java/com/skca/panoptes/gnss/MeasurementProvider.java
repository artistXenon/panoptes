package com.skca.panoptes.gnss;


import android.content.Context;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;
import android.os.SystemClock;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.skca.panoptes.hardware.DataManager;


/**
 * A container for measurement-related API calls. It binds the measurement providers with the
 * various {@link MeasurementListener} implementations.
 */
public class MeasurementProvider {

    private static MeasurementProvider _instance;
    public static final String TAG = "MeasurementProvider";
    private static final long LOCATION_RATE_GPS_MS = TimeUnit.SECONDS.toMillis(1L);
    private static final long LOCATION_RATE_NETWORK_MS = TimeUnit.SECONDS.toMillis(60L);

    public static synchronized MeasurementProvider get() { // Thread safe singleton class.
        if (_instance == null) throw new NullPointerException("Not initialized");
        return _instance;
    }

    public boolean listen = false;
    private boolean mLogLocations = true;
    private boolean mLogNavigationMessages = true;
    private boolean mLogMeasurements = true;
    private boolean mLogStatuses = true;
    private boolean mLogNmeas = true;
    private long registrationTimeNanos = 0L;
    private long firstLocationTimeNanos = 0L;
    private long ttff = 0L;
    private boolean firstTime = true;

    GoogleApiClient mGoogleApiClient;
    public final MainLogger mListeners;

    private final LocationManager mLocationManager;
    private final android.location.LocationListener mLocationListener =
            new android.location.LocationListener() {

                @Override
                public void onProviderEnabled(String provider) {
                    if (mLogLocations) {
                        mListeners.onProviderEnabled(provider);
                    }
                }

                @Override
                public void onProviderDisabled(String provider) {
                    if (mLogLocations) {
                        mListeners.onProviderDisabled(provider);
                    }
                }

                @Override
                public void onLocationChanged(Location location) {
                    if (firstTime && location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                        if (mLogLocations) {
                            firstLocationTimeNanos = SystemClock.elapsedRealtimeNanos();
                            ttff = firstLocationTimeNanos - registrationTimeNanos;
                            mListeners.onTTFFReceived(ttff);
                        }
                        firstTime = false;
                    }
                    if (mLogLocations) {
                        mListeners.onLocationChanged(location);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    if (mLogLocations) {
                        mListeners.onLocationStatusChanged(provider, status, extras);
                    }
                }
            };

    private final com.google.android.gms.location.LocationListener mFusedLocationListener =
            new com.google.android.gms.location.LocationListener() {

                @Override
                public void onLocationChanged(Location location) {
                    if (firstTime && location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                        if (mLogLocations) {
                                firstLocationTimeNanos = SystemClock.elapsedRealtimeNanos();
                                ttff = firstLocationTimeNanos - registrationTimeNanos;
                            mListeners.onTTFFReceived(ttff);
                        }
                        firstTime = false;
                    }
                    if (mLogLocations) {
                        mListeners.onLocationChanged(location);
                    }
                }
            };

    private final GnssMeasurementsEvent.Callback gnssMeasurementsEventListener =
            new GnssMeasurementsEvent.Callback() {
                @Override
                public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
                    if (mLogMeasurements) {
                        mListeners.onGnssMeasurementsReceived(event);
                    }
                }

                @Override
                public void onStatusChanged(int status) {
                    if (mLogMeasurements) {
                        mListeners.onGnssMeasurementsStatusChanged(status);
                    }
                }
            };

    private final GnssNavigationMessage.Callback gnssNavigationMessageListener =
            new GnssNavigationMessage.Callback() {
                @Override
                public void onGnssNavigationMessageReceived(GnssNavigationMessage event) {
                    if (mLogNavigationMessages) {
                        mListeners.onGnssNavigationMessageReceived(event);
                    }
                }

                @Override
                public void onStatusChanged(int status) {
                    if (mLogNavigationMessages) {
                        mListeners.onGnssNavigationMessageStatusChanged(status);
                    }
                }
            };

    private final GnssStatus.Callback gnssStatusListener =
            new GnssStatus.Callback() {
                @Override
                public void onStarted() {}

                @Override
                public void onStopped() {}

                @Override
                public void onFirstFix(int ttff) {}

                @Override
                public void onSatelliteStatusChanged(GnssStatus status) {
                    mListeners.onGnssStatusChanged(status);
                }
            };

    private final OnNmeaMessageListener nmeaListener =
            new OnNmeaMessageListener() {
                @Override
                public void onNmeaMessage(String s, long l) {
                    if (mLogNmeas) {
                        mListeners.onNmeaReceived(l, s);
                    }
                }
            };

    public MeasurementProvider(Context context, GoogleApiClient client, MainLogger loggers) {
        this.mListeners = loggers;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.mGoogleApiClient = client;
        MeasurementProvider._instance = this;
    }

    public LocationManager getLocationManager() {
        return mLocationManager;
    }

    public void setLogLocations(boolean value) {
        mLogLocations = value;
    }

    public boolean canLogLocations() {
        return mLogLocations;
    }

    public void setLogNavigationMessages(boolean value) {
        mLogNavigationMessages = value;
    }

    public boolean canLogNavigationMessages() {
        return mLogNavigationMessages;
    }

    public void setLogMeasurements(boolean value) {
        mLogMeasurements = value;
    }

    public boolean canLogMeasurements() {
        return mLogMeasurements;
    }

    public void setLogStatuses(boolean value) {
        mLogStatuses = value;
    }

    public boolean canLogStatuses() {
        return mLogStatuses;
    }

    public void setLogNmeas(boolean value) {
        mLogNmeas = value;
    }

    public boolean canLogNmeas() {
        return mLogNmeas;
    }

    public void registerLocation() {
        boolean isGpsProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGpsProviderEnabled) {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    LOCATION_RATE_NETWORK_MS,
                    0.0f /* minDistance */,
                    mLocationListener);
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_RATE_GPS_MS,
                    0.0f /* minDistance */,
                    mLocationListener);
        }
        logRegistration("LocationUpdates", isGpsProviderEnabled);
    }

    public void unregisterLocation() {
        mLocationManager.removeUpdates(mLocationListener);
    }

    public void registerFusedLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(100);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, mFusedLocationListener);
    }

    public void unRegisterFusedLocation() {
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mFusedLocationListener);
        }
    }

    public void registerSingleNetworkLocation() {
        boolean isNetworkProviderEnabled =
                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isNetworkProviderEnabled) {
            mLocationManager.requestSingleUpdate(
                    LocationManager.NETWORK_PROVIDER, mLocationListener, null);
        }
        logRegistration("LocationUpdates", isNetworkProviderEnabled);
    }

    public void registerSingleGpsLocation() {
        boolean isGpsProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGpsProviderEnabled) {
            this.firstTime = true;
            registrationTimeNanos = SystemClock.elapsedRealtimeNanos();
            mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, mLocationListener, null);
        }
        logRegistration("LocationUpdates", isGpsProviderEnabled);
    }

    public void registerMeasurements() {
        logRegistration(
                "GnssMeasurements",
                mLocationManager.registerGnssMeasurementsCallback(gnssMeasurementsEventListener));
    }

    public void unregisterMeasurements() {
        mLocationManager.unregisterGnssMeasurementsCallback(gnssMeasurementsEventListener);
    }

    public void registerNavigation() {
        logRegistration(
                "GpsNavigationMessage",
                mLocationManager.registerGnssNavigationMessageCallback(gnssNavigationMessageListener));
    }

    public void unregisterNavigation() {
        mLocationManager.unregisterGnssNavigationMessageCallback(gnssNavigationMessageListener);
    }

    public void registerGnssStatus() {
        logRegistration("GnssStatus", mLocationManager.registerGnssStatusCallback(gnssStatusListener));
    }

    public void unregisterGpsStatus() {
        mLocationManager.unregisterGnssStatusCallback(gnssStatusListener);
    }

    public void registerNmea() {
        mListeners.count = 0;
        logRegistration("Nmea", mLocationManager.addNmeaListener(nmeaListener));
    }

    public void unregisterNmea() {
        mLocationManager.removeNmeaListener(nmeaListener);
    }

    public void registerAll() {
        registerLocation();
        registerMeasurements();
        registerNavigation();
        registerGnssStatus();
        registerNmea();
    }

    public void unregisterAll() {
        unregisterLocation();
        unregisterMeasurements();
        unregisterNavigation();
        unregisterGpsStatus();
        unregisterNmea();
    }

    private void logRegistration(String listener, boolean result) {
        mListeners.onListenerRegistration(listener, result);
    }
}
