package com.luma.ocr;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

final class ItalianOcrCli {

    private final ItalianOcr ocr;
    private final Scanner scanner = new Scanner(System.in);

    ItalianOcrCli(ItalianOcr ocr) {
        this.ocr = ocr;
    }

    void run() {
        while (true) {
            List<String> images = ImageLocator.listAvailableImages();
            showMenu(images);
            Optional<String> choice = prompt("Your choice: ");
            if (choice.isEmpty() || isExit(choice.get())) return;
            Optional<String> name = pickName(choice.get(), images);
            if (name.isEmpty()) continue;
            if (process(name.get())) return;
        }
    }

    private void showMenu(List<String> images) {
        if (images.isEmpty()) {
            System.out.println("No images found in resources/images. Use option 0 to type a filename or path.");
            return;
        }
        System.out.println("Select an image by number (first image is 1). Option 0 lets you type a name. Type X to exit.");
        for (int i = 0; i < images.size(); i++) {
            System.out.printf("%d) %s%n", i + 1, images.get(i));
        }
    }

    private Optional<String> prompt(String message) {
        System.out.print(message);
        if (!scanner.hasNextLine()) {
            System.out.println("No input received. Exiting.");
            return Optional.empty();
        }
        return Optional.of(scanner.nextLine().replace("\uFEFF", "").trim());
    }

    private boolean isExit(String input) {
        return input.equalsIgnoreCase("x") || input.equalsIgnoreCase("exit");
    }

    private Optional<String> pickName(String input, List<String> images) {
        if ("0".equals(input)) {
            return prompt("Enter image filename or path: ");
        }
        try {
            int choice = Integer.parseInt(input);
            if (choice < 1 || choice > images.size()) {
                System.out.println("Selection out of range.\n");
                return Optional.empty();
            }
            return Optional.of(images.get(choice - 1));
        } catch (NumberFormatException ex) {
            System.out.println("Invalid selection. Please enter a number or X to exit.\n");
            return Optional.empty();
        }
    }

    private boolean process(String name) {
        try {
            Path imagePath = ImageLocator.resolveImagePath(name);
            String text = ocr.process(imagePath);
            System.out.println("------ OCR (italiano) ------");
            System.out.println(text);
            OutputWriter.save(imagePath, text);
            return true;
        } catch (Exception ex) {
            System.out.println("Image not found or could not be processed: " + ex.getMessage());
            System.out.println("Returning to menu...\n");
            return false;
        }
    }
}
