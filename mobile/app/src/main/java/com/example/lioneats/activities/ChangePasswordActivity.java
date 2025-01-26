package com.example.lioneats.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lioneats.R;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.dtos.PasswordChangeDTO;
import com.example.lioneats.api.RetrofitClient;
import com.google.gson.Gson;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class ChangePasswordActivity extends AppCompatActivity {
	private static final String TAG = "ChangePasswordActivity";

	private EditText oldPasswordText, newPasswordText, confirmPasswordText;
	private SharedPreferences userSessionPreferences, userPreferences;
	private Long userId;
	private String jwtToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);

		ImageView logoBtn = findViewById(R.id.logoBtn);
		Button submitBtn = findViewById(R.id.submitBtn);
		oldPasswordText = findViewById(R.id.oldPasswordText);
		newPasswordText = findViewById(R.id.newPasswordText);
		confirmPasswordText = findViewById(R.id.confirmPasswordText);

		userPreferences = getSharedPreferences("user", MODE_PRIVATE);
		userSessionPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
		userId = userSessionPreferences.getLong("user_id", -999);
		jwtToken = userSessionPreferences.getString("jwt","");

		logoBtn.setOnClickListener(v -> {
			Intent intent = new Intent(ChangePasswordActivity.this, MainActivity.class);
			startActivity(intent);
		});

		submitBtn.setOnClickListener(v -> changePassword());
	}

	private PasswordChangeDTO createPasswordChangeDTO() {
		String oldPassword = oldPasswordText.getText().toString().trim();
		String newPassword = newPasswordText.getText().toString().trim();
		String confirmNewPassword = confirmPasswordText.getText().toString().trim();

		PasswordChangeDTO passwordChange = new PasswordChangeDTO();
		passwordChange.setOldPassword(oldPassword);
		passwordChange.setNewPassword(newPassword);
		passwordChange.setConfirmNewPassword(confirmNewPassword);

		return passwordChange;
	}

	private boolean validateInputs() {
		boolean isValid = true;

		String oldPassword = oldPasswordText.getText().toString().trim();
		if (oldPassword.isEmpty()) {
			oldPasswordText.setError("Current password is required");
			isValid = false;
		} else if (oldPassword.length() < 8) {
			oldPasswordText.setError("Password must be at least 8 characters");
			isValid = false;
		}

		String newPassword = newPasswordText.getText().toString().trim();
		if (newPassword.isEmpty()) {
			newPasswordText.setError("New password is required");
			isValid = false;
		} else if (newPassword.length() < 8) {
			newPasswordText.setError("New password must be at least 8 characters");
			isValid = false;
		}

		String confirmNewPassword = confirmPasswordText.getText().toString().trim();
		if (confirmNewPassword.isEmpty()) {
			confirmPasswordText.setError("Please confirm the new password");
			isValid = false;
		} else if (!confirmNewPassword.equals(newPassword)) {
			confirmPasswordText.setError("Passwords do not match");
			isValid = false;
		}

		return isValid;
	}

	private void changePassword() {
		if (!validateInputs()) {
			Toast.makeText(this, "Invalid inputs", Toast.LENGTH_SHORT).show();
			return;
		}

		PasswordChangeDTO passwordChangeDTO = createPasswordChangeDTO();

		Gson gson = new Gson();
		String requestBody = gson.toJson(passwordChangeDTO);
		Log.d(TAG, "Request Body: " + requestBody);
		Log.d(TAG, "usrId: " + userId);

		ApiService apiService = RetrofitClient.getApiService(jwtToken);
		Call<ResponseBody> call = apiService.changePassword(userId, passwordChangeDTO);

		call.enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
				try {
					if (response.isSuccessful()) {
						String responseBody = response.body().string();
						Log.d(TAG, "Response Body: " + responseBody);

						submitSuccessDialog();
					} else {
						String errorBody = response.errorBody().string();
						Log.e(TAG, "Change password failed: " + errorBody);
						Toast.makeText(ChangePasswordActivity.this, "Change password failed", Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					Log.e(TAG, "Exception while reading response: ", e);
				}
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable t) {
				Toast.makeText(ChangePasswordActivity.this, "Network error", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "onFailure: ", t);
			}
		});
	}

	private void submitSuccessDialog() {
		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_custom, null);
		TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
		dialogMessage.setText("Your password is successfully updated!");

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(dialogView);
		builder.setCancelable(false);

		AlertDialog dialog = builder.create();
		dialog.show();

		new Handler().postDelayed(() -> {
			dialog.dismiss();
			redirectToLoginActivity();
		}, 2000);
	}

	private void redirectToLoginActivity() {
		logout();
		Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	private void logout() {
		SharedPreferences.Editor sessionEditor = userSessionPreferences.edit();
		sessionEditor.clear();
		sessionEditor.apply();

		SharedPreferences.Editor userEditor = userPreferences.edit();
		userEditor.clear();
		userEditor.apply();
	}
}