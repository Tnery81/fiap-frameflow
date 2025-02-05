package com.fiap.video.core.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VideoMessageTest {

    private VideoMessage videoMessage;

    @BeforeEach
    void setUp() {
        videoMessage = new VideoMessage();
    }

    @Test
    void shouldReturnNullIdWhenNotSet() {
        assertNull(videoMessage.getId(), "The ID should be null when not set");
    }

    @Test
    void shouldReturnNullUserWhenNotSet() {
        assertNull(videoMessage.getUser(), "The user should be null when not set");
    }

    @Test
    void shouldReturnNullEmailWhenNotSet() {
        assertNull(videoMessage.getEmail(), "The email should be null when not set");
    }

    @Test
    void shouldReturnNullVideoKeyS3WhenNotSet() {
        assertNull(videoMessage.getVideoKeyS3(), "The videoKeyS3 should be null when not set");
    }

    @Test
    void shouldReturnNullIntervalSecondsWhenNotSet() {
        assertNull(videoMessage.getIntervalSeconds(), "The intervalSeconds should be null when not set");
    }
}
