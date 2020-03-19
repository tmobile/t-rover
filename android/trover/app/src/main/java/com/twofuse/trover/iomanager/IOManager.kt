package com.twofuse.io.iomanager


import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import android.util.Log
import com.twofuse.trover.iomanager.IoClearQueueReq
import com.twofuse.trover.utils.TFLog
import java.util.concurrent.ArrayBlockingQueue

class IOManager {

    enum class ExecutionState {
        UNDEFINED, IN_QUEUE, EXECUTING, ERROR, FINISHED, ERROR_FINISHED
    }

    private var ioWorkers: ArrayList<IOWorker>? = null
    private var ioHandler: IOHandler? = null
    private var delayHandler: DelayHandler? = DelayHandler()

    val ioManagerHandler: Handler?
        get() = ioHandler

    private fun init(ioWorkersSize: Int) {
        ioHandler = IOHandler()
        ioWorkers = ArrayList(ioWorkersSize)
        for(index in 0..ioWorkersSize-1) {
            ioWorkers!!.add(IOWorker("IOWorker - " + index))
            ioWorkers!![index].start()
        }
        delayHandler = DelayHandler()
    }

    constructor() {
        init(DEFAULT_IO_WORKERS)
    }


    /**
     * Constructor that specifies number of workers, Threads, created and used by
     * IOManager.  More threads does not necessarily mean faster processing.
     *
     * @param numberOfWorkers - number or workers or threads used to process reqquests.
     */
    constructor(numberOfWorkers: Int) {
        init(numberOfWorkers)
    }

    fun addRequest(io: IOMgrRequest) {
        try {
            if (!queue.offer(io)) {
                Log.e(TAG, "Download queue full.")
            } else {
                io.executionState = ExecutionState.IN_QUEUE
            }
        } catch (x: IllegalStateException) {
            // Ignore if the log queue is full then drop the message.
            Log.e(TAG, "Exception trying to download files.  System must be to busy.  Check your application.")
        }

    }

    fun clearJobs() {
        addRequest(IoClearQueueReq())
    }

    fun interrupt() {
        clearJobs()
        var index: Int
        val count = ioWorkers!!.size
        index = 0
        while (index < count) {
            ioWorkers!![index].interrupt()
            index++
        }
    }

    private inner class DelayHandler : Handler() {

        override fun handleMessage(msg: Message) {
            // Add request back to the queue.
            queue.add(msg.obj as IOMgrRequest)
            if (DEBUG)
                TFLog.d(String.format("Request added back to queue: %d", (msg.obj as IOMgrRequest).requestId))
        }

    }


    /*
    This handler puts the request back on the main thread after all IO is done.
     */
    private class IOHandler : android.os.Handler() {



        override fun handleMessage(msg: Message) {
            if (DEBUG)
                TFLog.d("IOManager handle msg")
            val req = msg.obj as IOMgrRequest
            if (req != null && req.callback != null) {
                req.callback?.ioRequestFinished(req)
            }
        }
    }


    private inner class IOWorker(protected var threadName: String) : Thread() {

        protected var terminate = false
        private var currentJob: IOMgrRequest? = null

        override fun interrupt() {
            if (currentJob != null)
                currentJob!!.interrupt()
        }

        fun terminate() {
            terminate = true
        }


        override fun run() {
            val count = 0
            Thread.currentThread().name = threadName
            Looper.prepare()

            while (!terminate) {
                var io: IOMgrRequest? = null
                try {
                    io = queue.take()
                    io!!.executionState = ExecutionState.EXECUTING
                    currentJob = io
                    val startTime = SystemClock.elapsedRealtime()
                    val endTime: Long
                    if (io is IoClearQueueReq) {
                        if (DEBUG)
                            TFLog.d(TAG, "Terminating IOManager Queue")
                        queue.clear()
                        break
                    }

                    if (DEBUG)
                        TFLog.d(TAG, String.format("%s Take from Queue: %s", threadName, io.javaClass.name))

                    if (io.requiresNetwork()) {
                        io.executionState = ExecutionState.EXECUTING
                        io.executeRequest()
                    } else {
                        if (!io.requiresNetwork()) {
                            io.executeRequest()
                        } else {
                            io.requestStatusCode = IOMgrRequest.RequestStatusCode.FAILED
                        }
                    }

                    if (io.requestStatusCode != IOMgrRequest.RequestStatusCode.OK && io.backoff != null) {
                        if(io.requestStatusCode != IOMgrRequest.RequestStatusCode.PERMANT_FAILURE) {
                            val backoff = io.backoff
                            backoff!!.decrementRetries()
                            if (!backoff.retriesDone()) {
                                val msg = Message()
                                msg.obj = io
                                val delay = backoff.nextBackoffInMills()
                                val delayAdded:Boolean = delayHandler?.sendMessageDelayed(msg, delay) ?: false
                                if(DEBUG && delayAdded)
                                    TFLog.d("Requested added for delay of " + delay)
                                else if(!delayAdded)
                                    TFLog.w("Requested not getting added for delay")
                                continue
                            }
                        } else {
                            io.executionState = ExecutionState.ERROR_FINISHED
                        }
                    } else {
                        io.executionState = ExecutionState.FINISHED
                    }

                    endTime = SystemClock.elapsedRealtime()
                    io.requestDuration = endTime - startTime
                    val msg = ioHandler!!.obtainMessage(IO_MGR_REQUEST, io)
                    ioHandler!!.sendMessage(msg)

                    if (DEBUG)
                        TFLog.d(TAG, "Request Done: " + io.javaClass.name)

                } catch (x: Exception) {
                    if (io != null)
                        TFLog.e(TAG, String.format("Exception processing Request."))
                    else
                        TFLog.e(TAG, "IORequest is null.  Really bad situation!!!!!!!!!!!!!!!!!")
                    x.printStackTrace()

                    io?.executionState = ExecutionState.ERROR_FINISHED
                    val msg = ioHandler!!.obtainMessage(IO_MGR_REQUEST, io)
                    ioHandler!!.sendMessage(msg)
                }

            }
        }
    }

    companion object {

        private val MAX_DOWNLOAD_Q_SIZE = 300
        val IO_MGR_REQUEST = 2300
        private val TAG = "IOManager"
        private val DEBUG = true

        private val DEFAULT_IO_WORKERS = 3

        protected var queue = ArrayBlockingQueue<IOMgrRequest>(MAX_DOWNLOAD_Q_SIZE)

        fun checkCallback(`object`: Any, req: IOMgrRequest): Boolean {
            return true
        }


    }

}
