package com.documentmanager.util;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public final class FileStorageUtil {
    public static final Path DOCUMENT_ROOT = Path.of("documents");

    private FileStorageUtil() {
    }

    public static boolean isSupported(Path file) {
        String ext = extension(file);
        return ext.equals("PDF") || ext.equals("DOCX") || ext.equals("TXT");
    }

    public static String extension(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toUpperCase(Locale.ROOT);
    }

    public static Path copyToUserStorage(Path source, int userId) throws IOException {
        Path userDir = DOCUMENT_ROOT.resolve("user-" + userId);
        Files.createDirectories(userDir);
        Path target = userDir.resolve(source.getFileName().toString());
        if (Files.exists(target)) {
            throw new IOException("A file with this name already exists.");
        }
        return Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
    }

    public static void open(Path file) throws IOException {
        if (!Files.exists(file)) {
            throw new IOException("File does not exist.");
        }
        Desktop.getDesktop().open(file.toFile());
    }
}
