package com.example.lioneats.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lioneats.R;
import com.example.lioneats.activities.ShopDetailsActivity;
import com.example.lioneats.dtos.ShopDTO;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

	private static final String TAG = "RestaurantAdapter";
	private final List<ShopDTO> restaurantList;
	private final String username;

	public RestaurantAdapter(List<ShopDTO> restaurantList, String username) {
		this.restaurantList = restaurantList != null ? restaurantList : List.of();
		this.username = username;
	}

	@NonNull
	@Override
	public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant, parent, false);
		return new RestaurantViewHolder(view, username);
	}

	@Override
	public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
		if (restaurantList == null || restaurantList.size() <= position) {
			Log.e(TAG, "Attempted to bind view holder at position " + position + ", but the list size is " + (restaurantList == null ? "null" : restaurantList.size()));
			return;
		}

		Log.d(TAG, "Binding view holder at position: " + position + " with list size: " + restaurantList.size());

		ShopDTO restaurant = restaurantList.get(position);
		holder.bind(restaurant);

		holder.nameTextView.setText(restaurant.getName() != null ? restaurant.getName() : "Unknown");
		holder.addressTextView.setText(restaurant.getFormattedAddress() != null ? restaurant.getFormattedAddress() : "Unknown");
		holder.ratingTextView.setText(restaurant.getRating() != 0 ? String.valueOf(restaurant.getRating()) : "Unknown");
		holder.keyTextView.setText(restaurant.getKeyWord() != null ? restaurant.getKeyWord() : "Unknown");
		holder.priceTextView.setText(getPriceLevel(restaurant.getPriceLevel()));

		String photoUrl = null;
		if (restaurant.getPhotos() != null && !restaurant.getPhotos().isEmpty()) {
			photoUrl = restaurant.getPhotos().get(0).getPhotoReference();
		}

		if (photoUrl != null && !photoUrl.isEmpty()) {
			Glide.with(holder.photoImageView.getContext())
					.load(photoUrl)
					.placeholder(R.drawable.default_image)
					.error(R.drawable.default_image)
					.into(holder.photoImageView);
		} else {
			holder.photoImageView.setImageResource(R.drawable.default_image);
		}

		if (restaurantList.size() > position + 1) {
			String nextPhotoUrl = restaurantList.get(position + 1).getPhotos() != null ?
					restaurantList.get(position + 1).getPhotos().get(0).getPhotoReference() : null;
			if (nextPhotoUrl != null && !nextPhotoUrl.isEmpty()) {
				Glide.with(holder.photoImageView.getContext())
						.load(nextPhotoUrl)
						.preload();
			}
		}
	}

	@Override
	public int getItemCount() {
		return restaurantList.size();
	}

	public static class RestaurantViewHolder extends RecyclerView.ViewHolder {

		TextView nameTextView;
		TextView addressTextView;
		TextView ratingTextView;
		TextView keyTextView;
		TextView priceTextView;
		ImageView photoImageView;

		public RestaurantViewHolder(@NonNull View itemView, String username) {
			super(itemView);
			nameTextView = itemView.findViewById(R.id.restaurant_name);
			addressTextView = itemView.findViewById(R.id.restaurant_address);
			ratingTextView = itemView.findViewById(R.id.restaurant_rating);
			keyTextView = itemView.findViewById(R.id.restaurant_key);
			priceTextView = itemView.findViewById(R.id.restaurant_price);
			photoImageView = itemView.findViewById(R.id.restaurant_photo);

			if (username != null) {
				itemView.setOnClickListener(v -> {
					ShopDTO shop = (ShopDTO) itemView.getTag();
					Intent intent = new Intent(itemView.getContext(), ShopDetailsActivity.class);
					intent.putExtra("shop", shop);
					itemView.getContext().startActivity(intent);
				});
			}
		}

		public void bind(ShopDTO shop) {
			itemView.setTag(shop);
		}
	}

	private String getPriceLevel(int priceLevel) {
		switch (priceLevel) {
			case 1: return "$";
			case 2: return "$$";
			case 3: return "$$$";
			case 4: return "$$$$";
			default: return "Unknown";
		}
	}
}
