package redcube.android.capstone;

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

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavNextActivity.setupBottomNavigation(this, bottomNavigationView);

        furnitureImageView = findViewById(R.id.furnitureImageView);
        resultTextView = findViewById(R.id.resultTextView);
        priceTextView = findViewById(R.id.priceTextView);

        String imagePath = getIntent().getStringExtra("imagePath");
        String result = getIntent().getStringExtra("result");
        String price = getIntent().getStringExtra("price");

        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            furnitureImageView.setImageBitmap(bitmap);
        }

        resultTextView.setText("결과: " + result);

        priceTextView.setText("가격: " + price);
    }
}
