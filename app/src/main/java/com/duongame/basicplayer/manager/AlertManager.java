package com.duongame.basicplayer.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.duongame.basicplayer.R;
import com.duongame.basicplayer.activity.MainActivity;
import com.google.android.gms.ads.AdView;

import java.io.File;

/**
 * Created by namjungsoo on 2016-04-30.
 */
public class AlertManager {
    private static final String TAG = "AlertManager";

    public static void showAlertRecentFile(final Activity context, final String filename, final long time, final int rotation, DialogInterface.OnClickListener negListener) {
        final File file = new File(filename);

        //PRO
        AlertManager.showAlert(context, context.getResources().getString(R.string.dialog_recentfile), file.getName(), new DialogInterface.OnClickListener() {
            //        AlertManager.showAlertWithBanner(context, context.getResources().getString(R.string.dialog_recentfile), file.getName(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final MainActivity activity = (MainActivity) context;
                activity.openFile(filename, time, rotation);
            }
        }, negListener, null);

    }

//    public static void showAlertExit(final Activity context) {
//        AlertManager.showAlertWithBanner(context, context.getResources().getString(R.string.app_name), context.getString(R.string.dialog_exit), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Log.d(TAG, "AlertManager onClick finish");
//                        context.finish();
//                    }
//                }, null
//        );
//        AdBannerManager.initPopupAd(context);// 항상 초기화 해주어야 함
//    }
//
//    public static void showAlertRefresh(Activity context) {
//        AlertManager.showAlertWithBanner(context, context.getResources().getString(R.string.app_name), context.getString(R.string.dialog_new_partner), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                ClientManager.refresh();
//            }
//        }, null);
//        AdBannerManager.initPopupAd(context);// 항상 초기화 해주어야 함
//    }

    public static void showAlertWithBanner(Activity context, String title, String message, DialogInterface.OnClickListener posListener, DialogInterface.OnClickListener negListener, DialogInterface.OnKeyListener keyListener) {
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
                .setIcon(R.mipmap.ic_launcher)
                .setOnKeyListener(keyListener)
                .setPositiveButton(context.getString(R.string.ok), posListener)
                .setNegativeButton(context.getString(R.string.cancel), negListener)
                .show();
    }

    public static void showAlert(Activity context, String title, String message, DialogInterface.OnClickListener posListener, DialogInterface.OnClickListener negListener, DialogInterface.OnKeyListener keyListener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setIcon(R.mipmap.ic_launcher)
                .setOnKeyListener(keyListener)
                .setPositiveButton(context.getString(R.string.ok), posListener)
                .setNegativeButton(context.getString(R.string.cancel), negListener)
                .show();
    }

}
