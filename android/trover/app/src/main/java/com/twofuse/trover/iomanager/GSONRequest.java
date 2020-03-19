package com.twofuse.trover.iomanager;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.twofuse.trover.utils.TFLog;


import java.net.MalformedURLException;
import java.net.URL;

public class GSONRequest extends NetworkRequest {


    private JsonElement jsonElement = null;

    public GSONRequest(String urlStr) throws MalformedURLException {
        super(urlStr);
    }

    public GSONRequest(URL aURL) {
        super(aURL);
    }

    public void executeRequest() {
        // Add headers here.

        super.executeRequest();
        if(httpReturnCode == HTTP_REQ_OK){
            try {
                JsonParser jsonParser = new JsonParser();
                jsonElement = jsonParser.parse(httpReturnBody.toString());
                setRequestStatusCode(NetworkRequest.RequestStatusCode.OK);
            } catch(Exception x){
                TFLog.e("Error parsing json from server for url. " + url.toString());
            }
        } else {
            TFLog.e("Could not retrieve JSON for request: " + url.toString());
        }
    }

    public JsonElement getJsonElement() {
        return jsonElement;
    }

}
