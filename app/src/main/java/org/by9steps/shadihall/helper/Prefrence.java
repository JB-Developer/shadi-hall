package org.by9steps.shadihall.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefrence {

    String PREFS_FILENAME = "org.by9steps.shadihall.helper.Prefrence";
    String CLIENT_ID_SESSION = "client_id_session";
    String USER_RIGHTS_SESSION = "user_rights_session";
    String PROJECT_ID_SESSION = "project_id_session";
    String CLIENT_USER_ID_SESSION = "client_user_id_session";

    Context context;
    SharedPreferences prefs;

    public Prefrence(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getClientIDSession(){
        return prefs.getString(CLIENT_ID_SESSION,"0");
    }
    public void setClientIDSession(String value){
        prefs.edit().putString(CLIENT_ID_SESSION, value).apply();
    }
    public String getUserRighhtsSession(){
        return prefs.getString(USER_RIGHTS_SESSION,"0");
    }
    public void setUserRighhtsSession(String value){
        prefs.edit().putString(USER_RIGHTS_SESSION, value).apply();
    }
    public String getProjectIDSession(){
        return prefs.getString(PROJECT_ID_SESSION,"0");
    }
    public void setProjectIDSession(String value){
        prefs.edit().putString(PROJECT_ID_SESSION, value).apply();
    }
    public String getClientUserIDSession(){
        return prefs.getString(CLIENT_USER_ID_SESSION,"0");
    }
    public void setClientUserIDSession(String value){
        prefs.edit().putString(USER_RIGHTS_SESSION, value).apply();
    }
}
