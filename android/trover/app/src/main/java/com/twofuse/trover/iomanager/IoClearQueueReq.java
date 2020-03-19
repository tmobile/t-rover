package com.twofuse.trover.iomanager;


import com.twofuse.io.iomanager.IOMgrRequest;

public class IoClearQueueReq extends IOMgrRequest {

    private static final boolean DEBUG = true;
    private static final String TAG = "IoClearQueueReq";


    public IoClearQueueReq(){
    }

    @Override
    public void executeRequest() {
        super.executeRequest();
    }

    @Override
    public void interrupt() {
        // No interruptions for now.
    }

    @Override
    public boolean isInterrupted() {
        return false;
    }

}
