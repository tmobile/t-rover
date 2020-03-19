package com.twofuse.io.iomanager

import com.twofuse.trover.iomanager.Backoff
import com.twofuse.trover.iomanager.IOManagerCallback
import com.twofuse.trover.iomanager.IOReqInterface


/**
 */
abstract class IOMgrRequest : IOReqInterface {

    private var callback: IOManagerCallback? = null
    /**
     * Return an instance of Backoff used for this request.
     *
     * @return An instance of Backoff or null if not set.
     */
    var backoff: Backoff? = null


    private var requestId = 0
    private var requestDuration: Long = 0
    private var requestStatusCode = RequestStatusCode.UNDEFINED
    /**
     * Contains the state of the Request through IOManager.  For example
     * the state can be EXECUITNG which means IOManager is currently working
     * on the request.
     *
     * @return Current state. INTERUPTED, IN_QUEUE, EXECUTING, ERROR, FINISHED
     */
    @get:Synchronized
    @set:Synchronized
    var executionState = IOManager.ExecutionState.UNDEFINED
    private var requiresNetwork = true

    enum class RequestStatusCode {
        UNDEFINED, OK, FAILED, PERMANT_FAILURE
    }

    override fun getCallback(): IOManagerCallback? {
        return callback
    }

    override fun setCallback(callback: IOManagerCallback) {
        this.callback = callback
    }

    override fun getRequestStatusCode(): RequestStatusCode {
        return requestStatusCode
    }

    override fun setRequestStatusCode(requestStatusCode: RequestStatusCode) {
        this.requestStatusCode = requestStatusCode
    }

    override fun executeRequest() {
        executionState = IOManager.ExecutionState.EXECUTING
    }

    override fun requiresNetwork(): Boolean {
        return requiresNetwork
    }

    override fun setRequiresNetwork(requiresNetwork: Boolean) {
        this.requiresNetwork = requiresNetwork
    }

    override fun getRequestId(): Int {
        return requestId
    }

    fun setRequestId(requestId: Int) {
        this.requestId = requestId
    }


    abstract override fun interrupt()
    abstract override fun isInterrupted(): Boolean

    /**
     * Once request has finished requestDuraction will contain the
     * duration of request in milli seconds.
     *
     * @return  duration of request in milliseconds.
     */
    override fun getRequestDuration(): Long {
        return requestDuration
    }

    override fun setRequestDuration(requestDuration: Long) {
        this.requestDuration = requestDuration
    }

    companion object {

        private val TAG = "IOMgrRequest"
    }


}
