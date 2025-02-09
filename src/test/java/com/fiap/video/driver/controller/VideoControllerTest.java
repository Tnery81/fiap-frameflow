package com.fiap.video.driver.controller;

import com.fiap.video.core.application.usecases.ProcessVideoUseCase;
import com.fiap.video.core.domain.Video;
import com.fiap.video.core.domain.VideoMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoControllerTest {

    @InjectMocks
    private VideoController videoController;

    @Mock
    private ProcessVideoUseCase processVideoUseCase;

    @Mock
    private VideoMessage videoMessage;

    @Mock
    private Video video;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessVideoSuccess() {
        doNothing().when(processVideoUseCase).process(videoMessage);

        String response = videoController.processVideo(videoMessage);

        assertEquals("Processamento iniciado com sucesso!", response);
        verify(processVideoUseCase, times(1)).process(videoMessage);
    }

    @Test
    void testProcessVideoFailure() {
        doThrow(new RuntimeException("Erro ao processar o vídeo"))
                .when(processVideoUseCase).process(videoMessage);

        String response = videoController.processVideo(videoMessage);

        assertTrue(response.contains("Erro ao iniciar o processamento:"));
        assertTrue(response.contains("Erro ao processar o vídeo"));
        verify(processVideoUseCase, times(1)).process(videoMessage);
    }

    @Test
    void testGetVideoSuccess() {
        String videoKey = "video.mp4";
        when(processVideoUseCase.getVideo(videoKey)).thenReturn(video);

        ResponseEntity<Video> response = videoController.getVideo(videoKey);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(video, response.getBody());
        verify(processVideoUseCase, times(1)).getVideo(videoKey);
    }

    @Test
    void testGetVideoNotFound() {
        String videoKey = "video.mp4";
        when(processVideoUseCase.getVideo(videoKey)).thenThrow(new RuntimeException("Vídeo não encontrado"));

        ResponseEntity<Video> response = videoController.getVideo(videoKey);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(processVideoUseCase, times(1)).getVideo(videoKey);
    }

}
