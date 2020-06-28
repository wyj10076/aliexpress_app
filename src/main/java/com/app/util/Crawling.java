package com.app.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Crawling {

	private Integer index;
	private WebDriver driver;
	private JavascriptExecutor js;
	private WebDriverWait wait;
	private int retryCount;

	public Crawling(Integer index) {
		this.index = index;

		System.setProperty("webdriver.chrome.driver", Config.getDriverPath());

		ChromeOptions options = new ChromeOptions();

		options.addArguments("--disable-extensions");
		options.addArguments("window-size=1920x1080");
		options.addArguments("headless");
		options.addArguments("disable-infobars");
		options.addArguments("disable-gpu");

		this.driver = new ChromeDriver(options);
		this.js = (JavascriptExecutor) driver;

		this.wait = new WebDriverWait(driver, 10);
		beforeCrawling();
	}

	public void beforeCrawling() {
		try {
			String url = "https://ko.aliexpress.com/?lan=ko";
	
			driver.get(url);
			
			WebElement openMenu = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.switcher-info.notranslate")));
			js.executeScript("arguments[0].click();", openMenu);
			WebElement currencyList = driver.findElement(By.cssSelector("div.switcher-currency-c > span > a"));
			js.executeScript("arguments[0].click();", currencyList);
			WebElement currency = driver
					.findElement(By.xpath("//*[@id='nav-global']/div[3]/div/div/div/div[3]/div/ul/li[67]/a"));
			js.executeScript("arguments[0].click();", currency);
			WebElement saveButton = driver.findElement(By.cssSelector("div.switcher-btn.item.util-clearfix > button"));
			js.executeScript("arguments[0].click();", saveButton);
			
		} catch (Exception e) {
			beforeCrawling();
		}
	}

	public List<Properties> getContents(String keyword) {
		
		List<Properties> props = new ArrayList<Properties>();
		
		try {
			
			WebElement searchKeyword = wait
					.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#form-searchbar #search-key")));
			searchKeyword.clear();
			searchKeyword.sendKeys(keyword);
			searchKeyword.sendKeys(Keys.ENTER);

			List<WebElement> contents;
			
			int scrollY = 1000;
			int size = 0;
			
			// 필요한 content 개수
			final int CONTENT_COUNT = 20;
			do {
				contents = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("li.list-item")));
				
				if (contents.size() > 0) {
					
					size = contents.size();
					if (size < CONTENT_COUNT) {
						js.executeScript("window.scrollTo(0, " + scrollY + ");");
						scrollY += 1000;
						
					} else {
						for (int i = 0; i < CONTENT_COUNT; i++) {
							WebElement content = contents.get(i);
							
							Properties prop = new Properties();

							WebElement title = content.findElement(By.cssSelector("a.item-title"));
							WebElement image = content.findElement(By.cssSelector("img.item-img"));
							WebElement price = content.findElement(By.cssSelector("span.price-current"));
							WebElement shipping = content.findElement(By.cssSelector("span.shipping-value"));
							List<WebElement> rating = content.findElements(By.cssSelector("span.rating-value"));
							List<WebElement> saleVolume = content.findElements(By.cssSelector("a.sale-value-link"));
							WebElement store = content.findElement(By.cssSelector("a.store-name"));
							prop.put("link", title.getAttribute("href"));
							prop.put("title", title.getAttribute("innerHTML"));
							prop.put("image", image.getAttribute("src"));
							prop.put("price", price.getAttribute("innerHTML"));
							prop.put("shipping", shipping.getAttribute("innerHTML"));

							if (rating.size() > 0) {
								prop.put("rating", rating.get(0).getAttribute("innerHTML"));
							} else {
								prop.put("rating", "0");
							}

							if (saleVolume.size() > 0) {
								prop.put("saleVolume", saleVolume.get(0).getAttribute("innerHTML"));
							} else {
								prop.put("saleVolume", "0 판매");
							}

							prop.put("store", store.getAttribute("innerHTML"));
							props.add(prop);
						}
					}
				}
				
			} while (size < CONTENT_COUNT);
				
		} catch (Exception e) {
			
			if (retryCount < 3) {
				retryCount++;
				props = getContents(keyword);
				
			} else {
				retryCount = 0;
			}
		}
		
		return props;

	}
	
	// getter, setter
	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

}
