package com.hackproff.fifedit;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(FloatPlugin.class);
        super.onCreate(savedInstanceState);
        try {
            android.webkit.WebView w = getBridge().getWebView();
            w.addJavascriptInterface(new DirectIO(this, w), "DirectIO");
        } catch (Exception e) {
        }
        try {
            if (android.os.Build.VERSION.SDK_INT >= 33
                    && checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 77);
            }
        } catch (Exception e) {
        }
    }
}
