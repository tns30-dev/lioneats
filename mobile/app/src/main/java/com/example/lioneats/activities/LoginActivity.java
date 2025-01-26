package com.example.lioneats.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lioneats.R;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.dtos.LoginResponseDTO;
import com.example.lioneats.models.UserDTO;
import com.example.lioneats.api.RetrofitClient;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
	private static final String TAG = "LoginActivity";
	private EditText usernameEditText;
	private EditText passwordEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		ImageView logoBtn = findViewById(R.id.logoBtn);
		Button loginBtn = findViewById(R.id.loginBtn);
		usernameEditText = findViewById(R.id.username);
		passwordEditText = findViewById(R.id.password);
		TextView registerAcctBtn = findViewById(R.id.registerAcct);

		logoBtn.setOnClickListener(v -> navigateToHome());
		loginBtn.setOnClickListener(v -> login());
		registerAcctBtn.setOnClickListener(v -> {
			Intent intent = new Intent(LoginActivity.this, RegisterAccountActivity.class);
			startActivity(intent);
			finish();
		});
	}

	private void login() {
		String username = usernameEditText.getText().toString();
		String password = passwordEditText.getText().toString();

		if (validateInputs(username, password)) {
			authenticateUser(username, password);
		}
	}

	private boolean validateInputs(String username, String password) {
		boolean isValid = true;

		if (username.isEmpty()) {
			usernameEditText.setError("Username is required");
			isValid = false;
		} else if (username.length() > 50) {
			usernameEditText.setError("Username must be less than 50 characters");
			isValid = false;
		}

		if (password.isEmpty()) {
			passwordEditText.setError("Password is required");
			isValid = false;
		} else if (password.length() < 8) {
			passwordEditText.setError("Password must be at least 8 characters");
			isValid = false;
		}

		return isValid;
	}

	private void authenticateUser(String username, String password) {
		UserDTO user = new UserDTO();
		user.setUsername(username);
		user.setPassword(password);
		ApiService apiService = RetrofitClient.getApiServiceWithoutToken();
		Call<LoginResponseDTO> call = apiService.login(user);

		call.enqueue(new Callback<LoginResponseDTO>() {
			@Override
			public void onResponse(Call<LoginResponseDTO> call, Response<LoginResponseDTO> response) {
				if (response.isSuccessful() && response.body() != null) {
					LoginResponseDTO loginResponse = response.body();
					saveUserSession(loginResponse.getUserId(), loginResponse.getUsername(), password, loginResponse.getJwt());
					fetchUserData(loginResponse.getUserId(), loginResponse.getJwt());
				} else {
					Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<LoginResponseDTO> call, Throwable t) {
				Toast.makeText(LoginActivity.this, "Network error", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "onFailure: ", t);
			}
		});
	}

	private void saveUserSession(long userId, String username, String password, String jwt) {
		SharedPreferences userSessionPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
		SharedPreferences.Editor sessionEditor = userSessionPreferences.edit();
		sessionEditor.putLong("user_id", userId);
		sessionEditor.putString("username", username);
		sessionEditor.putString("password", password);
		sessionEditor.putString("jwt", jwt);
		sessionEditor.commit();
	}

	private void fetchUserData(long userId, String jwtToken) {
		ApiService apiService = RetrofitClient.getApiService(jwtToken);
		Call<UserDTO> call = apiService.viewUser(userId);

		call.enqueue(new Callback<UserDTO>() {
			@Override
			public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
				if (response.isSuccessful() && response.body() != null) {
					UserDTO user = response.body();
					saveUserToPreferences(user);
				} else {
					Toast.makeText(LoginActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<UserDTO> call, Throwable t) {
				Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "Network Error: ", t);
			}
		});
	}

	private void saveUserToPreferences(UserDTO user) {
		SharedPreferences userPreferences = getSharedPreferences("user", MODE_PRIVATE);
		SharedPreferences.Editor editor = userPreferences.edit();
		editor.putString("user", new Gson().toJson(user));
		editor.commit();
		navigateToHome();
	}

	private void navigateToHome() {
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}

