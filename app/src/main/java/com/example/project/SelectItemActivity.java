package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

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
            // 선택된 품목 값 넘기기
            String selectedItem = categorySpinner.getSelectedItem().toString();

            Intent intent = new Intent(SelectItemActivity.this, CameraReadyActivity.class);
            intent.putExtra("selectedCategory", selectedItem);
            startActivity(intent);
        });
    }
}
