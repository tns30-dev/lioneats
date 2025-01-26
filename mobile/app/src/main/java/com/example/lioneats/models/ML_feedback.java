package com.example.lioneats.models;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ML_feedback {
	private String imageLocation;
	private String ml_result;
	private String userDish;
	private String remarks;
}
