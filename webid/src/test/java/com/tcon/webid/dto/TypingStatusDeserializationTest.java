package com.tcon.webid.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TypingStatusDeserializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void deserialize_boolean_isTyping() throws Exception {
        String json = "{\"senderId\":\"s1\",\"recipientId\":\"r1\",\"isTyping\":true}";
        TypingStatus t = mapper.readValue(json, TypingStatus.class);
        assertNotNull(t);
        assertTrue(Boolean.TRUE.equals(t.isTyping()));
    }

    @Test
    public void deserialize_boolean_typing_name() throws Exception {
        String json = "{\"senderId\":\"s1\",\"recipientId\":\"r1\",\"typing\":false}";
        TypingStatus t = mapper.readValue(json, TypingStatus.class);
        assertNotNull(t);
        assertTrue(Boolean.FALSE.equals(t.isTyping()));
    }

    @Test
    public void deserialize_string_values() throws Exception {
        String json1 = "{\"senderId\":\"s1\",\"recipientId\":\"r1\",\"isTyping\":\"true\"}";
        TypingStatus t1 = mapper.readValue(json1, TypingStatus.class);
        assertTrue(Boolean.TRUE.equals(t1.isTyping()));

        String json2 = "{\"senderId\":\"s1\",\"recipientId\":\"r1\",\"typing\":\"0\"}";
        TypingStatus t2 = mapper.readValue(json2, TypingStatus.class);
        assertTrue(Boolean.FALSE.equals(t2.isTyping()));
    }

    @Test
    public void deserialize_numeric_values() throws Exception {
        String json1 = "{\"senderId\":\"s1\",\"recipientId\":\"r1\",\"isTyping\":1}";
        TypingStatus t1 = mapper.readValue(json1, TypingStatus.class);
        assertTrue(Boolean.TRUE.equals(t1.isTyping()));

        String json2 = "{\"senderId\":\"s1\",\"recipientId\":\"r1\",\"typing\":0}";
        TypingStatus t2 = mapper.readValue(json2, TypingStatus.class);
        assertTrue(Boolean.FALSE.equals(t2.isTyping()));
    }
}

