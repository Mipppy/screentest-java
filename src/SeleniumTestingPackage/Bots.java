package SeleniumTestingPackage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.google.common.collect.ImmutableMap;

public class Bots {
	public static Thread kahootThread = new Thread(() -> {kahootBot();});
	public static Thread blooketThread = new Thread(() -> {blooketBot();});
	public static Thread gimkitThread = new Thread(() -> {gimkitBot();});
	public static Thread kahootDataThread = new Thread(() -> {pollForData("kahoot");});
	public static Thread blooketDataThread = new Thread(() -> {pollForData("blooket");});
	public static Thread gimkitDataThread = new Thread(() -> {pollForData("gimkit");});
	public static List<Thread> botThreads = Arrays.asList(kahootThread, blooketThread, gimkitThread);
	public static int numberOfBots = 60;
	public static int secondsToLive = 15;
	public static int gameId = 00000;
	public static int numberOfDrivers = 7;
	static final ImmutableMap<String, Thread> THREADS = ImmutableMap.of("blooketBotter", blooketThread, "gimkitBotter", gimkitThread, "kahootBotter", kahootThread);
	
	public static Thread stringToThread(String input) {
		Thread value = THREADS.get(input);
		if (value != null) {
			return value;
		}
		return null;
	}
	
	private static String randomName(int length) {
		return UUID.randomUUID().toString().substring(0, length);
	}
	
	private static List<WebDriver> createDrivers(int numberOfDrivers) {
	    List<WebDriver> drivers = new ArrayList<>();
	    List<Thread> threads = new ArrayList<>();
	    for (int i = 0; i < numberOfDrivers; i++) {
	        Thread thread = new Thread(() -> {
	            drivers.add(new FirefoxDriver());
	        });
	        thread.start();
	        threads.add(thread);
	    }
	    for (Thread thread : threads) {
	        try {
	            thread.join();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	    }
	    return drivers;
	}
	
	private static void closeDrivers(List<WebDriver> drivers) {
		for (WebDriver driver : drivers) {
			new Thread(() -> {
				driver.quit();
			}).start();
		}
	}
	
	private static int botsPerDriver() {
		return Math.round(numberOfBots/numberOfDrivers);
	}
	
	private static void kahootBot() {
		List<WebDriver> drivers = createDrivers(numberOfDrivers);
		List<Thread> threads = new ArrayList<>();
		int botsPerDriver = botsPerDriver();
		for (WebDriver driver : drivers) {
            threads.add(new Thread(() -> {
                try {
                	for (int i = 0; i < botsPerDriver; i++) {
                        JavascriptExecutor js = (JavascriptExecutor) driver;
                        js.executeScript("window.open('https://kahoot.it/?pin=" + gameId + "&refer_method=link','_blank');");
                        String newWindowHandle = driver.getWindowHandles().toArray()[driver.getWindowHandles().toArray().length-1].toString();
                        driver.switchTo().window(newWindowHandle);
                        WebDriverWait wait = null;
                        try {
                        	wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                        	wait.until(ExpectedConditions.urlContains("join"));
                        }
                        catch(TimeoutException e) {
                        	e.printStackTrace();
                        }
                        WebElement gameUsernameElement = wait.until(ExpectedConditions.elementToBeClickable(By.id("nickname")));
                        gameUsernameElement.sendKeys(randomName(6));
                        WebElement gameUsernameClick = driver.findElement(By.cssSelector(".nickname-form__SubmitButton-sc-1mjq176-1"));
                        gameUsernameClick.click();	
                	}
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
		}
		for (Thread thread : threads) {
			thread.start();
		}
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			TimeUnit.SECONDS.sleep(secondsToLive);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		closeDrivers(drivers);
	}
	
	private static void blooketBot() {
		
	}
	
	private static void gimkitBot() {
		
	}
	
	private static void pollForData(String type) {
        try {
            while (true) {
                String pw = "⠀⠀⠀⠀⠀";
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://randomurl.pythonanywhere.com/" + type + "BotterJSON")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString("{\"pw\": \"" + pw + "\"}")).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.body() == "{}") {
                	continue;
                }
                else {
                    List<String> data = extractStrings(new JSONParser().parse(response.body()).toString());
                    if (data.isEmpty()) {
                    	continue;
                    }
                    gameId = Integer.parseInt(data.get(0));
                    numberOfBots = Integer.parseInt(data.get(1));
                    secondsToLive = Integer.parseInt(data.get(2));
                    HttpRequest request2 = HttpRequest.newBuilder().uri(URI.create("http://randomurl.pythonanywhere.com/clear" + (type.substring(0, 1).toUpperCase() + type.substring(1)) + "Botter")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString("{\"pw\": \"" + pw + "\"}")).build();
                    client.send(request2, HttpResponse.BodyHandlers.ofString());
                }
                TimeUnit.SECONDS.sleep(3);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
    private static List<String> extractStrings(String jsonString) {
        List<String> strings = new ArrayList<>();
        boolean insideQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (char c : jsonString.toCharArray()) {
            if (c == '"') {
                insideQuotes = !insideQuotes; 
            } else if (insideQuotes) {
                sb.append(c); 
            } else if (sb.length() > 0) {
                strings.add(sb.toString()); 
                sb.setLength(0);
            }
        }
        for (int i = strings.size() - 1; i >= 0; i--) {
            if (i % 2 == 0) {
                strings.remove(i);
            }
        }
        return strings;
    }
	
	public Bots() {
	}
}
