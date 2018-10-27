package com.duongame.basicplayer.util;

/**
 * Created by namjungsoo on 2016-06-21.
 */
public class TimeConverter {
    public final static long SEC_TO_US = 1000000L;
    public final static long SEC_TO_MS = 1000L;
    public final static float US_TO_SEC = 0.000001f;

    static String convertSecToString(long timeSec) {
        long hour = timeSec / 3600;
        long min = (timeSec - (hour * 3600)) / 60;
        long sec = timeSec - (hour * 3600) - min * 60;

        if (hour > 0)
            return String.format("%01d:%02d:%02d", hour, min, sec);
        else
            return String.format("%02d:%02d", min, sec);
    }

    public static String convertMsToString(long timeMs) {
        // 초단위로 변경
        long timeSec = timeMs / SEC_TO_MS;
        return convertSecToString(timeSec);
    }

    public static String convertUsToString(long timeUs) {
        // 초단위로 변경
        long timeSec = timeUs / SEC_TO_US;
        return convertSecToString(timeSec);
    }
}
