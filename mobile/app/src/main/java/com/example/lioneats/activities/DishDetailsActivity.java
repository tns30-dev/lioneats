package com.example.lioneats.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.lioneats.R;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.fragments.HeaderFragment;
import com.example.lioneats.models.Allergy;
import com.example.lioneats.models.Dish;
import com.example.lioneats.models.DishDetail;
import com.example.lioneats.api.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DishDetailsActivity extends AppCompatActivity {
	private static final String TAG = "DishDetailsActivity";

	private TextView dishNameText, dishAllergiesText, dishIngredientsText, dishHistoryText, dishDescriptionText;
	private ImageView dishImage;
	private List<Allergy> allergyList = new ArrayList<>();
	private List<Dish> dishList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dish_details);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.headerFragmentContainer, new HeaderFragment());
		transaction.commit();

		dishNameText = findViewById(R.id.dishNameText);
		dishAllergiesText = findViewById(R.id.dishAllergiesText);
		dishIngredientsText = findViewById(R.id.dishIngredientsText);
		dishHistoryText = findViewById(R.id.dishHistoryText);
		dishDescriptionText = findViewById(R.id.dishDescriptionText);
		dishImage = findViewById(R.id.dishImage);
		loadAllergiesFromPreferences();
		loadDishesFromPreferences();

		int dishID = getIntent().getIntExtra("dishID", -999);
		String dishImageUrl = getIntent().getStringExtra("dishImageUrl");
		if (dishID != -999) {
			setDishImage(dishImageUrl);
			fetchDishData(dishID);
		} else {
			Toast.makeText(this, "Invalid dish ID", Toast.LENGTH_SHORT).show();
		}
	}

	private void loadAllergiesFromPreferences() {
		SharedPreferences allergyListPreferences = getSharedPreferences("allergy_list", MODE_PRIVATE);
		String allergyJson = allergyListPreferences.getString("allergies", null);

		if (allergyJson != null) {
			Type listType = new TypeToken<List<Allergy>>() {}.getType();
			allergyList = new Gson().fromJson(allergyJson, listType);
		} else {
			Toast.makeText(this, "No allergies found", Toast.LENGTH_SHORT).show();
		}
	}

	private void loadDishesFromPreferences() {
		SharedPreferences dishListPreferences = getSharedPreferences("dish_list", MODE_PRIVATE);
		String dishJson = dishListPreferences.getString("dishes", null);

		if (dishJson != null) {
			Type listType = new TypeToken<List<Dish>>() {}.getType();
			dishList = new Gson().fromJson(dishJson, listType);
		} else {
			Toast.makeText(this, "No dishes found", Toast.LENGTH_SHORT).show();
			Log.e(TAG, "No dishes found in SharedPreferences");
		}
	}

	private void setDishImage(String dishImageUrl) {
		Glide.with(this)
				.load(dishImageUrl)
				.placeholder(R.drawable.default_image)
				.error(R.drawable.default_image)
				.into(dishImage);
	}

	private void fetchDishData(int dishID) {
		ApiService apiService = RetrofitClient.getApiServiceWithoutToken();
		Call<DishDetail> call = apiService.getDishById(dishID);

		call.enqueue(new Callback<DishDetail>() {
			@Override
			public void onResponse(Call<DishDetail> call, Response<DishDetail> response) {
				if (response.isSuccessful() && response.body() != null) {
					Toast.makeText(DishDetailsActivity.this, "Data fetched", Toast.LENGTH_SHORT).show();
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					String jsonResponse = gson.toJson(response.body());
					Log.d(TAG, "JSON Response: " + jsonResponse);

					DishDetail dishDetail = response.body();
					updateUI(dishDetail);
				} else {
					Toast.makeText(DishDetailsActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<DishDetail> call, Throwable t) {
				Toast.makeText(DishDetailsActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "Network Error: ", t);
			}
		});
	}

	private void updateUI(DishDetail dishDetail) {
		dishNameText.setText(dishDetail.getName());
		dishIngredientsText.setText(dishDetail.getIngredients());
		dishHistoryText.setText(dishDetail.getHistory());
		dishDescriptionText.setText(dishDetail.getDescription());

		StringBuilder dishAllergies = new StringBuilder();

		Log.d(TAG, "Dish Name: " + dishDetail.getName());
		for (Dish dish : dishList) {
			Log.d(TAG, "Dish Name: " + dish.getDishDetailName());
			if (dish.getDishDetailName().trim().equalsIgnoreCase(dishDetail.getName().trim())) {
				for (Allergy allergy : allergyList) {
					Log.d(TAG, "Checking allergy: " + allergy.getName());
					for (DishDetail allergyDish : allergy.getDishes()) {
						if (allergyDish.getName().trim().equalsIgnoreCase(dishDetail.getName().trim())) {
							Log.d(TAG, "Found allergy: " + allergy.getName());
							dishAllergies.append(allergy.getName()).append(" allergy    ");
							break;
						}
					}
				}
			}
		}
		dishAllergiesText.setText(dishAllergies.toString());
		Log.d(TAG, "Allergies: " + dishAllergies.toString());
	}
}
