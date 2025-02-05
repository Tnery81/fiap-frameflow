package com.fiap.video.core.domain;
import com.fiap.video.core.domain.SnsMessageWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


class SnsMessageWrapperTest {

    private SnsMessageWrapper snsMessageWrapper;

    @BeforeEach
    void setUp() {
        snsMessageWrapper = new SnsMessageWrapper();
    }

    @Test
    void shouldSetMessageCorrectly() {
        String expectedMessage = "Test Message";
        snsMessageWrapper.setMessage(expectedMessage);

        assertEquals("Message should be set correctly", expectedMessage,   snsMessageWrapper.getMessage());
    }

    @Test
    void shouldReturnNullWhenMessageNotSet() {
        assertNull( "Message should be null when not set",snsMessageWrapper.getMessage());
    }

    @Test
    void shouldOverwritePreviousMessage() {
        snsMessageWrapper.setMessage("First Message");
        snsMessageWrapper.setMessage("Second Message");

        assertEquals("Message should be overwritten with the new value","Second Message",  snsMessageWrapper.getMessage());
    }

    @Test
    void shouldHandleEmptyMessage() {
        snsMessageWrapper.setMessage("");

        assertEquals("Message should handle empty string correctly","",   snsMessageWrapper.getMessage());
    }

    @Test
    void shouldHandleNullMessage() {
        snsMessageWrapper.setMessage(null);
        assertNull("Message should be null when set to null", snsMessageWrapper.getMessage());
    }
}
