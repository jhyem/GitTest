package redcube.android.capstone;

import android.os.Bundle;
import android.widget.TextView;
import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity3 extends AppCompatActivity {
    private TextView distanceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);

        // TextView 초기화
        distanceTextView = findViewById(R.id.distance_text);

        // Intent에서 거리 값 가져오기
        float distance1 = getIntent().getFloatExtra("distance1", 0.0f);
        float distance2 = getIntent().getFloatExtra("distance2", 0.0f);
        String selectedItem = getIntent().getStringExtra("selectedCategory");

        // 거리 값 표시
        String distanceText = String.format(Locale.getDefault(),
            "%s\n"+
            "앵커 1-2 거리: %.1f cm\n" +
            "앵커 2-3 거리: %.1f cm",
            selectedItem,
            distance1,
            distance2);
        distanceTextView.setText(distanceText);
    }
}