package com.example.wlanscannerfh;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private ListView lvNetworks;
    private ArrayList<String> networkList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        lvNetworks = findViewById(R.id.lvNetworks);
        Button btnScan = findViewById(R.id.btnScan);

        networkList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, networkList);
        lvNetworks.setAdapter(adapter);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }

        btnScan.setOnClickListener(v -> {
            networkList.clear();
            try {
                List<ScanResult> results = wifiManager.getScanResults();
                if (results != null) {
                    for (ScanResult r : results) {
                        if (r.SSID != null && r.SSID.toLowerCase().startsWith("fh")) {
                            networkList.add(r.SSID + " (" + r.level + " dBm)");
                        }
                    }
                }
                if (networkList.isEmpty()) {
                    networkList.add("لا توجد شبكات fh أو الموقع مغلق");
                }
            } catch (Exception e) {
                networkList.add("خطأ في فحص الشبكات");
            }
            adapter.notifyDataSetChanged();
        });
    }
}

