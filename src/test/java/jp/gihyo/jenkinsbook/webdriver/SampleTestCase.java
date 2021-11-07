package jp.gihyo.jenkinsbook.webdriver;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.URL;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.devicefarm.*;
import software.amazon.awssdk.services.devicefarm.model.*;

import jp.gihyo.jenkinsbook.page.ResultPage;
import jp.gihyo.jenkinsbook.page.TopPage;

public class SampleTestCase {

	private static Properties prop = new Properties();
	private static WebDriver driver;
	

	@BeforeClass
	public static void setUpClass() throws IOException {
		prop.load(new FileInputStream("target/test-classes/selenium.properties"));
		String myProjectARN = "arn:aws:devicefarm:us-west-2:421850136157:project:d835a0a3-de5c-42d4-9fb5-2ee9442f3f37";
		DeviceFarmClient client  = DeviceFarmClient.builder().region(Region.US_WEST_2).build();
		CreateTestGridUrlRequest request = CreateTestGridUrlRequest.builder()
		.expiresInSeconds(300)
		.projectArn(myProjectARN)
		.build();
		CreateTestGridUrlResponse response = client.createTestGridUrl(request);
		DesiredCapabilities cap = DesiredCapabilities.edge();
		cap.setCapability("ms:edgeChromium", "true");
		URL testGridUrl = new URL(response.url());
		// You can now pass this URL into RemoteWebDriver.
		driver = new RemoteWebDriver(testGridUrl, cap);

	}

	@AfterClass
	public static void tearDownClass() throws IOException {
		driver.quit();
	}

	@Test
	public void testNormal01() {
		driver.get(prop.getProperty("baseUrl") + "/sampleproject");

		TopPage topPage = new TopPage(driver);
		assertEquals("名字", topPage.getLastNameLabel());
		assertEquals("名前", topPage.getFirstNameLabel());

		assertTrue(topPage.hasLastNameInput());
		assertTrue(topPage.hasFirstNameInput());
		assertTrue(topPage.hasSubmit());
	}

	@Test
	public void testNormal02() {
		driver.get(prop.getProperty("baseUrl") + "/sampleproject");

		TopPage topPage = new TopPage(driver);
		topPage.setLastName("Hoge");
		topPage.setFirstName("Foo");
		topPage.submit();

		String greeting = "";
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		if (hour < 12) {
			greeting = "Good morning";
		} else {
			greeting = "Good afternoon";
        }
		ResultPage resultPage = new ResultPage(driver);
		assertEquals(greeting + ", Hoge Foo!!", resultPage.getText());
	}

	@Test
	public void testError01() {
		driver.get(prop.getProperty("baseUrl") + "/sampleproject");

		TopPage page = new TopPage(driver);
		page.setFirstName("Hoge");
		page.submit();

		ResultPage resultPage = new ResultPage(driver);
		assertEquals("エラー", resultPage.getText());
	}

	@Test
	public void testError02() {
		driver.get(prop.getProperty("baseUrl") + "/sampleproject");

		TopPage page = new TopPage(driver);
		page.setLastName("Hoge");
		page.submit();

		ResultPage resultPage = new ResultPage(driver);
		assertEquals("エラー", resultPage.getText());
	}
}
