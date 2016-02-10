package org.jukov.lanchat.util;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.jukov.lanchat.dto.MessageDTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by jukov on 10.02.2016.
 */
public class JSONConverter {

    public static final String TAG = "LC_JSON";

    public static String toJSON(MessageDTO message) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        mapper.writeValue(byteArrayOutputStream, message);
        String returnString = byteArrayOutputStream.toString();
        Log.d(TAG, returnString);
        return returnString;
    }

    public static MessageDTO toJavaObject(String json) throws IOException {
        Log.d(TAG, json);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, MessageDTO.class);
    }

}
