package SeleniumTestingPackage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.parser.JSONParser;

/* 
 updater: Done
 locker: Not Done
 Bots: Not Done
 display: Done
 */

public class FirstClass {
	private static List<String> currentTasks;

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
//	     WebDriver driver = new FirefoxDriver();
//	     driver.navigate().to("http://www.google.com/");  
//	     driver.quit();
		new Updater();
		SmartBot bot = new SmartBot(7325412);
		bot.smartBotThread.start();
//		while (!currentTasks.isEmpty()) {
//			List<String> successfulTasks = new ArrayList<>();
//			List<Thread> usedThreads = new ArrayList<>();
//			for (String task : currentTasks) {
//				Thread th = bot.stringToThread(task);
//				if (th != null) {
//					th.start();
//					successfulTasks.add(task);
//					usedThreads.add(th);
//				}
//			}
//			
//			for (Thread thread : bot.botThreads) {
//				boolean found = false;
//				for (Thread threaded : usedThreads) {
//					if (threaded.toString() == thread.toString()) {
//						found = true;
//					}
//				}
//				if (!found) {
//					try {
//						thread.wait();
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
	}

	public static class Updater {
		public Updater() {
			createUpdaterThread();
		}

		private static void createUpdaterThread() {
			Thread checkThread = new Thread(() -> {
				while (true) {
					try {
						updateData();
						Thread.sleep(25000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
			checkThread.start();
		}

		private static void updateData() {
			try {
				String url = "http://randomurl.pythonanywhere.com/managementData";
				String pw = "⠀⠀⠀⠀⠀";
				HttpRequest request = HttpRequest.newBuilder().uri(new URI(url))
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString("{\"pw\": \"" + pw + "\"}")).build();
				HttpClient client = HttpClient.newHttpClient();
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				if (response.statusCode() == 200) {
					currentTasks = extractStrings(new JSONParser().parse(response.body()).toString());
					System.out.println(currentTasks);
				} else {
					System.out.println("Request failed with status code: " + response.statusCode());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public static List<String> extractStrings(String jsonString) {
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
			return strings;
		}

	}
}
