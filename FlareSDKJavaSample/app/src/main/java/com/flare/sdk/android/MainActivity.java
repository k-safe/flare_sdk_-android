package com.flare.sdk.android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sos.busbysideengine.Constants;

public class MainActivity extends AppCompatActivity {
    Button btnStandard, btnCustom, btnSOS, btnEnableFlareAware;
    RadioGroup rgEnvironment;
    RadioButton rbProduction, rbSandBox;

    String productionLicense = "your production key";
    String sandboxLicense = "your sandbox key";

    String mode = Constants.ENVIRONMENT_SANDBOX;

    private final int postNotificationCode = 1221;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        process();
        setListener();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, postNotificationCode);
            }
        }

    }

    public void init() {
        btnStandard = findViewById(R.id.btnStandard);
        btnCustom = findViewById(R.id.btnCustom);
        btnSOS = findViewById(R.id.btnSOS);
        btnEnableFlareAware = findViewById(R.id.btnEnableFlareAware);
        rgEnvironment = findViewById(R.id.rgEnvironment);
        rbProduction = findViewById(R.id.rbProduction);
        rbSandBox = findViewById(R.id.rbSandBox);
    }

    public void process() {
        btnSOS.setVisibility(View.VISIBLE);
        btnEnableFlareAware.setVisibility(View.VISIBLE);
    }

    public void setListener() {

        rgEnvironment.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if (checkedId == R.id.rbProduction) {
                // The switch is checked.
                rbProduction.setText(getString(R.string.production_mode));
                mode = Constants.ENVIRONMENT_PRODUCTION;
            } else {
                // The switch isn't checked.
                rbSandBox.setText(getString(R.string.sandbox_mode));
                mode = Constants.ENVIRONMENT_SANDBOX;
            }
            btnSOS.setVisibility(View.VISIBLE);
            btnEnableFlareAware.setVisibility(View.VISIBLE);
        });

        btnStandard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StandardThemeActivity.class);
            intent.putExtra("mode", mode);
            intent.putExtra("lic",
                    (mode.equals(Constants.ENVIRONMENT_PRODUCTION)) ?
                            productionLicense : sandboxLicense);
            startActivity(intent);
        });

        btnCustom.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CustomThemeActivity.class);
            intent.putExtra("mode", mode);
            intent.putExtra("lic",
                    (mode.equals(Constants.ENVIRONMENT_PRODUCTION)) ?
                            productionLicense : sandboxLicense);
            startActivity(intent);
        });

        btnSOS.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EmergencySOSActivity.class);
            intent.putExtra("lic", productionLicense);
            startActivity(intent);
        });

        btnEnableFlareAware.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EnableFlareAwareActivity.class);
            intent.putExtra("mode", mode);
            intent.putExtra("lic", (mode.equals(Constants.ENVIRONMENT_PRODUCTION)) ?
                    productionLicense : sandboxLicense);
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