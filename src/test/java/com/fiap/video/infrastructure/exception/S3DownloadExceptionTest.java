package com.fiap.video.infrastructure.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class S3DownloadExceptionTest {

    @Test
    void testExceptionMessageAndCause() {
        Exception cause = new RuntimeException("Falha na conexão com o S3");
        S3DownloadException exception = new S3DownloadException("Erro ao baixar vídeo do S3", cause);

        assertEquals("Erro ao baixar vídeo do S3", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}

