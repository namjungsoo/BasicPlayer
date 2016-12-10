package com.duongame.basicplayer.lib.util;

/**
 * Created by namjungsoo on 2016-06-21.
 */
public class TimeConverter {
    public final static long SEC_TO_US = 1000000L;
    public final static float US_TO_SEC = 0.000001f;

    public static String convertUsToString(long timeUs) {
        // 초단위로 변경
        timeUs = timeUs / SEC_TO_US;

        long hour = timeUs / 3600;
        long min = (timeUs - (hour * 3600)) / 60;
        long sec = timeUs - (hour * 3600) - min * 60;

        if(hour > 0)
            return String.format("%01d:%02d:%02d", hour, min, sec);
        else
            return String.format("%02d:%02d", min, sec);
    }

}
