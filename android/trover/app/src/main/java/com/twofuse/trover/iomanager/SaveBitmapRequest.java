package com.twofuse.trover.iomanager;

import android.graphics.Bitmap;

import com.twofuse.io.iomanager.IOMgrRequest;
import com.twofuse.trover.utils.FileUtils;

import java.io.File;

/**
 * Created by dmoffett on 12/10/17.
 */

public class SaveBitmapRequest extends IOMgrRequest {

    private Bitmap bitmap;
    private File file;

    public SaveBitmapRequest(Bitmap bitmap, File file){
        setRequiresNetwork(false);
        this.bitmap = bitmap;
        this.file = file;
    }

    @Override
    public void interrupt() {
    }

    @Override
    public boolean isInterrupted() {
        return false;
    }

    public void executeRequest(){
        super.executeRequest();
        if(file != null && bitmap != null){
            if(FileUtils.saveBitmapToFileNamed(bitmap, file)){
                setRequestStatusCode(RequestStatusCode.OK);
            } else {
                setRequestStatusCode(RequestStatusCode.PERMANT_FAILURE);
            }
        }
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public void nullBitmap(){
        bitmap = null;
    }

    public File getFile(){
        return file;
    }
}
