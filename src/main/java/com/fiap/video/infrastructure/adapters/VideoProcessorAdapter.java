package com.fiap.video.infrastructure.adapters;

import com.fiap.video.config.S3Config;
import com.fiap.video.core.domain.Video;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
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

    private final S3Config s3Config;

    @Value("${aws.s3.bucketZip}")
    private String bucketZipName;

    public VideoProcessorAdapter(S3Config s3Config) {
        this.s3Config = s3Config;
    }

    public String extractFrames(Video video, String zipFileName, int intervalSeconds) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(video.getPath());
             ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(zipBaos);
             Java2DFrameConverter converter = new Java2DFrameConverter()) {

            grabber.start();

            int frameRate = (int) grabber.getFrameRate();
            int frameInterval = frameRate * intervalSeconds;
            int frameNumber = 0;
            Frame frame;

            while ((frame = grabber.grabImage()) != null) {
                if (frameNumber % frameInterval == 0) {
                    BufferedImage bufferedImage = converter.getBufferedImage(frame);
                    try (ByteArrayOutputStream imageBaos = new ByteArrayOutputStream()) { // Também garantindo fechamento do stream
                        ImageIO.write(bufferedImage, "jpg", imageBaos);
                        byte[] imageBytes = imageBaos.toByteArray();
                        String fileName = "frame_" + String.format("%04d", frameNumber) + ".jpg";
                        zipOut.putNextEntry(new ZipEntry(fileName));
                        zipOut.write(imageBytes);
                        zipOut.closeEntry();
                    }
                }
                frameNumber++;
            }

            grabber.stop();
            zipOut.finish();
            return uploadToS3(zipFileName, zipBaos.toByteArray());

        } catch (IOException e) {
            return null;
        }
    }

    private String uploadToS3(String zipFileName, byte[] zipData) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketZipName)
                    .key(zipFileName)
                    .build();

            s3Config.getS3Client().putObject(putObjectRequest, RequestBody.fromBytes(zipData));

            return s3Config.getS3Client().utilities().getUrl(GetUrlRequest.builder()
                    .bucket(bucketZipName)
                    .key(zipFileName)
                    .build()).toString();

        } catch (Exception e) {

            return null;
        }
    }
}
