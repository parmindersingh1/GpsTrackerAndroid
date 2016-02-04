package in.ezzie.gps.helper;

/**
 * Created by parminder on 28/1/16.
 */
public class responseMessage {
    private boolean error;
    private String message;
    private UserProfile profile;

    public responseMessage(){

    }
    public responseMessage(String message){
        this.message = message;
    }
    public responseMessage(String message, boolean error){
        this.message = message;
        this.error = true;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public boolean getError(){
        return this.error;
    }

    public void setError(boolean error){
        this.error= error;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }




}
