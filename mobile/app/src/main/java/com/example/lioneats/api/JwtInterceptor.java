package com.example.lioneats.api;
import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class JwtInterceptor implements Interceptor {
	private String jwtToken;

	public JwtInterceptor(String jwtToken) {
		this.jwtToken = jwtToken;
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request originalRequest = chain.request();

		Request.Builder builder = originalRequest.newBuilder()
				.header("Authorization", "Bearer " + jwtToken);

		Request modifiedRequest = builder.build();
		return chain.proceed(modifiedRequest);
	}
}
