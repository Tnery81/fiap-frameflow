package com.fiap.video.infrastructure.adapters;

import com.fiap.video.config.S3Config;
import com.fiap.video.core.domain.Video;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoProcessorAdapterTest {

    @InjectMocks
    private VideoProcessorAdapter videoProcessorAdapter;

    @Mock
    private S3Config s3Config;

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
        when(s3Config.getS3Client()).thenReturn(s3Client);
        when(s3Client.utilities()).thenReturn(s3Utilities);
    }

    @Test
    void shouldReturnNullWhenNoFramesExtracted() throws Exception {
        when(video.getPath()).thenReturn("/path/to/empty_video.mp4");

        grabber = mock(FFmpegFrameGrabber.class);
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
