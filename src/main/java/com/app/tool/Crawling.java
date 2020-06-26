package com.app.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.app.aliexpress.MainApp;

public class Crawling {

	private WebDriver driver;
	private JavascriptExecutor js;

	public Crawling() {
		String driverPath = MainApp.class.getResource("/webDriver/chromedriver.exe").getPath().substring(1);

		System.setProperty("webdriver.chrome.driver", driverPath);

		ChromeOptions options = new ChromeOptions();

		options.addArguments("--disable-extensions");
		options.addArguments("window-size=1920x1080");
		// options.addArguments("start-maximized");
		options.addArguments("headless");
		options.addArguments("disable-infobars");
		options.addArguments("disable-gpu");
		// options.addArguments("--no-sandbox");

		this.driver = new ChromeDriver(options);
		this.js = (JavascriptExecutor) driver;

		this.driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

		beforeCrawling();
	}

	public void beforeCrawling() {
		String url = "https://ko.aliexpress.com/?lan=ko";

		driver.get(url);

		WebElement openMenu = driver.findElement(By.cssSelector("a.switcher-info.notranslate"));
		openMenu.sendKeys(Keys.ENTER);
		WebElement currencyList = driver.findElement(By.cssSelector("div.switcher-currency-c > span > a"));
		currencyList.sendKeys(Keys.ENTER);
		WebElement currency = driver.findElement(By.xpath("//*[@id='nav-global']/div[3]/div/div/div/div[3]/div/ul/li[67]/a"));
		js.executeScript("arguments[0].click();", currency);
		WebElement saveButton = driver.findElement(By.cssSelector("div.switcher-btn.item.util-clearfix > button"));
		js.executeScript("arguments[0].click();", saveButton);
		//saveButton.sendKeys(Keys.ENTER);
	}

	public List<Properties> getContents(String keyword) {

		WebElement searchKeyword = driver.findElement(By.cssSelector("#form-searchbar #search-key"));
		searchKeyword.clear();
		searchKeyword.sendKeys(keyword);
		WebElement searchButton = driver.findElement(By.cssSelector("#form-searchbar .search-button"));
		searchButton.submit();

		WebDriverWait wait = new WebDriverWait(driver, 30);
		List<WebElement> contents = wait
				.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("li.list-item")));

		if (contents.size() > 0) {

			List<Properties> props = new ArrayList<Properties>();

			for (WebElement content : contents) {
				try {
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
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return props;
		}

		// 9. HTML 저장.
		// saveHtml("twitter-selenium-loaded.html", driver.getPageSource() );

		return null;

	}
	
	public void quitBrowser() {
		driver.quit();
	}
}
