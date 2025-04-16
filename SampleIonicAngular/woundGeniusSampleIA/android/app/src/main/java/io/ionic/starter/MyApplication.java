package io.ionic.starter;

import android.app.Application;

public class MyApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    WoundGeniusSdkManager woundGeniusSdkManager = new WoundGeniusSdkManager();

    woundGeniusSdkManager.init(this);
  }
}
