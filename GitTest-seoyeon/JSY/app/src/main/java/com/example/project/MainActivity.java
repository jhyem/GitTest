package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, SelectItemActivity.class);
            startActivity(intent);
            finish(); // MainActivity 종료 (뒤로가기 방지)
        }, 3000); // 3초 대기
    }
}