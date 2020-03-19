package com.twofuse.trover.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by dmoffett on 1/2/18.
 */

public class KeyManager {

    static private final String KEY_VALUE_FILE_NAME = "key_values.out";

    private Context context;
    static private KeyManager keyManager = null;

    static public KeyManager getKeyManager(Context context) {
        if (keyManager == null) {
            if (context == null)
                new RuntimeException("A context must be supplied before KeyManager can be used.");
            keyManager = new KeyManager(context);
        }
        return keyManager;
    }

    private KeyManager(Context context) {
        this.context = context;
    }

    public class KeyValue {
        public String key;
        public String value;
    }

    public KeyValue getNextKeyValue() {
        if (context != null) {
            RandomAccessFile infile = null;
            try {
                try {
                    FileChannel fileChannel = openKeyValueFile();
                    if(fileChannel != null){
                        long fileSize = fileChannel.size();
                        int recordSize = 0;
                        // First determine record size by finding a line terminating char
                        // Note record cannot be longer than 1k.
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        fileChannel.read(byteBuffer);
                        final byte bytes[] = byteBuffer.array();
                        if(bytes != null){
                            for(byte aByte : bytes){
                                if((aByte == '\n') || (aByte == '\r')){
                                    break;
                                } else recordSize++;
                            }
                            // Increment once more to account for record terminator.
                            recordSize++;
                            // Now that we have record size read the last record
                            // and truncate file to remove last record.
                            long position = fileSize - recordSize;
                            byteBuffer = ByteBuffer.allocate(recordSize);
                            int bytesRead = fileChannel.read(byteBuffer, position);
                            if(bytesRead == recordSize) {
                                String keyValuePair = new String(byteBuffer.array(), 0, recordSize - 1);
                                KeyValue keyValue = new KeyValue();
                                String keyValueStrs[] = keyValuePair.split(":");
                                keyValue.key = keyValueStrs[0];
                                keyValue.value = keyValueStrs[1];
                                fileChannel.truncate(fileSize - recordSize);
                                return keyValue;
                            } else {
                                TFLog.e("Can't read next key.");
                            }
                        } else {
                            TFLog.e("No keys left?  Or error reading key value file?");
                        }
                    }
                } finally {
                    if (infile != null)
                        infile.close();
                }
            } catch (Exception x) {
                TFLog.e("Error trying to access next key.");
            }
        }
        return null;
    }

    private boolean copyFileKeyValueFromAssets(){
        try {
            AssetManager assetMgr = context.getAssets();
            BufferedInputStream fileInputStream = new BufferedInputStream(assetMgr.open(KEY_VALUE_FILE_NAME));
            BufferedOutputStream outputStream = new BufferedOutputStream(context.openFileOutput(KEY_VALUE_FILE_NAME,
                    Context.MODE_APPEND |
                            Context.MODE_PRIVATE));
            try {
                byte buf[] = new byte[8092];
                int bytesRead = 0;
                while((bytesRead = fileInputStream.read(buf)) > 0){
                    outputStream.write(buf, 0, bytesRead);
                }
            } finally {
                fileInputStream.close();
                outputStream.flush();
                outputStream.close();
            }
            return true;
        } catch(Exception x){
            TFLog.e("Error trying to copy file. Error: " + x.getMessage());
        }
        return false;
    }

    private String[] getFileNamesForDir(File dataDir){
        String fileNames[] = dataDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.equals(KEY_VALUE_FILE_NAME) ? true : false;
            }
        });
        return fileNames;
    }

    private FileChannel openKeyValueFile() {
        try {
            File dataDir;
            dataDir = context.getFilesDir();
            boolean fileExist = false;
            String fileNames[] = getFileNamesForDir(dataDir);
            // File does not exist.  Copy file from assets.
            if(fileNames.length == 0){
                fileExist = copyFileKeyValueFromAssets();
            } else {
                fileExist = true;
            }
            if(fileExist){
                File outFile = new File(dataDir, KEY_VALUE_FILE_NAME);
                RandomAccessFile randomAccessFile = new RandomAccessFile(outFile, "rwd");
                return randomAccessFile.getChannel();
            }
        } catch (Exception x) {
            TFLog.d("Error trying to open key value file.");
        }
        return null;
    }

}
