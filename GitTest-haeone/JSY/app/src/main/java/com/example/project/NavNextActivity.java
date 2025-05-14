package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavNextActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_item); // 기본 시작 화면

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_sticker) {
                startActivity(new Intent(NavNextActivity.this, GoogleMapActivity.class));
                return true;
            } else if (itemId == R.id.nav_home) {
                startActivity(new Intent(NavNextActivity.this, SelectItemActivity.class));
                return true;
            } else if (itemId == R.id.nav_chatbot) {
                startActivity(new Intent(NavNextActivity.this, ChatbotActivity.class));
                return true;
            }

            return false;
        });
    }
}