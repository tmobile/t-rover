package com.twofuse.trover.motion;

import com.twofuse.trover.utils.BusEvent;

/**
 * Created by dmoffett on 12/8/17.
 */

public class RatMotionEvent extends BusEvent {

    public enum MotionType {
        MotionDetected, Still
    }

    private MotionType motionType;
    private long time;

    public RatMotionEvent(MotionType type){
        motionType = type;
    }

    public MotionType getMotionType() {
        return motionType;
    }

    public void setMotionType(MotionType motionType) {
        this.motionType = motionType;
    }

}
