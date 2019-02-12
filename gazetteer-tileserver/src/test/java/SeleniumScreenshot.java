import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SeleniumScreenshot {
        public static void main(String[] args) throws IOException, InterruptedException {
            WebDriver driver = new ChromeDriver();
            driver.manage().window().setSize(new Dimension(1600, 1200));
            driver.get("http://localhost:8081/");
            Thread.sleep(1000);
            File source_file = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            Files.copy(source_file.toPath(), Paths.get("screenshots").resolve(String.format("%s.png", System.currentTimeMillis())));
            driver.quit();
        }
}
