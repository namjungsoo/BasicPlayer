package com.duongame.basicplayer.manager;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

/**
 * Created by namjungsoo on 2016-04-30.
 */
public class AdInterstitialManager {
    private final static String TAG = "AdInterstitialManager";

    // 코믹z뷰어 전용 id
    private static final String INTERSTITIAL_ID = "ca-app-pub-5576037828251153/9737551820";
    private static InterstitialAd interstitialAD = null;

    public static final int MODE_START = 0;
    public static final int MODE_EXIT = 1;
    public static final int MODE_REFRESH = 2;

    private static int mode = MODE_EXIT;
    private static boolean show = false;

    private static Activity context;

    private static void requestNewInterstitial() {
        final AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        interstitialAD.loadAd(adRequest);
    }

    public static void init(final Activity context) {
        Log.e(TAG, "init");
        AdInterstitialManager.context = context;

        interstitialAD = new InterstitialAd(context);            // 삽입 광고 생성관련 메소드들.
        interstitialAD.setAdUnitId(INTERSTITIAL_ID);
        requestNewInterstitial();

        show = true;

        interstitialAD.setAdListener(new AdListener() {

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(TAG, "onAdClosed");
                requestNewInterstitial();

                if (mode == MODE_EXIT) {
//                    AlertManager.showAlertExit(context);
                } else if (mode == MODE_REFRESH) {
//                    AlertManager.showAlertRefresh(context);
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                Log.d(TAG, "onAdFailedToLoad");
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                Log.d(TAG, "onAdLeftApplication");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Log.d(TAG, "onAdOpened");
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d(TAG, "onAdLoaded");

                if (show) {
                    // 항상 보여주는게 아니라 체크를 해서 보여주자
                    final int count = PreferenceManager.getStartCount(context);
                    Log.e(TAG, "count=" + count);

                    if (count % 4 == 3) {
                        Log.e(TAG, "showAd");
                        showAd(context, MODE_START);
                    }
                    PreferenceManager.setStartCount(context, count + 1);

                    show = false;
                }
            }
        });                                // 광고의 리스너를 설정합니다.

    }

    public static void setShowAtLoaded(boolean show) {
        AdInterstitialManager.show = show;
    }

    public static boolean showAd(Activity context, int mode) {
        AdInterstitialManager.mode = mode;
        if (interstitialAD.isLoaded()) {
            interstitialAD.show();
            Log.d(TAG, "show");
            return true;
        } else {
            Log.d(TAG, "finish");
            return false;
        }
    }
}
