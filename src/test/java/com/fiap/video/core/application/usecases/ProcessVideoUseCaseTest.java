package com.fiap.video.core.application.usecases;

import com.fiap.video.core.application.enums.VideoStatus;
import com.fiap.video.core.domain.Video;
import com.fiap.video.core.domain.VideoMessage;
import com.fiap.video.infrastructure.adapters.VideoDownloadAdapter;
import com.fiap.video.infrastructure.adapters.SNSAdapter;
import com.fiap.video.infrastructure.adapters.VideoProcessorAdapter;
import com.fiap.video.infrastructure.memory.InMemoryVideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;

import java.io.File;
import java.time.Duration;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ProcessVideoUseCaseTest {

    @InjectMocks
    private ProcessVideoUseCase processVideoUseCase;

    @Mock
    private VideoProcessorAdapter videoProcessorAdapter;

    @Mock
    private InMemoryVideoRepository videoRepository;

    @Mock
    private SNSAdapter snsAdapter;

    @Mock
    private VideoDownloadAdapter s3Service;

    @Mock
    private VideoMessage videoMessage;

    @Mock
    private File mockFile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessVideoSuccess() throws Exception {

        String videoKey = "video.mp4";
        String zipFileName = videoKey.replace(".mp4", ".zip");
        String expectedUrl = "mock-url-to-zip";
        int intervalSeconds = 10;

        when(videoMessage.getVideoKeyS3()).thenReturn(videoKey);
        when(videoMessage.getIntervalSeconds()).thenReturn(intervalSeconds);
        when(s3Service.downloadFile(videoKey)).thenReturn(mockFile);
        when(videoProcessorAdapter.extractFrames(any(), eq(zipFileName), eq(intervalSeconds))).thenReturn(expectedUrl);

        processVideoUseCase.process(videoMessage);

        verify(snsAdapter, times(1)).publishMessage(eq(videoMessage), eq(VideoStatus.IN_PROGRESS), eq(zipFileName), eq(VideoStatus.IN_PROGRESS.toString()));
        verify(snsAdapter, times(1)).publishMessage(eq(videoMessage), eq(VideoStatus.COMPLETED), eq(zipFileName), eq(expectedUrl));
        verify(videoRepository, times(1)).save(any(Video.class));
    }

    @Test
    void testProcessVideoFailure() throws Exception {
        String videoKey = "video.mp4";
        String zipFileName = videoKey.replace(".mp4", ".zip");

        when(videoMessage.getVideoKeyS3()).thenReturn(videoKey);
        when(s3Service.downloadFile(videoKey)).thenReturn(mockFile);
        OngoingStubbing<String> stringOngoingStubbing = when(videoProcessorAdapter.extractFrames(any(), eq(zipFileName), eq(10))).thenReturn(null);

        processVideoUseCase.process(videoMessage);

        verify(snsAdapter, times(1)).publishMessage(eq(videoMessage), eq(VideoStatus.IN_PROGRESS), eq(zipFileName), eq(VideoStatus.IN_PROGRESS.toString()));
        verify(snsAdapter, times(1)).publishMessage(eq(videoMessage), eq(VideoStatus.PROCESSING_ERROR), eq(VideoStatus.PROCESSING_ERROR.toString()), eq(".zip"));
        verify(videoRepository, times(0)).save(any());
    }

    @Test
    void testProcessVideoExceptionHandling() throws Exception {
        String videoKey = "video.mp4";
        String zipFileName = videoKey.replace(".mp4", ".zip");

        when(videoMessage.getVideoKeyS3()).thenReturn(videoKey);
        when(s3Service.downloadFile(videoKey)).thenThrow(new RuntimeException("Download failed"));

        processVideoUseCase.process(videoMessage);

        verify(snsAdapter, times(1)).publishMessage(eq(videoMessage), eq(VideoStatus.IN_PROGRESS), eq(zipFileName), eq(VideoStatus.IN_PROGRESS.toString()));
        verify(snsAdapter, times(1)).publishMessage(eq(videoMessage), eq(VideoStatus.PROCESSING_ERROR), eq(VideoStatus.PROCESSING_ERROR.toString()), eq(".zip"));
    }

    @Test
    void testGetVideoSuccess() throws Exception {
        String videoKey = "video.mp4";
        String zipKey = videoKey.replace(".mp4", ".zip");
        when(s3Service.downloadFile(zipKey)).thenReturn(mockFile);

        Video video = processVideoUseCase.getVideo(videoKey);

        assertNotNull(video, "Video should be returned");
        verify(s3Service, times(1)).downloadFile(zipKey);
    }

    @Test
    void testGetVideoFailure() throws Exception {
        String videoKey = "video.mp4";
        String zipKey = videoKey.replace(".mp4", ".zip");
        when(s3Service.downloadFile(zipKey)).thenThrow(new RuntimeException("Download failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> processVideoUseCase.getVideo(videoKey));
        assertEquals("Erro ao obter o vídeo ZIP do S3: Download failed", exception.getMessage());
    }
}
