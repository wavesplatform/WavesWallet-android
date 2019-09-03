package com.mysite.myapplication;

import android.app.Application;

import com.wavesplatform.sdk.WavesSdk;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        WavesSdk.init(this);
    }
}
