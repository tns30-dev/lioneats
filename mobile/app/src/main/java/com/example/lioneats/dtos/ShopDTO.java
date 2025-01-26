package com.example.lioneats.dtos;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	@SerializedName("place_id")
	private String placeId;

	@SerializedName("name")
	private String name;

	@SerializedName("formatted_address")
	private String formattedAddress;

	@SerializedName("formatted_phone_number")
	private String formattedPhoneNumber;

	@SerializedName("rating")
	private double rating;

	@SerializedName("price_level")
	private int priceLevel;

	@SerializedName("website")
	private String websiteUrl;

	@SerializedName("url")
	private String googleUrl;

	@SerializedName("user_ratings_total")
	private int userRatingsTotal;

	@SerializedName("opening_hours")
	private OpeningHourDTO openingHours;

	@SerializedName("reviews")
	private List<ReviewDTO> reviews;

	@SerializedName("photos")
	private List<PhotoDTO> photos;

	private String keyWord;

	@SerializedName("geometry")
	private GeometryDTO geometry;

	public double getLatitude() {
		return getGeometry() != null && getGeometry().getLocation() != null ? getGeometry().getLocation().getLatitude() : 0;
	}

	public double getLongitude() {
		return getGeometry() != null && getGeometry().getLocation() != null ? getGeometry().getLocation().getLongitude() : 0;
	}

	public void setLatitude(double latitude) {
		if (this.getGeometry() != null && this.getGeometry().getLocation() != null) {
			this.getGeometry().getLocation().setLatitude(latitude);
		}
	}

	public void setLongitude(double longitude) {
		if (this.getGeometry() != null && this.getGeometry().getLocation() != null) {
			this.getGeometry().getLocation().setLongitude(longitude);
		}
	}

	public GeometryDTO getGeometry() {
		return geometry;
	}

	public void setGeometry(GeometryDTO geometry) {
		this.geometry = geometry;
	}
}
