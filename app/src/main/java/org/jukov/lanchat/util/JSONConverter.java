package org.jukov.lanchat.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jukov.lanchat.dto.Data;
import org.jukov.lanchat.dto.MessagingData;

import java.io.IOException;
import java.util.Queue;

/**
 * Created by jukov on 10.02.2016.
 */
public class JSONConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJSON(Data message) throws IOException {
        return objectMapper.writeValueAsString(message);
    }

    public static String toJSON(Queue<?> queue) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writerFor(new TypeReference<Queue<MessagingData>>() {
        }).writeValueAsString(queue);
    }

    public static Object toPOJO(String json) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(json);
        if (jsonNode.isArray()) {
            return objectMapper.convertValue(jsonNode, new TypeReference<Queue<MessagingData>>() {});
        }
        String type = jsonNode.get("type").asText();
        Class<?> clazz;
        try {
            clazz = Class.forName(type);
            return objectMapper.convertValue(jsonNode, clazz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
