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

    private static SharedPreferences pref = null;

    private static void checkPrefManager(Context context) {
        if (pref == null)
            pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isShortcut(Context context) {
        checkPrefManager(context);
        final boolean prefIsShortcut = pref.getBoolean(PREF_IS_SHORTCUT, false);
        return prefIsShortcut;
    }

    public static boolean isExplorerHelp(Context context) {
        checkPrefManager(context);
        final boolean prefIsExplorerHelp = pref.getBoolean(PREF_IS_EXPLORER_HELP, true);
        return prefIsExplorerHelp;
    }

    public static void setShortcut(Context context, boolean shortcut) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(PREF_IS_SHORTCUT, shortcut);
        editor.commit();
    }

    public static void setExplorerHelp(Context context, boolean explorerHelp) {
        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(PREF_IS_EXPLORER_HELP, explorerHelp);
        editor.commit();
    }


    public static int getStartCount(Context context) {
        checkPrefManager(context);
        final int startCount = pref.getInt(START_COUNT, 0);
        return startCount;
    }

    public static void setStartCount(Context context, int count) {
        Log.e(TAG, "setStartCount count="+count);

        checkPrefManager(context);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putInt(START_COUNT, count);
        editor.commit();
    }


}
