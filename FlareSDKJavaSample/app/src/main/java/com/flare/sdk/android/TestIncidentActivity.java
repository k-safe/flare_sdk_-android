package com.flare.sdk.android;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;


public class TestIncidentActivity extends AppCompatActivity {

    ImageView ivCUIClose;
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_incident);
        ivCUIClose = findViewById(R.id.ivCUIClose);
        ivCUIClose.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onNavigateUp() {
        finish();
        return true;
    }
}