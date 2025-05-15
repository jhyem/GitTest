package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.List;

public class GoogleMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapview);

        // 지도 초기화
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 하단 네비게이션 처리
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, SelectItemActivity.class));
                return true;
            } else if (id == R.id.nav_chatbot) {
                startActivity(new Intent(this, ChatbotActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // 10개 스티커 판매소 위치
        List<LatLng> locations = Arrays.asList(
                new LatLng(37.4475941, 126.6938985), // 호산할인마트
                new LatLng(37.4467524, 126.6918178), // 조광할인마트
                new LatLng(37.445863, 126.6918487),  // 불티나슈퍼
                new LatLng(37.4455375, 126.6908963), // 대경마트
                new LatLng(37.4450433, 126.6946155), // 씨유 관교동아
                new LatLng(37.444183, 126.696813),   // 이마트24 관교승학점
                new LatLng(37.4438066, 126.6956953), // 한아름 마트
                new LatLng(37.442551, 126.6945463),  // 한우촌웰빙할인마트
                new LatLng(37.4411296, 126.6954341), // 굿모닝 할인마트
                new LatLng(37.4663049, 126.6619925)  // 고려 편의점
        );

        List<String> titles = Arrays.asList(
                "호산할인마트", "조광할인마트", "불티나슈퍼", "대경마트", "씨유 관교동아",
                "이마트24 관교승학점", "한아름 마트", "한우촌웰빙할인마트", "굿모닝 할인마트", "고려 편의점"
        );

        List<String> snippets = Arrays.asList(
                "인하로411번길 25", "인하로396번길 19", "관교동473-13", "주승로183",
                "인하로430번길9 동아아파트", "관교동", "관교동 13-6", "주승로 232",
                "경원대로640번길 30", "경인로 173"
        );

        // 지도 전체 확대 범위
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        for (int i = 0; i < locations.size(); i++) {
            mMap.addMarker(new MarkerOptions()
                    .position(locations.get(i))
                    .title(titles.get(i))
                    .snippet(snippets.get(i)));
            boundsBuilder.include(locations.get(i));
        }

        // 마커 전체가 보이는 화면
        LatLngBounds bounds = boundsBuilder.build();
        int padding = 100;
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

        // 확대/축소 버튼 UI 추가
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
}