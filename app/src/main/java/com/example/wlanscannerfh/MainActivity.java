package com.example.wlanscannerfh;

import android.app.AlertDialog;
import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.wifi.*;
import android.location.LocationManager;
import android.provider.Settings;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private WifiManager wifi;
    private LinearLayout list;
    private TextView status, empty;
    private Button scanButton;
    private final int REQ = 41;
    private final Map<String, ScanResult> found = new LinkedHashMap<>();
    private BroadcastReceiver receiver;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        getWindow().setStatusBarColor(0xFF101827);
        getWindow().setNavigationBarColor(0xFF101827);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(28), dp(20), dp(16));
        page.setBackgroundColor(0xFFF4F6FA);

        TextView title = new TextView(this);
        title.setText("WLAN Scanner FH");
        title.setTextSize(25); title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF142033);
        page.addView(title);

        TextView sub = new TextView(this);
        sub.setText("يعرض شبكات Wi-Fi التي يبدأ اسمها بـ fh فقط");
        sub.setTextSize(14); sub.setTextColor(0xFF596579);
        LinearLayout.LayoutParams sm = params(-1,-2); sm.topMargin=dp(6);
        page.addView(sub,sm);

        scanButton = new Button(this);
        scanButton.setText("بحث عن شبكات fh");
        LinearLayout.LayoutParams bm=params(-1,-2); bm.topMargin=dp(20);
        page.addView(scanButton,bm);

        status = new TextView(this);
        status.setText("اضغط بحث لعرض الشبكات القريبة.");
        status.setTextSize(13); status.setTextColor(0xFF596579);
        LinearLayout.LayoutParams stm=params(-1,-2); stm.topMargin=dp(12);
        page.addView(status,stm);

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lm=params(-1,-1); lm.topMargin=dp(10);
        scroll.addView(list);
        page.addView(scroll,lm);

        empty = new TextView(this);
        empty.setText("لا توجد نتائج بعد.");
        empty.setTextColor(0xFF667085); empty.setGravity(Gravity.CENTER);
        empty.setPadding(0,dp(35),0,dp(35));
        list.addView(empty);

        scanButton.setOnClickListener(v -> beginScan());
        setContentView(page);
        receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context c, Intent i) {
                boolean updated = i.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                showResults(updated);
            }
        };
        registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private void beginScan() {
        if (!hasPermissions()) {
            requestNeededPermissions();
            return;
        }
        if (!wifi.isWifiEnabled()) {
            status.setText("فعّل Wi-Fi من إعدادات الهاتف ثم اضغط البحث مجدداً.");
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            return;
        }
        if (Build.VERSION.SDK_INT < 33 && !locationEnabled()) {
            status.setText("فعّل خدمات الموقع للسماح لأندرويد بعرض نتائج مسح Wi-Fi.");
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }
        scanButton.setEnabled(false);
        status.setText("جارٍ البحث… قد يقيّد أندرويد تكرار عمليات المسح.");
        boolean started = wifi.startScan();
        if (!started) {
            scanButton.setEnabled(true);
            showResults(false);
            status.setText("لم يبدأ مسح جديد؛ أعرض آخر نتائج متاحة إن وجدت. جرّب لاحقاً.");
        }
    }

    private boolean hasPermissions() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return false;
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) return false;
        return true;
    }

    private void requestNeededPermissions() {
        ArrayList<String> p = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            p.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED)
            p.add(Manifest.permission.NEARBY_WIFI_DEVICES);
        requestPermissions(p.toArray(new String[0]), REQ);
    }

    @Override public void onRequestPermissionsResult(int r, String[] p, int[] g) {
        super.onRequestPermissionsResult(r,p,g);
        if (r == REQ) {
            if (hasPermissions()) beginScan();
            else status.setText("يلزم السماح بإذن الموقع وWi-Fi القريب لعرض أسماء الشبكات.");
        }
    }

    private boolean locationEnabled() {
        LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showResults(boolean fresh) {
        scanButton.setEnabled(true);
        found.clear();
        try {
            for (ScanResult sr : wifi.getScanResults()) {
                String s = sr.SSID;
                if (s != null && s.toLowerCase(Locale.ROOT).startsWith("fh")) {
                    String key = s + "|" + sr.BSSID;
                    ScanResult old = found.get(key);
                    if (old == null || sr.level > old.level) found.put(key, sr);
                }
            }
        } catch (SecurityException e) {
            status.setText("لا يوجد إذن كافٍ لقراءة نتائج Wi-Fi.");
            return;
        }
        list.removeAllViews();
        if (found.isEmpty()) {
            TextView none = new TextView(this);
            none.setText("لم يتم العثور على شبكة يبدأ اسمها بـ fh. تأكد من تشغيل Wi-Fi، وجرّب مرة أخرى.");
            none.setTextColor(0xFF667085); none.setPadding(0,dp(24),0,dp(24));
            list.addView(none);
        } else {
            for (ScanResult sr : found.values()) addNetworkCard(sr);
        }
        status.setText("الشبكات المطابقة: " + found.size() + (fresh ? " · نتائج مسح حديث" : " · آخر نتائج متاحة"));
    }

    private void addNetworkCard(ScanResult sr) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15),dp(13),dp(15),dp(13));
        card.setBackgroundColor(0xFFFFFFFF);
        LinearLayout.LayoutParams cpar=params(-1,-2); cpar.bottomMargin=dp(10);
        list.addView(card,cpar);

        TextView name = new TextView(this);
        name.setText(sr.SSID); name.setTextSize(18);
        name.setTypeface(null,Typeface.BOLD); name.setTextColor(0xFF142033);
        card.addView(name);

        int bars = WifiManager.calculateSignalLevel(sr.level, 5);
        TextView details = new TextView(this);
        details.setText("الإشارة: " + bars + "/4  ·  " + security(sr.capabilities));
        details.setTextSize(13); details.setTextColor(0xFF596579);
        LinearLayout.LayoutParams dpar=params(-1,-2); dpar.topMargin=dp(5);
        card.addView(details,dpar);

        Button analyze = new Button(this);
        analyze.setText("تحليل الاسم");
        LinearLayout.LayoutParams apar=params(-1,-2); apar.topMargin=dp(8);
        card.addView(analyze,apar);
        analyze.setOnClickListener(v -> showAnalysis(sr.SSID));
    }

    private String security(String c) {
        if (c == null || c.isEmpty()) return "نوع الحماية غير معروف";
        if (c.contains("WPA3")) return "WPA3";
        if (c.contains("WPA2")) return "WPA2";
        if (c.contains("WPA")) return "WPA";
        if (c.contains("WEP")) return "WEP";
        if (c.contains("ESS")) return "مفتوحة أو غير محددة";
        return c;
    }

    private void showAnalysis(String ssid) {
        String[] parts = ssid.split("_", -1);
        if (parts.length != 2 || !parts[0].equalsIgnoreCase("fh") || parts[1].isEmpty()) {
            new AlertDialog.Builder(this).setTitle(ssid)
                .setMessage("تم العثور على الاسم، لكن صيغته لا تطابق fh_ متبوعة برموز hex.")
                .setPositiveButton("حسناً",null).show();
            return;
        }
        StringBuilder out = new StringBuilder();
        for (char c : parts[1].toLowerCase(Locale.ROOT).toCharArray()) {
            if (c >= '0' && c <= '9') out.append((char)('f' - (c - '0')));
            else if (c >= 'a' && c <= 'f') out.append((char)('5' - (c - 'a')));
            else {
                new AlertDialog.Builder(this).setTitle("صيغة غير مدعومة")
                    .setMessage("الجزء بعد fh_ يجب أن يحتوي على أرقام hex فقط (0–9 و a–f).")
                    .setPositiveButton("حسناً",null).show();
                return;
            }
        }
        String result = "wlan_" + out;
        new AlertDialog.Builder(this).setTitle("تحليل اسم الشبكة")
            .setMessage("الاسم: " + ssid + "\nالنص المحوّل: " + result +
                "\n\nهذا تحويل للاسم فقط، وليس كلمة مرور مؤكدة أو اختباراً للاتصال.")
            .setPositiveButton("نسخ", (d,w) -> {
                ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("Converted name",result));
                Toast.makeText(this,"تم النسخ",Toast.LENGTH_SHORT).show();
            }).setNegativeButton("إغلاق",null).show();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            try { unregisterReceiver(receiver); } catch (Exception ignored) {}
        }
    }
    private LinearLayout.LayoutParams params(int w,int h) { return new LinearLayout.LayoutParams(w,h); }
    private int dp(int v) { return (int)(v*getResources().getDisplayMetrics().density+0.5f); }
}
