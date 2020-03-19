package com.twofuse.trover.motion;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.twofuse.trover.TRoverImageCapture;
import com.twofuse.trover.utils.MulticastPublisher;
import com.twofuse.trover.utils.TFLog;

/**
 * Created by dmoffett on 12/3/17.
 */

public class MotionDetector implements SensorEventListener {

    static private MotionDetector motionDetector = null;

    private SensorManager sensorManager;
    private Sensor sensor;
    private float lastValue = 0;

    private MulticastPublisher multicastPublisher = new MulticastPublisher();


    static public MotionDetector getMotionDetector(Context context){
        if(motionDetector == null) {
            motionDetector = new MotionDetector(context);
        }
        return motionDetector;
    }

    private MotionDetector(Context context){
        sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if(sensor != null){
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    public void unregister(){
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent != null){
            float newValue = (float) 0.0, difference;
            RatMotionEvent ratMotionEvent;
            for(float aVal : sensorEvent.values){
                newValue += Math.abs(aVal);
            }
            difference = Math.abs(Math.abs(newValue) - Math.abs(lastValue));
            lastValue = newValue;
            if(difference > .1){
                ratMotionEvent = new RatMotionEvent(RatMotionEvent.MotionType.MotionDetected);
                // multicastPublisher.multicast("LG in motion: " + difference);
            } else {
                ratMotionEvent = new RatMotionEvent(RatMotionEvent.MotionType.Still);
            }
            if(ratMotionEvent != null) {
                TRoverImageCapture.getEventBus().addEventToBus(ratMotionEvent);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        TFLog.d("AccuracyChanged");
    }
}
