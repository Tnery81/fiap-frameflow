package com.fiap.video.infrastructure.adapters;

import com.fiap.video.core.domain.Video;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class VideoProcessorAdapter {

    public void extractFrames(Video video, String zipFilePath, int intervalSeconds) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(video.getPath());
             FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
