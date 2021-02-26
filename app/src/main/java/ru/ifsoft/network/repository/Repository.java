package ru.ifsoft.network.repository;

import android.content.Context;

import ru.ifsoft.network.repository.network.Rest.ApiClient;
import ru.ifsoft.network.repository.network.Rest.ApiInterface;

public class Repository {

    private static final String TAG = "Repository";
    private static Repository instance;
    private static ApiInterface apiClient;
    Context mContext;

    public Repository(Context context) {

        mContext = context;

        apiClient = ApiClient.getApiClient(context);


    }

    public static synchronized Repository getInstance(Context context) {
        if (instance == null) {
            instance = new Repository(context);
        }
        return instance;
    }


}
