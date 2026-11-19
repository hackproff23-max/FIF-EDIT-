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
    }
}
