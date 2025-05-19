package com.example.project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.*;
import java.util.*;

public class GoogleMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Spinner provinceSpinner, citySpinner;
    private Map<String, Integer> csvFileMap;
    private Button mylocation;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapview);

        provinceSpinner = findViewById(R.id.provinceSpinner);
        citySpinner = findViewById(R.id.citySpinner);
        mylocation = findViewById(R.id.mylocation);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        checkAndRequestLocationPermission();
        setupProvince();
        setupCity();


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

        mylocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                checkGpsAndShowLocation();
            }
        });
    }

    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void checkGpsAndShowLocation() {
        if (!isGpsEnabled()) {
            Toast.makeText(this, "GPS가 꺼져 있어요", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        } else {
            requestRealTimeLocation(); // 실제 위치 요청
        }
    }

    private void requestRealTimeLocation() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null || locationResult.getLastLocation() == null) {
                    Toast.makeText(GoogleMapActivity.this, "위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //  파란 점 표시
                Location location = locationResult.getLastLocation();
                LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 16f));

                fusedLocationClient.removeLocationUpdates(this);
            }
        }, null);
    }

    private void setupProvince() {
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Collections.singletonList("인천"));
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        provinceSpinner.setAdapter(provinceAdapter);
        provinceSpinner.setEnabled(false);
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true); // 파란 점 표시 활성화
        }
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
                map.addMarker(new MarkerOptions().position(position).title(name).snippet(address));
                if (firstPosition == null) {
                    firstPosition = position;
                }
            }
            reader.close();
            if (firstPosition != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPosition, 15.0f));
            }
        } catch (Exception e) {
            Log.e("csv error", "CSV 파일 읽기 실패!", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true); // 파란 점 표시
                    }
                }
                checkGpsAndShowLocation();
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
