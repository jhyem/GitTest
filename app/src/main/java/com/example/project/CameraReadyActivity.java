package com.example.project;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class CameraReadyActivity extends AppCompatActivity {

    //권한 요청할 때 식별자로 쓰이는 숫자 100
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_ready);

        //checkSelfPermission 권한 상태를 확인
        //Manifest.permission.CAMERA → 요청할 권한
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION); //권한이 없을 경우, 사용자에게 권한을 요청
        } else {
            moveToCameraCapture();
        }
    }

    //사용자가 권한 요청 팝업에서 허용/거부 했을 때 실행되는 콜백 메서드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                moveToCameraCapture();
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void moveToCameraCapture() {
        Intent intent = new Intent(CameraReadyActivity.this, CameraCaptureActivity.class);
        intent.putExtra("selectedCategory", getIntent().getStringExtra("selectedCategory"));
        startActivity(intent);
        finish();
    }
}
