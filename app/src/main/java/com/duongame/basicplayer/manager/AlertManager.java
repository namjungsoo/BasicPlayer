package com.duongame.basicplayer.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.duongame.basicplayer.R;
import com.duongame.basicplayer.activity.MainActivity;

import java.io.File;

/**
 * Created by namjungsoo on 2016-04-30.
 */
public class AlertManager {
    private static final String TAG="AlertManager";

    public static void showAlertRecentFile(final Activity context, final String filename, final long time) {
        final File file = new File(filename);

        AlertManager.showAlertWithBanner(context, context.getResources().getString(R.string.dialog_recentfile), file.getName(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final MainActivity activity = (MainActivity)context;
                activity.openFile(filename, time);
            }
        }, null);

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

    public static void showAlertWithBanner(Activity context, String title, String message, DialogInterface.OnClickListener posListener, DialogInterface.OnKeyListener keyListener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setView(AdBannerManager.getAdPopupView())
                .setIcon(R.mipmap.ic_launcher)
                .setOnKeyListener(keyListener)
                .setPositiveButton(context.getString(R.string.ok), posListener)
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 아무것도 안함

            }
        }).show();
    }

    public static void showAlert(Activity context, String title, String message, DialogInterface.OnClickListener posListener, DialogInterface.OnKeyListener keyListener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setIcon(R.mipmap.ic_launcher)
                .setOnKeyListener(keyListener)
                .setPositiveButton(context.getString(R.string.ok), posListener)
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 아무것도 안함

                    }
                }).show();
    }

}
