package com.luma.ocr;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

final class ImageLocator {

    static List<String> listAvailableImages() {
        try {
            Path dir = Path.of("src/main/resources/images");
            if (Files.isDirectory(dir)) {
                return listFromDirectory(dir);
            }
            return listFromClasspath();
        } catch (Exception ex) {
            System.out.println("Could not list images: " + ex.getMessage());
            return List.of();
        }
    }

    static Path resolveImagePath(String arg) throws IOException {
        String cleaned = arg.startsWith("/") ? arg.substring(1) : arg;
        Path candidate = Path.of(cleaned);
        if (Files.exists(candidate)) {
            return candidate;
        }
        return loadFromClasspath(cleaned);
    }

    private static List<String> listFromDirectory(Path dir) throws IOException {
        try (var stream = Files.list(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(ImageLocator::isVisible)
                    .sorted(Comparator.comparing(Path::getFileName))
                    .map(p -> p.getFileName().toString())
                    .toList();
        }
    }

    private static List<String> listFromClasspath() throws IOException, URISyntaxException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("images");
        if (url == null) {
            return List.of();
        }
        if ("file".equals(url.getProtocol())) {
            return listFromDirectory(Path.of(url.toURI()));
        }
        if ("jar".equals(url.getProtocol())) {
            return listFromJar(url.toURI());
        }
        return List.of();
    }

    private static List<String> listFromJar(URI uri) throws IOException {
        String[] parts = uri.toString().split("!/");
        if (parts.length != 2) {
            return List.of();
        }
        try (FileSystem fs = FileSystems.newFileSystem(URI.create(parts[0]), Map.of());
             var stream = Files.list(fs.getPath(parts[1]))) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(ImageLocator::isVisible)
                    .sorted(Comparator.comparing(Path::getFileName))
                    .map(p -> p.getFileName().toString())
                    .toList();
        }
    }

    private static boolean isVisible(Path path) {
        return !path.getFileName().toString().startsWith(".");
    }

    private static Path loadFromClasspath(String name) throws IOException {
        String resourceName = "images/" + name;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream is = cl.getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IOException("Image not found. Provide a filename located in resources/images or an existing file path: " + name);
            }
            Path temp = Files.createTempFile("ocr-image-", extensionOrDefault(name));
            Files.copy(is, temp, StandardCopyOption.REPLACE_EXISTING);
            temp.toFile().deleteOnExit();
            return temp;
        }
    }

    private static String extensionOrDefault(String name) {
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot) : ".img";
    }
}
