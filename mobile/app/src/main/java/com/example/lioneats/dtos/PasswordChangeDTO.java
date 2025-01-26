package com.example.lioneats.dtos;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeDTO implements Serializable{
	@SerializedName("oldPassword")
	private String oldPassword;
	@SerializedName("newPassword")
	private String newPassword;
	@SerializedName("confirmNewPassword")
	private String confirmNewPassword;
}
