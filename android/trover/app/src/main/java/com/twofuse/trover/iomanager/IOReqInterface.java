package com.twofuse.trover.iomanager;


import com.twofuse.io.iomanager.IOMgrRequest;

/**
 */
public interface IOReqInterface {

    /**
     * This method will be executed in a thread created by IOManager.
     *
     * Classes should implement this method to have work done on a
     * background non UI thread.  Execute request is responsible for
     * doing all the work needed.  Some helper classes are provided
     * for doing network requests.  For example NetworkRequest.
     *
     */
    public void executeRequest();
    public IOManagerCallback getCallback();
    public void setCallback(IOManagerCallback callback);
    public IOMgrRequest.RequestStatusCode getRequestStatusCode();
    public void setRequestStatusCode(IOMgrRequest.RequestStatusCode requestStatusCode);
    public boolean requiresNetwork();
    public void setRequiresNetwork(boolean requiresNetwork);
    public void interrupt();
    public boolean isInterrupted();
    public int getRequestId();
    public long getRequestDuration();
    public void setRequestDuration(long aValue);

}
