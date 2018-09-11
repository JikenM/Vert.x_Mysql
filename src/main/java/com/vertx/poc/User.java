package com.vertx.poc;

import io.vertx.core.json.JsonObject;

public class User {
	private int id;
	private String country;
	private String name;

	public User(JsonObject json) {
		this.id = json.getInteger("id");
		this.country = json.getString("country");
		this.name = json.getString("name");

	}

	public User() {
		super();
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
}
