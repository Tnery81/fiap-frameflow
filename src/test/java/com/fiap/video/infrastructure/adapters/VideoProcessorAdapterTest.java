package com.fiap.video.infrastructure.adapters;

import com.fiap.video.config.ConfigS3;
import com.fiap.video.core.domain.Video;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.awt.image.BufferedImage;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoProcessorAdapterTest {

    @InjectMocks
    private VideoProcessorAdapter videoProcessorAdapter;

    @Mock
    private ConfigS3 configS3;

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Utilities s3Utilities;

    @Mock
    private Video video;

    @Mock
    private FFmpegFrameGrabber grabber;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(configS3.getS3Client()).thenReturn(s3Client);
        when(s3Client.utilities()).thenReturn(s3Utilities);
    }

//ajustar teste
    @Test
    void shouldExtractFramesAndUploadSuccessfully() throws Exception {
        when(video.getPath()).thenReturn("/path/to/video.mp4");
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(null);
        when(s3Utilities.getUrl(any(GetUrlRequest.class))).thenReturn(new URL("http://mock-url.com/video.zip"));

        String result = videoProcessorAdapter.extractFrames(video, "video.zip", 1);

        assertNotNull(result, "The URL should not be null after successful upload");
        assertEquals("http://mock-url.com/video.zip", result, "The URL should match the mocked S3 response");

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void shouldReturnNullWhenNoFramesExtracted() throws Exception {
        when(video.getPath()).thenReturn("/path/to/empty_video.mp4");

        FFmpegFrameGrabber grabber = mock(FFmpegFrameGrabber.class);
        when(grabber.grabImage()).thenReturn(null);

        String result = videoProcessorAdapter.extractFrames(video, "empty.zip", 1);

        assertNull(result, "The result should be null when no frames are extracted");
    }

    @Test
    void shouldHandleExceptionDuringProcessing() {
        when(video.getPath()).thenReturn("/invalid/path/video.mp4");

        String result = videoProcessorAdapter.extractFrames(video, "error.zip", 1);

        assertNull(result, "The result should be null when an exception occurs during processing");
    }
}
