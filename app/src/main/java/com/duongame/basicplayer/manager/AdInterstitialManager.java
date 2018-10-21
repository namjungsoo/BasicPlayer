package com.duongame.basicplayer.manager;

import android.app.Activity;
import android.util.Log;

import com.duongame.basicplayer.BuildConfig;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

/**
 * Created by namjungsoo on 2016-04-30.
 */
public class AdInterstitialManager {
    private final static String TAG = "AdInterstitialManager";

    private static final String INTERSTITIAL_ID = "ca-app-pub-8174809468024854/7217031038";
    private static InterstitialAd interstitialAD = null;

    public static final int MODE_START = 0;
    public static final int MODE_EXIT = 1;
    public static final int MODE_REFRESH = 2;

    private static int mode = MODE_EXIT;
    private static boolean show = false;
    private static boolean isShowing = false;

    private static Activity context;
    private static OnFinishListener onFinishListener;

    public interface OnFinishListener {
        void onFinish();
    }

    private static void requestNewInterstitial() {
        final AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        interstitialAD.loadAd(adRequest);
    }

    public static void init(final Activity context) {
        if (AdInterstitialManager.context != null) {
            return;
        }

        Log.e(TAG, "init");
        AdInterstitialManager.context = context;

        interstitialAD = new InterstitialAd(context);            // 삽입 광고 생성관련 메소드들.
        interstitialAD.setAdUnitId(INTERSTITIAL_ID);
//        show = true;

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

                isShowing = false;

                if (onFinishListener != null) {
                    onFinishListener.onFinish();
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
                        showAd(context, MODE_START, onFinishListener);
                    }
                    PreferenceManager.setStartCount(context, count + 1);

                    show = false;
                }
            }
        });                                // 광고의 리스너를 설정합니다.

        requestNewInterstitial();
    }

    public static void setShowAtLoaded(boolean show, OnFinishListener listener) {
        onFinishListener = listener;
        AdInterstitialManager.show = show;
    }

    public static boolean showAd(Activity context, int mode, OnFinishListener listener) {
        if (!BuildConfig.SHOW_AD)
            return false;

        if (isShowing)
            return false;

        AdInterstitialManager.mode = mode;
        if (interstitialAD.isLoaded()) {
            onFinishListener = listener;
            isShowing = true;

            interstitialAD.show();
            Log.d(TAG, "show");
            return true;
        } else {
            isShowing = false;
            Log.d(TAG, "finish");
            return false;
        }
    }
}
