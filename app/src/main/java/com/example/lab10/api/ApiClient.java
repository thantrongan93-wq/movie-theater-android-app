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
    public static final String BASE_URL = "http://76.13.212.30:6868/";
    private static String authToken = null;

    public static void setAuthToken(String token) {
        authToken = token;
        retrofit = null; 
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
            logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);

            OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
            
            httpClientBuilder.addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder builder = original.newBuilder()
                        .header("Accept", "application/json")
                    .header("User-Agent", "Android-App-Client");
                
                if (authToken != null && !authToken.isEmpty()) {
                    builder.header("Authorization", "Bearer " + authToken);
                }
                
                return chain.proceed(builder.build());
            });

            httpClientBuilder.addInterceptor(logging);

            httpClientBuilder.connectTimeout(30, TimeUnit.SECONDS);
            httpClientBuilder.readTimeout(30, TimeUnit.SECONDS);
            httpClientBuilder.writeTimeout(30, TimeUnit.SECONDS);
            httpClientBuilder.retryOnConnectionFailure(true);

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .registerTypeAdapter(Movie.class, new MovieDeserializer())
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClientBuilder.build())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    public static MovieApiService getApiService() {
        return getClient().create(MovieApiService.class);
    }

    public static void resetClient() {
        retrofit = null;
        authToken = null;
    }
}
