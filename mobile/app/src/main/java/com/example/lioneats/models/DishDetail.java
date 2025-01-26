package com.example.lioneats.models;

public class DishDetail {
	private String name;
	private Boolean isSpicy;
	private String ingredients;
	private String history;
	private String description;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getSpicy() {
		return isSpicy;
	}

	public void setSpicy(Boolean spicy) {
		isSpicy = spicy;
	}

	public String getIngredients() {
		return ingredients;
	}

	public void setIngredients(String ingredients) {
		this.ingredients = ingredients;
	}

	public String getHistory() {
		return history;
	}

	public void setHistory(String history) {
		this.history = history;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
