package in.ezzie.gps.service;

import android.app.IntentService;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


import in.ezzie.gps.MainActivity;
import in.ezzie.gps.app.Config;
import in.ezzie.gps.helper.PrefManager;
import in.ezzie.gps.helper.UserProfile;
import in.ezzie.gps.helper.responseMessage;

/**
 * Created by parminder on 27/1/16.
 */
public class HttpService extends IntentService {
    private static String TAG = HttpService.class.getSimpleName();

    public HttpService() {
        super(HttpService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String otp = intent.getStringExtra("otp");
            verifyOtp(otp);
        }
    }
    private void verifyOtp(final String otp) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("otp", otp);

            // Make the network request, posting the message, expecting a responseMessage in response from the server
            responseMessage response = Config.sendData(Config.URL_VERIFY_OTP,params,TAG);

            boolean error = response.getError();
            String message = response.getMessage();
            UserProfile profileObj = response.getProfile();

            if (!error) {

                String name = profileObj.getName();
                String vehicle_reg_no = profileObj.getVehicle_reg_no();
                String mobile = profileObj.getMobile();

                TelephonyManager tManager = (TelephonyManager)getSystemService(this.TELEPHONY_SERVICE);
                String uuid = tManager.getDeviceId();
                PrefManager pref = new PrefManager(getApplicationContext());
                pref.createLogin(name, vehicle_reg_no, mobile, uuid);

                Log.d(TAG,"Starting Main Activity");
                Intent intent = new Intent(HttpService.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

//                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

            } else {
                Log.d(TAG,message);
//                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

    }



}
