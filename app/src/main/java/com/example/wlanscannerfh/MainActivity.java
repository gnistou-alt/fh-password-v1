package com.example.wlanscannerfh;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQ_CODE = 123;
    private WifiManager wifiManager;
    private ListView lvNetworks;
    private ArrayList<String> networkList;
    private ArrayAdapter<String> adapter;
    
    private EditText etInputCode;
    private TextView tvConvertedResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        lvNetworks = findViewById(R.id.lvNetworks);
        Button btnScan = findViewById(R.id.btnScan);
        etInputCode = findViewById(R.id.etInputCode);
        Button btnConvert = findViewById(R.id.btnConvert);
        tvConvertedResult = findViewById(R.id.tvConvertedResult);

        networkList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, networkList);
        lvNetworks.setAdapter(adapter);

        checkPermissions();

        btnScan.setOnClickListener(v -> scanWiFiNetworks());

        btnConvert.setOnClickListener(v -> {
            String input = etInputCode.getText().toString().trim();
            if (input.startsWith("fh_")) {
                String converted = input.replace("fh_", "wlan_");
                tvConvertedResult.setText("النتيجة: " + converted);
            } else {
                tvConvertedResult.setText("يجب أن يبدأ الكود بـ fh_");
            }
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE
            }, PERMISSION_REQ_CODE);
        }
    }

    private void scanWiFiNetworks() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions();
            Toast.makeText(MainActivity.this, "يرجى منح صلاحية الموقع للبحث", Toast.LENGTH_SHORT).show();
            return;
        }

        networkList.clear();
        try {
            if (wifiManager != null) {
                List<ScanResult> results = wifiManager.getScanResults();
                if (results != null && !results.isEmpty()) {
                    for (ScanResult result : results) {
                        String ssid = result.SSID;
                        if (ssid != null && ssid.toLowerCase().startsWith("fh")) {
                            int signalLevel = result.level;
                            networkList.add(ssid + " | Power: " + signalLevel + " dBm");
                        }
                    }
                }
            }
            if (networkList.isEmpty()) {
                networkList.add("لا توجد شبكات fh قريبة أو تفعيل الموقع مغلق");
            }
        } catch (Exception e) {
            networkList.add("خطأ في جلب الشبكات، تأكد من تشغيل الموقع (GPS)");
        }
        adapter.notifyDataSetChanged();
    }
}
