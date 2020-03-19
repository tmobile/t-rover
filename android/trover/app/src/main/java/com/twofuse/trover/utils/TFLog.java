
package com.twofuse.trover.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

public class TFLog {

    private static final String TAG = "TFLog";  // Only used when system is to busy to archive logs.

	private static final int PREFACE_LENGTH = 60;
	private static final String SPACE_STR = "                                                              ";
	private static final String TIME_FORMAT = "dd hh:mm:ss:SSSS";
	private static final SimpleDateFormat dateFormater = new SimpleDateFormat(TIME_FORMAT);
	

	private static String getLogPreface(){
		String str;
		try {
			int stackIndex = 4;
			StackTraceElement[] stackElement = Thread.currentThread().getStackTrace();
			String className = stackElement[stackIndex].getClassName();
			int index = className.lastIndexOf('.');
			long now = System.currentTimeMillis();
			if (index != -1)
				className = className.substring(index + 1);
			str = String.format("2Fuse-> %s.%s:%d - %s >", className, stackElement[stackIndex].getMethodName(), stackElement[stackIndex].getLineNumber(), dateFormater.format(now));
			if (str.length() < PREFACE_LENGTH)
				str += SPACE_STR.substring(0, PREFACE_LENGTH - str.length());

		} catch (Exception e){
			return "";
		}
		return str;
	}
	

	public static void e(String str){
		if(!TextUtils.isEmpty(str))
			Log.e(getLogPreface(), str);
		else
			Log.e(getLogPreface(), "Empty Log Message.");
	}

	public static void w(String str){
		if(!TextUtils.isEmpty(str))
			Log.w(getLogPreface(), str);
		else
			Log.e(getLogPreface(), "Empty Log Message.");
	}

	public static void i(String str){
		if(!TextUtils.isEmpty(str))
			Log.i(getLogPreface(), str);
		else
			Log.e(getLogPreface(), "Empty Log Message.");
	}

	public static void d(String str){
		if(!TextUtils.isEmpty(str))
			Log.d(getLogPreface(), str);
		else
			Log.e(getLogPreface(), "Empty Log Message.");
	}

	public static void e(String tag, String str){
		if(!TextUtils.isEmpty(str))
			Log.e(getLogPreface(), str);
		else
			Log.e(getLogPreface(), "Empty Log Message.");
	}

	public static void w(String tag, String str){
		if(!TextUtils.isEmpty(str))
			Log.w(getLogPreface(), str);
		else
			Log.e(getLogPreface(), "Empty Log Message.");
	}

	public static void i(String tag, String str){
		if(!TextUtils.isEmpty(str))
			Log.i(getLogPreface(), str);
		else
			Log.e(getLogPreface(), "Empty Log Message.");
	}

	public static void d(String tag, String str){
		if(!TextUtils.isEmpty(str))
			Log.d(getLogPreface(), str);
		else
			Log.e(getLogPreface(), "Empty Log Message.");
	}

    public static void e(String tag, Exception x) {
        StringWriter strWriter = new StringWriter();
        PrintWriter sw = new PrintWriter(strWriter, true);
        x.printStackTrace(sw);
        Log.e(getLogPreface(), tag + "\n" + strWriter.getBuffer().toString());
    }

}
