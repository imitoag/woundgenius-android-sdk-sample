package io.ionic.starter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;

import io.imito.woundgenius.sdk.data.pojo.camera.cameramod.CameraMods;
import io.imito.woundgenius.sdk.ui.screen.bodypicker.BodyPartContract;
import io.imito.woundgenius.sdk.ui.screen.bodypicker.BodyPickerActivity;
import io.imito.woundgenius.sdk.ui.screen.measurecamera.MeasureCameraActivity;
import io.imito.woundgenius.sdk.ui.screen.measurecamera.MeasureCameraContract;

import com.getcapacitor.BridgeActivity;

import java.util.HashMap;

import io.imito.woundgenius.sdk.ui.screen.support.MeasureSupportActivity;
import io.imito.woundgenius.sdk.ui.screen.whatsnew.WhatsNewActivity;
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
      MeasureSupportActivity.open(
        MainActivity.this,
        CameraMods.MARKER_DETECT_MODE);
    });
  }
}
