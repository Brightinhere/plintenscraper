package org.example;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Product {
    private final String title;
    private final String link;
    private final String name;
    private final String description;
    private final String code;
    private final String material;
    private final String height;
    private final String width;
    private final String length;
    private final String pricePerMeter;
    private final String subcategory;
    private final String subcategory2;
    private final List<String> images;
    private final List<String> linkedProducts;
    private final List<String> sizes;
    private final List<String> finishes;
    private final List<String> lengths;

    public Product(String title, String link, String name, String description, String code, String material, String height, String width, String length, String pricePerMeter, String subcategory, String subcategory2, List<String> images, List<String> linkedProducts, List<String> sizes, List<String> finishes, List<String> lengths) {
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
        this.subcategory = subcategory;
        this.subcategory2 = subcategory2;
        this.images = images;
        this.linkedProducts = linkedProducts;
        this.sizes = sizes;
        this.finishes = finishes;
        this.lengths = lengths;
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
        this.pricePerMeter = values[9];
        this.subcategory = values[10];
        this.subcategory2 = values[11];

        // Handling linked products
        if (values.length > 12) {
            this.linkedProducts = Arrays.asList(values[12].split("%"));
        } else {
            this.linkedProducts = new ArrayList<>();
        }
        this.images = readImagesFromFolder(this.title.replaceAll(",", "_").replaceAll("[^a-zA-Z0-9.-]", "_"));
        this.sizes = values[13].equals("") ? new ArrayList<>() : Arrays.asList(values[13].split("%"));
        this.finishes = values[14].equals("") ? new ArrayList<>() : Arrays.asList(values[14].split("%"));
        this.lengths = values[15].equals("") ? new ArrayList<>() : Arrays.asList(values[15].split("%"));
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
        return description.replace(";", ",").replace("Let op: Dit is een uitlopend product. Aanbieding is geldig zolang de voorraad strekt.", "");
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

    public List<String> getSizes() {
        return sizes;
    }

    public List<String> getFinishes() {
        return finishes;
    }

    public List<String> getLengths() {
        return lengths;
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


