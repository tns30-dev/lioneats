package com.example.lioneats.dtos;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO implements Serializable{
	@SerializedName("jwt")
	private String jwt;

	@SerializedName("userId")
	private long userId;

	@SerializedName("username")
	private String username;

	@SerializedName("email")
	private String email;

	@SerializedName("message")
	private String message;
}
