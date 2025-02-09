package com.fiap.video.core.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class VideoTest {

    private Video video;

    @BeforeEach
    void setUp() {
        video = new Video("/path/to/video.mp4", Duration.ofMinutes(5));
    }

    @Test
    void shouldReturnCorrectPath() {
        String expectedPath = "/path/to/video.mp4";

        assertEquals(expectedPath, video.getPath(), "The path should match the initialized value");
    }

    @Test
    void shouldReturnCorrectDuration() {
        Duration expectedDuration = Duration.ofMinutes(5);

        assertEquals(expectedDuration, video.getDuration(), "The duration should match the initialized value");
    }

    @Test
    void shouldHandleEmptyPath() {
        Video emptyPathVideo = new Video("", Duration.ofMinutes(3));

        assertEquals("", emptyPathVideo.getPath(), "The path should handle empty string correctly");
    }

    @Test
    void shouldHandleNullPath() {
        Video nullPathVideo = new Video(null, Duration.ofMinutes(3));

        assertNull(nullPathVideo.getPath(), "The path should be null when set to null");
    }

    @Test
    void shouldHandleZeroDuration() {
        Video zeroDurationVideo = new Video("/path/to/video.mp4", Duration.ZERO);

        assertEquals(Duration.ZERO, zeroDurationVideo.getDuration(), "The duration should be zero when initialized as such");
    }

    @Test
    void shouldHandleNullDuration() {
        Video nullDurationVideo = new Video("/path/to/video.mp4", null);

        assertNull(nullDurationVideo.getDuration(), "The duration should be null when set to null");
    }
}
