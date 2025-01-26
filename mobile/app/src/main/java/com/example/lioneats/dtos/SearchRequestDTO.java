package com.example.lioneats.dtos;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequestDTO implements Serializable {
	private List<String> location;
	private List<String> allergies;
	private String budget;
	private List<String> dishes;
	private double minRating;
}
