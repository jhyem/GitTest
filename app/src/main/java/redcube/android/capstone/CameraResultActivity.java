package redcube.android.capstone;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class CameraResultActivity extends AppCompatActivity {
    private ImageView capturedImageView;
    private TextView furnitureInfoTextView;
    private ImageButton confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera_result);

        capturedImageView = findViewById(R.id.capturedImageView);
        furnitureInfoTextView = findViewById(R.id.furnitureInfoTextView);
        confirmButton = findViewById(R.id.confirmButton);

        // Intent에서 거리 값 가져오기

        String category = getIntent().getStringExtra("selectedCategory");
        float measuredDistance = getIntent().getFloatExtra("distance1", 0f); // cm 단위
        float measuredDistance2 = getIntent().getFloatExtra("distance2", 0f);
        String imagePath = getIntent().getStringExtra("imagePath");

        int price = WastePriceCalculator.calculatePrice(category, measuredDistance);

        // 거리 값 표시
        String distanceText = String.format(Locale.getDefault(),
                "가구: %s\n" +
                        "폐기물 규격 : %.1f cm x %.1f cm",
                category,
                measuredDistance,
                measuredDistance2);
        furnitureInfoTextView.setText(distanceText);


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
                        boolean isAreaBased = category.equals("가구류 - 거울") ||
                                category.equals("생활용품류 - 어항") ||
                                category.equals("생활용품류 - 조명기구") ||
                                category.equals("가구류 - 유리");

                        AlertDialog.Builder builder = new AlertDialog.Builder(CameraResultActivity.this);
                        builder.setTitle("규격을 입력해주세요");

                        LinearLayout layout = new LinearLayout(CameraResultActivity.this);
                        layout.setOrientation(LinearLayout.VERTICAL);
                        layout.setPadding(50, 40, 50, 10);

                        final EditText inputWidth = new EditText(CameraResultActivity.this);
                        inputWidth.setHint("가로(cm)");
                        inputWidth.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        layout.addView(inputWidth);

                        final EditText inputHeight = new EditText(CameraResultActivity.this);
                        if (isAreaBased) {
                            inputHeight.setHint("세로(cm)");
                            inputHeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                            layout.addView(inputHeight);
                        }

                        builder.setView(layout);

                        builder.setPositiveButton("확인", (innerDialog, whichButton) -> {
                            try {
                                float width = Float.parseFloat(inputWidth.getText().toString());
                                float resultValue;

                                if (isAreaBased) {
                                    float height = Float.parseFloat(inputHeight.getText().toString());
                                    resultValue = width * height;
                                } else {
                                    resultValue = width; // 길이 기반 항목
                                }

                                int newPrice = WastePriceCalculator.calculatePrice(category, resultValue);

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