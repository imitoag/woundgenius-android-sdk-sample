package io.ionic.starter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;

import io.imito.woundgenius.sdk.internal.data.pojo.camera.mode.ImitoCameraMode;
import io.imito.woundgenius.sdk.internal.ui.screen.bodypicker.BodyPartContract;
import io.imito.woundgenius.sdk.internal.ui.screen.bodypicker.BodyPickerActivity;
import io.imito.woundgenius.sdk.internal.ui.screen.measurecamera.MeasureCameraActivity;
import io.imito.woundgenius.sdk.internal.ui.screen.measurecamera.MeasureCameraContract;

import com.getcapacitor.BridgeActivity;

import java.util.HashMap;

import io.imito.woundgenius.sdk.internal.ui.screen.support.HelpScreenActivity;
import io.ionic.starter.databinding.ActivityMainBinding;

public class MainActivity extends BridgeActivity {
  private ActivityMainBinding binding;

  private ActivityResultLauncher<Intent> measureCameraLauncher = registerForActivityResult(
    new MeasureCameraContract(),
    assessment -> {
      // You will get the result here
    }
  );

  private ActivityResultLauncher<Intent> bodyPartLauncher = registerForActivityResult(
    new BodyPartContract(),
    bodyPart -> {
      // You will get the result here
    }
  );


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);


    binding.openCameraButtonACTV.setOnClickListener(v -> {
      MeasureCameraActivity.Companion.openWithResult(
        measureCameraLauncher,
        MainActivity.this,  // Replaceable by "fragment = this" if using an fragment
        MainActivity.this.getCacheDir().toString()
      );
    });

    binding.openBodyPickerButtonACTV.setOnClickListener(v -> {
      BodyPickerActivity.Companion.openWithResult(
        bodyPartLauncher,
        MainActivity.this,// Replaceable by "fragment = this" if using an fragment
        null,
        null,
        null
      );
    });

    binding.openHelpScreenButtonACTV.setOnClickListener(v -> {
      HelpScreenActivity.open(
        MainActivity.this,
        ImitoCameraMode.MARKER_DETECT_MODE);
    });
  }
}
