import io.gazetteer.tileserver.TileServer;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class SeleniumScreenshot {
        public static void main(String[] args) throws IOException {
            TileServer.main(new String[] {"config/config.yaml"});
            System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
            WebDriver driver = new ChromeDriver();
            driver.manage().window().maximize();
            driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
            driver.get("http://localhost:8081/");
            File source_file = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            Files.copy(source_file.toPath(), Paths.get(String.format("%s.png", System.currentTimeMillis())));
            driver.quit();
        }
}
