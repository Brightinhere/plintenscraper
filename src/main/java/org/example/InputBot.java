package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class InputBot {

    public static void main(String[] args) {
        final String baseUrl = "https://plintendiscount.nl/wp-login.php";
        final String username = Config.getUsername();
        final String password = Config.getPassword();
        System.setProperty("webdriver.chrome.driver", "../chromedriver-win64/chromedriver.exe");
        WebDriver driver = new ChromeDriver();

        try {
            login(baseUrl, username, password, driver);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String csvFile = "plinten.csv";
        String line;
        String delimiter = ";";
        int counter = 1;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String headerLine = br.readLine();
            String[] headers = headerLine.split(delimiter);
            while ((line = br.readLine()) != null) {
                counter++;
//                if (counter <= 65) {
//                    continue;
//                }
                String[] values = line.split(delimiter);

                Product product = new Product(values);
//                addNewProduct(driver, product);
                try {
                    System.out.println("Starting to add options for product: " + product.getCode() + " (" + counter + ")");
                    addOptionsForProduct(product, driver);
                    System.out.println("Added options for product: " + product.getCode());
                } catch (Exception e) {
                    System.out.println("Failed to add options for product: " + product.getCode());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void addOptionsForProduct(Product product, WebDriver driver) {
        driver.get("https://plintendiscount.nl/wp-admin/post-new.php?post_type=wpc_product_option");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#title")));
        driver.findElement(By.cssSelector("#title")).sendKeys(product.getName() + " - " + product.getCode());

        Select tagDropdown = new Select(driver.findElement(By.cssSelector("#wpcpo_configuration > div.inside > table > tbody > tr > td > select")));
        tagDropdown.selectByValue("product_tag");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#wpcpo_configuration > div.inside > table > tbody > tr > td > div > span > span.selection > span > ul > li > input")));
        scrollToElementAndSendKeys(driver, By.cssSelector("#wpcpo_configuration > div.inside > table > tbody > tr > td > div > span > span.selection > span > ul > li > input"), product.getCode());
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("li.select2-results__option.select2-results__option--highlighted")));
        scrollToElementAndClick(driver, By.cssSelector("li.select2-results__option.select2-results__option--highlighted"));

        Select dropdown = new Select(driver.findElement(By.cssSelector("#wpcpo-item-type")));
        dropdown.selectByValue("select");
        driver.findElement(By.cssSelector("#wpcpo_fields > div.inside > div > div.wpcpo-items-new > input")).click();

        if (product.getSizes().size() > 0) {
            addSizeOptions(product, driver, wait);
        }

        if (product.getLengths().size() > 0) {
            if (product.getSizes().size() == 0) {
                addLengthsOptionsOnly(product, driver, wait);
            } else {
                addLengthsOptions(product, driver, wait);
            }
        }

        if (product.getFinishes().size() > 0) {
            addFinishesOptions(product, driver, wait);
        }

        if (product.getSizes().size() > 0 || product.getLengths().size() > 0 || product.getFinishes().size() > 0) {
            scrollToElementAndClick(driver, By.cssSelector("#publish"));
            System.out.println("Saving option for: " + product.getCode());
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#publish")));
        }
    }

    public static void addSizeOptions(Product product, WebDriver driver, WebDriverWait wait) {
        int counter = 1;
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#wpcpo_fields > div.inside > div > div.wpcpo-items.ui-sortable > div > div.wpcpo-item-header > span.wpcpo-item-label")));
        scrollToElementAndClick(driver, By.cssSelector("#wpcpo_fields > div.inside > div > div.wpcpo-items.ui-sortable > div > div.wpcpo-item-header > span.wpcpo-item-label"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(2) > label > input")));
        scrollToElementAndSendKeys(driver, By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(2) > label > input"), "Afmeting");

        checkBoxes(driver, "[id^='tab-wpcpo-'][id$='-general']");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(4) > label > textarea")));
        scrollToElementAndSendKeys(driver, By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(4) > label > textarea"), "Dikte x hoogte in millimeters.");

        // For each finish, add a new option
        for (String size : product.getSizes()) {
            if (counter == 1) {
                scrollToElementAndClick(driver, By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(6) > div > div.inner-footer > button"));
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(6) > div > div.inner-content.ui-sortable > div > input.option-name")));
                driver.findElement(By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(6) > div > div.inner-content.ui-sortable > div > input.option-name")).sendKeys(size);
                driver.findElement(By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(6) > div > div.inner-content.ui-sortable > div > input.option-value.wpcpo-input-not-empty")).sendKeys(size);
            } else {
                scrollToElementAndClick(driver, By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(6) > div > div.inner-footer > button"));
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(6) > div > div.inner-content.ui-sortable > div:nth-child(" + counter + ") > input.option-name")));
                scrollToElementAndSendKeys(driver, By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(6) > div > div.inner-content.ui-sortable > div:nth-child(" + counter + ") > input.option-name"), size);
                scrollToElementAndSendKeys(driver, By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(6) > div > div.inner-content.ui-sortable > div:nth-child(" + counter + ") > input.option-value.wpcpo-input-not-empty"), size);
            }
            counter++;
        }
    }

    public static void addLengthsOptions(Product product, WebDriver driver, WebDriverWait wait) {
        scrollToElementAndClick(driver, By.cssSelector("#wpcpo_fields > div.inside > div > div.wpcpo-items-new > input"));
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#wpcpo_fields > div.inside > div > div.wpcpo-items.ui-sortable > div:nth-child(2) > div.wpcpo-item-header > span.wpcpo-item-label")));

        String uniquePhrase = getPhrase(driver, 1).replace("][desc]", "");
        String baseSelector = "#tab-wpcpo-" + uniquePhrase + "-general";

        scrollToElementAndClick(driver, By.cssSelector("#wpcpo_fields > div.inside > div > div.wpcpo-items.ui-sortable > div:nth-child(2) > div.wpcpo-item-header > span.wpcpo-item-label"));
        scrollToElementAndSendKeys(driver, By.cssSelector(baseSelector + " > div:nth-child(2) > label > input"), "Lengte");
        checkBoxes(driver, baseSelector);

        int counter = 1;
        for (String length : product.getLengths()) {
            if (counter == 1) {
                scrollToElementAndClick(driver, By.cssSelector(baseSelector + " > div:nth-child(6) > div > div.inner-footer > button"));
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(baseSelector + " > div:nth-child(6) > div > div.inner-content.ui-sortable > div > input.option-name")));
                driver.findElement(By.cssSelector(baseSelector + " > div:nth-child(6) > div > div.inner-content.ui-sortable > div > input.option-name")).sendKeys(length);
                driver.findElement(By.cssSelector(baseSelector + " > div:nth-child(6) > div > div.inner-content.ui-sortable > div > input.option-value.wpcpo-input-not-empty")).sendKeys(length);
            } else {
                scrollToElementAndClick(driver, By.cssSelector(baseSelector + " > div:nth-child(6) > div > div.inner-footer > button"));
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(baseSelector + " > div:nth-child(6) > div > div.inner-content.ui-sortable > div:nth-child(" + counter + ") > input.option-name")));
                scrollToElementAndSendKeys(driver, By.cssSelector(baseSelector + " > div:nth-child(6) > div > div.inner-content.ui-sortable > div:nth-child(" + counter + ") > input.option-name"), length);
                scrollToElementAndSendKeys(driver, By.cssSelector(baseSelector + " > div:nth-child(6) > div > div.inner-content.ui-sortable > div:nth-child(" + counter + ") > input.option-value.wpcpo-input-not-empty"), length);
            }
            counter++;
        }
    }

    public static void addLengthsOptionsOnly(Product product, WebDriver driver, WebDriverWait wait) {
        int counter = 1;
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#wpcpo_fields > div.inside > div > div.wpcpo-items.ui-sortable > div > div.wpcpo-item-header > span.wpcpo-item-label")));
        scrollToElementAndClick(driver, By.cssSelector("#wpcpo_fields > div.inside > div > div.wpcpo-items.ui-sortable > div > div.wpcpo-item-header > span.wpcpo-item-label"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(2) > label > input")));
        scrollToElementAndSendKeys(driver, By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(2) > label > input"), "Lengte");

        checkBoxes(driver, "[id^='tab-wpcpo-'][id$='-general']");

        // For each finish, add a new option
        for (String length : product.getLengths()) {
            if (counter == 1) {
                scrollToElementAndClick(driver, By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(6) > div > div.inner-footer > button"));
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(6) > div > div.inner-content.ui-sortable > div > input.option-name")));
                driver.findElement(By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(6) > div > div.inner-content.ui-sortable > div > input.option-name")).sendKeys(length);
                driver.findElement(By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(6) > div > div.inner-content.ui-sortable > div > input.option-value.wpcpo-input-not-empty")).sendKeys(length);
            } else {
                scrollToElementAndClick(driver, By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(6) > div > div.inner-footer > button"));
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(6) > div > div.inner-content.ui-sortable > div:nth-child(" + counter + ") > input.option-name")));
                scrollToElementAndSendKeys(driver, By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(6) > div > div.inner-content.ui-sortable > div:nth-child(" + counter + ") > input.option-name"), length);
                scrollToElementAndSendKeys(driver, By.cssSelector("[id^='tab-wpcpo-'][id$='-general'] > div:nth-child(6) > div > div.inner-content.ui-sortable > div:nth-child(" + counter + ") > input.option-value.wpcpo-input-not-empty"), length);
            }
            counter++;
        }
    }
    public static void addFinishesOptions(Product product, WebDriver driver, WebDriverWait wait) {
        scrollToElementAndClick(driver, By.cssSelector("#wpcpo_fields > div.inside > div > div.wpcpo-items-new > input"));
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#wpcpo_fields > div.inside > div > div.wpcpo-items.ui-sortable > div:nth-child(3) > div.wpcpo-item-header > span.wpcpo-item-label")));

        String uniquePhrase = getPhrase(driver, 2).replace("][desc]", "");
        String baseSelector = "#tab-wpcpo-" + uniquePhrase + "-general";

        scrollToElementAndClick(driver, By.cssSelector("#wpcpo_fields > div.inside > div > div.wpcpo-items.ui-sortable > div:nth-child(3) > div.wpcpo-item-header > span.wpcpo-item-label"));
        scrollToElementAndSendKeys(driver, By.cssSelector(baseSelector + " > div:nth-child(2) > label > input"), "Afwerking");
        checkBoxes(driver, baseSelector);

        int counter = 1;
        for (String finish : product.getFinishes()) {
            if (counter == 1) {
                scrollToElementAndClick(driver, By.cssSelector(baseSelector + " > div:nth-child(6) > div > div.inner-footer > button"));
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(baseSelector + " > div:nth-child(6) > div > div.inner-content.ui-sortable > div > input.option-name")));
                driver.findElement(By.cssSelector(baseSelector + " > div:nth-child(6) > div > div.inner-content.ui-sortable > div > input.option-name")).sendKeys(finish);
                driver.findElement(By.cssSelector(baseSelector + " > div:nth-child(6) > div > div.inner-content.ui-sortable > div > input.option-value.wpcpo-input-not-empty")).sendKeys(finish);
            } else {
                scrollToElementAndClick(driver, By.cssSelector(baseSelector + " > div:nth-child(6) > div > div.inner-footer > button"));
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(baseSelector + " > div:nth-child(6) > div > div.inner-content.ui-sortable > div:nth-child(" + counter + ") > input.option-name")));
                scrollToElementAndSendKeys(driver, By.cssSelector(baseSelector + " > div:nth-child(6) > div > div.inner-content.ui-sortable > div:nth-child(" + counter + ") > input.option-name"), finish);
                scrollToElementAndSendKeys(driver, By.cssSelector(baseSelector + " > div:nth-child(6) > div > div.inner-content.ui-sortable > div:nth-child(" + counter + ") > input.option-value.wpcpo-input-not-empty"), finish);
            }
            counter++;
        }
    }
    public static void checkBoxes(WebDriver driver, String selector) {
        WebElement requiredCheckbox = driver.findElement(By.cssSelector(selector + " > div:nth-child(5) > label > input[type=checkbox]"));
        if (!requiredCheckbox.isSelected()) {
            requiredCheckbox.click();
        }

        WebElement descriptionCheckbox = driver.findElement(By.cssSelector(selector + " > div:nth-child(4) > label > input[type=checkbox]"));
        if (!descriptionCheckbox.isSelected()) {
            descriptionCheckbox.click();
        }
    }

    public static void addNewProduct(WebDriver driver, Product product) throws IOException {
        driver.get("https://plintendiscount.nl/wp-admin/post-new.php?post_type=product");
        waitForClickable();

        assignBasicInfo(product, driver);
        assignCategories(product.getSubcategory(), product.getSubcategory2(), driver);
        addProperties(product, driver);
        addImages(product, driver);
        addTag(product, driver);
        saveProduct(driver);
    }

    private static void addTag(Product product, WebDriver driver) {
        scrollToElementAndSendKeys(driver, By.cssSelector("#new-tag-product_tag"), product.getCode());
        scrollToElementAndClick(driver, By.cssSelector("#product_tag > div > div.ajaxtag.hide-if-no-js > input.button.tagadd"));
        waitForClickable();
    }

    private static void saveProduct(WebDriver driver) {
        scrollToElementAndClick(driver, By.cssSelector("#publish"));
    }

    public static void addImages(Product product, WebDriver driver) {
        if (product.getImages().size() == 0) {
            return;
        }
        scrollToElementAndClick(driver, By.cssSelector("#woocommerce-product-images > div.inside > p > a"));
        waitForClickable();
        scrollToElementAndClick(driver, By.cssSelector(("#menu-item-upload")));

        // Wait for the popup to load
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#__wp-uploader-id-1")));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("document.querySelector(\"input[id^='html5_']\").style.display = 'block';");

        WebElement fileInput = driver.findElement(By.cssSelector("input[id^='html5_']"));

        // Determine the image with the largest size
        String largestSizeImagePath = null;
        long maxSize = 0;
        for (String imagePath : product.getImages()) {
            File file = new File(imagePath);
            if (file.length() > maxSize) {
                maxSize = file.length();
                largestSizeImagePath = imagePath;
            }
        }

        for (String imagePath : product.getImages()) {
            if (!imagePath.equals(largestSizeImagePath)) {
                fileInput.sendKeys(imagePath);
                waitForClickable();
            }
        }

        // Wait for the images to be uploaded
        String saveButtonSelector = "#__wp-uploader-id-0 > div.media-frame-toolbar > div > div.media-toolbar-primary.search-form > button";
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(saveButtonSelector)));
        driver.findElement(By.cssSelector(saveButtonSelector)).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#set-post-thumbnail")));
        addMainPicture(largestSizeImagePath, driver);
    }

    public static void addMainPicture(String imagePath, WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        scrollToElementAndClick(driver, By.cssSelector("#set-post-thumbnail"));
        List<WebElement> elements = driver.findElements(By.id("menu-item-upload"));
        elements.get(1).click();

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("document.querySelectorAll(\"input[id^='html5_']\")[1].style.display = 'block';");

        List<WebElement> fileInputs = driver.findElements(By.cssSelector("input[id^='html5_']"));
        if (fileInputs.size() > 1) {
            fileInputs.get(1).sendKeys(imagePath);
        } else {
            // Handle the scenario where there's only one or no matching element
            System.err.println("Expected more than one matching element, but found less.");
        }
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#__wp-uploader-id-3 > div.media-frame-toolbar > div > div.media-toolbar-primary.search-form > button")));
        driver.findElement(By.cssSelector("#__wp-uploader-id-3 > div.media-frame-toolbar > div > div.media-toolbar-primary.search-form > button")).click();
    }


    public static void assignCategories(String category, String subcategory, WebDriver driver) {
        WebElement categoryCheckbox = driver.findElement(By.xpath("//li[label[contains(text(), '" + category + "')]]/label/input"));
        if (!categoryCheckbox.isSelected()) {
            scrollToElementAndClick(driver, By.xpath("//li[label[contains(text(), '" + category + "')]]/label/input"));
        }

        WebElement subcategoryCheckbox = driver.findElement(By.xpath("//li[label[contains(text(), '" + subcategory + "')]]/label/input"));
        if (!subcategoryCheckbox.isSelected()) {
            subcategoryCheckbox.click();
            scrollToElementAndClick(driver, By.xpath("//li[label[contains(text(), '" + subcategory + "')]]/label/input"));
        }
    }

    public static void addProperties(Product product, WebDriver driver) {
        // CSS Selectors
        String propertiesTabSelector = "#woocommerce-product-data > div.inside > div > ul > li.attribute_options.attribute_tab > a";
        String materialNameSelector = "#product_attributes > div.product_attributes.wc-metaboxes.ui-sortable > div > div > table > tbody > tr:nth-child(1) > td.attribute_name > input.attribute_name";
        String materialValueSelector = "#product_attributes > div.product_attributes.wc-metaboxes.ui-sortable > div > div > table > tbody > tr:nth-child(1) > td:nth-child(2) > textarea";
        String heightNameSelector = "#product_attributes > div.product_attributes.wc-metaboxes.ui-sortable > div:nth-child(2) > div > table > tbody > tr:nth-child(1) > td.attribute_name > input.attribute_name";
        String heightValueSelector = "#product_attributes > div.product_attributes.wc-metaboxes.ui-sortable > div:nth-child(2) > div > table > tbody > tr:nth-child(1) > td:nth-child(2) > textarea";
        String widthNameSelector = "#product_attributes > div.product_attributes.wc-metaboxes.ui-sortable > div:nth-child(3) > div > table > tbody > tr:nth-child(1) > td.attribute_name > input.attribute_name";
        String widthValueSelector = "#product_attributes > div.product_attributes.wc-metaboxes.ui-sortable > div:nth-child(3) > div > table > tbody > tr:nth-child(1) > td:nth-child(2) > textarea";
        String lengthNameSelector = "#product_attributes > div.product_attributes.wc-metaboxes.ui-sortable > div:nth-child(4) > div > table > tbody > tr:nth-child(1) > td.attribute_name > input.attribute_name";
        String lengthValueSelector = "#product_attributes > div.product_attributes.wc-metaboxes.ui-sortable > div:nth-child(4) > div > table > tbody > tr:nth-child(1) > td:nth-child(2) > textarea";

        scrollToElementAndClick(driver, By.cssSelector(propertiesTabSelector));

        // Wait for the properties tab to load
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(materialNameSelector)));

        // Material
        scrollToElementAndSendKeys(driver, By.cssSelector(materialNameSelector), "Materiaal");
        scrollToElementAndSendKeys(driver, By.cssSelector(materialValueSelector), product.getMaterial());

//        clickNewPropertyButton(driver);

        // Height
//        scrollToElementAndSendKeys(driver, By.cssSelector(heightNameSelector), "Hoogte");
//        scrollToElementAndSendKeys(driver, By.cssSelector(heightValueSelector), product.getHeight());
//
//        clickNewPropertyButton(driver);
//
//        // Width
//        scrollToElementAndSendKeys(driver, By.cssSelector(widthNameSelector), "Breedte");
//        scrollToElementAndSendKeys(driver, By.cssSelector(widthValueSelector), product.getWidth());
//
//        clickNewPropertyButton(driver);
//
//        // Length
//        scrollToElementAndSendKeys(driver, By.cssSelector(lengthNameSelector), "Lengte");
//        scrollToElementAndSendKeys(driver, By.cssSelector(lengthValueSelector), product.getSplitLength());
    }


    public static void clickNewPropertyButton(WebDriver driver) {
        // Click on driver and wait for it to load
        scrollToElementAndClick(driver, By.cssSelector("#product_attributes > div.toolbar.toolbar-top > div.actions > button"));
        waitForClickable();
    }

    public static void scrollToElementAndClick(WebDriver driver, By by) {
        WebElement element = driver.findElement(by);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, -200);"); // Scroll up by 200 pixels
        waitForClickable();
        element.click();
    }

    public static void scrollToElementAndSendKeys(WebDriver driver, By by, String text) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.elementToBeClickable(by));
        WebElement element = driver.findElement(by);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, -200);"); // Scroll up by 200 pixels
        element.clear();
        element.sendKeys(text);
    }

    public static String getPhrase(WebDriver driver, int index) {
        List<WebElement> elements = driver.findElements(By.cssSelector("input[class='input-block sync-label wpcpo-input-not-empty']"));
        WebElement secondElement = elements.get(index);
        String nameValue = secondElement.getAttribute("name");
        String[] parts = nameValue.split("\\[wpcpo-|\\]\\[title\\]");
        return parts[1];
    }

    public static void waitForClickable() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void assignBasicInfo(Product product, WebDriver driver) {
        driver.findElement(By.id("title")).sendKeys(product.getName());

        WebElement iframe = driver.findElement(By.id("content_ifr"));
        driver.switchTo().frame(iframe);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#tinymce > p")));
        driver.findElement(By.cssSelector("#tinymce > p")).sendKeys(product.getDescription());
        driver.switchTo().defaultContent();

        driver.findElement(By.id("_regular_price")).sendKeys(product.getPricePerMeter());

        // Inventory tab
        driver.findElement(By.cssSelector("#woocommerce-product-data > div.inside > div > ul > li.inventory_options.inventory_tab.show_if_simple.show_if_variable.show_if_grouped.show_if_external > a")).click();
        driver.findElement(By.id("_sku")).sendKeys(product.getCode());
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

