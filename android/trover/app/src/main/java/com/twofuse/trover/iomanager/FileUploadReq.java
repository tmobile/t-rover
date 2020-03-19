package com.twofuse.trover.iomanager;



import com.twofuse.io.iomanager.IOManager;
import com.twofuse.trover.utils.TFLog;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by dmoffett on 1/7/18.
 */

public class FileUploadReq extends NetworkRequest {

    private static final boolean DEBUG = true;
    private static final int BUF_SIZE = 65536;

    private File file = null;
    private String contentType = "image/jpeg";

    public FileUploadReq(URL url, File file, String contentType) {
        super(url);
        this.file = file;
        this.contentType = contentType;
    }

    public FileUploadReq(String urlStr, File file, String contentType) throws MalformedURLException {
        super(urlStr);
        this.file = file;
        this.contentType = contentType;
    }

    public void executeRequest(){

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image_file", file.getName(),
                        RequestBody.create(MediaType.parse(contentType), file))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        Response response = null;

        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response == null || !response.isSuccessful()) {
            TFLog.w("Example", "Unable to upload to server.");
            setRequestStatusCode(RequestStatusCode.OK);
            setExecutionState(IOManager.ExecutionState.FINISHED);
        } else {
            TFLog.d("Example", "Upload was successful.");
            setRequestStatusCode(RequestStatusCode.PERMANT_FAILURE);
            setExecutionState(IOManager.ExecutionState.ERROR);
        }
    }

}
