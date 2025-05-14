package com.example.project;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ChatbotActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatbot); // 👉 여기서 chatbot.xml을 화면에 연결함

        // 하단 네비게이션 클릭 이벤트 처리
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // 홈 버튼 클릭 시 SelectItemActivity로 이동
                startActivity(new Intent(ChatbotActivity.this, SelectItemActivity.class));
                return true;
            } else if (itemId == R.id.nav_sticker) {
                startActivity(new Intent(ChatbotActivity.this, GoogleMapActivity.class));
                return true;
            }
            // 현재 화면이 챗봇이므로 별도 처리 불필요
            return false;
        });
    }
}