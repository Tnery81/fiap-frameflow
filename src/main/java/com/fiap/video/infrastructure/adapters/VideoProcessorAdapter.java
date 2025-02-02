package com.fiap.video.infrastructure.adapters;

import com.fiap.video.config.ConfigS3;
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

    private final ConfigS3 configS3;

    @Value("${aws.s3.bucketZip}")
    private String bucketZipName;

    public VideoProcessorAdapter(ConfigS3 configS3) {
        this.configS3 = configS3;
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
            int savedFrames = 0;

            Frame frame;
            while ((frame = grabber.grabImage()) != null) {
                if (frameNumber % frameInterval == 0) {
                    BufferedImage bufferedImage = converter.getBufferedImage(frame);

                    ByteArrayOutputStream imageBaos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "jpg", imageBaos);
                    byte[] imageBytes = imageBaos.toByteArray();

                    String fileName = "frame_" + String.format("%04d", frameNumber) + ".jpg";
                    zipOut.putNextEntry(new ZipEntry(fileName));
                    zipOut.write(imageBytes);
                    zipOut.closeEntry();

                    savedFrames++;
                }
                frameNumber++;
            }

            grabber.stop();
            zipOut.finish();

            if (savedFrames == 0) {
                System.out.println("Nenhum frame foi extraído. O ZIP pode estar vazio.");
                return null;
            }

            System.out.println("Total de frames extraídos: " + savedFrames);
            return uploadToS3(zipFileName, zipBaos.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String uploadToS3(String zipFileName, byte[] zipData) {
        try {
            System.out.println("Iniciando upload para o bucket: " + bucketZipName + " com key: " + zipFileName);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketZipName)
                    .key(zipFileName)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            configS3.getS3Client().putObject(putObjectRequest, RequestBody.fromBytes(zipData));

            String fileUrl = configS3.getS3Client().utilities().getUrl(GetUrlRequest.builder()
                    .bucket(bucketZipName)
                    .key(zipFileName)
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
