package com.fiap.video.infrastructure.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VideoRetrievalExceptionTest {

    @Test
    void shouldRetainMessageAndCause() {
        Throwable cause = new RuntimeException("Causa original");
        String message = "Erro ao recuperar vídeo";

        VideoRetrievalException exception = new VideoRetrievalException(message, cause);

        assertThat(exception).hasMessage(message).hasCause(cause);
    }

    @Test
    void shouldThrowVideoRetrievalException() {
        Throwable cause = new IllegalStateException("Problema interno");

        assertThatThrownBy(() -> {
            throw new VideoRetrievalException("Falha ao recuperar o vídeo", cause);
        })
                .isInstanceOf(VideoRetrievalException.class)
                .hasMessage("Falha ao recuperar o vídeo")
                .hasCauseInstanceOf(IllegalStateException.class);
    }
}
