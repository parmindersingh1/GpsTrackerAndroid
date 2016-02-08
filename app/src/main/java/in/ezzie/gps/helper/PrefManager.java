package in.ezzie.gps.helper;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by parminder on 27/1/16.
 */
public class PrefManager {

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "EZZIE_GPS";

    // All Shared Preferences Keys
    private static final String KEY_IS_WAITING_FOR_SMS = "IsWaitingForSms";
    private static final String KEY_MOBILE_NUMBER = "mobile_number";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_NAME = "name";
    private static final String KEY_REG = "registration_no";
    private static final String KEY_MOBILE = "mobile";
    private static final String KEY_UUID = "uuid";
    private static final String KEY_IMAGE = "uImage";
    private static final String KEY_SESSION = "uImage";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setIsWaitingForSms(boolean isWaiting) {
        editor.putBoolean(KEY_IS_WAITING_FOR_SMS, isWaiting);
        editor.commit();
    }

    public boolean isWaitingForSms() {
        return pref.getBoolean(KEY_IS_WAITING_FOR_SMS, false);
    }

    public void setMobileNumber(String mobileNumber) {
        editor.putString(KEY_MOBILE_NUMBER, mobileNumber);
        editor.commit();
    }

    public String getMobileNumber() {
        return pref.getString(KEY_MOBILE_NUMBER, null);
    }

    public void setUUID(String uuid) {
        editor.putString(KEY_UUID, uuid);
        editor.commit();
    }

    public String getUUID() {
        return pref.getString(KEY_UUID, null);
    }

    public String getSessionID() {
        return pref.getString(KEY_SESSION, null);
    }

    public void setSessionID() {
        editor.putString(KEY_SESSION, UUID.randomUUID().toString());
        editor.commit();
    }
    public void removeSessionID() {
        editor.remove(KEY_SESSION);
        editor.commit();
    }

    public void setImage(String base64) {
        editor.putString(KEY_IMAGE, base64);
        editor.commit();
    }

    public String getImage() {
        return pref.getString(KEY_IMAGE, null);
    }

    public void createLogin(String name, String vehicle_reg_no, String mobile, String uuid) {
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_REG, vehicle_reg_no);
        editor.putString(KEY_MOBILE, mobile);
        editor.putString(KEY_UUID,uuid);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);

        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void clearSession() {
        editor.clear();
        editor.commit();
    }

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> profile = new HashMap<>();
        profile.put("name", pref.getString(KEY_NAME, null));
        profile.put("vehicle_reg_no", pref.getString(KEY_REG, null));
        profile.put("mobile", pref.getString(KEY_MOBILE, null));
        return profile;
    }
}
