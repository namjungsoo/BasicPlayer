package com.duongame.basicplayer.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by 정수 on 2015-11-15.
 */
public class PreferenceManager {
    private static final String TAG="PreferenceManager";

    private static final String PREF_NAME = "BasicPlayer";

    private static final String PREF_IS_SHORTCUT = "is_shortcut";
    private static final String PREF_IS_EXPLORER_HELP = "is_explorer_help";

    // 3번중에서 1번을 보여준다.
    private static final String START_COUNT = "start_count";

    private static final String RECENT_FILENAME = "recent_filename";
    private static final String RECENT_TIME = "recent_time";

    private static SharedPreferences pref = null;

    private static void checkPrefManager(Context context) {
        if (pref == null)
            pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isShortcut(Context context) {
        checkPrefManager(context);
        return pref.getBoolean(PREF_IS_SHORTCUT, false);
    }

    public static boolean isExplorerHelp(Context context) {
        checkPrefManager(context);
        return pref.getBoolean(PREF_IS_EXPLORER_HELP, true);
    }

    public static void setShortcut(Context context, boolean shortcut) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(PREF_IS_SHORTCUT, shortcut);
        editor.apply();
    }

    public static void setExplorerHelp(Context context, boolean explorerHelp) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(PREF_IS_EXPLORER_HELP, explorerHelp);
        editor.apply();
    }

    public static int getStartCount(Context context) {
        checkPrefManager(context);
        return pref.getInt(START_COUNT, 0);
    }

    public static void setStartCount(Context context, int count) {
        Log.e(TAG, "setStartCount count="+count);

        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putInt(START_COUNT, count);
        editor.apply();
    }


    public static String getRecentFilename(Context context) {
        checkPrefManager(context);
        return pref.getString(RECENT_FILENAME, "");
    }

    public static void setRecentFilename(Context context, String filename) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putString(RECENT_FILENAME, filename);
        editor.apply();
    }

    public static long getRecentTime(Context context) {
        checkPrefManager(context);
        return pref.getLong(RECENT_TIME, 0);
    }

    public static void setRecentTime(Context context, long time) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putLong(RECENT_TIME, time);
        editor.apply();
    }

    public static void saveRecentFile(Context context, String filename, long time) {
        PreferenceManager.setRecentFilename(context, filename);
        PreferenceManager.setRecentTime(context, time);
    }

}
