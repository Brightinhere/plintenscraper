package org.example;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;

public class InputBot {

    public static void main(String[] args) {
        final String baseUrl = "https://plintendiscount.nl/wp-login.php";
        final String username = Config.getUsername();
        final String password = Config.getPassword();
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Irfan Bilir\\IdeaProjects\\chromedriver-win64\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();

        try {
            login(baseUrl, username, password, driver);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String csvFile = "plinten.csv";
        String line;
        String delimiter = ";";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String headerLine = br.readLine();
            String[] headers = headerLine.split(delimiter);

            while ((line = br.readLine()) != null) {
                String[] values = line.split(delimiter);

                Product product = new Product(values);

                addNewProduct(driver, product);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        driver.quit();
    }

    public static void addNewProduct(WebDriver driver, Product product) throws IOException {
        driver.get("https://plintendiscount.nl/wp-admin/post-new.php?post_type=product");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        driver.findElement(By.id("title")).sendKeys(product.getTitle());

        WebElement iframe = driver.findElement(By.id("content_ifr"));
        driver.switchTo().frame(iframe);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#tinymce > p")));
        driver.findElement(By.cssSelector("#tinymce > p")).sendKeys(product.getDescription());
        driver.switchTo().defaultContent();

        driver.findElement(By.id("_regular_price")).sendKeys(product.getPricePerMeter());
        driver.findElement(By.id("_sku")).sendKeys(product.getCode());
        driver.findElement(By.id("_height")).sendKeys(product.getHeight());
        driver.findElement(By.id("_width")).sendKeys(product.getWidth());
        driver.findElement(By.id("_length")).sendKeys(product.getLength());

        // Properties tab
        driver.findElement(By.cssSelector("#woocommerce-product-data > div.inside > div > ul > li.attribute_options.attribute_tab.active > a")).click();
        driver.findElement(By.cssSelector("#product_attributes > div.product_attributes.wc-metaboxes.ui-sortable > div > div > table > tbody > tr:nth-child(1) > td.attribute_name > input.attribute_name")).sendKeys("Materiaal");
        driver.findElement(By.cssSelector("#product_attributes > div.product_attributes.wc-metaboxes.ui-sortable > div > div > table > tbody > tr:nth-child(1) > td:nth-child(2) > textarea")).sendKeys(product.getMaterial());

        try {
            Thread.sleep(100000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void login(String baseUrl, String username, String password, WebDriver driver) throws IOException {
        driver.get(baseUrl);
        driver.findElement(By.id("user_login")).sendKeys(username);
        driver.findElement(By.id("user_pass")).sendKeys(password);

        int answer = calculateCaptcha(driver);

        driver.findElement(By.id("jetpack_protect_answer")).sendKeys(String.valueOf(answer));

        driver.findElement(By.id("wp-submit")).click();
        driver.manage().window().maximize();
    }

    public static int calculateCaptcha(WebDriver driver) {
        WebElement label = driver.findElement(By.cssSelector("label[for='jetpack_protect_answer']"));

        String labelText = label.getText();

        String[] parts = labelText.split("\\s+");
        int operand1 = Integer.parseInt(parts[0]);
        int operand2 = Integer.parseInt(parts[2]);
        String operator = parts[1];

        int answer = 0;
        if (operator.equals("+")) {
            answer = operand1 + operand2;
        }
        return answer;
    }
}
