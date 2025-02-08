package com.fiap.video.infrastructure.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SnsPublishingExceptionTest {

    @Test
    void shouldRetainMessageAndCause() {
        // Simula uma causa para a exceção
        Throwable cause = new RuntimeException("Falha na comunicação com SNS");

        SnsPublishingException exception = new SnsPublishingException("Erro ao publicar mensagem no SNS", cause);

        // Verifica se a mensagem foi armazenada corretamente
        assertEquals("Erro ao publicar mensagem no SNS", exception.getMessage());

        // Verifica se a causa foi armazenada corretamente
        assertNotNull(exception.getCause());
        assertEquals(cause, exception.getCause());
    }
}
