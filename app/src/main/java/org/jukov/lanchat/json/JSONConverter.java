package org.jukov.lanchat.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.jukov.lanchat.dto.Data;

import java.io.IOException;

/**
 * Created by jukov on 10.02.2016.
 */
public class JSONConverter {

    public static ObjectMapper objectMapper = new ObjectMapper();

    public static String toJSON(Data message) throws IOException {
        return objectMapper.writeValueAsString(message);
    }

    public static Data toJavaObject(String json) throws IOException {
        return objectMapper.readValue(json, Data.class);
    }

}
