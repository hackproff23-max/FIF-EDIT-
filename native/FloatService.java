package com.hackproff.fifedit;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;

public class FloatService extends Service {
    static FloatService inst;
    private WindowManager wm;
    private TextView bubble;
    private LinearLayout panel;
    private WebView web;
    private WindowManager.LayoutParams bp, pp;
    private boolean shown = false;
    private ValueCallback<Uri[]> fileCb;

    static void deliver(Uri[] uris) {
        if (inst != null && inst.fileCb != null) {
            inst.fileCb.onReceiveValue(uris);
            inst.fileCb = null;
        }
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }

    private int overlayType() {
        return Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
    }

    @Override public IBinder onBind(Intent i) { return null; }
    @Override public int onStartCommand(Intent i, int f, int id) { return START_NOT_STICKY; }

    @Override
    public void onCreate() {
        super.onCreate();
        inst = this;
        startFg();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        buildBubble();
        buildPanel();
        wm.addView(bubble, bp);
    }

    private void startFg() {
        String ch = "fifedit";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(ch, "FIF-EDIT flutuante", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(c);
        }
        Notification.Builder nb = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, ch) : new Notification.Builder(this);
        nb.setContentTitle("FIF-EDIT flutuante").setContentText("Toque na bolinha para abrir o editor")
                .setSmallIcon(android.R.drawable.ic_menu_edit);
        Notification n = nb.build();
        if (Build.VERSION.SDK_INT >= 34) startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        else startForeground(1, n);
    }

    private void buildBubble() {
        bubble = new TextView(this);
        bubble.setText("⚽");
        bubble.setTextSize(26);
        bubble.setGravity(Gravity.CENTER);
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(0xEE0B3D2E);
        g.setStroke(dp(2), Color.WHITE);
        bubble.setBackground(g);
        bp = new WindowManager.LayoutParams(dp(56), dp(56), overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        bp.gravity = Gravity.TOP | Gravity.START;
        bp.x = 0;
        bp.y = dp(120);
        bubble.setOnTouchListener(new View.OnTouchListener() {
            int sx, sy; float tx, ty; boolean moved;
            @Override public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        sx = bp.x; sy = bp.y; tx = e.getRawX(); ty = e.getRawY(); moved = false; return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = e.getRawX() - tx, dy = e.getRawY() - ty;
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) moved = true;
                        if (moved) { bp.x = sx + (int) dx; bp.y = sy + (int) dy; wm.updateViewLayout(bubble, bp); }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved) toggle();
                        return true;
                }
                return false;
            }
        });
    }

    private void toggle() {
        if (shown) { wm.removeView(panel); shown = false; }
        else { wm.addView(panel, pp); shown = true; }
    }

    private void buildPanel() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int w = Math.min(dm.widthPixels, Math.max(dp(380), (int) (dm.widthPixels * 0.5)));
        int h = (int) (dm.heightPixels * 0.85);
        pp = new WindowManager.LayoutParams(w, h, overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        pp.gravity = Gravity.TOP | Gravity.START;
        pp.x = dp(70);
        pp.y = dp(10);
        pp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;

        panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundColor(0xFF0B1620);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setBackgroundColor(0xFF0B3D2E);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = new TextView(this);
        title.setText("  ⚽ FIF-EDIT  (arraste aqui)");
        title.setTextColor(Color.WHITE);
        title.setTextSize(14);
        title.setOnTouchListener(new View.OnTouchListener() {
            int sx, sy; float tx, ty;
            @Override public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN: sx = pp.x; sy = pp.y; tx = e.getRawX(); ty = e.getRawY(); return true;
                    case MotionEvent.ACTION_MOVE:
                        pp.x = sx + (int) (e.getRawX() - tx); pp.y = sy + (int) (e.getRawY() - ty);
                        if (shown) wm.updateViewLayout(panel, pp);
                        return true;
                }
                return true;
            }
        });
        bar.addView(title, new LinearLayout.LayoutParams(0, dp(44), 1f));
        Button min = new Button(this);
        min.setText("–");
        min.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { if (shown) toggle(); } });
        bar.addView(min, new LinearLayout.LayoutParams(dp(56), dp(44)));
        Button close = new Button(this);
        close.setText("✕");
        close.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { stopSelf(); } });
        bar.addView(close, new LinearLayout.LayoutParams(dp(56), dp(44)));
        panel.addView(bar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));

        web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(true);
        web.getSettings().setAllowContentAccess(true);
        web.addJavascriptInterface(new NativeBridge(), "Native");
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (fileCb != null) fileCb.onReceiveValue(null);
                fileCb = cb;
                Intent i = new Intent(FloatService.this, PickerActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return true;
            }
            @Override public boolean onJsAlert(WebView v, String u, String m, JsResult r) { dlg(m, null, null, r, false); return true; }
            @Override public boolean onJsConfirm(WebView v, String u, String m, JsResult r) { dlg(m, null, null, r, true); return true; }
            @Override public boolean onJsPrompt(WebView v, String u, String m, String d, JsPromptResult r) { dlg(m, d, r, r, true); return true; }
        });
        web.loadUrl("file:///android_asset/public/index.html");
        panel.addView(web, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
    }

    private void dlg(String msg, String def, final JsPromptResult pr, final JsResult jr, boolean cancelable) {
        AlertDialog.Builder b = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Material_Light_Dialog_Alert));
        b.setMessage(msg);
        final EditText et = pr != null ? new EditText(this) : null;
        if (et != null) { et.setText(def == null ? "" : def); b.setView(et); }
        b.setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
            @Override public void onClick(android.content.DialogInterface d, int w) {
                if (pr != null) pr.confirm(et.getText().toString()); else jr.confirm();
            }
        });
        if (cancelable) b.setNegativeButton("Cancelar", new android.content.DialogInterface.OnClickListener() {
            @Override public void onClick(android.content.DialogInterface d, int w) { jr.cancel(); }
        });
        b.setOnCancelListener(new android.content.DialogInterface.OnCancelListener() {
            @Override public void onCancel(android.content.DialogInterface d) { jr.cancel(); }
        });
        AlertDialog d = b.create();
        d.getWindow().setType(overlayType());
        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        d.show();
    }

    private class NativeBridge {
        @JavascriptInterface
        public void save(String name, String b64) {
            try {
                File f = new File(getCacheDir(), name.replaceAll("[^A-Za-z0-9._-]", "_"));
                FileOutputStream o = new FileOutputStream(f);
                o.write(Base64.decode(b64, Base64.DEFAULT));
                o.close();
                Uri u = FileProvider.getUriForFile(FloatService.this, getPackageName() + ".fileprovider", f);
                Intent s = new Intent(Intent.ACTION_SEND);
                s.setType("application/octet-stream");
                s.putExtra(Intent.EXTRA_STREAM, u);
                s.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Intent c = Intent.createChooser(s, "Salvar " + name);
                c.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(c);
            } catch (Exception e) {
                Toast.makeText(FloatService.this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        try { if (shown) wm.removeView(panel); } catch (Exception e) { }
        try { wm.removeView(bubble); } catch (Exception e) { }
        if (web != null) web.destroy();
        inst = null;
        super.onDestroy();
    }
}
