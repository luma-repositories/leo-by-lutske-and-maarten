package com.luma.ocr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

final class OutputWriter {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");

    static void save(Path imagePath, String text) throws IOException {
        String baseName = imagePath.getFileName().toString();
        String timestamp = FORMAT.format(LocalDateTime.now());
        String safeTimestamp = timestamp.replace(':', '-');
        String fileName = baseName + safeTimestamp + ".txt";
        Path cwd = Path.of("").toAbsolutePath();
        Path target = cwd.endsWith("leon-image-to-text")
                ? cwd.resolve("src/main/resources/output")
                : cwd.resolve("leon-image-to-text/src/main/resources/output");
        Files.createDirectories(target);
        Path output = target.resolve(fileName);
        Files.writeString(output, text, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("Saved OCR output to " + output);
    }
}
