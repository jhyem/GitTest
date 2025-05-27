package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

public class CameraResultActivity extends AppCompatActivity {
    private ImageView capturedImageView;
    private TextView furnitureInfoTextView;
    private ImageButton confirmButton;
    private int priceFromServer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera_result);

        capturedImageView = findViewById(R.id.capturedImageView);
        furnitureInfoTextView = findViewById(R.id.furnitureInfoTextView);
        confirmButton = findViewById(R.id.confirmButton);

        String category = getIntent().getStringExtra("selectedCategory");
        float measuredDistance = getIntent().getFloatExtra("distance1", 0f);
        float measuredDistance2 = getIntent().getFloatExtra("distance2", 0f);
        String imagePath = getIntent().getStringExtra("imagePath");

        // 거리 텍스트 표시
        String distanceText = String.format(Locale.getDefault(),
                "가구: %s\n폐기물 규격 : %.1f cm x %.1f cm",
                category,
                measuredDistance,
                measuredDistance2);
        furnitureInfoTextView.setText(distanceText);

        // 서버에서 가격 가져오기
        RequestQueue queue = Volley.newRequestQueue(this);
        String encodedItem;
        try {
            encodedItem = URLEncoder.encode(category, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(this, "품목 인코딩 실패", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = null;
        try {
            url = "http://10.0.2.2:8080/api/price?item=" + URLEncoder.encode(category, "UTF-8")
                    + "&width=" + measuredDistance + "&height=" + measuredDistance2;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        priceFromServer = response.getInt("fee");
                        Log.d("CameraResult", "받은 가격: " + priceFromServer);
                    } catch (JSONException e) {
                        Toast.makeText(this, "서버 응답 파싱 실패", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "서버 연결 실패", Toast.LENGTH_SHORT).show()
        );
        queue.add(request);

        // 확인 버튼 눌렀을 때
        confirmButton.setOnClickListener(v -> {
            new AlertDialog.Builder(CameraResultActivity.this)
                    .setTitle("확인")
                    .setMessage("규격이 같습니까?")
                    .setPositiveButton("예", (dialog, which) -> {
                        Intent intent = new Intent(CameraResultActivity.this, StickerPriceActivity.class);
                        intent.putExtra("imagePath", imagePath);
                        intent.putExtra("result", category);
                        intent.putExtra("price", priceFromServer);
                        startActivity(intent);
                    })
                    .setNegativeButton("아니오", (dialog, which) -> {
                        showManualInputDialog(category, imagePath);
                    })
                    .show();
        });
    }
    private void showManualInputDialog(String category, String imagePath) {
        boolean isAreaBased = category.equals("거울") ||
                category.equals("어항") ||
                category.equals("조명기구") ||
                category.equals("유리");

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
                float height = isAreaBased ? Float.parseFloat(inputHeight.getText().toString()) : 0f;

                // 서버에 요청 보내기
                String encodedItem = URLEncoder.encode(category, "UTF-8");
                String url = "http://10.0.2.2:8080/api/price?item=" + encodedItem
                        + "&width=" + width + "&height=" + height;

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                        response -> {
                            try {
                                int serverPrice = response.getInt("fee");

                                Intent intent = new Intent(CameraResultActivity.this, StickerPriceActivity.class);
                                intent.putExtra("imagePath", imagePath);
                                intent.putExtra("result", category);
                                intent.putExtra("price", serverPrice);
                                startActivity(intent);

                            } catch (JSONException e) {
                                Toast.makeText(this, "서버 응답 파싱 실패", Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> Toast.makeText(this, "서버 요청 실패", Toast.LENGTH_SHORT).show()
                );

                Volley.newRequestQueue(CameraResultActivity.this).add(request);

            } catch (Exception e) {
                Toast.makeText(CameraResultActivity.this, "숫자를 정확히 입력해주세요", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("취소", (innerDialog, whichButton) -> innerDialog.cancel());
        builder.show();
    }

}