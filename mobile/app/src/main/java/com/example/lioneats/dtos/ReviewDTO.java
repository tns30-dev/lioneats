package com.example.lioneats.dtos;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	@SerializedName("author_name")
	private String authorName;

	@SerializedName("rating")
	private double rating;

	@SerializedName("text")
	private String text;}

