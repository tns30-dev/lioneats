package com.example.lioneats.dtos;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLocationDTO  implements Serializable {
	@SerializedName("latitude")
	private double latitude;
	@SerializedName("longitude")
	private double longitude;
}
