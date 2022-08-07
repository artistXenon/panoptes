package com.skca.panoptes.gnss;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import org.jetbrains.annotations.NotNull;

public class GoogleApiCallbacks  implements OnConnectionFailedListener, ConnectionCallbacks {

    private static final String TAG = "GoogleApiCallbacks";

    @Override
    public void onConnectionFailed(@NonNull @NotNull ConnectionResult connectionResult) {
        if (Log.isLoggable(TAG, Log.INFO)){
            Log.i(TAG,  "Connection failed: ErrorCode = " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onConnected(@Nullable @org.jetbrains.annotations.Nullable Bundle bundle) {
        if (Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, "Connected to GoogleApiClient");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

}
