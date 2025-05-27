package com.example.project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CameraResultActivity extends AppCompatActivity {

    private ImageView capturedImageView;
    private TextView furnitureInfoTextView;
    private ImageButton confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_result);

        capturedImageView = findViewById(R.id.capturedImageView);
        furnitureInfoTextView = findViewById(R.id.furnitureInfoTextView);
        confirmButton = findViewById(R.id.confirmButton);

        String imagePath = getIntent().getStringExtra("imagePath");
        String selectedCategory = getIntent().getStringExtra("selectedCategory");

        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            capturedImageView.setImageBitmap(bitmap);
        }

        // 예: 가구 정보 추정
        furnitureInfoTextView.setText("가구: " + selectedCategory + "\n폐기물 규격: ?");

        confirmButton.setOnClickListener(v -> {
            Intent intent = new Intent(CameraResultActivity.this, StickerPriceActivity.class);
            intent.putExtra("imagePath", imagePath);
            intent.putExtra("result", selectedCategory);
            startActivity(intent);
        });
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(CameraResultActivity.this, SelectItemActivity.class));
                return true;
            } else if (itemId == R.id.nav_chatbot) {
                startActivity(new Intent(CameraResultActivity.this, ChatbotActivity.class));
                return true;
            } else if (itemId == R.id.nav_sticker) {
                startActivity(new Intent(CameraResultActivity.this, GoogleMapActivity.class));
                return true;
            }

            return false;
        });
    }
}
