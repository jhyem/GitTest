package com.example.project;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SelectItemActivity extends AppCompatActivity {

    private Spinner categorySpinner;
    private ImageButton confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_item);

        categorySpinner = findViewById(R.id.categorySpinner);
        confirmButton = findViewById(R.id.confirmButton);

        confirmButton.setOnClickListener(v -> {
            String selectedItem = categorySpinner.getSelectedItem().toString();

            Intent intent = new Intent(SelectItemActivity.this, CameraReadyActivity.class);
            intent.putExtra("selectedCategory", selectedItem);
            startActivity(intent);
        });
            // 하단 네비게이션 바 부분 이동 함수
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_chatbot) {
                startActivity(new Intent(SelectItemActivity.this, ChatbotActivity.class));
                return true;
            } else if (itemId == R.id.nav_sticker) {
                startActivity(new Intent(SelectItemActivity.this, GoogleMapActivity.class));
                return true;
            }
            return false;
        });
    }
}
