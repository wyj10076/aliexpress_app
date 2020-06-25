package com.app.api;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.app.aliexpress.MainApp;

public class Crawling {

	private static final String WEB_DRIVER_ID = "webdriver.chrome.driver";

	public static void getContents(String keyword) {
		String url = "https://ko.aliexpress.com/af/" + keyword
				+ ".html?trafficChannel=af&d=y&CatId=0&ltype=affiliate&SortType=total_tranpro_desc&groupsort=1&page=1";

		String driverPath = MainApp.class.getResource("/webDriver/chromedriver.exe").getPath().substring(1);

		System.setProperty(WEB_DRIVER_ID, driverPath);

		ChromeOptions options = new ChromeOptions();
		//options.addArguments("window-size=1920x1080");
		options.addArguments("--start-maximized");
		options.addArguments("headless");
		options.addArguments("--disable-gpu");
		//options.addArguments("--no-sandbox");

		WebDriver driver = new ChromeDriver(options);
		JavascriptExecutor js = (JavascriptExecutor) driver;
		
		driver.get(url);
		
		js.executeScript("window.scrollBy(0,1000)");

		try {
			WebDriverWait wait = new WebDriverWait(driver, 60);
			WebElement parent = wait
					.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.gallery-wrap.product-list")));

			// 7. 트윗 콘텐츠 조회
			List<WebElement> contents = parent.findElements(By.cssSelector("li.list-item"));
			System.out.println("조회된 콘텐츠 수 : " + contents.size());

			List<String> stringList = new ArrayList<>();
			
			if (contents.size() > 0) {
				// 8. 트윗 상세 내용 탐색
				for (int i = 0; i < 10; i++) {
					WebElement content = contents.get(i);
					
					try {
						String link = content.findElement(By.cssSelector("a.item-title")).getAttribute("href");
						String title = content.findElement(By.cssSelector("a.item-title")).getText();
						String img = content.findElement(By.cssSelector("img.item-img")).getAttribute("src");
						String price = content.findElement(By.cssSelector("span.price-current")).getText();
						String shipping = content.findElement(By.cssSelector("span.shipping-value")).getText();
						String rating = content.findElement(By.cssSelector("span.rating-value")).getText();
						String saleVolume = content.findElement(By.cssSelector("a.sale-value-link")).getText();
						String store = content.findElement(By.cssSelector("a.store-name")).getText();
						stringList.add(store);
					} catch (NoSuchElementException e) {
						// pass
					}
				}
			}
			for (String str : stringList) {
				System.out.println(str);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("목록을 찾을 수 없습니다.");
		} finally {
			// 9. HTML 저장.
			// saveHtml("twitter-selenium-loaded.html", driver.getPageSource() );
			System.out.println("종료");
			driver.quit();
		}
	}
}
