package org.example;

import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        try {
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
//            exportProducts(products);
            exportToCSVList(products);
            Runtime.getRuntime().exec("curl -d \"Done running the script \" https://ntfy.sh/niemand_komt_hier_achter123566");
        } catch (Exception e) {
            Runtime.getRuntime().exec("curl -d \"Java Error: " + e.getMessage() + " \" https://ntfy.sh/niemand_komt_hier_achter123566");
            System.err.println("Exception occurred: " + e.getMessage());
        }
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

        Elements images = productPageDocument.select(".productThumbnail");
        // array of images
        List<String> imagesSrc = new ArrayList<String>(images.size());
        List<String> linkedProducts = new ArrayList<String>(images.size());

        for (Element image : images) {
            imagesSrc.add(image.attr("src").replaceFirst("thumb_", ""));
        }

//        if (imagesSrc.size() == 0) {
//            try {
//                String singleImage = productPageDocument.select("#productCarousel > div > div > a > img").get(0).attr("src");
//                imagesSrc.add(singleImage);
//            } catch (Exception e) {
//                try {
//                    String singleImage = productPageDocument.select("#productView > div > div > div.col-xs-12.col-sm-7.col-md-5 > div.relative > div > img").get(0).attr("src");
//                    imagesSrc.add(singleImage);
//                } catch (Exception ex) {
//                    System.out.println("No images found for product: " + productTitle);
//                }
//                System.out.println("No images found for product: " + productTitle);
//            }
//        }
        if (imagesSrc.size() == 0) {
            Elements selectedImages1 = productPageDocument.select("#productCarousel > div > div > a > img");
            Elements selectedImages2 = productPageDocument.select("#productView > div > div > div.col-xs-12.col-sm-7.col-md-5 > div.relative > div > img");

            if (selectedImages1.size() > 0) {
                imagesSrc.add(selectedImages1.get(0).attr("src"));
            } else if (selectedImages2.size() > 0) {
                imagesSrc.add(selectedImages2.get(0).attr("src"));
            } else {
                System.out.println("No images found for product: " + productTitle + " and this link: " + page);
            }
        }

        Elements linkedElements = productPageDocument.select(".filter-alternative-model-wrapper");

        for(Element linkedElement: linkedElements) {
            linkedProducts.add(linkedElement.text());
        }

        List<String> sizes = new ArrayList<>();
        List<String> finishes = new ArrayList<>();
        List<String> lengths = new ArrayList<>();
        Elements options = productPageDocument.select("#inputProductSelection option");
        for (Element option : options) {
            sizes.add(option.text());
        }

        Elements finishOptions = productPageDocument.select("#inputProductMaterialFinish option");
        for (Element finish : finishOptions) {
            finishes.add(finish.text());
        }

        Elements lengthOptions = productPageDocument.select("#inputProductLength option");
        for (Element finish : lengthOptions) {
            if(finish.text().contains("mm")) {
                lengths.add(finish.text());
            } else {
                lengths.add(finish.text() + " mm");
            }
        }

        return new Product(productTitle, page, name,description, code, material, height, width, length, pricePerMeter, subcategory, subcategory2, imagesSrc, linkedProducts, sizes, finishes, lengths);
    }

    private static void exportProducts(List<Product> products) {
        String fileName = "plinten.csv";

        String baseDir = "product_images"; // Base directory to save images

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            // Write the header
            writer.println("Title;Link;Name;Description;Code;Material;Height;Width;Length;PricePerMeter;Subcategory;Subcategory2;LinkedProducts;Sizes;Finishes;Lengths");


            // Write the product data
            for (Product product : products) {
                String linkedProducts = String.join("%", product.getLinkedProducts());
                writer.println(String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s",
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
                        product.getSubcategory(),
                        product.getSubcategory2(),
                        linkedProducts,
                        product.getSizes().size() > 0 ? String.join("%", product.getSizes()) : "",
                        product.getFinishes().size() > 0 ? String.join("%", product.getFinishes()) : "",
                        product.getLengths().size() > 0 ? String.join("%", product.getLengths()) : ""
                ));
                saveImages(baseDir, product);

            }
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean saveImages(String baseDir, Product product) throws IOException {
        // Get the base directory path
        Path baseDirectoryPath = Paths.get(baseDir);
        Files.createDirectories(baseDirectoryPath);

        List<Pair<String, Integer>> imageUrlSizePairs = new ArrayList<>();

        for (String imageUrl : product.getImages()) {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int imageSize = connection.getContentLength();
            imageUrlSizePairs.add(new Pair<>(imageUrl, imageSize));
            connection.disconnect();
        }

        imageUrlSizePairs.sort((a, b) -> {
            int sizeComparison = -Integer.compare(a.getValue(), b.getValue());
            if (sizeComparison == 0) {
                return -Integer.compare(imageUrlSizePairs.indexOf(a), imageUrlSizePairs.indexOf(b));
            }
            return sizeComparison;
        });

        boolean isImageSaved = false;
        int index = 1;
        for (Pair<String, Integer> pair : imageUrlSizePairs) {
            String imageUrl = pair.getKey();
            try (InputStream in = new URL(imageUrl).openStream()) {
                Path outputPath = baseDirectoryPath.resolve("v1_" + product.getCode() + "-" + index + ".jpg"); // adjust the extension if needed
                Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
                isImageSaved = true;
            } catch (IOException e) {
                System.out.println("Error downloading/saving image for product: " + product.getCode());
            }
            index++;
        }

        return isImageSaved;
    }

    public static void exportToCSVList(List<Product> products) throws IOException {
        String fileName = "plinten_list.csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("Title;Link;Name;Description;Code;Tag;MaterialName;Material;PricePerMeter;Category;Images;ImageSaved");
            for (Product product : products) {
                boolean isImageSaved = saveImages("product_images_2", product);
                writer.println(String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%b",
                        product.getTitle(),
                        product.getLink(),
                        product.getName(),
                        product.getDescription(),
                        product.getCode(),
                        product.getCode(),
                        "Materiaal",
                        product.getMaterial(),
                        product.getPricePerMeter(),
                        product.getSubcategory() + " > " + product.getSubcategory2(),
                        imagesToString(product),
                        isImageSaved
                ));
            }
        }
    }


    public static String imagesToString(Product product) {
        StringBuilder result = new StringBuilder();
        int index = 1; // for creating the title with an index

        for (String image : product.getImages()) {
            result.append("https://plintendiscount.nl/wp-content/uploads/2023/09/")
                    .append("v1_")
                    .append(product.getCode())
                    .append("-")
                    .append(index)
                    .append(".jpg")
                    .append("|");
            index++;
        }

        if (result.length() > 0) {
            result.setLength(result.length() - 1);
        }

        return result.toString();
    }


}
