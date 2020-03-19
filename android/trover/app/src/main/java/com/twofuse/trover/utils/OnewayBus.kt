package com.twofuse.trover.utils

import android.os.Handler
import android.os.Message
import java.util.*
import java.util.concurrent.ArrayBlockingQueue

class OnewayBus : Runnable {
    private var name: String? = null
    private val inputQueue: ArrayBlockingQueue<Any>
    private val endPointHandlers: HashMap<Thread, EndPointTracker>
    private val running = true
    var eventEnum: Enum<*>? = null

    interface EventFilter {
        /**
         * Filters events based on the return of filterEvent.  When true
         * is return the anEvent will not be shared with Endpoint.
         *
         * @param anEvent - The event to be considered for filtering.
         *
         * @return True if filtered and not shared with end point.  False otherwise.
         */
        fun filterEvent(anEvent: BusEvent?): Boolean
    }

    interface EndPoint {
        fun newEvent(anEvent: BusEvent)
    }

    private inner class MessagePayload {
        var payload: Any? = null
        var recipient: EndPointTracker? = null
    }

    private inner class EndPointTracker {
        var handler: EventHandler? = null
        var eventFilter: EventFilter? = null
        var endPoints: MutableList<EndPoint> = ArrayList()
    }

    private inner class EventHandler : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.obj != null) {
                val messagePayload = msg.obj as MessagePayload
                val endPointTracker = messagePayload.recipient
                for (endPoint in endPointTracker!!.endPoints) {
                    endPoint.newEvent(messagePayload.payload as BusEvent)
                }
            }
        }
    }

    override fun run() {
        if (name != null) Thread.currentThread().name = name else {
            Thread.currentThread().name = TAG + busCount++
        }
        while (running) {
            var event: BusEvent
            event = try {
                inputQueue.take() as BusEvent
            } catch (e: InterruptedException) {
                if (e.message != null) TFLog.e(e.message)
                TFLog.w("Interuppted exception")
                continue
            }
            sendEventToEndPoints(event)
        }
    }

    fun setName(name: String?) {
        this.name = name
    }

    /**
     * Add endpoint which is the on the same thread where the end point will receive
     * events.
     *
     * @param endPoint
     */
    @JvmOverloads
    fun addEndpoint(endPoint: EndPoint, filter: EventFilter? = null) {
        synchronized(endPointHandlers) {
            val thread = Thread.currentThread()
            if (endPointHandlers.containsKey(thread)) {
                val tracker = endPointHandlers[thread]
                tracker!!.endPoints.add(endPoint)
            } else {
                val endPointTracker = EndPointTracker()
                endPointTracker.endPoints.add(endPoint)
                // Create handler on this thread.
                endPointTracker.handler = EventHandler()
                endPointHandlers.put(thread, endPointTracker)
            }
        }
    }

    fun removeEndpoint(endPoint: EndPoint) {
        synchronized(endPointHandlers) {
            val thread = Thread.currentThread()
            val endPointTracker = endPointHandlers[thread]
            endPointTracker?.endPoints?.remove(endPoint)
        }
    }

    private fun sendEventToEndPoints(anEvent: BusEvent?) {
        for (endPointTracker in endPointHandlers.values) {
            if (endPointTracker.eventFilter != null) {
                if (endPointTracker.eventFilter!!.filterEvent(anEvent)) continue
            }
            val msg = Message()
            val messagePayload = MessagePayload()
            messagePayload.payload = anEvent
            messagePayload.recipient = endPointTracker
            msg.obj = messagePayload
            endPointTracker.handler!!.sendMessage(msg)
        }
    }

    fun addEventToBus(obj: Any) {
        val added = inputQueue.offer(obj)
        if (!added) TFLog.w(String.format("Queue full.  Could not add event: %s", obj.javaClass.simpleName))
    }

    companion object {
        private const val TAG = "OnewayBus"
        private var busCount = 0
        private const val DEFAULT_Q_SIZE = 30
    }

    init {
        inputQueue = ArrayBlockingQueue(DEFAULT_Q_SIZE)
        endPointHandlers = HashMap()
        val thread = Thread(this)
        thread.start()
    }
}