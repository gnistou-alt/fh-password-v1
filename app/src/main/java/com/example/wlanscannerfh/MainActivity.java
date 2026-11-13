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
import android.widget.LinearLayout;
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
    private ArrayAdapter<String> adapter;
    private ArrayList<String> networkList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // بناء واجهة Cyberpunk سوداء وحمراء برمجياً لتجنب أي أخطاء XML
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFF0B0B0F);
        layout.setPadding(30, 30, 30, 30);

        TextView title = new TextView(this);
        title.setText("FH PASSWORD\nby nsitou");
        title.setTextColor(0xFFFF1744);
        title.setTextSize(22);
        title.setGravity(android.view.Gravity.CENTER);
        title.setPadding(0, 0, 0, 30);
        layout.addView(title);

        Button btnScan = new Button(this);
        btnScan.setText("بحث عن شبكات fh 🔍");
        btnScan.setBackgroundColor(0xFFD50000);
        btnScan.setTextColor(0xFFFFFFFF);
        layout.addView(btnScan);

        EditText etInput = new EditText(this);
        etInput.setHint("أدخل الكود مثل: fh_b7c1e0");
        etInput.setTextColor(0xFFFFFFFF);
        etInput.setHintTextColor(0xFF78909C);
        layout.addView(etInput);

        Button btnConvert = new Button(this);
        btnConvert.setText("تحويل الكود");
        btnConvert.setBackgroundColor(0xFF37474F);
        btnConvert.setTextColor(0xFFFFFFFF);
        layout.addView(btnConvert);

        TextView tvResult = new TextView(this);
        tvResult.setText("النتيجة ستظهر هنا");
        tvResult.setTextColor(0xFF00E676);
        tvResult.setTextSize(16);
        tvResult.setPadding(0, 20, 0, 20);
        layout.addView(tvResult);

        ListView listView = new ListView(this);
        layout.addView(listView);

        setContentView(layout);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        networkList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, networkList);
        listView.setAdapter(adapter);

        // طلب الصلاحيات
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQ_CODE);
        }

        btnScan.setOnClickListener(v -> {
            networkList.clear();
            try {
                List<ScanResult> results = wifiManager.getScanResults();
                if (results != null) {
                    for (ScanResult r : results) {
                        if (r.SSID != null && r.SSID.toLowerCase().startsWith("fh")) {
                            networkList.add(r.SSID + " | Signal: " + r.level + " dBm");
                        }
                    }
                }
                if (networkList.isEmpty()) {
                    networkList.add("لا توجد شبكات fh قريبة أو تأكد من تفعيل الموقع");
                }
            } catch (Exception e) {
                networkList.add("خطأ في الفحص، تأكد من تفعيل الـ GPS");
            }
            adapter.notifyDataSetChanged();
        });

        btnConvert.setOnClickListener(v -> {
            String text = etInput.getText().toString().trim();
            if (text.startsWith("fh_")) {
                tvResult.setText("النتيجة: " + text.replace("fh_", "wlan_"));
            } else {
                tvResult.setText("خطأ: يجب أن يبدأ بـ fh_");
            }
        });
    }
}
