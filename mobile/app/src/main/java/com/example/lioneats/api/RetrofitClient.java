package com.example.lioneats.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

	//private static final String BASE_URL = "http://10.0.2.2:8080";
	private static final String BASE_URL = "https://lioneat.azurewebsites.net";
	private static Retrofit retrofitWithToken = null;
	private static Retrofit retrofitWithoutToken = null;

	private RetrofitClient() {}

	private static Retrofit getRetrofitInstanceWithToken(String jwtToken) {
		if (retrofitWithToken == null) {
			OkHttpClient client = new OkHttpClient.Builder()
					.addInterceptor(new JwtInterceptor(jwtToken))
					.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
					.connectTimeout(30, TimeUnit.SECONDS)
					.writeTimeout(30, TimeUnit.SECONDS)
					.readTimeout(30, TimeUnit.SECONDS)
					.build();

			retrofitWithToken = new Retrofit.Builder()
					.baseUrl(BASE_URL)
					.client(client)
					.addConverterFactory(GsonConverterFactory.create())
					.build();
		}
		return retrofitWithToken;
	}

	private static Retrofit getRetrofitInstanceWithoutToken() {
		if (retrofitWithoutToken == null) {
			OkHttpClient client = new OkHttpClient.Builder().build();

			retrofitWithoutToken = new Retrofit.Builder()
					.baseUrl(BASE_URL)
					.client(client)
					.addConverterFactory(GsonConverterFactory.create())
					.build();
		}
		return retrofitWithoutToken;
	}

	public static ApiService getApiService(String jwtToken) {
		if (jwtToken != null && !jwtToken.isEmpty()) {
			return getRetrofitInstanceWithToken(jwtToken).create(ApiService.class);
		} else {
			return getRetrofitInstanceWithoutToken().create(ApiService.class);
		}
	}

	public static ApiService getApiServiceWithoutToken() {
		return getRetrofitInstanceWithoutToken().create(ApiService.class);
	}
}

