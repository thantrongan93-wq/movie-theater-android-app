package com.example.lab10.api;

import com.example.lab10.models.Movie;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Protocol;
import okhttp3.ConnectionPool;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Collections;
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
                        .header("User-Agent", "Android-App-Client")
                        .header("Accept-Encoding", "identity")
                        .header("TE", "trailers")
                        .header("Connection", "close");
                
                if (authToken != null && !authToken.isEmpty()) {
                    builder.header("Authorization", "Bearer " + authToken);
                }
                
                return chain.proceed(builder.build());
            });

            httpClientBuilder.addInterceptor(logging);
            
            // Network interceptor to strip problematic transfer encoding
            httpClientBuilder.addNetworkInterceptor(chain -> {
                okhttp3.Response response = chain.proceed(chain.request());
                return response.newBuilder()
                        .removeHeader("Transfer-Encoding")
                        .removeHeader("Content-Encoding")
                        .header("Connection", "close")
                        .build();
            });
            
            // Ép sử dụng HTTP/1.1 duy nhất
            httpClientBuilder.protocols(Collections.singletonList(Protocol.HTTP_1_1));
            
            // Tối ưu connection pool
            httpClientBuilder.connectionPool(new ConnectionPool(2, 5, TimeUnit.MINUTES));
            
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
