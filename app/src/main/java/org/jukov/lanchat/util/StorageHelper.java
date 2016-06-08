package org.jukov.lanchat.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by jukov on 07.06.2016.
 */
public class StorageHelper {

    public static File getSdCardFolder(Context context) {
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                "Android" + File.separator +
                "data" + File.separator +
                context.getPackageName() + File.separator +
                "files" + File.separator +
                "profile_pictures");
    }

    public static void storeProfilePicture(Context context, Bitmap bitmap, String fileName) {
        try {
            File sdCardFile = getSdCardFolder(context);
            //noinspection ResultOfMethodCallIgnored
            sdCardFile.mkdirs();
            File imageFile = new File(sdCardFile, fileName);
            FileOutputStream fileOutputStream;

            fileOutputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);

            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap loadProfilePicture(Context context, String fileName) {
        if (checkFileExisting(context, fileName)) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                    "Android" + File.separator +
                    "data" + File.separator +
                    context.getPackageName() + File.separator +
                    "files" + File.separator +
                    "profile_pictures" + File.separator +
                    fileName, options);
        }
        return null;
    }

    public static boolean checkFileExisting(Context context, String fileName) {
        File pictureFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                "Android" + File.separator +
                "data" + File.separator +
                context.getPackageName() + File.separator +
                "files" + File.separator +
                "profile_pictures" + File.separator +
                fileName);
        return (pictureFile.exists() && !pictureFile.isDirectory());
    }
}
