package com.duongame.basicplayer.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

//import com.crashlytics.android.Crashlytics;
import com.duongame.basicplayer.BuildConfig;
import com.duongame.basicplayer.PlayerApplication;
import com.duongame.basicplayer.R;
import com.duongame.basicplayer.manager.AdInterstitialManager;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

//import io.fabric.sdk.android.Fabric;

/**
 * Created by namjungsoo on 2016-06-23.
 */
public class BaseActivity extends AppCompatActivity {
    protected FirebaseAnalytics firebaseAnalytics;
    protected FirebaseRemoteConfig mFirebaseRemoteConfig;
    protected PlayerApplication application;
    protected Tracker mTracker;

    void gotoAppStorePage(String packageName) {
        try {
            final Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
            marketLaunch.setData(Uri.parse("market://details?id=" + packageName));
            this.startActivity(marketLaunch);
        } catch (ActivityNotFoundException e) {// FIX: ActivityNotFoundException
            final Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
            marketLaunch.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            this.startActivity(marketLaunch);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        application = (PlayerApplication) getApplication();
        mTracker = application.getDefaultTracker();

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
//        Fabric.with(this, new Crashlytics());
        if (BuildConfig.SHOW_AD)
            AdInterstitialManager.init(this);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.fetch(0).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mFirebaseRemoteConfig.activate();

                    // 최신버전 업데이트 관련
                    long version = mFirebaseRemoteConfig.getLong("latest_version");
                    boolean force = mFirebaseRemoteConfig.getBoolean("force_update");
                    if (BuildConfig.VERSION_CODE < version) {
                        Toast.makeText(BaseActivity.this, R.string.toast_new_version, Toast.LENGTH_SHORT).show();
                        if (force) {
                            // 강제로 플레이 스토어로 이동함
                            gotoAppStorePage(getApplicationContext().getPackageName());
                        }
                    }

                    // 앱 마이그레이션 관련
                    String from = mFirebaseRemoteConfig.getString("migration_from");
                    String to = mFirebaseRemoteConfig.getString("migration_to");
                    if(from.equals(getApplicationContext().getPackageName())) {
                        gotoAppStorePage(to);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mTracker != null) {
            mTracker.setScreenName(this.getClass().getSimpleName());
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }
}
