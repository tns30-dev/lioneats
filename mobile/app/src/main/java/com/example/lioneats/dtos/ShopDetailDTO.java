package com.example.lioneats.dtos;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopDetailDTO implements Serializable {

	@SerializedName("result")
	private ShopDTO shopDetail;
}

