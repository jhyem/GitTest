package com.example.project;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
            new AlertDialog.Builder(CameraResultActivity.this)
                    .setTitle("확인")
                    .setMessage("규격이 같습니까?")
                    .setPositiveButton("예", (dialog, which) -> {
                        Intent intent = new Intent(CameraResultActivity.this, StickerPriceActivity.class);
                        intent.putExtra("imagePath", imagePath);
                        intent.putExtra("result", category);
                        intent.putExtra("price", price);
                        startActivity(intent);
                    })
                    .setNegativeButton("아니오", (dialog, which) -> {
                        // EditText 입력 다이얼로그 표시
                        AlertDialog.Builder builder = new AlertDialog.Builder(CameraResultActivity.this);
                        builder.setTitle("규격을 입력해주세요");

                        final EditText input = new EditText(CameraResultActivity.this);
                        input.setHint("예: 12");
                        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                        builder.setView(input);

                        builder.setPositiveButton("확인", (innerDialog, whichButton) -> {
                            try {
                                float userInputDistance = Float.parseFloat(input.getText().toString());
                                int newPrice = WastePriceCalculator.calculatePrice(category, userInputDistance);

                                Intent intent = new Intent(CameraResultActivity.this, StickerPriceActivity.class);
                                intent.putExtra("imagePath", imagePath);
                                intent.putExtra("result", category);
                                intent.putExtra("price", newPrice);
                                startActivity(intent);
                            } catch (NumberFormatException e) {
                                Toast.makeText(CameraResultActivity.this, "숫자를 정확히 입력해주세요", Toast.LENGTH_SHORT).show();
                            }
                        });

                        builder.setNegativeButton("취소", (innerDialog, whichButton) -> innerDialog.cancel());

                        builder.show();
                    })
                    .show();
        });



    }
}

