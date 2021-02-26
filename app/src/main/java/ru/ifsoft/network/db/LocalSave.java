package ru.ifsoft.network.db;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ru.ifsoft.network.model.PostInShared;

public class LocalSave {
    private static final String KEY_TOKEN = "api_token";
    private static final String KEY_USER_INFO = "user_info";
    private static final String IS_LOGIN      = "is_login";
    private static final String IS_FIRST      = "isFirstTime";
    private static final String KEY_SAVE_POST      = "savePost";



    private static LocalSave instance = null;
    private Context mContext;

    private LocalSave(Context mContext) {
        this.mContext = mContext;
    }

    public static LocalSave getInstance(Context context) {
        if (instance == null) {
            instance = new LocalSave(context);
        }
        return instance;
    }

    private Context getmContext() {
        return mContext;
    }

    public void saveUserToken(String token) {
        SharedPrefs.save(getmContext(), KEY_TOKEN, token);
    }

    public String getUserToken() {
        return SharedPrefs.getString(getmContext(), KEY_TOKEN);
    }
    public void setFirstTime(boolean isFirstTime){
        SharedPrefs.save(getmContext(),IS_FIRST,isFirstTime);
    }
    public boolean isFirstTime(){
        return SharedPrefs.getBoolean(getmContext(),IS_FIRST,true);
    }


/*
    public void saveCurrentUser(EditProfile user) {
        SharedPrefs.save(getmContext(), KEY_USER, new Gson().toJson(user));
    }
    public EditProfile getCurrentUser() {
        return new Gson().fromJson(SharedPrefs.getString(getmContext(), KEY_USER), EditProfile.class);
    }

    public void seveUserInformation(EditProfile editProfile){
        SharedPrefs.save(getmContext(),KEY_USER_INFO,new Gson().toJson(editProfile));
    }
    public EditProfile editProfile(){
        return new Gson().fromJson(SharedPrefs.getString(getmContext(),KEY_USER_INFO),EditProfile.class);
    }
*/

    public void savePost(PostInShared postInShared){
        GsonBuilder gsonBuilder = new GsonBuilder();

        // This is the main class for using Gson. Gson is typically used by first constructing a Gson instance and then invoking toJson(Object) or fromJson(String, Class) methods on it.
        // Gson instances are Thread-safe so you can reuse them freely across multiple threads.
        Gson gson = gsonBuilder.create();

        SharedPrefs.save(getmContext(),KEY_SAVE_POST,gson.toJson(postInShared));
    }
    public PostInShared getPost(){
        GsonBuilder gsonBuilder = new GsonBuilder();

        // This is the main class for using Gson. Gson is typically used by first constructing a Gson instance and then invoking toJson(Object) or fromJson(String, Class) methods on it.
        // Gson instances are Thread-safe so you can reuse them freely across multiple threads.
        Gson gson = gsonBuilder.create();
        return gson.fromJson(SharedPrefs.getString(getmContext(),KEY_SAVE_POST),PostInShared.class);
    }
    public void clear() {
        SharedPrefs.removeKey(getmContext(), KEY_TOKEN);
        SharedPrefs.removeKey(getmContext(), KEY_USER_INFO);
        SharedPrefs.removeKey(getmContext(),IS_LOGIN);
    }
    public void clearPost(){
        SharedPrefs.removeKey(getmContext(),KEY_SAVE_POST);
    }

    public void setLoginAsGuest(boolean isLogin) {
        SharedPrefs.save(getmContext(),IS_LOGIN,isLogin);

    }
    public boolean isLoginGuest(){
        return SharedPrefs.getBoolean(getmContext(),IS_LOGIN);
    }
}
