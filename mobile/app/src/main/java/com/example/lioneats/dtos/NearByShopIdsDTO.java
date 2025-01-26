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
public class NearByShopIdsDTO implements Serializable {

	@SerializedName("results")
	private List<ShopPlaceIdDTO> results;

	@SerializedName("next_page_token")
	private String nextPageToken;
}
