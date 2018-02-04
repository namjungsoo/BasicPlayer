package com.duongame.basicplayer.manager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.duongame.basicplayer.BuildConfig;
import com.duongame.basicplayer.R;
import com.google.android.gms.ads.AdView;

import java.io.File;

/**
 * Created by namjungsoo on 2016-04-30.
 */
public class AlertManager {
    private static final String TAG = AlertManager.class.getSimpleName();

    public static void showAlertRecentFile(final Context context, final String filename, final long time, final int rotation, DialogInterface.OnClickListener negListener) {
        final File file = new File(filename);

        //PRO
        if (BuildConfig.SHOW_AD) {
            AlertManager.showAlertWithBanner(context, context.getResources().getString(R.string.dialog_recentfile), file.getName(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    FileManager.openFile(context, filename, time, rotation);
                }
            }, negListener, null);
        } else {
            AlertManager.showAlert(context, context.getResources().getString(R.string.dialog_recentfile), file.getName(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    FileManager.openFile(context, filename, time, rotation);
                }
            }, negListener, null);
        }

    }

    public static void showAlertWithBanner(Context context, String title, String message, DialogInterface.OnClickListener posListener, DialogInterface.OnClickListener negListener, DialogInterface.OnKeyListener keyListener) {
        final AdView view = AdBannerManager.getAdPopupView();
        final ViewParent parent = view.getParent();
        if (parent != null) {
            final ViewGroup vg = (ViewGroup) parent;
            vg.removeView(view);
        }

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setView(view)
                .setIcon(R.drawable.player)
                .setOnKeyListener(keyListener)
                .setPositiveButton(context.getString(R.string.ok), posListener)
                .setNegativeButton(context.getString(R.string.cancel), negListener)
                .show();
    }

    public static void showAlert(Context context, String title, String message, DialogInterface.OnClickListener posListener, DialogInterface.OnClickListener negListener, DialogInterface.OnKeyListener keyListener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.player)
                .setOnKeyListener(keyListener)
                .setPositiveButton(context.getString(R.string.ok), posListener)
                .setNegativeButton(context.getString(R.string.cancel), negListener)
                .show();
    }

}
