package com.fiap.video.infrastructure.adapters;

import com.fiap.video.config.S3Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoDownloadAdapterTest {

    @InjectMocks
    private VideoDownloadAdapter videoDownloadAdapter;

    @Mock
    private S3Config s3Config;

    @Mock
    private S3Client s3Client;

    @Mock
    private ResponseInputStream<GetObjectResponse> responseInputStream;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(s3Config.getS3Client()).thenReturn(s3Client);
    }

    @Test
    void shouldDownloadFileSuccessfully() throws Exception {
        String key = "video.mp4";
        byte[] content = "Test content".getBytes();
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);
        when(responseInputStream.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            int length = content.length;
            System.arraycopy(content, 0, buffer, 0, length);
            return -1;
        });
        File downloadedFile = videoDownloadAdapter.downloadFile(key);
        assertNotNull(downloadedFile, "Downloaded file should not be null");
        assertTrue(downloadedFile.exists(), "Downloaded file should exist");
        verify(s3Client, times(1)).getObject(any(GetObjectRequest.class));
        downloadedFile.delete();
    }

    @Test
    void shouldHandleDownloadFailure() {
        String key = "invalid_video.mp4";
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(S3Exception.builder().message("S3 error").build());
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                videoDownloadAdapter.downloadFile(key)
        );
        assertEquals("Erro ao baixar vídeo do S3", exception.getMessage());
        verify(s3Client, times(1)).getObject(any(GetObjectRequest.class));
    }
}
