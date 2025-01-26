package com.example.lioneats.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import com.example.lioneats.R;
import com.example.lioneats.dtos.ShopDTO;
import com.example.lioneats.fragments.HeaderFragment;

public class ShopDetailsActivity extends AppCompatActivity {

	private static final String TAG = "ShopDetailsActivity";
	private TextView shopName, shopAddress, shopPhone, shopRating, shopUrl, shopPrice, shopReview1, openingHoursText;
	private ImageView shopDishImage1, shopDishImage2, shopDishImage3;
	private TableLayout openingHoursTable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shop_details);

		shopName = findViewById(R.id.shopName);
		shopAddress = findViewById(R.id.shopAddress);
		shopPhone = findViewById(R.id.shopPhone);
		shopRating = findViewById(R.id.shopRating);
		shopUrl = findViewById(R.id.shopUrl);
		shopPrice = findViewById(R.id.shopPrice);
		shopReview1 = findViewById(R.id.shopReview1);
		shopDishImage1 = findViewById(R.id.shopDishImage1);
		shopDishImage2 = findViewById(R.id.shopDishImage2);
		shopDishImage3 = findViewById(R.id.shopDishImage3);
		openingHoursTable = findViewById(R.id.openingHoursTable);
		openingHoursText = findViewById(R.id.openingHoursText);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.headerFragmentContainer, new HeaderFragment());
		transaction.commit();

		Intent intent = getIntent();
		ShopDTO shop = (ShopDTO) intent.getSerializableExtra("shop");
		if (shop != null) {
			Log.d(TAG, "Shop received: " + shop.getName());
			updateUI(shop);
		} else {
			Log.e(TAG, "No shop data found in intent extras");
		}
	}

	private void updateUI(ShopDTO shop) {
		shopName.setText(shop.getName() != null ? shop.getName() : "Name Not Available");
		shopAddress.setText(shop.getFormattedAddress() != null ? shop.getFormattedAddress() : "Address Not Available");
		shopPhone.setText(shop.getFormattedPhoneNumber() != null ? shop.getFormattedPhoneNumber() : "Phone Not Available");
		shopRating.setText(shop.getRating() != 0 ? String.valueOf(shop.getRating()) : "Ratings Not Available");
		shopUrl.setText(shop.getWebsiteUrl() != null ? shop.getWebsiteUrl() : "Website Not Available");
		shopPrice.setText(getPriceLevel(shop.getPriceLevel()));

		shopUrl.setOnClickListener(v -> {
			String url = shop.getWebsiteUrl();
			if (url != null && !url.isEmpty()) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(intent);
			}
		});

		openingHoursTable.removeAllViews();
		if (shop.getOpeningHours() != null && shop.getOpeningHours().getWeekdayText() != null) {
			for (String hours : shop.getOpeningHours().getWeekdayText()) {
				TableRow row = new TableRow(this);
				row.setLayoutParams(new TableRow.LayoutParams(
						TableRow.LayoutParams.MATCH_PARENT,
						TableRow.LayoutParams.WRAP_CONTENT
				));

				String[] dayAndHours = hours.split(": ", 2);
				if (dayAndHours.length == 2) {
					TextView dayView = new TextView(this);
					dayView.setText(dayAndHours[0]);
					dayView.setPadding(8, 8, 8, 8);

					TextView hoursView = new TextView(this);
					hoursView.setText(dayAndHours[1]);
					hoursView.setPadding(8, 8, 8, 8);

					row.addView(dayView);
					row.addView(hoursView);
				}
				openingHoursTable.addView(row);
			}
		} else {
			openingHoursText.setText("Opening Hours Not Available");
		}

		if (shop.getReviews() != null && !shop.getReviews().isEmpty()) {
			shopReview1.setText(shop.getReviews().get(0).getText());
		} else {
			shopReview1.setText("Reviews Not Available");
		}

		if (shop.getPhotos() != null && shop.getPhotos().size() >= 3) {
			loadImage(shop.getPhotos().get(0).getPhotoReference(), shopDishImage1);
			loadImage(shop.getPhotos().get(1).getPhotoReference(), shopDishImage2);
			loadImage(shop.getPhotos().get(2).getPhotoReference(), shopDishImage3);
		} else {
			shopDishImage1.setImageResource(R.drawable.default_image);
			shopDishImage2.setImageResource(R.drawable.default_image);
			shopDishImage3.setImageResource(R.drawable.default_image);
		}
	}

	private void loadImage(String photoReference, ImageView imageView) {
		Glide.with(imageView.getContext())
				.load(photoReference)
				.placeholder(R.drawable.default_image)
				.error(R.drawable.default_image)
				.into(imageView);
	}

	private String getPriceLevel(int priceLevel) {
		switch (priceLevel) {
			case 1:
				return "$";
			case 2:
				return "$$";
			case 3:
				return "$$$";
			case 4:
				return "$$$$";
			default:
				return "Unknown";
		}
	}
}