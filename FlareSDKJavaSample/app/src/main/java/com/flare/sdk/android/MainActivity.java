package com.flare.sdk.android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.flare.sdk.android.databinding.ActivityMainBinding;
import com.flaresafety.sideengine.Constants;

public class MainActivity extends AppCompatActivity {

    private final String productionLicense = "4afb485e-a181-4ce7-98f6-38cfe1afc748";
    private final String sandboxLicense = "b6dd8509-d50e-48cc-af9e-ce9dcd712132";
    private final String secretKey = "EN7nPbKOc57COfYaPy66j8bXhlvOkrcX87c7mC76";

    String mode = Constants.ENVIRONMENT_SANDBOX;

    private final int postNotificationCode = 1221;
    String region = "";
    private boolean isHazardEnabled = true;

    private ActivityMainBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        process();
        setListener();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        postNotificationCode);
            }
        }

    }

    public void process() {
        viewBinding.btnSOS.setVisibility(View.VISIBLE);
        viewBinding.btnEnableFlareAware.setVisibility(View.VISIBLE);

        if(viewBinding.rgEnvironment.getCheckedRadioButtonId() == R.id.rbSandBox) {
            mode = Constants.ENVIRONMENT_SANDBOX;
        } else {
            mode = Constants.ENVIRONMENT_PRODUCTION;
        }
        region = getResources().getStringArray(R.array.region_list)[0];

    }

    public void setListener() {

        viewBinding.switchHazard.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            isHazardEnabled = isChecked;
        });

        viewBinding.spinRegion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                region = getResources().getStringArray(R.array.region_list)[position];

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        viewBinding.rgEnvironment.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if (checkedId == R.id.rbProduction) {
                // The switch is checked.
                viewBinding.rbProduction.setText(getString(R.string.production_mode));
                mode = Constants.ENVIRONMENT_PRODUCTION;
            } else {
                // The switch isn't checked.
                viewBinding.rbSandBox.setText(getString(R.string.sandbox_mode));
                mode = Constants.ENVIRONMENT_SANDBOX;
            }
            viewBinding.btnSOS.setVisibility(View.VISIBLE);
            viewBinding.btnEnableFlareAware.setVisibility(View.VISIBLE);
        });

        viewBinding.btnStandard.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, StandardThemeActivity.class);
            intent.putExtra("mode", mode);
            intent.putExtra("secretKey", secretKey);
            intent.putExtra("region", region);
            intent.putExtra("isHazardEnabled", isHazardEnabled);
            intent.putExtra("lic",
                    (mode.equals(Constants.ENVIRONMENT_PRODUCTION)) ? productionLicense : sandboxLicense);
            startActivity(intent);
        });

        viewBinding.btnCustom.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, CustomThemeActivity.class);
            intent.putExtra("mode", mode);
            intent.putExtra("secretKey", secretKey);
            intent.putExtra("region", region);
            intent.putExtra("isHazardEnabled", isHazardEnabled);
            intent.putExtra("lic",
                    (mode.equals(Constants.ENVIRONMENT_PRODUCTION)) ? productionLicense : sandboxLicense);
            startActivity(intent);
        });

        viewBinding.btnSOS.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EmergencySOSActivity.class);
            intent.putExtra("lic", productionLicense);
            intent.putExtra("secretKey", secretKey);
            intent.putExtra("region", region);
            intent.putExtra("isHazardEnabled", isHazardEnabled);
            startActivity(intent);
        });

        viewBinding.btnEnableFlareAware.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, EnableFlareAwareActivity.class);
            intent.putExtra("mode", mode);
            intent.putExtra("secretKey", secretKey);
            intent.putExtra("region", region);
            intent.putExtra("isHazardEnabled", isHazardEnabled);
            intent.putExtra("lic", (mode.equals(Constants.ENVIRONMENT_PRODUCTION)) ?
                    productionLicense : sandboxLicense);
            startActivity(intent);
        });

        viewBinding.btnHazards.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, HazardsActivity.class);
            intent.putExtra("mode", mode);
            intent.putExtra("secretKey", secretKey);
            intent.putExtra("region", region);
            intent.putExtra("isHazardEnabled", isHazardEnabled);
            intent.putExtra("lic", Constants.ENVIRONMENT_PRODUCTION.equals(mode) ? productionLicense : sandboxLicense);
            startActivity(intent);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == postNotificationCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted. You can now send notifications.
            } else {
                // Permission denied. Handle accordingly (e.g., show a message or disable notification functionality).
            }
        }
    }

}