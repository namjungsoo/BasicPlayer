package com.duongame.basicplayer.manager;

import java.util.HashMap;

public class TimeTextManager {
    private static HashMap<String, String> cacheTimeTextMap = new HashMap<String, String>();

    public static void addTimeText(String path, String timeText) {
        cacheTimeTextMap.put(path, timeText);
    }

    public static void clear() {
        cacheTimeTextMap.clear();
    }

    public static String getTimeText(String path) {
        return cacheTimeTextMap.get(path);
    }

}
