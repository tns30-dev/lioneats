package com.example.lioneats.models;

import java.util.List;

public class Allergy {
	private Long id;
	private String name;
	private List<DishDetail> dishes;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<DishDetail> getDishes() {
		return dishes;
	}

	public void setDishes(List<DishDetail> dishes) {
		this.dishes = dishes;
	}
}
