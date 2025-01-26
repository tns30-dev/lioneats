package com.example.lioneats.dtos;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	@SerializedName("lat")
	private double latitude;

	@SerializedName("lng")
	private double longitude;
}
