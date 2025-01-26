package com.example.lioneats.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;

import com.bumptech.glide.Glide;
import com.example.lioneats.R;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.models.Allergy;
import com.example.lioneats.models.Dish;
import com.example.lioneats.models.UserDTO;
import com.example.lioneats.api.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterAccountActivity extends AppCompatActivity {
	private static final String TAG = "RegisterAccountActivity";
	private EditText nameText, usernameText, passwordText, emailText, ageText, countryText;
	private RadioGroup genderOptions, budgetOptions, spicyOptions;
	private LinearLayout dishPreferencesSection, allergySection;
	private TextView setAllergiesBtn;
	private GridLayout dishContainer, allergyOptionsGrid;
	private List<Dish> dishList = new ArrayList<>();
	private List<Allergy> allergyList = new ArrayList<>();
	private List<CheckBox> allergyCheckboxes = new ArrayList<>();
	private List<Dish> dishSelections = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_account);

		nameText = findViewById(R.id.nameText);
		usernameText = findViewById(R.id.usernameText);
		passwordText = findViewById(R.id.passwordText);
		emailText = findViewById(R.id.emailText);
		countryText = findViewById(R.id.countryText);
		ageText = findViewById(R.id.ageText);
		genderOptions = findViewById(R.id.genderOptions);
		budgetOptions = findViewById(R.id.budgetOptions);
		spicyOptions = findViewById(R.id.spicyOptions);
		dishContainer = findViewById(R.id.dishContainer);
		allergyOptionsGrid = findViewById(R.id.allergyOptionsGrid);
		allergySection = findViewById(R.id.allergySection);
		dishPreferencesSection = findViewById(R.id.dishPreferencesSection);
		setAllergiesBtn = findViewById(R.id.setAllergiesBtn);

		loadDishesFromPreferences();
		loadAllergiesFromPreferences();

		dishPreferencesSection.setEnabled(false);
		dishPreferencesSection.setBackground(ContextCompat.getDrawable(this, R.drawable.disabled_background));
		disableAllChildViews(dishContainer);

		setAllergiesBtn.setOnClickListener(v -> {
			for (CheckBox checkBox : allergyCheckboxes) {
				checkBox.setEnabled(false);
			}
			allergySection.setBackground(ContextCompat.getDrawable(this, R.drawable.disabled_background));

			dishPreferencesSection.setEnabled(true);
			dishPreferencesSection.setBackground(null);
			fetchSafeDishes();
		});

		Button homeBtn = findViewById(R.id.homeBtn);
		homeBtn.setOnClickListener(v -> {
			Intent intent = new Intent(RegisterAccountActivity.this, MainActivity.class);
			startActivity(intent);
		});

		Button registerBtn = findViewById(R.id.registerBtn);
		registerBtn.setOnClickListener(v -> registerUser());
	}

	private void loadAllergiesFromPreferences() {
		SharedPreferences allergyListPreferences = getSharedPreferences("allergy_list", MODE_PRIVATE);
		String allergyJson = allergyListPreferences.getString("allergies", null);

		if (allergyJson != null) {
			Type listType = new TypeToken<List<Allergy>>() {}.getType();
			allergyList = new Gson().fromJson(allergyJson, listType);
			populateAllergyCheckBoxes();
		} else {
			Toast.makeText(this, "No allergies found", Toast.LENGTH_SHORT).show();
		}
	}

	private void populateAllergyCheckBoxes() {
		allergyOptionsGrid.removeAllViews();

		for (Allergy allergy : allergyList) {
			CheckBox checkBox = new CheckBox(this);
			checkBox.setText(allergy.getName());
			checkBox.setTextSize(15);
			checkBox.setId(View.generateViewId());

			GridLayout.LayoutParams params = new GridLayout.LayoutParams();
			params.setMargins(8, 8, 8, 8);
			checkBox.setLayoutParams(params);

			allergyCheckboxes.add(checkBox);
			allergyOptionsGrid.addView(checkBox);
		}
	}

	private void fetchSafeDishes() {
		List<String> selectedAllergies = getSelectedAllergies();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String requestBody = gson.toJson(selectedAllergies);
		Log.d(TAG, "Request Body: " + requestBody);

		ApiService apiService = RetrofitClient.getApiServiceWithoutToken();
		Call<List<Dish>> call = apiService.getSafeDishes(selectedAllergies);

		call.enqueue(new Callback<List<Dish>>() {
			@Override
			public void onResponse(Call<List<Dish>> call, Response<List<Dish>> response) {
				if (response.isSuccessful() && response.body() != null) {
					String responseBody = gson.toJson(response.body());
					Log.d(TAG, "Response Body: " + responseBody);

					List<Dish> safeDishes = response.body();
					updateDishSelectionUI(safeDishes);
				} else {
					try {
						if (response.errorBody() != null) {
							Log.e(TAG, "Response Error Body: " + response.errorBody().string());
						}
					} catch (IOException e) {
						Log.e(TAG, "Error reading error body", e);
					}
					Toast.makeText(RegisterAccountActivity.this, "Failed to fetch safe dishes", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<List<Dish>> call, Throwable t) {
				Log.e(TAG, "Network Error: ", t);
				Toast.makeText(RegisterAccountActivity.this, "Network Error!", Toast.LENGTH_SHORT).show();
			}
		});
	}


	private void updateDishSelectionUI(List<Dish> safeDishes) {
		dishContainer.removeAllViews();

		for (Dish dish : dishList) {
			View dishView = LayoutInflater.from(this).inflate(R.layout.item_dish, dishContainer, false);
			ImageView dishImage = dishView.findViewById(R.id.dishImage);
			TextView dishName = dishView.findViewById(R.id.dishName);

			Glide.with(this)
					.load(dish.getImageUrl())
					.placeholder(R.drawable.default_image)
					.error(R.drawable.default_image)
					.into(dishImage);

			dishName.setText(dish.getDishDetailName());

			if (safeDishes.contains(dish)) {
				dishView.setOnClickListener(v -> toggleDishSelection(dishView, dish));
				dishView.setBackground(ContextCompat.getDrawable(this, R.drawable.selector_background));
				dishView.setClickable(true);
				dishView.setEnabled(true);
			} else {
				dishView.setOnClickListener(null);
				dishView.setBackground(ContextCompat.getDrawable(this, R.drawable.disabled_background));
				dishView.setClickable(false);
				dishView.setEnabled(false);
			}
			dishContainer.addView(dishView);
		}
	}

	private void toggleDishSelection(View dishView, Dish dish) {
		if (dishSelections.contains(dish)) {
			dishSelections.remove(dish);
			dishView.setBackground(ContextCompat.getDrawable(this, R.drawable.selector_background));
		} else {
			dishSelections.add(dish);
			dishView.setBackground(ContextCompat.getDrawable(this, R.drawable.selected_background));
		}

		Log.d(TAG, "Selected Dishes: " + dishSelections.toString());
	}

	private void loadDishesFromPreferences() {
		SharedPreferences sharedPreferences = getSharedPreferences("dish_list", MODE_PRIVATE);
		String dishJson = sharedPreferences.getString("dishes", null);

		if (dishJson != null) {
			Type listType = new TypeToken<List<Dish>>() {}.getType();
			dishList = new Gson().fromJson(dishJson, listType);
			populateDishPreferences();
		} else {
			Toast.makeText(this, "No dishes found", Toast.LENGTH_SHORT).show();
			Log.e(TAG, "No dishes found in SharedPreferences");
		}
	}

	private void populateDishPreferences() {
		dishContainer.removeAllViews();

		for (Dish dish : dishList) {
			View dishView = LayoutInflater.from(this).inflate(R.layout.item_dish, dishContainer, false);
			ImageView dishImage = dishView.findViewById(R.id.dishImage);
			TextView dishName = dishView.findViewById(R.id.dishName);

			Glide.with(this)
					.load(dish.getImageUrl())
					.placeholder(R.drawable.default_image)
					.error(R.drawable.default_image)
					.into(dishImage);

			dishName.setText(dish.getDishDetailName());
			dishView.setOnClickListener(v -> toggleDishSelection(dishView, dish));
			dishContainer.addView(dishView);
		}
	}

	private List<String> getSelectedAllergies() {
		List<String> selectedAllergies = new ArrayList<>();
		for (CheckBox checkBox : allergyCheckboxes) {
			if (checkBox.isChecked()) {
				String allergyName = checkBox.getText().toString();
				selectedAllergies.add(allergyName);
			}
		}
		return selectedAllergies;
	}

	private void registerUser() {
		if (!validateInputs()) {
			Toast.makeText(this, "Invalid inputs", Toast.LENGTH_SHORT).show();
			return;
		}

		UserDTO user = createUserFromInput();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String userJson = gson.toJson(user);
		Log.d(TAG, "UserDTO JSON: " + userJson);

		ApiService apiService = RetrofitClient.getApiServiceWithoutToken();
		Call<ResponseBody> call = apiService.registerUser(user);

		call.enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
				if (response.isSuccessful()) {
					try {
						String responseBody = response.body().string();
						Log.d(TAG, "Registration Successful: " + responseBody);
						Toast.makeText(RegisterAccountActivity.this, "Registration Successful: " + responseBody, Toast.LENGTH_LONG).show();
						registerSuccessDialog();
					} catch (IOException e) {
						Log.e(TAG, "Error reading response", e);
						Toast.makeText(RegisterAccountActivity.this, "Error reading response", Toast.LENGTH_SHORT).show();
					}
				} else {
					try {
						String errorBody = response.errorBody().string();
						Log.e(TAG, "Registration Failed: " + errorBody);
						Toast.makeText(RegisterAccountActivity.this, "Registration Failed: " + errorBody, Toast.LENGTH_LONG).show();
					} catch (IOException e) {
						Log.e(TAG, "Error parsing error response", e);
						Toast.makeText(RegisterAccountActivity.this, "Error parsing error response", Toast.LENGTH_SHORT).show();
					}
				}
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable t) {
				Log.e(TAG, "Network Error: ", t);
				Toast.makeText(RegisterAccountActivity.this, "Network Error!", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private boolean validateInputs() {
		boolean isValid = true;

		String name = nameText.getText().toString().trim();
		if (name.isEmpty()) {
			nameText.setError("Name is required");
			isValid = false;
		} else if (name.length() > 100) {
			nameText.setError("Name must be less than 100 characters");
			isValid = false;
		}

		String username = usernameText.getText().toString().trim();
		if (username.isEmpty()) {
			usernameText.setError("Username is required");
			isValid = false;
		} else if (username.length() > 50) {
			usernameText.setError("Username must be less than 50 characters");
			isValid = false;
		}

		String password = passwordText.getText().toString().trim();
		if (password.isEmpty()) {
			passwordText.setError("Password is required");
			isValid = false;
		} else if (password.length() < 8) {
			passwordText.setError("Password must be at least 8 characters");
			isValid = false;
		}

		String email = emailText.getText().toString().trim();
		if (email.isEmpty()) {
			emailText.setError("Email is required");
			isValid = false;
		} else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			emailText.setError("Email should be valid");
			isValid = false;
		}

		String ageStr = ageText.getText().toString().trim();
		if (ageStr.isEmpty()) {
			ageText.setError("Age must be provided");
			isValid = false;
		} else {
			try {
				int age = Integer.parseInt(ageStr);
				if (age < 0) {
					ageText.setError("Age must be a positive number");
					isValid = false;
				} else if (age > 150) {
					ageText.setError("Age must be less than or equal to 150");
					isValid = false;
				}
			} catch (NumberFormatException e) {
				ageText.setError("Invalid age");
				isValid = false;
			}
		}

		if (genderOptions.getCheckedRadioButtonId() == -1) {
			Toast.makeText(this, "Gender is required", Toast.LENGTH_SHORT).show();
			isValid = false;
		}

		if (budgetOptions.getCheckedRadioButtonId() == -1) {
			Toast.makeText(this, "Preferred budget is required", Toast.LENGTH_SHORT).show();
			isValid = false;
		}

		return isValid;
	}

	private UserDTO createUserFromInput() {
		String name = nameText.getText().toString();
		String username = usernameText.getText().toString().trim();
		String password = passwordText.getText().toString().trim();
		String email = emailText.getText().toString();
		String country = countryText.getText().toString();
		Integer age = Integer.parseInt(ageText.getText().toString());
		String gender = getSelectedGender();
		String budget = getSelectedBudget();
		boolean likesSpicy = getSpicyPreference();
		List<String> selectedAllergies = getSelectedAllergies();
		List<String> selectedDishes = getSelectedDishes();

		UserDTO user = new UserDTO();
		user.setName(name);
		user.setUsername(username);
		user.setPassword(password);
		user.setEmail(email);
		user.setAge(age);
		user.setGender(gender);
		user.setCountry(country);
		user.setLikesSpicy(likesSpicy);
		user.setPreferredBudget(budget);
		user.setAllergies(selectedAllergies);
		user.setDishPreferences(selectedDishes);
		return user;
	}

	private String getSelectedGender() {
		int selectedId = genderOptions.getCheckedRadioButtonId();
		if (selectedId == R.id.genderOption1) {
			return "Male";
		} else if (selectedId == R.id.genderOption2) {
			return "Female";
		} else if (selectedId == R.id.genderOption3) {
			return "Other";
		} else {
			return "";
		}
	}

	private String getSelectedBudget() {
		int selectedId = budgetOptions.getCheckedRadioButtonId();
		if (selectedId == R.id.budgetOption1) {
			return "LOW";
		} else if (selectedId == R.id.budgetOption2) {
			return "MEDIUM";
		} else if (selectedId == R.id.budgetOption3) {
			return "HIGH";
		} else {
			return "";
		}
	}

	private boolean getSpicyPreference() {
		int selectedId = spicyOptions.getCheckedRadioButtonId();
		return selectedId == R.id.spicyOption1;
	}

	private List<String> getSelectedDishes() {
		List<String> selectedDishes = new ArrayList<>();
		for (Dish dish : dishSelections) {
			String dishName = dish.getDishDetailName();
			selectedDishes.add(dishName);
		}
		return selectedDishes;
	}

	private void registerSuccessDialog() {
		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_custom, null);
		TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
		dialogMessage.setText("Successful Registration!");

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(dialogView);
		builder.setCancelable(false);

		AlertDialog dialog = builder.create();
		dialog.show();

		new Handler().postDelayed(() -> {
			dialog.dismiss();
			redirectToLoginActivity();
		}, 3000);
	}

	private void redirectToLoginActivity() {
		Intent intent = new Intent(RegisterAccountActivity.this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}

	private void disableAllChildViews(ViewGroup parent) {
		for (int i = 0; i < parent.getChildCount(); i++) {
			View child = parent.getChildAt(i);
			child.setEnabled(false);
			child.setBackground(ContextCompat.getDrawable(this, R.drawable.disabled_background));
		}
	}
}