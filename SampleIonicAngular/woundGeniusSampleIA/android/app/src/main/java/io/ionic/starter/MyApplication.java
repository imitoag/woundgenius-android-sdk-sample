package io.ionic.starter;

import android.app.Application;

import com.example.woundsdk.di.WoundGeniusSDK;

public class MyApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    WoundGeniusSDK woundGeniusSDK = WoundGeniusSDK.INSTANCE;
    woundGeniusSDK.init(this, "io.ionic.starter", "licenseKey");
  }
}
