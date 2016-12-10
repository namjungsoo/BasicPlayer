package com.duongame.basicplayer.lib.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

//import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by namjungsoo on 2016-06-23.
 */
public class BaseActivity extends AppCompatActivity {
//    protected FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain the FirebaseAnalytics instance.
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }
}
