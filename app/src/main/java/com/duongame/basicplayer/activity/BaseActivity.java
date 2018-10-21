package com.duongame.basicplayer.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.duongame.basicplayer.manager.AdInterstitialManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Created by namjungsoo on 2016-06-23.
 */
public class BaseActivity extends AppCompatActivity {
    protected FirebaseAnalytics firebaseAnalytics;
    protected FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Fabric.with(this, new Crashlytics());
        AdInterstitialManager.init(this);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.fetch(0).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mFirebaseRemoteConfig.activateFetched();
                    long version = mFirebaseRemoteConfig.getLong("latest_version");
//                    if (BuildConfig.VERSION_CODE < version) {
//                        ToastHelper.info(BaseMainActivity.this, R.string.toast_new_version);
//                    }
                }
            }
        });

    }
}
