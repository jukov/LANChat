package org.jukov.lanchat.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by jukov on 06.06.2016.
 */
public class Base64Converter {

    public static String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteBitmap = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteBitmap, Base64.DEFAULT);
    }

    public static Bitmap getBitmapFromString(String encodedBitmap) {
        byte[] decodedString = Base64.decode(encodedBitmap, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

}
