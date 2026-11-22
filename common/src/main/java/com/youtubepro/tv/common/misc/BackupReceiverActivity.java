package com.youtubepro.tv.common.misc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.liskovsoft.sharedutils.helpers.AppInfoHelpers;
import com.liskovsoft.sharedutils.helpers.PermissionHelpers;
import com.youtubepro.tv.common.R;

public class BackupReceiverActivity extends Activity {
    private BackupAndRestoreHelper mRestoreHelper;
    private Runnable mPendingHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRestoreHelper = new BackupAndRestoreHelper(this);

        // Android 11+ (API 30+) removed the need for storage permission
        // when accessing files via content:// URIs. Apps are granted temporary
        // access to the URI by the sender, so you can read the file without
        // READ_EXTERNAL_STORAGE.
        if (PermissionHelpers.hasStoragePermissions(this) || AppInfoHelpers.getRealSdkVersion(this) > 29) {
            confirmAndRestore();
        } else {
            mPendingHandler = this::confirmAndRestore;
            PermissionHelpers.verifyStoragePermissions(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        confirmAndRestore();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionHelpers.REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mPendingHandler != null) {
                    mPendingHandler.run();
                    mPendingHandler = null;
                }
            }
            finish();
        }
    }

    /**
     * Audit F-05: the ZIP usually comes from an external app (file manager, browser, messenger).
     * Never touch user data before an explicit user confirmation.
     */
    private void confirmAndRestore() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.backup_confirm_restore_title)
                .setMessage(R.string.backup_confirm_restore_message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> mRestoreHelper.handleIncomingZip(getIntent()))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> finish())
                .setOnCancelListener(dialog -> finish())
                .show();
    }
}
