package com.example.lioneats.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
	private String name;
	private String username;
	private String password;
	private String email;
	private Integer age;
	private String gender;
	private String country;
	private String preferredBudget;
	private boolean likesSpicy;
	private List<String> allergies;
	private List<String> dishPreferences;

}
