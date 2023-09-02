package org.example;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Product {
    private String title;
    private String link;
    private String name;
    private String description;
    private String code;
    private String material;
    private String height;
    private String width;
    private String length;
    private String pricePerMeter;
    private String priceTax;
    private String subcategory;
    private String subcategory2;
    private List<String> images;
    private List<String> linkedProducts;

    public Product(String title, String link, String name, String description, String code, String material, String height, String width, String length, String pricePerMeter, String priceTax, String subcategory, String subcategory2, List<String> images, List<String> linkedProducts) {
        this.title = title;
        this.link = link;
        this.name = name;
        this.description = description;
        this.code = code;
        this.material = material;
        this.height = height;
        this.width = width;
        this.length = length;
        this.pricePerMeter = pricePerMeter;
        this.priceTax = priceTax;
        this.subcategory = subcategory;
        this.subcategory2 = subcategory2;
        this.images = images;
        this.linkedProducts = linkedProducts;
    }

    public Product(String[] values) {
        this.title = values[0];
        this.link = values[1];
        this.name = values[2];
        this.description = values[3];
        this.code = values[4];
        this.material = values[5];
        this.height = values[6];
        this.width = values[7];
        this.length = values[8];
        this.pricePerMeter = extractNumericValue(values[9]);
        this.priceTax = extractNumericValue(values[10]);
        this.subcategory = values[11];
        this.subcategory2 = values[12];

        // You can set the images by calling a method that reads them from the folder
        // this.images = readImagesFromFolder("product_images/" + this.title);

        // Handling linked products
        if (values.length > 13) {
            this.linkedProducts = Arrays.asList(values[13].split(";"));
        } else {
            this.linkedProducts = new ArrayList<>();
        }
        this.images = readImagesFromFolder(this.title.replaceAll("[^a-zA-Z0-9.-]", "_"));
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description.replace(";", ",");
    }

    public String getCode() {
        return code;
    }

    public String getMaterial() {
        return material;
    }

    public String getHeight() {
        return height;
    }

    public String getWidth() {
        return width;
    }

    public String getLength() {
        return length;
    }

    public String getSplitLength() {
        return length.replace(", ", " |").replace(",", "|");
    }

    public String getPricePerMeter() {
        String[] parts = pricePerMeter.split(" ");
        String price = parts.length > 1 ? parts[1] : pricePerMeter;
        return price.replace(",", ".");
    }

    public String getPriceTax() {
        return priceTax;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public String getSubcategory2() {
        return subcategory2;
    }

    public List<String> getImages() {
        return images;
    }

    public List<String> getLinkedProducts() {
        return linkedProducts;
    }


    private static List<String> readImagesFromFolder(String productName) {
        List<String> imagePaths = new ArrayList<>();

        String baseDir = "product_images";
        File productFolder = new File(baseDir, productName);

        if (productFolder.exists() && productFolder.isDirectory()) {
            for (File file : productFolder.listFiles()) {
                if (isImageFile(file)) {
                    imagePaths.add(file.getAbsolutePath());
                }
            }
        }

        return imagePaths;
    }

    private static boolean isImageFile(File file) {
        String[] validExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
        String fileName = file.getName().toLowerCase();

        for (String ext : validExtensions) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }


    public static String extractNumericValue(String value) {
        return value.replaceAll("[^0-9,]", "").trim();
    }

}


