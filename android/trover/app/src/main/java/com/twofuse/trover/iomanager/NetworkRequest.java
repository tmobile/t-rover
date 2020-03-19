package com.twofuse.trover.iomanager;




import com.twofuse.io.iomanager.IOManager;
import com.twofuse.io.iomanager.IOMgrRequest;
import com.twofuse.trover.utils.TFLog;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;


public class NetworkRequest extends IOMgrRequest {

    static private boolean DEBUG = true;
    static private int BUF_SIZE = 8192;
    static private int DEFAULT_TIMEOUT = 1000;

    static public int HTTP_REQ_OK = 200;

    protected URL url;
    private int timeout = DEFAULT_TIMEOUT;
    protected int httpReturnCode;
    protected StringBuffer httpReturnBody;

    public NetworkRequest(String urlStr) throws MalformedURLException {
        url = new URL(urlStr);
    }

    public NetworkRequest(URL aURL) {
        url = aURL;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void interrupt() {
    }

    @Override
    public boolean isInterrupted() {
        return false;
    }

    protected void prepare(HttpURLConnection urlConnection) {
        urlConnection.setConnectTimeout(timeout);
        urlConnection.setReadTimeout(timeout);
        urlConnection.setInstanceFollowRedirects(true);
    }

    private StringBuffer read(InputStream is) throws IOException {
        char[] buffer = new char[BUF_SIZE];
        StringBuffer buf = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            int count;
            while ((count = reader.read(buffer, 0, buffer.length)) != -1) {
                buf.append(buffer, 0, count);
            }
            return buf;
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                TFLog.d("Error occured when calling stream close");
            }
        }
    }

    public String getUrlString(){
        return url.toString();
    }

    public URL getUrl(){
        return url;
    }

    public void executeRequest() {
        setExecutionState(IOManager.ExecutionState.EXECUTING);
        HttpURLConnection urlConnection = null;
        byte[] responseContents;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            prepare(urlConnection);
            try {
                httpReturnCode = urlConnection.getResponseCode();
                if (DEBUG) TFLog.d("httpReturnCode = " + httpReturnCode);
                if(httpReturnCode == HTTP_REQ_OK) {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    httpReturnBody = read(in);
                    setRequestStatusCode(RequestStatusCode.OK);
                    setExecutionState(IOManager.ExecutionState.FINISHED);
                } else {
                    switch(httpReturnCode) {
                        case HttpURLConnection.HTTP_BAD_REQUEST:
                            setRequestStatusCode(RequestStatusCode.PERMANT_FAILURE);
                            setExecutionState(IOManager.ExecutionState.ERROR);
                            break;
                    }
                }
            } finally {
                urlConnection.disconnect();
            }
        } catch (SocketTimeoutException e) {
            TFLog.e("Socket Timeout.");
            setRequestStatusCode(RequestStatusCode.FAILED);
            setExecutionState(IOManager.ExecutionState.ERROR);
        } catch (IOException e) {
            TFLog.e("IOException.");
            setRequestStatusCode(RequestStatusCode.FAILED);
            setExecutionState(IOManager.ExecutionState.ERROR);
        }
    }
}
