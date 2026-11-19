package com.hackproff.fifedit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "Float")
public class FloatPlugin extends Plugin {
    @PluginMethod
    public void start(PluginCall call) {
        Context c = getContext();
        JSObject r = new JSObject();
        if (!Settings.canDrawOverlays(c)) {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + c.getPackageName()));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(i);
            r.put("started", false);
            r.put("needsPermission", true);
            call.resolve(r);
            return;
        }
        Intent s = new Intent(c, FloatService.class);
        if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(s); else c.startService(s);
        r.put("started", true);
        call.resolve(r);
    }

    @PluginMethod
    public void stop(PluginCall call) {
        getContext().stopService(new Intent(getContext(), FloatService.class));
        call.resolve();
    }
}
