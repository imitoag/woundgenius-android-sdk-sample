package io.ionic.starter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;

import com.example.woundsdk.ui.screen.measurecamera.MeasureCameraActivity;
import com.example.woundsdk.ui.screen.measurecamera.MeasureCameraContract;
import com.getcapacitor.BridgeActivity;

import io.ionic.starter.databinding.ActivityMainBinding;

public class MainActivity extends BridgeActivity {
  private ActivityMainBinding binding;

  private ActivityResultLauncher<Intent> measureCameraLauncher = registerForActivityResult(
    new MeasureCameraContract(),
    assessment -> {
      // You will get the result here
    }
  );


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);

    binding.openSdkButtonACTV.setOnClickListener(v -> {
      MeasureCameraActivity.Companion.openWithResult(
        measureCameraLauncher,
        MainActivity.this,  // Replaceable by "fragment = this" if using an fragment
        MainActivity.this.getCacheDir().toString()
      );
    });
  }
}
