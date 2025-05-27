package com.example.project;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ChatbotActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatbot);

        // 하단 네비게이션 클릭 이벤트 처리
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_chatbot);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ChatbotActivity.this, SelectItemActivity.class));
                return true;
            } else if (itemId == R.id.nav_sticker) {
                startActivity(new Intent(ChatbotActivity.this, GoogleMapActivity.class));
                return true;
            }

            return false;
        });
    }
}