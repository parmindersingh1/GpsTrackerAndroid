package in.ezzie.gps.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;

import in.ezzie.gps.R;
import in.ezzie.gps.app.Config;
import in.ezzie.gps.helper.PrefManager;
import in.ezzie.gps.helper.responseMessage;

/**
 * Created by parminder on 18/2/16.
 */
public class RegistrationIntentService extends IntentService {
    private static final String TAG = "RegisterIntentService";
    private static final String[] TOPICS = {"global"};
    private static final String WEB_SERVER_URL = "http://192.168.1.4/demo_gcm/register_user.php";
    private PrefManager pref;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        pref =  new PrefManager(getApplicationContext());

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_sender_id),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);
            sendRegistrationToServer(token);

        } catch (Exception e) {
            Log.e(TAG, "Failed to complete token refresh", e);
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
//        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.

        MultiValueMap<String, String> params = new LinkedMultiValueMap<String,String>();
        params.add("regId",token);
        params.add("mobile",pref.getMobileNumber());
        responseMessage responseMsg = Config.sendData(Config.URL_GCM_REGISTER, params, TAG);
        Log.i(TAG,responseMsg.getMessage());

//        URL url = null;
//        try {
//            url = new URL(WEB_SERVER_URL);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        Map<String, String> dataMap = new HashMap<String, String>();
//        dataMap.put("regId", token);
//
//        StringBuilder postBody = new StringBuilder();
//        Iterator iterator = dataMap.entrySet().iterator();
//
//        while (iterator.hasNext()) {
//            Map.Entry param = (Map.Entry) iterator.next();
//            postBody.append(param.getKey()).append('=')
//                    .append(param.getValue());
//            if (iterator.hasNext()) {
//                postBody.append('&');
//            }
//        }
//        String body = postBody.toString();
//        byte[] bytes = body.getBytes();
//
//        HttpURLConnection conn = null;
//        try {
//            conn = (HttpURLConnection) url.openConnection();
//            conn.setDoOutput(true);
//            conn.setUseCaches(false);
//            conn.setFixedLengthStreamingMode(bytes.length);
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type",
//                    "application/x-www-form-urlencoded;charset=UTF-8");
//
//            OutputStream out = conn.getOutputStream();
//            out.write(bytes);
//            out.close();
//
//            int status = conn.getResponseCode();
//            if (status == 200) {
//                // Request success
//                Log.i(TAG,"Request Success");
//            } else {
//                throw new IOException("Request failed with error code "
//                        + status);
//            }
//        } catch (ProtocolException pe) {
//            pe.printStackTrace();
//            Log.e(TAG, "Request Failure");
//        } catch (IOException io) {
//            io.printStackTrace();
//            Log.e(TAG, "Request Failure");
//        } finally {
//            if (conn != null) {
//                conn.disconnect();
//            }
//        }


    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]
}
