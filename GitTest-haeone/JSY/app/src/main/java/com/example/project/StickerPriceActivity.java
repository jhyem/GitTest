package com.example.project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StickerPriceActivity extends AppCompatActivity {

    private ImageView furnitureImageView;
    private TextView resultTextView;
    private TextView priceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_price);

        furnitureImageView = findViewById(R.id.furnitureImageView);
        resultTextView = findViewById(R.id.resultTextView);
        priceTextView = findViewById(R.id.priceTextView);

        String imagePath = getIntent().getStringExtra("imagePath");
        String result = getIntent().getStringExtra("result");

        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            furnitureImageView.setImageBitmap(bitmap);
        }

        resultTextView.setText("결과: " + result);

        // 예시 가격 계산
        priceTextView.setText("2000원");
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(StickerPriceActivity.this, SelectItemActivity.class));
                return true;
            } else if (itemId == R.id.nav_chatbot) {
                startActivity(new Intent(StickerPriceActivity.this, ChatbotActivity.class));
                return true;
            } else if (itemId == R.id.nav_sticker) {
                startActivity(new Intent(StickerPriceActivity.this, GoogleMapActivity.class));
                return true;
            }
            return false;
        });
    }
}
