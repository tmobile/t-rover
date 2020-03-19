package com.twofuse.trover.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class FileUtils {
    private static final boolean DEBUG = false;


    static public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    static public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    static public File getDynamicPreloadFilesDir(Context context) {
        File path;
        if (isExternalStorageReadable() && isExternalStorageWritable()) {
            path = context.getExternalFilesDir(null);
            if (path == null || !path.exists()) {
                path = context.getFilesDir();
            }
        } else
            path = context.getFilesDir();
        if (DEBUG) {
            try {
                TFLog.d("Storing files in: " + path.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return path;
    }

    static private void privateChmodDirStructure(File file) {
        if (file == null)
            return;
        privateChmodDirStructure(file.getParentFile());
        file.setExecutable(true, false);
    }

    static public void chmodDirStructure(File file) {
        if (file != null) {
            file.setReadable(true, false);
            privateChmodDirStructure(file);
        }
    }

    static public void removeDir(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        removeDir(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
            dir.delete();
        }
    }

    static public boolean saveBitmapToFileNamed(Bitmap bitmap, File filename) {
        FileOutputStream out = null;
        try {
            // TODO Remove world readable used for testing.
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return true;
        } catch (Exception e) {
            if(e.getMessage() != null)
                TFLog.e(e.getMessage());
            else
                TFLog.e("Error saving bitmap.");
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return false;
    }

}
