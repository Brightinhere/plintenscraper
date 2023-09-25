package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FilterProducts {

    public static void main(String[] args) {
        String txtFilePath = "output.txt";
        String csvFilePath = "plinten - kopie.csv";
        String outputCsvFilePath = "filtered_plinten_data.csv";

        Set<String> failedProducts = fetchFailedProducts(txtFilePath);
        filterCsvBasedOnFailedProducts(csvFilePath, outputCsvFilePath, failedProducts);
    }

    private static Set<String> fetchFailedProducts(String txtFilePath) {
        Set<String> failedProducts = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(txtFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Failed to add options for product: ")) {
                    String productCode = line.split(" ")[6];
                    failedProducts.add(productCode);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return failedProducts;
    }

    private static void filterCsvBasedOnFailedProducts(String csvFilePath, String outputCsvFilePath, Set<String> failedProducts) {
        try (
                BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
                FileWriter fw = new FileWriter(outputCsvFilePath)
        ) {
            String line;
            boolean headerWritten = false; // to handle the CSV header

            while ((line = br.readLine()) != null) {
                String[] columns = line.split(";");
                if (columns.length > 4) {
                    String productCode = columns[4]; // Column E
                    if (failedProducts.contains(productCode)) {
                        if (!headerWritten) {
                            fw.write(line + "\n");
                            headerWritten = true;
                        } else {
                            fw.write(line + "\n");
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
