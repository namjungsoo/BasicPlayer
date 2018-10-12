package com.duongame.basicplayer.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.duongame.basicplayer.manager.AdInterstitialManager;
import com.google.firebase.analytics.FirebaseAnalytics;

import io.fabric.sdk.android.Fabric;

/**
 * Created by namjungsoo on 2016-06-23.
 */
public class BaseActivity extends AppCompatActivity {
    protected FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Fabric.with(this, new Crashlytics());
        AdInterstitialManager.init(this);
    }
}
