package com.example.lioneats.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.lioneats.R;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.fragments.HeaderFragment;
import com.example.lioneats.models.Dish;
import com.example.lioneats.models.ML_feedback;
import com.example.lioneats.api.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageResultActivity extends AppCompatActivity {
	private static final String TAG = "ImageResultActivity";

	private ML_feedback feedback;
	private TextView resultTextView, viewDishBtn;
	private ProgressBar progressBar;
	private EditText remarksEditText;
	private String selectedDishName;
	private List<Dish> dishList;
	private String jwtToken;
	private Uri imageUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_result);

		initializeUIComponents();
		setupHeaderFragment();

		jwtToken = getJwtToken();

		List<String> dishNames = getDishNames();
		setupSpinner(dishNames);

		String imageUriString = getIntent().getStringExtra("imageUri");
		if (imageUriString != null) {
			imageUri = Uri.parse(imageUriString);
			displayImage(imageUri);
			uploadImage(imageUri);
		}

		setupSubmitButton();
	}

	private void initializeUIComponents() {
		feedback = new ML_feedback();
		resultTextView = findViewById(R.id.resultText);
		progressBar = findViewById(R.id.progressBar);
		progressBar.setVisibility(View.VISIBLE);
		remarksEditText = findViewById(R.id.remarks);
		viewDishBtn = findViewById(R.id.viewDishBtn);
	}

	private void setupHeaderFragment() {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.headerFragmentContainer, new HeaderFragment());
		transaction.commit();
	}

	private String getJwtToken() {
		SharedPreferences userSessionPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
		return userSessionPreferences.getString("jwt", "");
	}

	private void setupSpinner(List<String> dishNames) {
		Spinner spinnerDishName = findViewById(R.id.spinnerDishName);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dishNames);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerDishName.setAdapter(adapter);

		spinnerDishName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				selectedDishName = position == 0 ? null : parentView.getItemAtPosition(position).toString();
				if (selectedDishName != null) {
					Log.d(TAG, "Selected: " + selectedDishName);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				selectedDishName = null;
			}
		});
	}

	private List<String> getDishNames() {
		SharedPreferences dishListPreferences = getSharedPreferences("dish_list", MODE_PRIVATE);
		String jsonDishes = dishListPreferences.getString("dishes", "");

		Gson gson = new Gson();
		Type listType = new TypeToken<List<Dish>>() {}.getType();
		dishList = gson.fromJson(jsonDishes, listType);

		List<String> dishNames = new ArrayList<>();
		dishNames.add("Select the correct dish");
		for (Dish dish : dishList) {
			dishNames.add(dish.getDishDetailName());
		}
		return dishNames;
	}

	private void displayImage(Uri imageUri) {
		ImageView imageView = findViewById(R.id.imageView);
		imageView.setImageURI(imageUri);
	}

	private void uploadImage(Uri imageUri) {
		try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
			byte[] bytes = new byte[inputStream.available()];
			inputStream.read(bytes);

			String mimeType = getContentResolver().getType(imageUri);
			mimeType = (mimeType != null) ? mimeType : "image/jpeg";

			RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), bytes);
			MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", "image.jpg", requestFile);

			Log.d(TAG, "JWT Token: " + jwtToken);
			ApiService apiService = RetrofitClient.getApiService(jwtToken);

			Log.d(TAG, "API Request: Uploading image to server");
			Call<ResponseBody> call = apiService.uploadImage(imagePart);

			call.enqueue(new ImageUploadCallback());
		} catch (IOException e) {
			progressBar.setVisibility(View.GONE);
			Log.e(TAG, "Failed to open image: " + e.getMessage(), e);
			Toast.makeText(this, "Failed to open image", Toast.LENGTH_SHORT).show();
		}
	}

	private class ImageUploadCallback implements Callback<ResponseBody> {
		@Override
		public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
			progressBar.setVisibility(View.GONE);
			if (response.isSuccessful() && response.body() != null) {
				handleSuccessfulImageUpload(response);
			} else {
				String errorBody = "";
				try {
					if (response.errorBody() != null) {
						errorBody = response.errorBody().string();
					}
				} catch (IOException e) {
					Log.e(TAG, "Error reading error body: " + e.getMessage(), e);
				}
				Log.e(TAG, "API Error: " + response.message() + " | Error Body: " + errorBody);
				Toast.makeText(ImageResultActivity.this, "Upload failed: " + response.message(), Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onFailure(Call<ResponseBody> call, Throwable t) {
			progressBar.setVisibility(View.GONE);
			Log.e(TAG, "Image upload failed: " + t.getMessage(), t);
			Toast.makeText(ImageResultActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
		}
	}

	private void handleSuccessfulImageUpload(Response<ResponseBody> response) {
		try {
			String apiResponse = response.body().string();
			Log.d(TAG, "API Response: " + apiResponse);

			Gson gson = new Gson();
			Type type = new TypeToken<Map<String, String>>() {}.getType();
			Map<String, String> responseMap = gson.fromJson(apiResponse, type);

			String predictedDish = responseMap.get("predictedDish");
			String imageLocation = responseMap.get("imageUrl");
			feedback.setMl_result(predictedDish);
			feedback.setImageLocation(imageLocation);

			resultTextView.setText(predictedDish);
			viewDishBtn.setClickable(true);
			viewDishBtn.setOnClickListener(v -> viewDishDetail(predictedDish));
		} catch (IOException | JsonSyntaxException e) {
			Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
			Toast.makeText(this, "Failed to parse response", Toast.LENGTH_SHORT).show();
		}
	}

	private void viewDishDetail(String dishDetailName) {
		int dishID = getDishIDByName(dishDetailName);
		if (dishID != -1) {
			Intent intent = new Intent(ImageResultActivity.this, DishDetailsActivity.class);
			intent.putExtra("dishID", dishID);
			intent.putExtra("dishImageUrl", getDishImageUrl(dishID));
			startActivity(intent);
		} else {
			Toast.makeText(this, "Dish not found", Toast.LENGTH_SHORT).show();
		}
	}

	private int getDishIDByName(String dishDetailName) {
		for (Dish dish : dishList) {
			if (dish.getDishDetailName().equalsIgnoreCase(dishDetailName)) {
				return dish.getId();
			}
		}
		return -1;
	}

	private String getDishImageUrl(int dishID) {
		for (Dish dish : dishList) {
			if (dish.getId() == dishID) {
				return dish.getImageUrl();
			}
		}
		return null;
	}

	private void setupSubmitButton() {
		Button submitBtn = findViewById(R.id.submitBtn);
		submitBtn.setOnClickListener(v -> submitFeedback());
	}

	private void submitFeedback() {
		String remarks = remarksEditText.getText().toString();

		if (selectedDishName != null && !selectedDishName.isEmpty()) {
			feedback.setUserDish(selectedDishName);
			feedback.setRemarks(remarks);

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String feedbackJson = gson.toJson(feedback);
			Log.d(TAG, "Feedback JSON: " + feedbackJson);

			ApiService apiService = RetrofitClient.getApiService(jwtToken);
			Log.d(TAG, "API Request: Submitting feedback");
			Call<ResponseBody> call = apiService.submitFeedback(feedback);

			call.enqueue(new FeedbackCallback());
		} else {
			Toast.makeText(this, "Please enter a dish name", Toast.LENGTH_SHORT).show();
		}
	}

	private class FeedbackCallback implements Callback<ResponseBody> {
		@Override
		public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
			if (response.isSuccessful()) {
				Log.d(TAG, "Feedback submitted successfully");
				submitSuccessDialog();
				finish();
			} else {
				Log.e(TAG, "Feedback submission failed: " + response.message());
				Toast.makeText(ImageResultActivity.this, "Feedback submission failed", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onFailure(Call<ResponseBody> call, Throwable t) {
			Log.e(TAG, "Feedback submission error: " + t.getMessage(), t);
			Toast.makeText(ImageResultActivity.this, "Failed to submit feedback", Toast.LENGTH_SHORT).show();
		}
	}

	private void submitSuccessDialog() {
		if (!isFinishing() && !isDestroyed()) {
			LayoutInflater inflater = getLayoutInflater();
			View dialogView = inflater.inflate(R.layout.dialog_custom, null);
			TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
			dialogMessage.setText("Thank you for your feedback!");

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(dialogView);
			builder.setCancelable(false);

			AlertDialog dialog = builder.create();
			dialog.show();

			new Handler().postDelayed(() -> {
				if (!isFinishing() && !isDestroyed()) {
					dialog.dismiss();
					redirectToMainActivity();
				}
			}, 3000);
		}
	}

	private void redirectToMainActivity() {
		Intent intent = new Intent(ImageResultActivity.this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}
}
