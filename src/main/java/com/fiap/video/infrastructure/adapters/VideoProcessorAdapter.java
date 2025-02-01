package com.fiap.video.infrastructure.adapters;

import com.fiap.video.core.domain.Video;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class VideoProcessorAdapter {

    public void extractFrames(Video video, String outputFolder, int intervalSeconds) {
        try {
            Files.createDirectories(Paths.get(outputFolder));

            try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(video.getPath())) {
                frameGrabber.start();
                int frameRate = (int) frameGrabber.getFrameRate();
                int frameInterval = frameRate * intervalSeconds;
                Java2DFrameConverter converter = new Java2DFrameConverter();
                int frameNumber = 0;

                for (int i = 0; i < frameGrabber.getLengthInFrames(); i += frameInterval) {
                    frameGrabber.setFrameNumber(i);
                    Frame frame = frameGrabber.grabImage();
                    if (frame != null) {
                        BufferedImage image = converter.convert(frame);
                        if (image != null) {
                            String frameFileName = String.format("%s/frame_%04d.jpg", outputFolder, frameNumber++);
                            ImageIO.write(image, "jpg", new File(frameFileName));
                        }
                    }
                }
                frameGrabber.stop();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error during frame extraction", e);
        }
    }

    public void compressFrames(String folderPath, String zipFilePath) {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(Paths.get(zipFilePath)))) {
            Files.walk(Paths.get(folderPath))
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            zos.putNextEntry(new ZipEntry(Paths.get(folderPath).relativize(path).toString()));
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to zip file", e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Failed to create ZIP file", e);
        }
    }
}