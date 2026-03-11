package com.example.lab10.api;

import com.example.lab10.models.Movie;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static Retrofit retrofit = null;

    // Android Emulator -> localhost:  "http://10.0.2.2:8080/"
    // Thiet bi that (thay IP may tinh): "http://192.168.x.x:8080/"
    // Backend deploy: "https://your-domain.com/"
    public static final String BASE_URL = "http://76.13.212.30:6868/";

    // JWT Token duoc set sau khi login thanh cong
    private static String authToken = null;

    public static void setAuthToken(String token) {
        authToken = token;
        retrofit = null; // reset de interceptor dung token moi
    }

    public static String getAuthToken() {
        return authToken;
    }

    public static void clearAuthToken() {
        authToken = null;
        retrofit = null;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            // JWT Auth interceptor - tu dong them Bearer token vao moi request
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Accept", "application/json")
                        .method(original.method(), original.body());

                if (authToken != null && !authToken.isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + authToken);
                }

                return chain.proceed(requestBuilder.build());
            });

            httpClient.addInterceptor(logging);
            httpClient.connectTimeout(30, TimeUnit.SECONDS);
            httpClient.readTimeout(30, TimeUnit.SECONDS);
            httpClient.writeTimeout(30, TimeUnit.SECONDS);

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .registerTypeAdapter(Movie.class, new MovieDeserializer())
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    public static MovieApiService getApiService() {
        return getClient().create(MovieApiService.class);
    }

    /** Goi sau khi logout de reset client va xoa token */
    public static void resetClient() {
        retrofit = null;
        authToken = null;
    }
}
