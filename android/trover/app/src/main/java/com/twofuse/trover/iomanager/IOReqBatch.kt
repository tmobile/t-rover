package com.twofuse.io.iomanager


import android.util.Log
import com.twofuse.trover.iomanager.IOReqInterface
import java.util.*

/**
 */
class IOReqBatch : IOMgrRequest() {
    protected var terminateApiRequest = false
    var requests = ArrayList<IOMgrRequest>()
        protected set

    fun addRequest(req: IOMgrRequest) {
        requests.add(req)
    }


    override fun executeRequest() {
        var req: IOReqInterface? = null
        try {
            var index: Int
            val count = requests.size
            index = 0
            while (index < count) {
                req = requests[index]
                req.executeRequest()
                if (req.requestStatusCode !== IOMgrRequest.RequestStatusCode.OK) {
                    req.requestStatusCode = IOMgrRequest.RequestStatusCode.FAILED
                }
                index++
            }
        } catch (x: Exception) {
            Log.e(TAG, "Error processing request.", x)
            if (req != null)
                req.requestStatusCode = IOMgrRequest.RequestStatusCode.FAILED
        }

    }

    override fun interrupt() {
        var req: IOMgrRequest
        terminateApiRequest = true
        var index: Int
        val count = requests.size
        index = 0
        while (index < count) {
            req = requests[index]
            req.interrupt()
            index++
        }
    }

    override fun isInterrupted(): Boolean {
        return terminateApiRequest
    }

    companion object {
        private val TAG = "IOReqBatch"
    }

}
