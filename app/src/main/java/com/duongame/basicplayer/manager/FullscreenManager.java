package com.duongame.basicplayer.manager;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.View;

/**
 * Created by namjungsoo on 16. 6. 17..
 */
public class FullscreenManager {
    private static final String TAG = "FullscreenManager";

    private static boolean isFullscreen = false;

    public static boolean isFullscreen() {
        return isFullscreen;
    }
    /**
     * Detects and toggles immersive mode (also known as "hidey bar" mode).
     */
    public static void setFullscreen(Activity context, boolean newFullscreenMode) {

        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = context.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i(TAG, "Turning immersive mode mode off. ");
        } else {
            Log.i(TAG, "Turning immersive mode mode on.");
        }

        // isImmersiveModeEnabled: 현재 풀스크린 모드
        // newFullscreenMode: 새로운 풀스크린 모드
        // 새로 원하는 모드가 현재 모드와 같으면 바꿀 필요가 없다
        if (newFullscreenMode == isImmersiveModeEnabled)
            return;

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        context.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        //END_INCLUDE (set_ui_flags)

        // 후처리를 해주어야 한다
        isFullscreen = newFullscreenMode;
    }
}
