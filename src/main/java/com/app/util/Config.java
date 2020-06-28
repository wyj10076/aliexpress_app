package com.app.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Properties;

public class Config {

	// PATH
	public static String NO_RESULT_IMAGE_PATH;
	public static String WEB_DRIVER_PATH;
	public static String PREVIEW_DIR_PATH;

	// ALI EXPRESS
	public static String ALI_URL;
	public static String ALI_APP_KEY;
	public static String ALI_SECRET; 
	
	// EMAIL
	public static String PLATFORM;
	public static String EMAIL;
	public static String PASSWORD;
	
	// count per keyword
	public static int ITEM_COUNT;
	
	// concurrent crawling count
	public static int WEB_DRIVER_COUNT;
	
	public static void loadConfig() throws Exception {
		FileReader config = new FileReader("config");
		Properties prop = new Properties();
		prop.load(config);
		
		NO_RESULT_IMAGE_PATH = prop.getProperty("NO_RESULT_IMAGE_PATH");
		WEB_DRIVER_PATH = prop.getProperty("WEB_DRIVER_PATH");
		PREVIEW_DIR_PATH  = prop.getProperty("PREVIEW_DIR_PATH ");
		
		ALI_URL = prop.getProperty("ALI_URL");
		ALI_APP_KEY = prop.getProperty("ALI_APP_KEY");
		ALI_SECRET = prop.getProperty("ALI_SECRET");
		
		PLATFORM = prop.getProperty("PLATFORM");
		EMAIL = prop.getProperty("EMAIL");
		PASSWORD = prop.getProperty("PASSWORD");
		
		ITEM_COUNT = Integer.parseInt(prop.getProperty("ITEM_COUNT"));
		WEB_DRIVER_COUNT = Integer.parseInt(prop.getProperty("WEB_DRIVER_COUNT"));
	}
	
	public static void createConfigFile() throws Exception {
		String config ="# aliexpress.exe 파일과 같은 폴더에 위치\n"
					+ "\n"
					+ "#------- PATH(상대 경로 - 현재 폴더 기준)\n"
					+ "# webDriver 경로(크롬만 지원)\n"
					+ "WEB_DRIVER_PATH = webDriver/chromedriver.exe\n"
					+ "\n"
					+ "# 검색 결과 없을 시에 보낼 이이미지 경로\n"
					+ "NO_RESULT_IMAGE_PATH = images/no_result.png\n"
					+ "# 미리보기 임시 파일 폴더\n"
					+ "PREVIEW_DIR_PATH  = preview\n"
					+ "#------------------------------------------------------\n"
					+ "\n"
					+ "#------- Ali Express\n"
					+ "ALI_URL = http://api.taobao.com/router/rest\n"
					+ "ALI_APP_KEY = app_key\n"
					+ "ALI_SECRET = secret_key\n"
					+ "#------------------------------------------------------\n"
					+ "\n"
					+ "#------- Email\n"
					+ "# PLATFORM = [naver OR gmail]\n"
					+ "PLATFORM = gmail\n"
					+ "EMAIL = email@gmail.com\n"
					+ "PASSWORD = password\n"
					+ "#------------------------------------------------------\n"
					+ "\n"
					+ "# 키워드 당 아이템 개수\n"
					+ "ITEM_COUNT = 10\n"
					+ "\n"
					+ "# 동시 가동 WebDriver 개수(크롤링)\n"
					+ "WEB_DRIVER_COUNT = 2\n";
		
		File file = new File("config");
		
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(config.getBytes());
		fos.close();
	}
}
