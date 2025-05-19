package com.example.project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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

    private LocationManager locationManager; // 현위치 가져오기
    private Button mylocation; // 내 위치 버튼
    private FusedLocationProviderClient fusedLocationClient; // 위치 클라이언트

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapview);

        provinceSpinner = findViewById(R.id.provinceSpinner);
        citySpinner = findViewById(R.id.citySpinner);
        mylocation = findViewById(R.id.mylocation);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupProvince();
        setupCity();

        // 지도 초기화
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 하단 네비게이션
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

        // 내 위치 버튼 클릭
        mylocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                showMyLocation();
            }
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
        loadCsvMarkers(mMap, R.raw.micho); // 초기값: 미추홀구
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
                    firstPosition = position;
                }
            }
            reader.close();
            if (firstPosition != null) {
                float zoomLevel = 15.0f;
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPosition, zoomLevel));
            } else {
                Log.w("map", "CSV 위치 정보가 없습니다!");
            }

        } catch (Exception e) {
            Log.e("csv error", "csv 파일 읽기 실패!", e);
        }
    }

    private void showMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 10초마다 요청
        locationRequest.setFastestInterval(5000);

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || locationResult.getLastLocation() == null) {
                    Toast.makeText(GoogleMapActivity.this, "정확한 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Location location = locationResult.getLastLocation();
                LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(myLatLng).title("내 위치"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 16f));

                // 위치 업데이트 중지 (1회만 받도록)
                fusedLocationClient.removeLocationUpdates(this);
            }
        }, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showMyLocation();
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}