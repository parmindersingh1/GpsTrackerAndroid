package in.ezzie.gps.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.ezzie.gps.app.Config;
import in.ezzie.gps.helper.GPSDatabase;
import in.ezzie.gps.helper.PrefManager;
import in.ezzie.gps.helper.responseMessage;

/**
 * Created by parminder on 8/2/16.
 */
public class InternetConnectionReceiver  extends BroadcastReceiver {
    private static final String TAG = InternetConnectionReceiver.class.getSimpleName();
    private PrefManager pref;
    protected GPSDatabase myDatabase;
    protected String json_string;
    protected Button sendLocButton;
    protected Cursor cursor;
    @Override
    public void onReceive(Context context, Intent intent) {
        // Make sure it's an event we're listening for ...
        if (!intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) &&
                !intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION) &&
                !intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            return;
        }
        if(Config.isConnected(context)) {

            myDatabase=new GPSDatabase(context);
            myDatabase.open();
            cursor=myDatabase.getAllRows();
            cursor.moveToFirst();
            pref=  new PrefManager(context);
            HashMap<String, String> profile = pref.getUserDetails();

            json_string ="{\"upload_locations\":[";

            List listContents= new ArrayList();
            try {
                for (int i = 0; i < cursor.getCount(); i++) {
                    listContents.add("Lat=" +cursor.getString(1) +"  "+"Log "+ cursor.getString(2));
                    //Repeat and loop this until all objects are added (and add try+catch)
                    JSONObject obj_new = new JSONObject();
                    obj_new.put("latitude", cursor.getString(1));
                    obj_new.put("longitude", cursor.getString(2));
                    obj_new.put("mobile", profile.get("mobile"));
                    obj_new.put("vehicle_id", profile.get("vehicle_reg_no"));
                    obj_new.put("event_type", Config.EVENT_TYPE);
                    obj_new.put("uuid", pref.getUUID());
                    obj_new.put("session_id", cursor.getString(3));
                    obj_new.put("gpsTime", cursor.getString(4));
                    json_string = json_string + obj_new.toString() + ",";
                    cursor.moveToNext();
                }
            } catch (JSONException e){

            } finally {
                myDatabase.close();
            }
            //Close JSON string
            json_string = json_string.substring(0, json_string.length()-1);
            json_string += "]}";

            MultiValueMap<String, String> params = new LinkedMultiValueMap<String,String>();
            params.add("locations",json_string);
            new OfflineLocationsTask(params).execute();
        }

    }


    public class OfflineLocationsTask extends AsyncTask<Void,Void,responseMessage> {
        private  MultiValueMap<String, String> params;

        public OfflineLocationsTask(MultiValueMap<String, String> params){
            this.params = params;
        }
        @Override
        protected responseMessage doInBackground(Void... params) {
            return Config.sendData(Config.URL_LOCATIONS_UPLOAD,this.params,"InternetConnectionReceiver");
        }

        @Override
        protected void onPostExecute(responseMessage responseMessage) {
            super.onPostExecute(responseMessage);
            if(!responseMessage.getError()){
                myDatabase.open();
                myDatabase.deleteAll();
                myDatabase.close();
            }
            Log.d("InternetConnectionReceiver",responseMessage.getMessage());
        }
    }
}
