package com.twofuse.trover;

import android.app.Application;

import com.twofuse.io.iomanager.IOManager;
import com.twofuse.trover.motion.MotionDetector;
import com.twofuse.trover.utils.OnewayBus;

/**
 * Created by dmoffett on 12/5/17.
 */

public class TRoverImageCapture extends Application {

    private static MotionDetector motionDetector;
    private static OnewayBus eventBus = new OnewayBus();
    private static IOManager ioManager = new IOManager(3);

    @Override
    public void onCreate(){
        super.onCreate();
        motionDetector = MotionDetector.getMotionDetector(this);
    }

    @Override
    public void onTerminate(){
        // Terminate listening to motion changes.
        motionDetector.unregister();
        motionDetector = null;
        super.onTerminate();
    }

    public static OnewayBus getEventBus(){
        return eventBus;
    }

    public static IOManager getIoManager(){
        return ioManager;
    }
}
