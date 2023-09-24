package org.example;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class ImageSorter {

    private static final String DIRECTORY_PATH = "product_images_2";
    private static final int PADDING = 10;

    public static void main(String[] args) throws IOException {
        File directory = new File(DIRECTORY_PATH);
        File[] files = directory.listFiles();

        if (files == null) {
            System.out.println("Directory not found or no files in the directory.");
            return;
        }

        // Sort the files based on product ID and index.
        Arrays.sort(files, Comparator.comparing(File::getName));

        String currentProductId = "";
        for (File file : files) {
            String productId = file.getName().split("-")[0];
            if (!productId.equals(currentProductId)) {
                currentProductId = productId;
                processGroup(files, productId);
            }
        }
    }

    private static void processGroup(File[] files, String productId) throws IOException {
        File whiteCornerFile = null;

        // First, rename all images except the one with the white corner.
        int newIndex = 1;
        for (File file : files) {
            if (file.getName().startsWith(productId + "-") && !file.getName().contains("-temp")) {
                BufferedImage image = ImageIO.read(file);

                if (isTopRightPixelWhite(image)) {
                    whiteCornerFile = new File(file.getParent(), productId + "-temp.jpg");
                    if (!file.renameTo(whiteCornerFile)) {
                        System.out.println("Failed to rename: " + file.getName());
                    }
                } else {
                    File newFile = new File(file.getParent(), productId + "-" + newIndex + ".jpg");
                    if (!file.renameTo(newFile)) {
                        System.out.println("Failed to rename: " + file.getName());
                    } else {
                        System.out.println("Renamed " + file.getName() + " to " + newFile.getName());
                        newIndex++;
                    }
                }
            }
        }

        // Now, rename the -temp.jpg file (if exists)
        if (whiteCornerFile != null) {
            File newFile = new File(whiteCornerFile.getParent(), productId + "-" + newIndex + ".jpg");
            if (!whiteCornerFile.renameTo(newFile)) {
                System.out.println("Failed to rename: " + whiteCornerFile.getName());
            } else {
                System.out.println("Renamed " + whiteCornerFile.getName() + " to " + newFile.getName());
            }
        }
    }

    private static boolean isTopRightPixelWhite(BufferedImage image) {
        int width = image.getWidth();
        int rgb = image.getRGB(width - PADDING, PADDING);
        return (rgb == 0xFFFFFFFF);
    }
}

