package ru.ifsoft.network.repository.local;

import android.content.Context;

import ru.ifsoft.network.db.SharedPrefs;


public class LocalSave {
    private static final String KEY_TOKEN = "api_token";
    private static final String KEY_USER = "user";
    private static final String KEY_USER_INFO = "user_info";
    private static final String IS_LOGIN      = "is_login";
    private static final String IS_FIRST      = "isFirstTime";
    private static final String SHARE_URL = "share_url";
    private static final String SHARE_KEY = "share_key";


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


    public void clear() {
        SharedPrefs.removeKey(getmContext(), KEY_USER);
        SharedPrefs.removeKey(getmContext(), KEY_TOKEN);
        SharedPrefs.removeKey(getmContext(), KEY_USER_INFO);
        SharedPrefs.removeKey(getmContext(),IS_LOGIN);
        SharedPrefs.removeKey(getmContext(),SHARE_KEY);
        SharedPrefs.removeKey(getmContext(),SHARE_URL);
    }

    public void setLoginAsGuest(boolean isLogin) {
        SharedPrefs.save(getmContext(),IS_LOGIN,isLogin);

    }
    public boolean isLoginGuest(){
        return SharedPrefs.getBoolean(getmContext(),IS_LOGIN);
    }

    public String getShareUrl() {
        return ru.ifsoft.network.repository.local.prefs.SharedPrefs.getString(getmContext(),SHARE_URL);
    }

    public void setShareUrl(String url){
        ru.ifsoft.network.repository.local.prefs.SharedPrefs.save(mContext,SHARE_URL,url);
    }

    public void setShareCode(String s) {
        ru.ifsoft.network.repository.local.prefs.SharedPrefs.save(mContext,SHARE_KEY,s);
    }

    public String getShareCode() {
        return ru.ifsoft.network.repository.local.prefs.SharedPrefs.getString(getmContext(),SHARE_KEY);

    }
}
