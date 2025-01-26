package com.example.lioneats.dtos;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@SerializedName("photo_reference")
	private String photoReference;

	@SerializedName("height")
	private int height;

	@SerializedName("width")
	private int width;
}