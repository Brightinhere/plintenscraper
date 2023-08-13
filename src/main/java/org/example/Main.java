package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        final String baseUrl = "https://www.plintenfabriek.nl";
        final String initialUrl = "https://www.plintenfabriek.nl/plinten";
        int count = 1;
        List<Product> products = new ArrayList<>();

        System.out.println("=================================");
        System.out.println("Pagina: " + count);
        System.out.println("=================================");
        String nextPage = scrapePageInformation(initialUrl, products);

        while (nextPage != null) {
            count++;
            System.out.println("=================================");
            System.out.println("Pagina: " + count);
            System.out.println("=================================");
            nextPage = scrapePageInformation(baseUrl + nextPage, products);
        }

        System.out.println("=================================");
        System.out.println("Done scrapping, starting to export");
        System.out.println("=================================");
        exportProducts(products);
    }

    public static String scrapePageInformation(String url, List<Product> products) {
        try {
            final Document document = Jsoup.connect(url).get();

            // Select all divs with class "product"
            Elements productElements = document.select("div.product");

            for (Element productElement : productElements) {
                // Within each product, find the "product-image" div and select an element within
                Element productLinkElement = productElement.select("div.product-image > a").first();

                // Extract the href attribute
                String productLink = productLinkElement.attr("abs:href");

                Product product = getProductInformation(productLink);
                products.add(product);
            }
            Element nextButton = document.select("#contentwrapper > div:nth-child(4) > div > div.col-md-9.push-down-30 > div.pagination > div > div.col-md-2.text-right > a").get(0);

            if (!nextButton.attr("href").equals("")){
                return nextButton.attr("href");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Product getProductInformation(String page) throws IOException {
        // Visit the product page
        Document productPageDocument = Jsoup.connect(page).get();
        String productTitle = productPageDocument.select("#main-title").text();

        String subcategory = productPageDocument.select("#breadcrumbs > ul > li:nth-child(3) > a").get(0).text();
        String subcategory2 = productPageDocument.select("#breadcrumbs > ul > li:nth-child(4) > a").get(0).text();
        String name = productPageDocument.select("#specifications > table > tbody > tr:nth-child(1) > td:nth-child(2)").text();
        String description = productPageDocument.select("#productinformation").text();
        String code = productPageDocument.select("#specifications > table > tbody > tr:nth-child(2) > td:nth-child(2)").text();
        String material = productPageDocument.select("#specifications > table > tbody > tr:nth-child(3) > td:nth-child(2)").text();
        String height = productPageDocument.select("#specifications > table > tbody > tr:nth-child(4) > td:nth-child(2)").text();
        String width = productPageDocument.select("#specifications > table > tbody > tr:nth-child(5) > td:nth-child(2)").text();
        String length = productPageDocument.select("#specifications > table > tbody > tr:nth-child(6) > td:nth-child(2)").text();

        String pricePerMeter = productPageDocument.select("#productUnitPrice > span").text();
        String priceTax = productPageDocument.select("#productPrice > span").text();

        Elements images = productPageDocument.select(".productThumbnail");
        // array of images
        List<String> imagesSrc = new ArrayList<String>(images.size());
        List<String> linkedProducts = new ArrayList<String>(images.size());

        for (Element image : images) {
            imagesSrc.add(image.attr("src").replace("thumb_", ""));
        }

        Elements linkedElements = productPageDocument.select(".filter-alternative-model-wrapper");

        for(Element linkedElement: linkedElements) {
            linkedProducts.add(linkedElement.text());
        }

        return new Product(productTitle, page, name,description, code, material, height, width, length, pricePerMeter, priceTax, subcategory, subcategory2, imagesSrc, linkedProducts);
    }

    private static void exportProducts(List<Product> products) {
        String fileName = "plinten.csv";

        String baseDir = "product_images"; // Base directory to save images

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            // Write the header
            writer.println("Title;Link;Name;Description;Code;Material;Height;Width;Length;PricePerMeter;PriceTax;Subcategory;Subcategory2");


            // Write the product data
            for (Product product : products) {
                String linkedProducts = String.join(";", product.getLinkedProducts());
                writer.println(String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s",
                        product.getTitle(),
                        product.getLink(),
                        product.getName(),
                        product.getDescription(),
                        product.getCode(),
                        product.getMaterial(),
                        product.getHeight(),
                        product.getWidth(),
                        product.getLength(),
                        product.getPricePerMeter(),
                        product.getPriceTax(),
                        product.getSubcategory(),
                        product.getSubcategory2(),
                        linkedProducts
                ));

                // Create a directory for the product's images
                Path productDir = Paths.get(baseDir, product.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_"));
                Files.createDirectories(productDir);

                // Download and save each image
                for (String imageUrl : product.getImages()) {
                    try (InputStream in = new URL(imageUrl).openStream()) {
                        Path outputPath = productDir.resolve(Paths.get(new URL(imageUrl).getPath()).getFileName());
                        // check if file already exists
                        if (Files.exists(outputPath)) {
                            continue;
                        }
                        Files.copy(in, outputPath);
                    } catch (IOException e) {
                        System.out.println("Error downloading image: " + imageUrl);
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
