package org.example;

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
        return description;
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

    public String getPricePerMeter() {
        return pricePerMeter;
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

    @Override
    public String toString() {
        return "Product{" +
                "title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", code='" + code + '\'' +
                ", material='" + material + '\'' +
                ", height='" + height + '\'' +
                ", width='" + width + '\'' +
                ", length='" + length + '\'' +
                ", pricePerMeter='" + pricePerMeter + '\'' +
                ", priceTax='" + priceTax + '\'' +
                ", subcategory='" + subcategory + '\'' +
                ", subcategory2='" + subcategory2 + '\'' +
                ", images='" + images + '\'' +
                '}';
    }
}
