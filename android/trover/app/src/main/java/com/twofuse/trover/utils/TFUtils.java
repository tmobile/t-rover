package com.twofuse.trover.utils;

import android.graphics.Bitmap;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Random;

/**
 * Created by dmoffett on 10/24/16.
 */

public class TFUtils {

    public static Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    static final Random rand = new Random();

    private static char randomChar(){
        long value;
        char rChar;
        long mask = 0xFF;
        while( true ){
            value = rand.nextLong();
            rChar = ( char )(value & mask);
            if(( rChar >= 'a' && rChar <= 'z' ) || ( rChar >= 'A' && rChar <= 'Z' )
                    || ( rChar >= '0' && rChar <= '9' ))
                break;
        }
        return rChar;
    }

    /**
     * Creates a psuedo random string of size length.
     *
     * @param length - size of sting to create.
     * @return Random String of size length.
     */
    public static String randomString( int length ){
        StringBuffer strBuf = new StringBuffer();
        int index;
        for( index = 0; index < length; index++ ){
            strBuf.append( randomChar());
        }
        return strBuf.toString();
    }

    /**
     * Method to validate IP addresses are valid.
     * @param ipAddress
     * @return true - if the format of the
     */
    public final static boolean validateIPAddress(String ipAddress) {
        try {
            String[] parts = ipAddress.split("\\.");
            if (parts.length != 4) {
                return false;
            }

            for (String s : parts) {
                int i = Integer.parseInt(s);
                if ((i < 0) || (i > 255)) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException x) {
            return false;
        }
    }

    public final static boolean validHost(String hostName){
        try {
            InetAddress.getByName(hostName);
            return true;
        } catch(Exception x){
            TFLog.d(x.getMessage());
        }
        return true;
    }

    private static NetworkInterface multicastInterface = null;

    public static NetworkInterface getMulticastInterface(){
        if(multicastInterface == null) {
            try {
                Enumeration<NetworkInterface> interator = NetworkInterface.getNetworkInterfaces();
                while (interator.hasMoreElements()) {
                    NetworkInterface networkInterface = interator.nextElement();
                    if (networkInterface.supportsMulticast())
                        return networkInterface;
                }
            } catch (Exception x) {
                TFLog.e("Error trying to get multicast interface.");
            }
        } else {
            return multicastInterface;
        }
        return null;
    }


}
