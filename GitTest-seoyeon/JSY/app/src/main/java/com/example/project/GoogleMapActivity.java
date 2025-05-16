package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class GoogleMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Spinner provinceSpinner, citySpinner;
    private Map<String, Integer> csvFileMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapview);

        provinceSpinner = findViewById(R.id.provinceSpinner);
        citySpinner = findViewById(R.id.citySpinner);

        setupProvince();
        setupCity();

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

    private void setupProvince() {
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Collections.singletonList("인천"));
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        provinceSpinner.setAdapter(provinceAdapter);
        provinceSpinner.setEnabled(false); // 인천 고정
    }

    private void setupCity() {
        List<String> cities = new ArrayList<>();
        cities.add("구 선택해주세요");
        cities.add("연수구");
        cities.add("미추홀구");

        csvFileMap = new HashMap<>();
        csvFileMap.put("연수구", R.raw.yeonsu);
        csvFileMap.put("미추홀구", R.raw.micho);

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, cities);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(cityAdapter);

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedCity = cities.get(position);
                if (mMap != null && csvFileMap.containsKey(selectedCity)) {
                    mMap.clear();
                    loadCsvMarkers(mMap, csvFileMap.get(selectedCity));
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        loadCsvMarkers(mMap, R.raw.micho);
    }

    private void loadCsvMarkers(GoogleMap map, int csvResId) {
        try {
            InputStream is = getResources().openRawResource(csvResId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;

            reader.readLine(); // 헤더 건너뜀

            LatLng firstPosition = null;

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length != 4) continue;

                String name = tokens[0].trim();
                String address = tokens[1].trim();
                double lat = Double.parseDouble(tokens[2].trim());
                double lng = Double.parseDouble(tokens[3].trim());

                LatLng position = new LatLng(lat, lng);
                map.addMarker(new MarkerOptions()
                        .position(position)
                        .title(name)
                        .snippet(address));
                if (firstPosition == null) {
                    firstPosition = position; // 첫 번째 마커 기준으로 위치 저장
                }
            }
            reader.close();
            if (firstPosition != null) {
                float zoomLevel = 15.0f; // 줌 레벨
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPosition, zoomLevel));
            } else {
                Log.w("map", "CSV 위치 정보가 없습니다!");
            }



        } catch (Exception e) {
            Log.e("csv error", "csv 파일 읽기 실패!", e); // 로그 에러 확인용(테스트)
        }
    }
}