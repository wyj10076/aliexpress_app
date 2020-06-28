package com.app.util;

import java.io.File;
import java.io.IOException;

public class Config {

	private static String imagePath;
	private static String driverPath;
	private static String tempFilePath;
	
	private static String email;
	private static String password;
	
	public Config() {
		setEmail("qowlgh18@gmail.com");
		setPassword("Bae@ji06ho20");
		
		try {
			setImagePath(new File("images/no_result.png").getCanonicalPath());
			setDriverPath(new File("webDriver/chromedriver.exe").getCanonicalPath());
			setTempFilePath(new File("tmp").getCanonicalPath());
			
			System.out.println(getDriverPath());
			System.out.println(getTempFilePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public static String getImagePath() {
		return imagePath;
	}

	public static void setImagePath(String imagePath) {
		Config.imagePath = imagePath;
	}

	public static String getDriverPath() {
		return driverPath;
	}

	public static void setDriverPath(String driverPath) {
		Config.driverPath = driverPath;
	}

	public static String getEmail() {
		return email;
	}

	public static void setEmail(String email) {
		Config.email = email;
	}

	public static String getPassword() {
		return password;
	}

	public static void setPassword(String password) {
		Config.password = password;
	}

	public static String getTempFilePath() {
		return tempFilePath;
	}

	public static void setTempFilePath(String tempFilePath) {
		Config.tempFilePath = tempFilePath;
	}
}
