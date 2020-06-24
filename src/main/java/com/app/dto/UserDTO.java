package com.app.dto;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class UserDTO {

	private SimpleIntegerProperty num;
	private SimpleStringProperty email;
	private SimpleStringProperty firstKeyword;
	private SimpleStringProperty secondKeyword;

	public UserDTO() {
	}

	public UserDTO(Integer num, String email, String firstKeyword, String secondKeyword) {
		this.num = new SimpleIntegerProperty(num);
		this.email = new SimpleStringProperty(email);
		this.firstKeyword = new SimpleStringProperty(firstKeyword);
		this.secondKeyword = new SimpleStringProperty(secondKeyword);
	}

	public Integer getNum() {
		return num.get();
	}

	public void setNum(Integer num) {
		this.num.set(num);
	}

	public String getEmail() {
		return email.get();
	}

	public void setEmail(String email) {
		this.email.set(email);
	}

	public String getFirstKeyword() {
		return firstKeyword.get();
	}

	public void setFirstKeyword(String firstKeyword) {
		this.firstKeyword.set(firstKeyword);
	}

	public String getSecondKeyword() {
		return secondKeyword.get();
	}

	public void setSecondKeyword(String secondKeyword) {
		this.secondKeyword.set(secondKeyword);
	}

}
