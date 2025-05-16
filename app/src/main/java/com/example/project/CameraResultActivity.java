package com.example.project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

        String category = getIntent().getStringExtra("selectedCategory");
        float measuredDistance = getIntent().getFloatExtra("measuredDistance", 0f); // m 단위
        String imagePath = getIntent().getStringExtra("imagePath");

        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            capturedImageView.setImageBitmap(bitmap);
        }

        int price = WastePriceCalculator.calculatePrice(category, measuredDistance);


        // UI에 표시
        furnitureInfoTextView.setText(
                "가구: " + category + "\n" +
                        "스티커 가격: " + price + "원"
        );

        confirmButton.setOnClickListener(v -> {
            Intent intent = new Intent(CameraResultActivity.this, StickerPriceActivity.class);
            intent.putExtra("imagePath", imagePath);
            intent.putExtra("result", category);
            intent.putExtra("price", price);
            startActivity(intent);
        });
    }
}
