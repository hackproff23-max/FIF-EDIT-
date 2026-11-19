package com.hackproff.fifedit;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class DirectPickActivity extends Activity {
    private String slot = "save";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        String s = getIntent().getStringExtra("slot");
        if (s != null) slot = s;
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("*/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(i, 1);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        boolean ok = false;
        if (res == RESULT_OK && data != null && data.getData() != null) {
            Uri u = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(u,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } catch (Exception e) {
            }
            getSharedPreferences("direct", 0).edit().putString(slot, u.toString()).apply();
            ok = true;
        }
        DirectIO.picked(slot, ok);
        finish();
    }
}
