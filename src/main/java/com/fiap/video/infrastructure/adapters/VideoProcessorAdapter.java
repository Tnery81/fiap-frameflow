package com.fiap.video.infrastructure.adapters;

import com.fiap.video.core.domain.Video;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class VideoProcessorAdapter {

    private final S3Client s3Client;

    @Value("${aws.s3.bucketZip}")
    private String bucketName;

    public VideoProcessorAdapter(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String extractFrames(Video video, String zipFileName, int intervalSeconds) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(video.getPath());
             ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(zipBaos)) {

            grabber.start();
            Java2DFrameConverter converter = new Java2DFrameConverter();
            int frameRate = (int) grabber.getFrameRate();
            int frameInterval = frameRate * intervalSeconds;
            int frameNumber = 0;

            Frame frame;
            while ((frame = grabber.grabImage()) != null) {
                if (frameNumber % frameInterval == 0) {
                    BufferedImage bufferedImage = converter.getBufferedImage(frame);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "jpg", baos);
                    baos.flush();

                    ZipEntry zipEntry = new ZipEntry("frame_" + String.format("%04d", frameNumber) + ".jpg");
                    zipOut.putNextEntry(zipEntry);
                    zipOut.write(baos.toByteArray());
                    zipOut.closeEntry();

                    baos.close();
                }
                frameNumber++;
            }

            grabber.stop();

            return uploadToS3(zipFileName, zipBaos.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String uploadToS3(String zipFileName, byte[] zipData) {
        try {
            String fileKey = zipFileName; // Nome do arquivo gerado

            System.out.println("Iniciando upload para o bucket: " + bucketName + " com key: " + fileKey);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(zipData));

            String fileUrl = s3Client.utilities().getUrl(GetUrlRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build()).toString();

            System.out.println("Upload concluído. URL: " + fileUrl);
            return fileUrl;

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao fazer upload para S3: " + e.getMessage());
            return null;
        }
    }


}