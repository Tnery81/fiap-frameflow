package com.fiap.video.infrastructure.adapters;

import com.fiap.video.config.ConfigS3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;

@Component
public class VideoDownloadAdapter {

    private final ConfigS3 configS3;

    @Value("${aws.s3.bucketVideo}")
    private String bucketVideoName;

    public VideoDownloadAdapter(ConfigS3 configS3) {
        this.configS3 = configS3;

    }

    public File downloadFile(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketVideoName)
                    .key(key)
                    .build();

            File tempFile = Files.createTempFile("video", ".mp4").toFile();
            try (InputStream inputStream = configS3.getS3Client().getObject(request);
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao baixar vídeo do S3", e);
        }
    }
}
