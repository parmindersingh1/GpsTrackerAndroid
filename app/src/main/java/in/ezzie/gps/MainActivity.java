package in.ezzie.gps;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import in.ezzie.gps.app.Config;
import in.ezzie.gps.gcm.RegistrationIntentService;
import in.ezzie.gps.helper.PrefManager;
import in.ezzie.gps.helper.responseMessage;
import in.ezzie.gps.service.BackgroundLocationService;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener  {
    private PrefManager pref;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private Button mLocationUpdatesButton;
    private Button mCurrentLocationButton;
    private int zoomLevel = Config.ZOOMLEVEL;
    private LocationUpdateReceiver receiver;

    BackgroundLocationService mLocationService;

    // boolean flags
    private boolean mServiceBound = false;
    private boolean mRequestingLocationUpdates = false;
    private boolean mBroacastRegistered = false;
    // Google Map
    private GoogleMap mGoogleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = new PrefManager(getApplicationContext());

//        TelephonyManager tManager = (TelephonyManager)getSystemService(this.TELEPHONY_SERVICE);
//        pref.setUUID(tManager.getDeviceId());

        // Checking if user session
        // if not logged in, take user to sms screen
        if (!pref.isLoggedIn()) {
            logout();
        }
        if (checkPlayServices() && pref.getKeyGcmRegid() == null) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

        // Displaying user information from shared preferences
//        HashMap<String, String> profile = pref.getUserDetails();
//        name.setText("Name: " + profile.get("name"));
//         ........
//        mobile.setText("Mobile: " + profile.get("mobile"));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mCurrentLocationButton = (Button) findViewById(R.id.btnCurrentLocation);

        mLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRequestingLocationUpdates) {
                    // means service is on
                    stopService();
                } else {
                    // start service
                    if(!Config.isConnected(MainActivity.this)) {
                        Toast.makeText(MainActivity.this,"Service Can't Started in Offline Mode",Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(!Config.isGpsEnabled(MainActivity.this)) {
                        Toast.makeText(MainActivity.this,"Enable Gps for Service",Toast.LENGTH_LONG).show();
                        return;
                    }
                    startService();

                }
            }
        });

        mCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCurrentLocation();
            }
        });

    }

    protected void startService(){
        Intent intent = new Intent(MainActivity.this, BackgroundLocationService.class);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        pref.setSessionID();
        updateUI();
    }

    protected void stopService(){
        Intent intent = new Intent(MainActivity.this, BackgroundLocationService.class);
        unRegisterLocationReciever();
        stopService(intent);
        pref.removeSessionID();
        updateUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unRegisterLocationReciever();
    }

    protected void showCurrentLocation() {
        if (mServiceBound) {
            Location location = mLocationService.getCurrentLocation();
            if (location != null) {
                Log.i("MyACTIVITY", "get current location" + String.valueOf(location.getLatitude()));
                handleNewLocation(location);
            }
        } else {
            Toast.makeText(MainActivity.this, "Service is not running",Toast.LENGTH_SHORT).show();
        }

    }

    private void updateUI() {
        if (Config.isMyServiceRunning(this, BackgroundLocationService.class)) {
            mRequestingLocationUpdates = true;
            registerLocationReciever();
            mLocationUpdatesButton.setText(R.string.btn_stop_location_updates);
        } else {
            mRequestingLocationUpdates = false;
            mLocationUpdatesButton.setText(R.string.btn_start_location_updates);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        MarkerOptions options = new MarkerOptions().position(latLng);
        options.title(getAddressFromLatLng(latLng));

        options.icon(BitmapDescriptorFactory.defaultMarker());
        showCurrentLocation();
        mGoogleMap.addMarker(options);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);

        mGoogleMap = googleMap;
    }

    private String getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this);
        String address = "";
        try {
            address = geocoder
                    .getFromLocation(latLng.latitude, latLng.longitude, 1)
                    .get(0).getAddressLine(0);
        } catch (IOException e) {
            return address;
        }
        return address;
    }

    public void handleNewLocation(Location location ){
        Log.i(TAG, String.valueOf(location.getLatitude()));
        //sendLocation(location);
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("I am here!");
        mGoogleMap.addMarker(options);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoomLevel));
    }


    private void registerLocationReciever() {
        //register broadcast reciever
        IntentFilter filter = new IntentFilter(LocationUpdateReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new LocationUpdateReceiver();
        registerReceiver(receiver, filter);
        mBroacastRegistered = true;
    }

    private void unRegisterLocationReciever() {
        if(mBroacastRegistered) {
            unregisterReceiver(receiver);
            mBroacastRegistered = false;
        }
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BackgroundLocationService.LocationBinder myBinder = (BackgroundLocationService.LocationBinder) service;
            mLocationService = myBinder.getServerInstance();
            mServiceBound = true;
        }
    };




//    private void updateMap() {
//        // Removes all markers, overlays, and polylines from the map.
//        map.clear();
//
//        // Move the camera instantly to defaultLatLng.
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, zoomLevel));
//        map.addMarker(new MarkerOptions().position(newLatLng)
//                .title("This is the title")
//                .snippet("This is the snippet within the InfoWindow"));
//    }




    /**
     * Logging out user
     * will clear all user shared preferences and navigate to
     * sms activation screen
     */
    private void logout() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String,String>();
        params.add("mobile", pref.getMobileNumber());
        new LogoutTask(params).execute();

    }

    /**
     * Upload user Image
     */
    private void uploadImage() {
        startActivity(new Intent(MainActivity.this, ImageUpload.class));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        if (id == R.id.action_upload) {
            uploadImage();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
    // formatted for mysql datetime format
//    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    dateFormat.setTimeZone(TimeZone.getDefault());
//    Date date = new Date(location.getTime());
//    dateFormat.format(date)

    public void sendLocation(Location location) {
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
            return Config.sendData(Config.URL_SEND_LOCATION,this.params,TAG);
        }

        @Override
        protected void onPostExecute(responseMessage responseMsg) {
            super.onPostExecute(responseMsg);
        }
    }

    public class LocationUpdateReceiver extends BroadcastReceiver {
        public static final String PROCESS_RESPONSE = "in.ezzie.locationupdates.intent.action.PROCESS_RESPONSE";
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(BackgroundLocationService.RESPONSE_LOC);
            Double responseLat = location.getLatitude();
            Double reponseLong = location.getLongitude();
            if(location!=null)
                handleNewLocation(location);
        }
    }

    private class LogoutTask extends AsyncTask<Void,Void,responseMessage> {
        private MultiValueMap<String, String> params;

        public LogoutTask(MultiValueMap<String, String> params){
            this.params = params;
        }
        @Override
        protected responseMessage doInBackground(Void... voids) {
            return Config.sendData(Config.URL_LOGOUT,this.params,TAG);
        }

        @Override
        protected void onPostExecute(responseMessage responseMsg) {
            super.onPostExecute(responseMsg);
            if(!responseMsg.getError()) {
                pref.clearSession();
                Intent intent = new Intent(MainActivity.this, SmsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);

                finish();
            }
        }
    }
}
