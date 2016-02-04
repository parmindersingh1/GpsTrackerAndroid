package in.ezzie.gps.app;


import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import in.ezzie.gps.helper.responseMessage;

/**
 * Created by parminder on 27/1/16.
 */
public class Config {
    // server URL configuration
     public static final String BASE_URL=  "http://192.168.1.2/gps";
//    public static final String BASE_URL=  "http://mygpstracking.esy.es";
    public static final String URL_REQUEST_SMS = BASE_URL + "/request_sms.php";
    public static final String URL_VERIFY_OTP = BASE_URL + "/verify_otp.php";
    public static final String URL_SEND_LOCATION = BASE_URL + "/update_location.php";
    public static final String URL_LOGOUT = BASE_URL + "/logout.php";
    public static final String URL_IMAGE_UPLOAD = BASE_URL + "/upload_image.php";

    // SMS provider identification
    // It should match with your SMS gateway origin
    // You can use  MSGIND, TESTER and ALERTS as sender ID
    public static final String SMS_ORIGIN = "CABSHR";

    public static final String EVENT_TYPE = "Android";
    // special character to prefix the otp. Make sure this character appears only once in the sms
    public static final String OTP_DELIMITER = ":";

    // Location updates intervals in sec
    public static int UPDATE_INTERVAL = 60 * 1000; // 1 min
    public static int FATEST_INTERVAL = 60 * 500; // 5 sec
    public static int DISPLACEMENT = 0; // 10 meters
    public static int ZOOMLEVEL = 13; // Map Zoom Level


    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;




    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public static responseMessage sendData(String url, MultiValueMap<String, String> params, String TAG){
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Connection", "Close");

        // Sending multipart/form-data
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Populate the MultiValueMap being serialized and headers in an HttpEntity object to use for the request
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(
                params, requestHeaders);

        // Create a new RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        // Add the Jackson and String message converters
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        if(requestHeaders ==null){
            return new responseMessage("request Header null");
        }
        if (requestEntity == null) {
            return new responseMessage("request Entity null");
        }
        if (restTemplate == null) {
            return new responseMessage("rest Template null");
        }
        if (restTemplate == null) {
            return new responseMessage("rest Template null");
        }
        try {
            // Make the network request, posting the message, expecting a String in response from the server
            ResponseEntity<responseMessage> response =  restTemplate.exchange(url, HttpMethod.POST, requestEntity,
                    responseMessage.class);
            Log.i(TAG, "Location Sent Successfully");
            return  response.getBody();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return  new responseMessage(e.getMessage(),true);
        }
    }

}
