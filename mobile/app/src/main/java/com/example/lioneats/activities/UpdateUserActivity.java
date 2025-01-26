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
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateUserActivity extends AppCompatActivity {
	private static final String TAG = "UpdateUserActivity";

	private TextView usernameText;
	private TextView setAllergiesBtn;
	private EditText nameEditText, emailEditText, ageEditText, countryEditText;
	private RadioGroup genderOptions, budgetOptions, spicyOptions;
	private GridLayout dishContainer, allergyOptionsGrid;
	private LinearLayout allergySection, dishPreferencesSection;
	private List<Dish> dishList = new ArrayList<>();
	private List<Allergy> allergyList = new ArrayList<>();
	private List<CheckBox> allergyCheckboxes = new ArrayList<>();
	private Long userId;
	private String password, jwtToken;
	private List<Dish> dishSelections = new ArrayList<>();
	private SharedPreferences userSessionPreferences, userPreferences, dishListPreferences, allergyListPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_user);

		initializeUIComponents();
		initializeSharedPreferences();
		userId = userSessionPreferences.getLong("user_id", -999);
		password = userSessionPreferences.getString("password", "");
		jwtToken = userSessionPreferences.getString("jwt", "");

		loadDishesFromPreferences();
		loadAllergiesFromPreferences();
		setFieldsEditable(false);

		dishPreferencesSection.setEnabled(false);
		dishPreferencesSection.setBackground(ContextCompat.getDrawable(this, R.drawable.disabled_background));
		disableAllChildViews(dishContainer);

		setupButtonListeners();
		loadUserFromPreferencesOrFetch();
	}

	private void initializeUIComponents() {
		usernameText = findViewById(R.id.usernameText);
		setAllergiesBtn = findViewById(R.id.setAllergiesBtn);
		nameEditText = findViewById(R.id.nameEditText);
		emailEditText = findViewById(R.id.emailEditText);
		countryEditText = findViewById(R.id.countryEditText);
		ageEditText = findViewById(R.id.ageEditText);
		genderOptions = findViewById(R.id.genderOptions);
		budgetOptions = findViewById(R.id.budgetOptions);
		spicyOptions = findViewById(R.id.spicyOptions);
		dishContainer = findViewById(R.id.dishContainer);
		allergyOptionsGrid = findViewById(R.id.allergyOptionsGrid);
		dishPreferencesSection = findViewById(R.id.dishPreferencesSection);
		allergySection = findViewById(R.id.allergySection);
	}

	private void initializeSharedPreferences() {
		userSessionPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
		userPreferences = getSharedPreferences("user", MODE_PRIVATE);
		dishListPreferences = getSharedPreferences("dish_list", MODE_PRIVATE);
		allergyListPreferences = getSharedPreferences("allergy_list", MODE_PRIVATE);
	}

	private void setupButtonListeners() {
		TextView passwordBtn = findViewById(R.id.passwordBtn);
		passwordBtn.setOnClickListener(v -> {
			Intent intent = new Intent(UpdateUserActivity.this, ChangePasswordActivity.class);
			startActivity(intent);
			finish();
		});

		Button homeBtn = findViewById(R.id.homeBtn);
		homeBtn.setOnClickListener(v -> {
			Intent intent = new Intent(UpdateUserActivity.this, MainActivity.class);
			startActivity(intent);
		});

		Button editBtn = findViewById(R.id.editBtn);
		Button updateBtn = findViewById(R.id.updateBtn);
		editBtn.setOnClickListener(v -> {
			setFieldsEditable(true);
			setAllergiesBtn.setEnabled(true);
			setAllergiesBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.enabled_background));
			dishPreferencesSection.setEnabled(false);
			dishPreferencesSection.setBackground(ContextCompat.getDrawable(this, R.drawable.disabled_background));
			editBtn.setVisibility(View.GONE);
			updateBtn.setVisibility(View.VISIBLE);
		});

		updateBtn.setOnClickListener(v -> {
			updateUser();
			editBtn.setVisibility(View.VISIBLE);
			updateBtn.setVisibility(View.GONE);
		});
		updateBtn.setVisibility(View.GONE);

		setAllergiesBtn.setOnClickListener(v -> {
			disableAllergySelection();
			allergySection.setBackground(ContextCompat.getDrawable(this, R.drawable.disabled_background));

			dishPreferencesSection.setEnabled(true);
			dishPreferencesSection.setBackground(null);
			fetchSafeDishes();
		});
	}

	private void loadAllergiesFromPreferences() {
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

	private void loadDishesFromPreferences() {
		String dishJson = dishListPreferences.getString("dishes", null);

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

	private void loadUserFromPreferencesOrFetch() {
		String userJson = userPreferences.getString("user", null);

		if (userJson != null) {
			UserDTO user = new Gson().fromJson(userJson, UserDTO.class);
			updateUI(user);
		} else {
			fetchUserData();
		}
	}

	private void fetchUserData() {
		ApiService apiService = RetrofitClient.getApiService(jwtToken);
		Call<UserDTO> call = apiService.viewUser(userId);

		call.enqueue(new Callback<UserDTO>() {
			@Override
			public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
				if (response.isSuccessful() && response.body() != null) {
					UserDTO user = response.body();
					saveUserToPreferences(user);
					updateUI(user);
				} else {
					Toast.makeText(UpdateUserActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<UserDTO> call, Throwable t) {
				Toast.makeText(UpdateUserActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "Network Error: ", t);
			}
		});
	}

	private void saveUserToPreferences(UserDTO user) {
		SharedPreferences.Editor editor = userPreferences.edit();
		editor.putString("user", new Gson().toJson(user));
		editor.apply();
	}

	private void updateUI(UserDTO user) {
		if (user == null) {
			Log.e(TAG, "UserDTO data is null");
			return;
		}

		nameEditText.setText(user.getName());
		usernameText.setText(user.getUsername());
		emailEditText.setText(user.getEmail());
		countryEditText.setText(user.getCountry());
		ageEditText.setText(String.valueOf(user.getAge()));
		if (user.isLikesSpicy()) {
			spicyOptions.check(R.id.spicyOption1);
		} else {
			spicyOptions.check(R.id.spicyOption2);
		}
		switch (user.getGender()) {
			case "Male":
				genderOptions.check(R.id.genderOption1);
				break;
			case "Female":
				genderOptions.check(R.id.genderOption2);
				break;
			case "Other":
				genderOptions.check(R.id.genderOption3);
				break;
		}
		switch (user.getPreferredBudget()) {
			case "LOW":
				budgetOptions.check(R.id.budgetOption1);
				break;
			case "MEDIUM":
				budgetOptions.check(R.id.budgetOption2);
				break;
			case "HIGH":
				budgetOptions.check(R.id.budgetOption3);
				break;
		}
		updateAllergySelections(user.getAllergies());
		updateDishSelections(user.getDishPreferences());
	}

	private void updateAllergySelections(List<String> userAllergies) {
		for (CheckBox checkBox : allergyCheckboxes) {
			String allergyName = checkBox.getText().toString();
			boolean isSelected = userAllergies.contains(allergyName);
			checkBox.setChecked(isSelected);
		}
	}

	private void updateDishSelections(List<String> dishPrefs) {
		for (int i = 0; i < dishContainer.getChildCount(); i++) {
			View dishView = dishContainer.getChildAt(i);
			TextView dishNameView = dishView.findViewById(R.id.dishName);

			if (dishNameView != null) {
				String dishName = dishNameView.getText().toString();
				if (dishPrefs.contains(dishName)) {
					dishView.setBackground(ContextCompat.getDrawable(this, R.drawable.selected_background));
				} else {
					dishView.setBackground(ContextCompat.getDrawable(this, R.drawable.selector_background));
				}
			}
		}
	}

	private void setFieldsEditable(boolean enabled) {
		emailEditText.setEnabled(enabled);
		countryEditText.setEnabled(enabled);
		ageEditText.setEnabled(enabled);

		for (int i = 0; i < genderOptions.getChildCount(); i++) {
			genderOptions.getChildAt(i).setEnabled(enabled);
		}

		for (int i = 0; i < budgetOptions.getChildCount(); i++) {
			budgetOptions.getChildAt(i).setEnabled(enabled);
		}

		for (int i = 0; i < spicyOptions.getChildCount(); i++) {
			spicyOptions.getChildAt(i).setEnabled(enabled);
		}

		for (CheckBox checkBox : allergyCheckboxes) {
			checkBox.setClickable(enabled);
			checkBox.setEnabled(enabled);
		}

		if (enabled) {
			setAllergiesBtn.setEnabled(true);
			setAllergiesBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.enabled_background));
		} else {
			setAllergiesBtn.setEnabled(false);
			setAllergiesBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.disabled_background));
		}

		dishPreferencesSection.setEnabled(false);
		dishPreferencesSection.setBackground(ContextCompat.getDrawable(this, R.drawable.disabled_background));
		disableAllChildViews(dishContainer);
	}

	private void enableAllergySelection() {
		for (CheckBox checkBox : allergyCheckboxes) {
			checkBox.setEnabled(true);
		}
	}

	private void disableAllergySelection() {
		for (CheckBox checkBox : allergyCheckboxes) {
			checkBox.setEnabled(false);
		}
	}

	private void fetchSafeDishes() {
		List<String> selectedAllergies = getSelectedAllergies();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String requestBody = gson.toJson(selectedAllergies);
		Log.d(TAG, "Request Body: " + requestBody);

		ApiService apiService = RetrofitClient.getApiService(jwtToken);
		Call<List<Dish>> call = apiService.getSafeDishes(selectedAllergies);

		call.enqueue(new Callback<List<Dish>>() {
			@Override
			public void onResponse(Call<List<Dish>> call, Response<List<Dish>> response) {
				if (response.isSuccessful() && response.body() != null) {
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
					Toast.makeText(UpdateUserActivity.this, "Failed to fetch safe dishes", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<List<Dish>> call, Throwable t) {
				Log.e(TAG, "Network Error: ", t);
				Toast.makeText(UpdateUserActivity.this, "Network Error!", Toast.LENGTH_SHORT).show();
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

	private void disableAllChildViews(ViewGroup parent) {
		for (int i = 0; i < parent.getChildCount(); i++) {
			View child = parent.getChildAt(i);
			child.setEnabled(false);
			child.setBackground(ContextCompat.getDrawable(this, R.drawable.disabled_background));
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

	public void updateUser() {
		if (!validateInputs()) {
			Toast.makeText(this, "Invalid inputs", Toast.LENGTH_SHORT).show();
			return;
		}

		UserDTO user = createUserFromInput();

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String userJson = gson.toJson(user);
		Log.d(TAG, "UserDTO JSON: " + userJson);

		ApiService apiService = RetrofitClient.getApiService(jwtToken);
		Call<ResponseBody> call = apiService.updateUser(userId, user);

		call.enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
				if (response.isSuccessful()) {
					Log.d(TAG, "Update Successful: " + response.body().toString());
					setFieldsEditable(false);
					logout();
					updateSuccessDialog();
				} else {
					try {
						Log.e(TAG, "Update Failed: " + response.errorBody().string());
					} catch (IOException e) {
						Log.e(TAG, "Error parsing error response", e);
					}
					Toast.makeText(UpdateUserActivity.this, "Update Failed!", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable t) {
				Log.e(TAG, "Network Error: ", t);
				Toast.makeText(UpdateUserActivity.this, "Network Error!", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private boolean validateInputs() {
		boolean isValid = true;

		String name = nameEditText.getText().toString().trim();
		if (name.isEmpty()) {
			nameEditText.setError("Name is required");
			isValid = false;
		} else if (name.length() > 100) {
			nameEditText.setError("Name must be less than 100 characters");
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

		String email = emailEditText.getText().toString().trim();
		if (email.isEmpty()) {
			emailEditText.setError("Email is required");
			isValid = false;
		} else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			emailEditText.setError("Email should be valid");
			isValid = false;
		}

		String ageStr = ageEditText.getText().toString().trim();
		if (ageStr.isEmpty()) {
			ageEditText.setError("Age must be provided");
			isValid = false;
		} else {
			try {
				int age = Integer.parseInt(ageStr);
				if (age < 0) {
					ageEditText.setError("Age must be a positive number");
					isValid = false;
				} else if (age > 150) {
					ageEditText.setError("Age must be less than or equal to 150");
					isValid = false;
				}
			} catch (NumberFormatException e) {
				ageEditText.setError("Invalid age");
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
		String name = nameEditText.getText().toString();
		String username = usernameText.getText().toString().trim();
		String email = emailEditText.getText().toString();
		String country = countryEditText.getText().toString();
		Integer age = Integer.parseInt(ageEditText.getText().toString());
		String gender = getSelectedGender();
		String budget = getSelectedBudget();
		boolean likesSpicy = getSpicyPreference();
		List<String> selectedAllergies = getSelectedAllergies();
		List<String> selectedDishes = getSelectedDishes();

		UserDTO user = new UserDTO();
		user.setName(name);
		user.setUsername(username);
		user.setEmail(email);
		user.setAge(age);
		user.setGender(gender);
		user.setCountry(country);
		user.setLikesSpicy(likesSpicy);
		user.setPreferredBudget(budget);
		user.setAllergies(selectedAllergies);
		user.setDishPreferences(selectedDishes);
		user.setPassword(password);
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

	private List<String> getSelectedDishes() {
		List<String> selectedDishes = new ArrayList<>();
		for (Dish dish : dishSelections) {
			String dishName = dish.getDishDetailName();
			selectedDishes.add(dishName);
		}
		return selectedDishes;
	}

	private void updateSuccessDialog() {
		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_custom, null);
		TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
		dialogMessage.setText("Your profile is successfully updated!");

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(dialogView);
		builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.show();

		new Handler().postDelayed(() -> {
			dialog.dismiss();
			redirectToMainActivity();
		}, 3000);
	}

	private void redirectToMainActivity() {
		Intent intent = new Intent(UpdateUserActivity.this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
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
