package com.twofuse.trover.iomanager;


import java.util.Random;

public class Backoff {
	
	public final int DEFAULT_RETRIES = 3;
	public final double DEFAULT_PERCENT_INCREASE = 1.5d;
	public final long MAX_BACKOFF_WAIT = 30 * 60 * 1000;  // 30 minutes
	public final long MIN_BACKOFF_WAIT = 3 * 1000;  // 3 seconds

	private int retries;
	private double backoffPercentIncrease;
	private double randomInterval;
	private long lastBackOffTime = MIN_BACKOFF_WAIT;
	
	
	public Backoff() {
		retries = DEFAULT_RETRIES;
		backoffPercentIncrease = DEFAULT_PERCENT_INCREASE;
		randomInterval = randomizeInterval();
	}
	
	public Backoff(int maxRetries, double backoffPercentIncrease) {
		this.retries = maxRetries;
		this.backoffPercentIncrease = backoffPercentIncrease;
		randomInterval = randomizeInterval();
	}
	
	/**
	 * Return a value between 0 and .25.
	 *
	 * @return double value less than .25 and greater than or equal to 0
	 */
	static double randomizeInterval() {
		final Random random = new Random(System.currentTimeMillis());
		double interval;
		while ((interval = random.nextDouble()) > .005);
		return interval;
	}
	
	public long nextBackoffInMills() {
		double nextBackoffTime = lastBackOffTime + lastBackOffTime * backoffPercentIncrease;
		lastBackOffTime = (long) ((nextBackoffTime * randomInterval) + lastBackOffTime * (1 + backoffPercentIncrease));
		if (lastBackOffTime > MAX_BACKOFF_WAIT) { lastBackOffTime = MAX_BACKOFF_WAIT; }
		return lastBackOffTime;
	}
	
	public int getRetries() {
		return retries;
	}

	public void decrementRetries(){
		retries--;
	}

	public boolean retriesDone(){
		return retries > 0 ? false:true;
	}
	
	public long getMAX_BACKOFF_WAIT() {
		return MAX_BACKOFF_WAIT;
	}
	
}
