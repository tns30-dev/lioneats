package com.example.lioneats.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.lioneats.R;
import com.example.lioneats.adapters.ImageAdapter;
import com.example.lioneats.adapters.RestaurantAdapter;
import com.example.lioneats.api.ApiService;
import com.example.lioneats.api.RetrofitClient;
import com.example.lioneats.dtos.MRTDTO;
import com.example.lioneats.dtos.SearchRequestDTO;
import com.example.lioneats.dtos.ShopDTO;
import com.example.lioneats.dtos.UserLocationDTO;
import com.example.lioneats.fragments.HeaderFragment;
import com.example.lioneats.models.Allergy;
import com.example.lioneats.models.Dish;
import com.example.lioneats.models.UserDTO;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener {

	private static final int REQUEST_CODE = 100;
	private static final String TAG = "MainActivity";

	private Handler handler;
	private Runnable runnable;
	private int currentItem = 0;
	private ViewPager2 viewPager;
	private List<Dish> dishList = new ArrayList<>();
	private List<Allergy> allergyList = new ArrayList<>();
	private List<MRTDTO> mrtList = new ArrayList<>();
	private final List<String> dishNames = new ArrayList<>();
	private final List<String> allergyNames = new ArrayList<>();
	private final List<String> mrtNames = new ArrayList<>();
	private List<String> locationMRTs;
	private List<String> selectedDish;
	private List<String> selectedLocation;
	private List<String> selectedAllergies;
	private String selectedBudget;
	private double selectedRating;
	private String username;
	private UserDTO user;
	private FusedLocationProviderClient fusedLocationClient;
	private LocationCallback locationCallback;
	private UserLocationDTO currentLocation;
	private String jwtToken;
	private SharedPreferences userSessionPreferences, userPreferences, dishListPreferences, allergyListPreferences, mrtListPreferences;
	private ProgressBar progressBar;
	private RestaurantAdapter restaurantAdapter;
	private final List<ShopDTO> restaurantList = new ArrayList<>();
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private TextView emptyView;
	private RecyclerView recyclerView;
	private ExecutorService executorService;
	private Handler mainHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		executorService = Executors.newSingleThreadExecutor();
		mainHandler = new Handler(Looper.getMainLooper());

		initPreferences();
		username = userSessionPreferences.getString("username", null);
		Log.d(TAG, "Username from preferences: " + username);

		setupUI();
		setupLocationServices();
		loadListsFromPreferencesOrFetch();
		locationMRTs = new ArrayList<>();
		if (username != null) {
			handleUserLogin();
		} else {
			handleShopFetchingLogic();
		}
	}

	private void handleUserLogin() {
		LinearLayout userHomeLayout = findViewById(R.id.userHomeLayout);
		userHomeLayout.setVisibility(View.VISIBLE);
		user = new UserDTO();
		String userJson = userPreferences.getString("user", null);
		if (userJson != null) {
			user = new Gson().fromJson(userJson, UserDTO.class);
		}
		jwtToken = userSessionPreferences.getString("jwt", "");
		filterAndDisplayShops();
		setupSpinners();
	}

	private void handleShopFetchingLogic() {
		if (isLocationPermissionGranted()) {
			if (isCurrentLocationAvailable()) {
				fetchShopsByLocation(currentLocation);
			} else {
				getLastLocation();
			}
		} else {
			askPermission();
		}
	}

	private void initPreferences() {
		userSessionPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
		userPreferences = getSharedPreferences("user", MODE_PRIVATE);
		dishListPreferences = getSharedPreferences("dish_list", MODE_PRIVATE);
		allergyListPreferences = getSharedPreferences("allergy_list", MODE_PRIVATE);
		mrtListPreferences = getSharedPreferences("mrt_list", MODE_PRIVATE);
	}

	private void loadListsFromPreferencesOrFetch() {
		loadDishesFromPreferences();
		loadAllergiesFromPreferences();
		loadMRTsFromPreferences();

		for (Dish dish : dishList) {
			dishNames.add(dish.getDishDetailName());
		}
		for (Allergy allergy : allergyList) {
			allergyNames.add(allergy.getName());
		}
		for (MRTDTO mrt : mrtList) {
			mrtNames.add(mrt.getName());
		}
	}

	private void loadDishesFromPreferences() {
		String dishesJson = dishListPreferences.getString("dishes", null);
		if (dishesJson != null) {
			dishList = new Gson().fromJson(dishesJson, new TypeToken<List<Dish>>() {
			}.getType());
			setupViewPager();
		} else {
			fetchAndUpdateDishes();
		}
	}

	private void loadAllergiesFromPreferences() {
		String allergiesJson = allergyListPreferences.getString("allergies", null);
		if (allergiesJson != null) {
			allergyList = new Gson().fromJson(allergiesJson, new TypeToken<List<Allergy>>() {
			}.getType());
		} else {
			fetchAndUpdateAllergies();
		}
	}

	private void loadMRTsFromPreferences() {
		String mrtJson = mrtListPreferences.getString("MRTs", null);
		if (mrtJson != null) {
			mrtList = new Gson().fromJson(mrtJson, new TypeToken<List<MRTDTO>>() {
			}.getType());
		} else {
			fetchAndUpdateMRTs();
		}
	}

	private void fetchAndUpdateDishes() {
		ApiService apiService = RetrofitClient.getApiServiceWithoutToken();

		Call<List<Dish>> call = apiService.getAllDishes();

		call.enqueue(new Callback<List<Dish>>() {
			@Override
			public void onResponse(Call<List<Dish>> call, Response<List<Dish>> response) {
				if (response.isSuccessful() && response.body() != null) {
					Log.d(TAG, "Dishes Response Body: " + gson.toJson(response.body()));
					dishList = response.body();
					SharedPreferences.Editor dishEditor = dishListPreferences.edit();
					dishEditor.putString("dishes", gson.toJson(dishList));
					dishEditor.apply();
					setupViewPager();
				} else {
					Toast.makeText(MainActivity.this, "Failed to fetch dish list", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<List<Dish>> call, Throwable t) {
				Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "Network Error: ", t);
			}
		});
	}

	private void fetchAndUpdateAllergies() {
		ApiService apiService = RetrofitClient.getApiServiceWithoutToken();

		Call<List<Allergy>> call = apiService.getAllergies();

		call.enqueue(new Callback<List<Allergy>>() {
			@Override
			public void onResponse(Call<List<Allergy>> call, Response<List<Allergy>> response) {
				if (response.isSuccessful() && response.body() != null) {
					Log.d(TAG, "Allergies Response Body: " + gson.toJson(response.body()));
					allergyList = response.body();
					SharedPreferences.Editor allergyEditor = allergyListPreferences.edit();
					allergyEditor.putString("allergies", gson.toJson(allergyList));
					allergyEditor.apply();
				} else {
					Toast.makeText(MainActivity.this, "Failed to fetch allergy list", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<List<Allergy>> call, Throwable t) {
				Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "Network Error: ", t);
			}
		});
	}

	private void fetchAndUpdateMRTs() {
		ApiService apiService = RetrofitClient.getApiServiceWithoutToken();

		Call<List<MRTDTO>> call = apiService.getMRTList();

		call.enqueue(new Callback<List<MRTDTO>>() {
			@Override
			public void onResponse(Call<List<MRTDTO>> call, Response<List<MRTDTO>> response) {
				if (response.isSuccessful() && response.body() != null) {
					Log.d(TAG, "MRTs Response Body: " + gson.toJson(response.body()));
					mrtList = response.body();
					SharedPreferences.Editor mrtEditor = mrtListPreferences.edit();
					mrtEditor.putString("MRTs", gson.toJson(mrtList));
					mrtEditor.apply();
				} else {
					Toast.makeText(MainActivity.this, "Failed to fetch MRT list", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onFailure(Call<List<MRTDTO>> call, Throwable t) {
				Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "Network Error: ", t);
			}
		});
	}

	private void setupUI() {
		progressBar = findViewById(R.id.progressBar);
		progressBar.setVisibility(View.VISIBLE);

		emptyView = findViewById(R.id.empty_view);
		emptyView.setVisibility(View.GONE);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.headerFragmentContainer, new HeaderFragment());
		transaction.commit();

		viewPager = findViewById(R.id.viewPager);

		recyclerView = findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		restaurantAdapter = new RestaurantAdapter(restaurantList, username);
		recyclerView.setAdapter(restaurantAdapter);
	}

	private void setupLocationServices() {
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
		locationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				if (locationResult != null) {
					Location location = locationResult.getLastLocation();
					if (location != null) {
						currentLocation = new UserLocationDTO(location.getLatitude(), location.getLongitude());
						fetchShopsByLocation(currentLocation);
					} else {
						fetchShopsByDefaultLocation();
					}
				} else {
					fetchShopsByDefaultLocation();
				}
			}
		};
	}

	private boolean isLocationPermissionGranted() {
		return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
	}

	private boolean isCurrentLocationAvailable() {
		if (currentLocation == null) {
			getLastLocation();
		}
		return currentLocation != null;
	}

	private void getLastLocation() {
		if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			fusedLocationClient.getLastLocation()
					.addOnSuccessListener(location -> {
						if (location != null) {
							currentLocation = new UserLocationDTO(location.getLatitude(), location.getLongitude());
							fetchShopsByLocation(currentLocation);
						} else {
							requestNewLocationData();
						}
					})
					.addOnFailureListener(e -> {
						Log.e(TAG, "Error trying to get location", e);
						Toast.makeText(MainActivity.this, "Error trying to get location", Toast.LENGTH_SHORT).show();
						fetchShopsByDefaultLocation();
					});
		} else {
			askPermission();
		}
	}

	private void requestNewLocationData() {
		if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
					.setMinUpdateIntervalMillis(5000)
					.build();

			fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
		} else {
			askPermission();
		}
	}

	private void askPermission() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
			Toast.makeText(this, "Location permission is needed to show nearby restaurants.", Toast.LENGTH_LONG).show();
		}
		ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				getLastLocation();
			} else {
				Toast.makeText(this, "Permission denied. Using default location.", Toast.LENGTH_SHORT).show();
				fetchShopsByDefaultLocation();
			}
		}
	}

	private void fetchShopsByLocation(UserLocationDTO location) {
		ApiService apiService = RetrofitClient.getApiServiceWithoutToken();
		String requestBody = gson.toJson(location);
		Log.d(TAG, "Request Body: " + requestBody);

		Call<List<ShopDTO>> call = apiService.getShopsByLocation(location);

		call.enqueue(new Callback<List<ShopDTO>>() {
			@Override
			public void onResponse(Call<List<ShopDTO>> call, Response<List<ShopDTO>> response) {
				if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
					List<ShopDTO> shops = response.body();
					processRestaurantListInBackground(shops);
				} else {
					String errorMsg = response.message() != null ? response.message() : "Unknown error";
					Log.e(TAG, "Failed to fetch restaurants: " + errorMsg + " (Code: " + response.code() + ")");
					handleFetchError("Failed to fetch restaurants: " + errorMsg);
				}
			}

			@Override
			public void onFailure(Call<List<ShopDTO>> call, Throwable t) {
				Log.e(TAG, "Network Error: ", t);
				handleFetchError("Network Error: " + t.getMessage());
			}
		});
	}

	private void fetchShopsByDefaultLocation() {
		ApiService apiService = RetrofitClient.getApiServiceWithoutToken();

		Call<List<ShopDTO>> call = apiService.getShopsDefault();

		call.enqueue(new Callback<List<ShopDTO>>() {
			@Override
			public void onResponse(Call<List<ShopDTO>> call, Response<List<ShopDTO>> response) {
				if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
					List<ShopDTO> shops = response.body();
					processRestaurantListInBackground(shops);
				} else {
					handleFetchError("No restaurants available for default location: " + response.message());
				}
			}

			@Override
			public void onFailure(Call<List<ShopDTO>> call, Throwable t) {
				handleFetchError("Network error while fetching default location restaurants", t);
			}
		});
	}

	private void processRestaurantListInBackground(List<ShopDTO> newShops) {
		executorService.execute(() -> {
			List<ShopDTO> processedShops = processShops(newShops);
			mainHandler.post(() -> updateRestaurantList(processedShops));
		});
	}

	private List<ShopDTO> processShops(List<ShopDTO> shops) {
		List<ShopDTO> processedShops = new ArrayList<>();
		for (ShopDTO shop : shops) {
			if (shop.getRating() > 4.0) {
				processedShops.add(shop);
			}
		}
		return processedShops;
	}

	private void handleFetchError(String message) {
		progressBar.setVisibility(View.GONE);
		emptyView.setVisibility(View.VISIBLE);
		Log.e(TAG, message);
	}

	private void handleFetchError(String message, Throwable t) {
		progressBar.setVisibility(View.GONE);
		emptyView.setVisibility(View.VISIBLE);
		Log.e(TAG, message, t);
	}

	@Override
	protected void onPause() {
		super.onPause();
		fusedLocationClient.removeLocationUpdates(locationCallback);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isLocationPermissionGranted()) {
			getLastLocation();
		} else {
			fetchShopsByDefaultLocation();
		}
	}

	private void setupViewPager() {
		ImageAdapter adapter = new ImageAdapter(this, dishList, this);
		viewPager.setAdapter(adapter);

		handler = new Handler(Looper.getMainLooper());
		runnable = new Runnable() {
			@Override
			public void run() {
				if (currentItem == dishList.size()) {
					currentItem = 0;
				}
				viewPager.setCurrentItem(currentItem++, true);
				handler.postDelayed(this, 2000);
			}
		};
		handler.postDelayed(runnable, 2000);
	}

	@Override
	public void onItemClick(int position) {
		Dish selectedDish = dishList.get(position);
		Intent intent = new Intent(MainActivity.this, DishDetailsActivity.class);
		intent.putExtra("dishID", selectedDish.getId());
		intent.putExtra("dishImageUrl", selectedDish.getImageUrl());
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (handler != null) {
			handler.removeCallbacks(runnable);
		}
	}

	private void setupSpinners() {
		setupBudgetSpinner();
		setupRatingSpinner();
		setupDishSpinner();
		setupAllergySpinner();
		setupLocationSpinner();
		findViewById(R.id.refreshBtn).setOnClickListener(v -> filterAndDisplayShops());
	}

	private void setupBudgetSpinner() {
		Spinner budgetSpinner = findViewById(R.id.spinnerBudget);
		List<String> budgetOptions = new ArrayList<>();
		budgetOptions.add("");
		budgetOptions.addAll(Arrays.asList(getResources().getStringArray(R.array.spinnerBudget_items)));

		ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
				android.R.layout.simple_spinner_item, budgetOptions);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		budgetSpinner.setAdapter(adapter);

		if (user != null && user.getPreferredBudget() != null) {
			int position = adapter.getPosition(user.getPreferredBudget());
			budgetSpinner.setSelection(position);
		}

		budgetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				selectedBudget = position != 0 ? parent.getItemAtPosition(position).toString() : "";
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				selectedBudget = "";
			}
		});
	}

	private void setupRatingSpinner() {
		Spinner ratingSpinner = findViewById(R.id.spinnerRating);
		List<String> ratingOptions = new ArrayList<>();
		ratingOptions.add("");
		ratingOptions.addAll(Arrays.asList(getResources().getStringArray(R.array.spinnerRating_items)));
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
				android.R.layout.simple_spinner_item, ratingOptions);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ratingSpinner.setAdapter(adapter);

		ratingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				selectedRating = position != 0 ? Double.parseDouble(parent.getItemAtPosition(position).toString()) : 0.0;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				selectedRating = 0.0;
			}
		});
	}

	private void showMultiSelectionDialog(String title, List<String> items, List<String> selectedItems, Spinner spinner) {
		boolean[] checkedItems = new boolean[items.size()];

		for (int i = 0; i < items.size(); i++) {
			checkedItems[i] = selectedItems.contains(items.get(i));
		}

		String[] itemsArray = items.toArray(new String[0]);

		DialogInterface.OnMultiChoiceClickListener listener = (dialog, which, isChecked) -> {
			if (isChecked) {
				selectedItems.add(items.get(which));
			} else {
				selectedItems.remove(items.get(which));
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMultiChoiceItems(itemsArray, checkedItems, listener);
		builder.setPositiveButton("OK", (dialog, which) -> {
			String text = selectedItems.isEmpty() ? title : selectedItems.toString();
			ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Collections.singletonList(text));
			spinner.setAdapter(adapter);
		});
		builder.setNegativeButton("Cancel", null);
		builder.show();
	}

	private void setupDishSpinner() {
		Spinner dishSpinner = findViewById(R.id.spinnerDish);
		selectedDish = new ArrayList<>();

		if (user != null && user.getDishPreferences() != null) {
			selectedDish.addAll(user.getDishPreferences());
		}

		dishSpinner.setOnTouchListener((v, event) -> {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				v.performClick();
				showMultiSelectionDialog("Select Dishes", dishNames, selectedDish, dishSpinner);
			}
			return true;
		});
	}

	private void setupAllergySpinner() {
		Spinner allergySpinner = findViewById(R.id.spinnerAllergy);
		selectedAllergies = new ArrayList<>();

		if (user != null && user.getAllergies() != null) {
			selectedAllergies.addAll(user.getAllergies());
		}

		allergySpinner.setOnTouchListener((v, event) -> {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				v.performClick();
				showMultiSelectionDialog("Select Allergies", allergyNames, selectedAllergies, allergySpinner);
			}
			return true;
		});
	}

	private void setupLocationSpinner() {
		Spinner locationSpinner = findViewById(R.id.spinnerLocation);
		selectedLocation = new ArrayList<>();

		if (isLocationPermissionGranted() && isCurrentLocationAvailable()) {
			CountDownLatch latch = new CountDownLatch(1);

			fetchMRTbyCurrentLocation(currentLocation, locationMRTs -> {
				this.locationMRTs = locationMRTs;
				latch.countDown();
			});

			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				handleFetchError("Failed to fetch MRT stations in time.");
			}
		}

		if (locationMRTs != null && !locationMRTs.isEmpty()) {
			selectedLocation.addAll(locationMRTs);
		}

		locationSpinner.setOnTouchListener((v, event) -> {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				v.performClick();
				showMultiSelectionDialog("Select Locations", mrtNames, selectedLocation, locationSpinner);
			}
			return true;
		});
	}

	private void filterAndDisplayShops() {
		executorService.execute(() -> {
			SearchRequestDTO searchRequest = setupSearchRequest();
			Log.d(TAG, "API Request: Filtering Shops: " + gson.toJson(searchRequest));

			ApiService apiService = RetrofitClient.getApiService(jwtToken);
			Call<List<ShopDTO>> call = apiService.filterShops(searchRequest);

			call.enqueue(new Callback<List<ShopDTO>>() {
				@Override
				public void onResponse(Call<List<ShopDTO>> call, Response<List<ShopDTO>> response) {
					if (response.isSuccessful() && response.body() != null) {
						List<ShopDTO> filteredShops = response.body();
						mainHandler.post(() -> updateRestaurantList(filteredShops));
					} else {
						mainHandler.post(() -> handleFetchError("No restaurants available for the selected filters: " + response.message()));
					}
				}

				@Override
				public void onFailure(Call<List<ShopDTO>> call, Throwable t) {
					mainHandler.post(() -> handleFetchError("Network error while filtering restaurants", t));
				}
			});
		});
	}

	private void updateRestaurantList(List<ShopDTO> processedShops) {
		mainHandler.post(() -> {
			restaurantList.clear();
			restaurantList.addAll(processedShops);
			restaurantAdapter.notifyDataSetChanged();

			if (restaurantList.isEmpty()) {
				recyclerView.setVisibility(View.GONE);
				emptyView.setVisibility(View.VISIBLE);
			} else {
				recyclerView.setVisibility(View.VISIBLE);
				emptyView.setVisibility(View.GONE);
			}

			progressBar.setVisibility(View.GONE);
		});
	}

	private SearchRequestDTO setupSearchRequest() {
		SearchRequestDTO searchRequest = new SearchRequestDTO();

		if (isLocationPermissionGranted() && isCurrentLocationAvailable()) {
			CountDownLatch latch = new CountDownLatch(1);

			fetchMRTbyCurrentLocation(currentLocation, locationMRTs -> {
				this.locationMRTs = locationMRTs;
				latch.countDown();
			});

			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				handleFetchError("Failed to fetch MRT stations in time.");
			}
		}

		searchRequest.setAllergies(selectedAllergies != null ? selectedAllergies : new ArrayList<>());
		searchRequest.setDishes(selectedDish != null ? selectedDish : new ArrayList<>());
		searchRequest.setLocation(selectedLocation != null && !selectedLocation.isEmpty() ? selectedLocation : locationMRTs != null ? locationMRTs : new ArrayList<>());
		searchRequest.setBudget(selectedBudget != null ? selectedBudget : "");
		searchRequest.setMinRating(selectedRating > 0 ? selectedRating : 0.0);

		return searchRequest;
	}

	private void fetchMRTbyCurrentLocation(UserLocationDTO location, Consumer<List<String>> callback) {
		ApiService apiService = RetrofitClient.getApiServiceWithoutToken();
		Call<List<MRTDTO>> call = apiService.getNearestMRTs(location);

		call.enqueue(new Callback<List<MRTDTO>>() {
			@Override
			public void onResponse(Call<List<MRTDTO>> call, Response<List<MRTDTO>> response) {
				if (response.isSuccessful() && response.body() != null) {
					List<String> locationMRTs = response.body().stream()
							.map(MRTDTO::getName)
							.collect(Collectors.toList());
					callback.accept(locationMRTs);
				} else {
					handleFetchError("Failed to get MRTs: " + response.message());
					callback.accept(Collections.emptyList());
				}
			}

			@Override
			public void onFailure(Call<List<MRTDTO>> call, Throwable t) {
				handleFetchError("Network Error: " + t.getMessage());
				callback.accept(Collections.emptyList());
			}
		});
	}
}

