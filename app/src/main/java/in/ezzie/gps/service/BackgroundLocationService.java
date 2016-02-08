package in.ezzie.gps.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import in.ezzie.gps.MainActivity;
import in.ezzie.gps.app.Config;
import in.ezzie.gps.helper.PrefManager;
import in.ezzie.gps.helper.responseMessage;

/**
 * Created by parminder on 2/2/16.
 */
public class BackgroundLocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    protected static final String TAG = "BackgroundLocationService";

    public static final String RESPONSE_LOC = "newLocation";

    public static final int UPDATE_INTERVAL_IN_MILLISECONDS = Config.UPDATE_INTERVAL;
    public static final int FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = Config.FATEST_INTERVAL;
    public static final int DISPLACEMENT = Config.DISPLACEMENT;

    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;

    private PrefManager pref;

    IBinder mBinder = new LocationBinder();

    public class LocationBinder extends Binder {
        public BackgroundLocationService getServerInstance() {
            return BackgroundLocationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
        pref = new PrefManager(getApplicationContext());
        buildGoogleApiClient();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (mGoogleApiClient.isConnected()) {
            Log.i(TAG + " onStartCommand", "GoogleApiClient Connected");
            return START_STICKY;
        }

        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
            Log.i(TAG + " onStartCommand", "GoogleApiClient not Connected");
            mGoogleApiClient.connect();
        }

        return START_STICKY;
    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        Log.i(TAG, "createLocationRequest()");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        Log.i(TAG, "Started Location Updates");

        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        Log.i(TAG, "Stopped Location Updates");

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        startLocationUpdates();
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "----onLocationChanged");
        if(location!= null) {
            sendLocationForMap(location);
            sendLocationToServer(location);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    public Location getCurrentLocation() {
        // fetch the current location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        return location;
    }

       @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    protected void sendLocationForMap(Location location) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.LocationUpdateReceiver.PROCESS_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(RESPONSE_LOC, location);
        sendBroadcast(broadcastIntent);
    }

    protected  void sendLocationToServer(Location location) {
        // formatted for mysql datetime format
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date date = new Date(location.getTime());

        HashMap<String, String> profile = pref.getUserDetails();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String,String>();
        params.add("latitude", String.valueOf(location.getLatitude()));
        params.add("longitude", String.valueOf(location.getLongitude()));
        params.add("mobile", profile.get("mobile"));
        params.add("vehicle_id", profile.get("vehicle_reg_no"));
        params.add("event_type", Config.EVENT_TYPE);
        params.add("uuid", pref.getUUID());
        params.add("session_id",pref.getSessionID());
        params.add("gpsTime", dateFormat.format(date));

        new SendLocationTask(params).execute();

    }

    private class SendLocationTask extends AsyncTask<Void,Void,responseMessage> {
        private MultiValueMap<String, String> params;

        public SendLocationTask(MultiValueMap<String, String> params){
            this.params = params;
        }
        @Override
        protected responseMessage doInBackground(Void... voids) {
            if(Config.isConnected(BackgroundLocationService.this)) {
                return Config.sendData(Config.URL_SEND_LOCATION, this.params, TAG);
            } else{
                return new responseMessage("Not Connected to Network");
            }
        }

        @Override
        protected void onPostExecute(responseMessage responseMsg) {
            super.onPostExecute(responseMsg);
            Log.i(TAG,responseMsg.getMessage());
        }
    }


}
