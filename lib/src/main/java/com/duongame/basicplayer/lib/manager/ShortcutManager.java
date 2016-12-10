package com.duongame.basicplayer.lib.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import com.duongame.basicplayer.lib.R;

/**
 * Created by 정수 on 2015-11-15.
 */
public class ShortcutManager {
    public static void checkShortcut(Context context) {
        initShortcut(context);
    }

    private static void initShortcut(Context context) {
        // 바로가기 중복 생성 방지
        if (!PreferenceManager.isShortcut(context)) {
            addShortcut(context);
        }
    }

    private static void addShortcut(Context context) {
        final Intent shortcutIntent = new Intent();
        shortcutIntent.setAction(Intent.ACTION_MAIN);
        shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        shortcutIntent.setClassName(context, context.getClass().getName());
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final Parcelable iconResource = Intent.ShortcutIconResource.fromContext(context, R.mipmap.ic_launcher);

        final Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getResources().getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
        intent.putExtra("duplicate", false);
        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        context.sendBroadcast(intent);

        // 바로가기 생성했다고 체크함
        PreferenceManager.setShortcut(context, true);
    }

}
