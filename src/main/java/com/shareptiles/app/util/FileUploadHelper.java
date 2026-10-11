package com.shareptiles.app.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileUploadHelper {

    public static File getCanonicalDir(String uploadDir, String subDir) {
        File baseDir = new File(uploadDir).isAbsolute() ?
                new File(uploadDir) :
                new File(System.getProperty("user.dir"), uploadDir).getAbsoluteFile();
        File targetDir = (subDir != null && !subDir.isBlank()) ?
                new File(baseDir, subDir).getAbsoluteFile() :
                baseDir;
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        return targetDir;
    }

    public static String saveUploadedFile(MultipartFile file, String uploadDir, String subDir) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        File targetDir = getCanonicalDir(uploadDir, subDir);

        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains(".")) ?
                originalName.substring(originalName.lastIndexOf(".")) : ".jpg";
        String sanitized = (originalName != null) ? originalName.replaceAll("[^a-zA-Z0-9._-]", "_") : "file";
        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + sanitized;
        if (!fileName.toLowerCase().endsWith(ext.toLowerCase())) {
            fileName += ext;
        }

        File destFile = new File(targetDir, fileName).getAbsoluteFile();
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return (subDir != null && !subDir.isBlank()) ? (subDir + "/" + fileName) : fileName;
    }
}
