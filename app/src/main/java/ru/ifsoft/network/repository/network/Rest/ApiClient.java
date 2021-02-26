package ru.ifsoft.network.repository.network.Rest;

/**
 * Created by ibraheem on 10/19/2018.
 */

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.ifsoft.network.app.App;
import ru.ifsoft.network.constants.AppConstants;
import ru.ifsoft.network.db.Session;


public class ApiClient {


    public static final String TAG = ApiClient.class.getSimpleName();
    /**
     * get http client for short time
     *
     * @param context
     * @param baseUrl
     * @return
     */
    public static final String SERVER_URL = "https://tasweeeg.com";
    public static final String API_VERSION = "v1";
    public static final String BASE_URL = SERVER_URL + "/api/" + API_VERSION + "/";
    private static Retrofit retrofit = null;

    public static ApiInterface getApiClient(Context context) {

        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();


        File cacheFile = new File(context.getCacheDir(), "http_cache");
        cacheFile.mkdir();
        Cache cache = new Cache(cacheFile, 10 * 1000 * 1000);


        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(50, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .cache(cache)
                .build();


        // Gson gson1 = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Gson gson1 = new GsonBuilder()
                .setLenient()
                .create();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson1))
                .client(okHttpClient)
                .build();
        return retrofit.create(ApiInterface.class);

    }


    private static class LoggingInterceptor implements Interceptor {


        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            /**
             * Add Headers to Request
             */
//            boolean isLoggedIn = AuthManager.getInstance().isUserLoggedIn();

            request = request.newBuilder()
                    .header(AppConstants.AUTHORIZATION, "Bearer " + Session.getInstance(App.getInstance().getApplicationContext()).getCurrentUserToken())
                    .header("Accept", "application/json")
                    .method(request.method(), request.body()).build();

            long t1 = System.nanoTime();

            Response response = chain.proceed(request);
            long t2 = System.nanoTime();

            String responseLog = String.format(Locale.US, "Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers());

            String bodyString = response.body().string();
            Log.d("TAG", "response" + "\n" + responseLog + "\n Response Body : " + bodyString);


            return response.newBuilder()
                    .body(ResponseBody.create(response.body().contentType(), bodyString))

                    .build();
        }
    }
}
