package com.example.lioneats.dtos;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpeningHourDTO implements Serializable{
	private static final long serialVersionUID = 1L;

	@SerializedName("weekday_text")
	private List<String> weekdayText;

}
