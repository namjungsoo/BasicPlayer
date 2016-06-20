package com.duongame.basicplayer.util;

import android.content.res.Resources;

/**
 * Created by namjungsoo on 2016-06-20.
 */
public class UnitConverter {
    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}
