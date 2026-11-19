package com.hackproff.fifedit;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.widget.Toast;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DirectIO {
    private final Context ctx;
    private final WebView web;
    static DirectIO requester;

    public DirectIO(Context c, WebView w) {
        ctx = c.getApplicationContext();
        web = w;
    }

    private Uri uriOf(String slot) {
        String s = ctx.getSharedPreferences("direct", 0).getString(slot, null);
        return s == null ? null : Uri.parse(s);
    }

    private File tmp(String slot) {
        return new File(ctx.getCacheDir(), "direct_" + slot.replaceAll("[^A-Za-z0-9]", "_") + ".tmp");
    }

    @JavascriptInterface
    public boolean has(String slot) {
        return uriOf(slot) != null;
    }

    @JavascriptInterface
    public String name(String slot) {
        Uri u = uriOf(slot);
        if (u == null) return "";
        Cursor c = null;
        try {
            c = ctx.getContentResolver().query(u, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (c != null && c.moveToFirst()) {
                String n = c.getString(0);
                if (n != null) return n;
            }
        } catch (Exception e) {
        } finally {
            if (c != null) c.close();
        }
        String p = u.getLastPathSegment();
        return p == null ? "" : p.substring(p.lastIndexOf('/') + 1);
    }

    @JavascriptInterface
    public int size(String slot) {
        Uri u = uriOf(slot);
        if (u == null) return -1;
        ParcelFileDescriptor p = null;
        try {
            p = ctx.getContentResolver().openFileDescriptor(u, "r");
            long n = p.getStatSize();
            return n > Integer.MAX_VALUE ? -1 : (int) n;
        } catch (Exception e) {
            return -1;
        } finally {
            try { if (p != null) p.close(); } catch (Exception e) { }
        }
    }

    @JavascriptInterface
    public void pick(String slot) {
        requester = this;
        Intent i = new Intent(ctx, DirectPickActivity.class);
        i.putExtra("slot", slot);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    @JavascriptInterface
    public String read(String slot, int off, int len) {
        Uri u = uriOf(slot);
        if (u == null) return "ERR:arquivo nao escolhido";
        ParcelFileDescriptor p = null;
        try {
            p = ctx.getContentResolver().openFileDescriptor(u, "r");
            FileInputStream in = new FileInputStream(p.getFileDescriptor());
            in.getChannel().position(off);
            byte[] b = new byte[len];
            int got = 0;
            while (got < len) {
                int r = in.read(b, got, len - got);
                if (r < 0) break;
                got += r;
            }
            if (got < len) {
                byte[] t = new byte[got];
                System.arraycopy(b, 0, t, 0, got);
                b = t;
            }
            return Base64.encodeToString(b, Base64.NO_WRAP);
        } catch (Exception e) {
            return "ERR:" + e.getMessage();
        } finally {
            try { if (p != null) p.close(); } catch (Exception e) { }
        }
    }

    @JavascriptInterface
    public String wchunk(String slot, String b64, boolean first) {
        try {
            FileOutputStream o = new FileOutputStream(tmp(slot), !first);
            o.write(Base64.decode(b64, Base64.DEFAULT));
            o.close();
            return "";
        } catch (Exception e) {
            return "ERR:" + e.getMessage();
        }
    }

    private void copyTo(Uri u, File f) throws Exception {
        InputStream in = ctx.getContentResolver().openInputStream(u);
        if (in == null) return;
        FileOutputStream o = new FileOutputStream(f);
        byte[] buf = new byte[65536];
        int n;
        while ((n = in.read(buf)) > 0) o.write(buf, 0, n);
        o.close();
        in.close();
    }

    private void backup(Uri u, String slot) {
        try {
            File d = ctx.getExternalFilesDir("backup");
            if (d == null) return;
            String nm = name(slot).replaceAll("[^A-Za-z0-9._-]", "_");
            File orig = new File(d, nm + ".original");
            if (!orig.exists()) copyTo(u, orig);
            copyTo(u, new File(d, nm + ".anterior"));
        } catch (Exception e) {
        }
    }

    @JavascriptInterface
    public String commit(String slot) {
        Uri u = uriOf(slot);
        if (u == null) return "ERR:arquivo nao escolhido";
        File t = tmp(slot);
        try {
            if (!t.exists()) return "ERR:nada para gravar";
            backup(u, slot);
            OutputStream o = ctx.getContentResolver().openOutputStream(u, "wt");
            if (o == null) return "ERR:sem acesso de escrita";
            FileInputStream in = new FileInputStream(t);
            byte[] buf = new byte[65536];
            int n;
            while ((n = in.read(buf)) > 0) o.write(buf, 0, n);
            in.close();
            o.flush();
            o.close();
            t.delete();
            return "";
        } catch (Exception e) {
            return "ERR:" + e.getMessage();
        }
    }

    @JavascriptInterface
    public boolean canFloat() {
        return Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(ctx);
    }

    @JavascriptInterface
    public String startFloat() {
        try {
            if (!canFloat()) {
                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + ctx.getPackageName()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(i);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override public void run() {
                        Toast.makeText(ctx, "Ative \"Sobrepor a outros apps\" para o FIF-EDIT e volte", Toast.LENGTH_LONG).show();
                    }
                });
                return "perm";
            }
            Intent s = new Intent(ctx, FloatService.class);
            if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(s); else ctx.startService(s);
            return "ok";
        } catch (Exception e) {
            return "ERR:" + e.getMessage();
        }
    }

    static void picked(String slot, boolean ok) {
        final DirectIO r = requester;
        if (r == null) return;
        final String js = "window.__directPicked&&window.__directPicked('" + slot + "'," + ok + ")";
        r.web.post(new Runnable() {
            @Override public void run() { r.web.evaluateJavascript(js, null); }
        });
    }
}
