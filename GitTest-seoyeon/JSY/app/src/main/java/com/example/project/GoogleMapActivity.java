package com.example.project;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

        //인천광역시 미추홀구_대형폐기물 스티커 판매소에 있는 호산할인마트
        LatLng seoul = new LatLng(37.4475941000, 126.6938985000);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 15));

        // 마커 표시
        mMap.addMarker(new MarkerOptions()
                .position(seoul)
                .title("인천 미추홀구")
                .snippet("스티커 판매소-호산 할인마트"));
    }
}
