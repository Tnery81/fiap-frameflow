package com.fiap.video.infrastructure.memory;

import com.fiap.video.core.domain.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryVideoRepositoryTest {

    private InMemoryVideoRepository videoRepository;

    @BeforeEach
    void setUp() {
        videoRepository = new InMemoryVideoRepository();
    }

    @Test
    void testSave_shouldStoreVideo() {
        // Arrange
        Video video = new Video("/path/to/video.mp4", Duration.ofSeconds(120));

        // Act
        videoRepository.save(video);

        // Assert
        assertEquals(video, videoRepository.findByPath(video.getPath()));
    }

    @Test
    void testGetVideo_shouldReturnNullWhenNotFound() {
        // Act
        Video result = videoRepository.findByPath("/invalid/path.mp4");

        // Assert
        assertNull(result);
    }
}