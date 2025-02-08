package com.fiap.video.infrastructure.adapters;

import com.fiap.video.config.S3Config;
import com.fiap.video.infrastructure.exception.S3DownloadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;

@Component
public class VideoDownloadAdapter {

    private final S3Config s3Config;

    @Value("${aws.s3.bucketVideo}")
    private String bucketVideoName;

    public VideoDownloadAdapter(S3Config s3Config) {
        this.s3Config = s3Config;

    }

    public File downloadFile(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketVideoName)
                    .key(key)
                    .build();

            File tempFile = Files.createTempFile("video", ".mp4").toFile();
            try (InputStream inputStream = s3Config.getS3Client().getObject(request);
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (Exception e) {
            throw new S3DownloadException("Erro ao baixar vídeo do S3", e);
        }
    }
}
