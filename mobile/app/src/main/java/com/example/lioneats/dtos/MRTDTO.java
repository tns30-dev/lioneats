package com.example.lioneats.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MRTDTO {
	private Long id;
	private String name;
	private double latitude;
	private double longitude;
	private List<String> lines;
	private double distance;

}
