package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnStandardTheme, btnCustomTheme;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        setListener();
    }

    public void init() {
        btnStandardTheme = findViewById(R.id.btnStandardTheme);
        btnCustomTheme = findViewById(R.id.btnCustomTheme);
    }

    public void setListener() {
        btnStandardTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StandardThemeActivity.class);
                startActivity(intent);
            }
        });

        btnCustomTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CustomThemeActivity.class);
                startActivity(intent);
            }
        });
    }
}