package com.account;
import static org.testng.Assert.assertEquals;
import java.util.concurrent.TimeUnit;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.path.json.JsonPath;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
public class VerifySalesforceAccount {
	

	
		String accessToken = " ";
		String id = " ";
		String firstName = "Mike";
		String lastName = "Ross";
		String Email = "test@email.in";
		String Phone = "1234567890";

		@Test(priority = 0)
		void getAccessToken_POST() {

			RestAssured.baseURI = "https://login.salesforce.com";
			RequestSpecification request = RestAssured.given();
			// Building the PostRequest-->
			Response response = request.queryParam("client_id", ClientCredentials.client_id)
					.queryParam("client_secret", ClientCredentials.client_secret)
					.queryParam("username", ClientCredentials.username).queryParam("password", ClientCredentials.password)
					.queryParam("grant_type", ClientCredentials.grant_type).post("/services/oauth2/token");
			// Verifying the http statusCode
			int statusCode = response.getStatusCode();
			assertEquals(statusCode, 200);
			// Extracting the value from Response
			JsonPath jsonPath = new JsonPath(response.asString());
			accessToken = jsonPath.get("access_token").toString();
			//System.out.println(accessToken);
		}

		@Test(priority = 1)
		void createAccount_POST() {
			//System.out.println("accessToken-->" + accessToken);
			RestAssured.baseURI = "https://empathetic-shark-a7palw-dev-ed.my.salesforce.com";
			RequestSpecification request = RestAssured.given();
			// Building the PostRequest-->
			request.header("Authorization", "Bearer " + accessToken).header("Sforce-Auto-Assign", true)
					.header("Content-Type", ClientCredentials.Content_type);

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("FirstName", firstName);
			jsonObject.put("Email", Email);
			jsonObject.put("Phone", Phone);
			jsonObject.put("LastName", lastName);
			jsonObject.put("Role__c", "CEO");
			request.body(jsonObject.toJSONString());

			Response response = request.post("/services/data/v48.0/sobjects/Contact");
			// Verifying the http statusCode
			int statusCode = response.getStatusCode();
			assertEquals(statusCode, 201);
			// Extracting the value from Response
			JsonPath jsonPath = new JsonPath(response.asString());
			id = jsonPath.get("id").toString();
			//System.out.println(id);
		}

		@Test(priority = 2)
		void loginToSalesforceAccount() {

			String userName = firstName + " " + lastName;
			String baseURI = "https://login.salesforce.com";
			String userURI = "https://empathetic-shark-a7palw-dev-ed.lightning.force.com/";
			//Put the driver at drivers/chromedriver_win32/chromedriver.exe 
			System.setProperty("webdriver.chrome.driver",
					"/drivers/chromedriver_win32/chromedriver.exe");

			// Instantiate a ChromeDriver class.
			WebDriver driver = new ChromeDriver();
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			driver.get(baseURI);
			driver.findElement(By.xpath("//label[text()='Username']//following::input[@name='username']"))
					.sendKeys("testforibusiness@ibusiness.in");
			driver.findElement(By.xpath("//label[text()='Password']//following::input[@name='pw']")).sendKeys("Test@123");
			driver.findElement(By.xpath("//input[@name='Login']")).click();

			boolean elementDisplayed = driver.findElement(By.xpath("//h1//span[text()='Home']")).isDisplayed();
			driver.navigate().to(userURI + id);
			String userFullName = driver.findElement(By.xpath("//span[text()='" + userName + "']")).getText();
			//System.out.println(userFullName);
			assertEquals(userFullName, userName);
			String userEmail = driver.findElement(By.xpath("//a[text()='" + Email + "']")).getText();
			//System.out.println(userEmail);
			assertEquals(userEmail, Email);
			driver.close();

		}

	}


