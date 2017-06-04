package com.duongame.basicplayer.manager;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

/**
 * Created by namjungsoo on 2016-04-30.
 */
public class AdBannerManager {
    private final static String TAG = "AdBannerManager";

    // 코믹z뷰어 전용 id
    private static final String TOP_BANNER_ID    = "ca-app-pub-5576037828251153/1527040225";
    private static final String BOTTOM_BANNER_ID = "ca-app-pub-5576037828251153/4620107424";
    private static final String POPUP_ID         = "ca-app-pub-5576037828251153/3003773421";

    private static AdView adTopBannerView;
    private static AdView adBottomBannerView;
    private static AdView adPopupView;

    public static AdView createAd(Activity context, String adid, AdSize adtype) {
        final AdView adView = new AdView(context);
        adView.setAdUnitId(adid);

        // 좌우를 꽉채워주는 배너 타입
        adView.setAdSize(adtype);

        // 기본 요청을 시작합니다.
        final AdRequest adRequest = new AdRequest.Builder().build();

        // 광고 요청으로 adView를 로드합니다.
        adView.loadAd(adRequest);
        return adView;
    }

    public static void initTopBannerAd(Activity context) {
        adTopBannerView = createAd(context, TOP_BANNER_ID, AdSize.SMART_BANNER);
        adTopBannerView.setAdListener(new AdListener() {
            private static final String TAG = "adBannerView";

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(TAG, "onAdClosed");
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
            }
        });
    }

    public static void initBottomBannerAd(Activity context) {
        adBottomBannerView = createAd(context, BOTTOM_BANNER_ID, AdSize.SMART_BANNER);
        adBottomBannerView.setAdListener(new AdListener() {
            private static final String TAG = "adBannerView";

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(TAG, "onAdClosed");
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
            }
        });
    }

    public static void initPopupAd(Activity context) {
        adPopupView = createAd(context, POPUP_ID, AdSize.MEDIUM_RECTANGLE);

        adPopupView.setAdListener(new AdListener() {
            private static final String TAG = "adPopupView";

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(TAG, "onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                Log.d(TAG, "onAdFailedToLoad " + errorCode);
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
            }
        });
    }

    public static void init(Activity context) {
        initBottomBannerAd(context);
        initTopBannerAd(context);
        initPopupAd(context);
    }

    public static AdView getAdBottomBannerView() {
        return adBottomBannerView;
    }

    public static AdView getAdTopBannerView() {
        return adTopBannerView;
    }

    public static AdView getAdPopupView() {
        return adPopupView;
    }
}
