package com.youtubepro.tv.common.misc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build.VERSION;

import com.liskovsoft.sharedutils.helpers.DateHelper;
import com.liskovsoft.sharedutils.helpers.FileHelpers;
import com.liskovsoft.sharedutils.helpers.MessageHelpers;
import com.youtubepro.tv.common.R;
import com.youtubepro.tv.common.app.presenters.settings.BackupSettingsPresenter;
import com.youtubepro.tv.common.misc.MediaServiceManager.OnError;
import com.youtubepro.tv.common.misc.MotherActivity.OnResult;
import com.youtubepro.tv.common.prefs.GeneralData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BackupAndRestoreHelper implements OnResult {
    public static final String BACKUP_FOLDER_NAME = "YouTubeProBackup";
    private static final int REQ_PICK_FILES = 1001;
    private final Context mContext;
    private Runnable mOnSuccess;
    private final String[] mPreferredFileManagers = {
            "com.ghisler.android.TotalCommander",
            "com.lonelycatgames.Xplore",
            "com.alphainventor.filemanager",
            "pl.solidexplorer2"
    };

    public BackupAndRestoreHelper(Context context) {
        mContext = context;
    }

    public void exportAppMediaFolder() {
        File mediaDir = FileHelpers.getExternalMediaDirectory(mContext);
        File dataDir = new File(mediaDir, "data");
        if (!dataDir.exists() || FileHelpers.isEmpty(dataDir) || VERSION.SDK_INT < 29) return;

        String backupZipName = getSavedBackupZipName();

        MediaStoreFile file = new MediaStoreFile(mContext, backupZipName, BACKUP_FOLDER_NAME);
        if (!file.isWritable()) {
            backupZipName = createBackupZipNameWithTimestamp();
            getGeneralData().setBackupZipName(backupZipName);
            file = new MediaStoreFile(mContext, backupZipName, BACKUP_FOLDER_NAME);
        }

        if (!file.isWritable()) {
            deleteTimeStamp(); // User copied full old media directory (with the old timestamp)
            backupZipName = createBackupZipNameWithTimestamp();
            getGeneralData().setBackupZipName(backupZipName);
            file = new MediaStoreFile(mContext, backupZipName, BACKUP_FOLDER_NAME);
        }

        if (file.isWritable()) {
            // Audit F-01: write to a temp file first, then swap.
            // A failed/interrupted export must never leave a broken zip in the backup folder.
            final File zipFile = new File(mediaDir, backupZipName);
            final File tmpZipFile = new File(mediaDir, backupZipName + ".tmp");

            try {
                FileHelpers.delete(tmpZipFile);
                ZipHelper2.zipDirectory(dataDir, tmpZipFile);

                if (!tmpZipFile.exists() || tmpZipFile.length() == 0) {
                    throw new IOException("Failed to create backup zip");
                }

                if (zipFile.exists() && !zipFile.delete()) {
                    throw new IOException("Failed to replace the old backup zip");
                }
                if (!tmpZipFile.renameTo(zipFile)) {
                    throw new IOException("Failed to finalize backup zip");
                }

                file.copyFrom(zipFile);
                // Delete temporary zip
                zipFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
                FileHelpers.delete(tmpZipFile);
            }
        }

        //Uri uri = FileProvider.getUriForFile(
        //        mContext,
        //        mContext.getPackageName() + ".update_provider",
        //        zipFile
        //);
        //
        //try {
        //    openFileManager(uri);
        //} catch (Exception e) {
        //    // Activity launch may fail if called from background (e.g. WorkManager)
        //    e.printStackTrace();
        //}
    }

    private void openFileManager(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        //intent.setType("application/zip");
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PackageManager pm = mContext.getPackageManager();

        for (String pkg : mPreferredFileManagers) {
            Intent targeted = new Intent(intent);
            targeted.setPackage(pkg);
            if (targeted.resolveActivity(pm) != null) {
                mContext.grantUriPermission(pkg, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mContext.startActivity(targeted);
                return;
            }
        }

        mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.app_backup)));
    }

    /**
     * NOTE: The file picker relies on apps that support the Storage Access Framework (SAF).
     * At the moment, no known third-party file manager properly supports selecting ZIP
     * archives through this API, so the backup file may not appear in the picker.
     */
    public void importAppMediaFolder(Runnable onSuccess) {
        if (VERSION.SDK_INT < 19 || onSuccess == null) {
            return;
        }

        mOnSuccess = onSuccess;

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        //intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
        //        "application/zip",
        //        "application/x-zip-compressed"
        //});

        ((MotherActivity) mContext).addOnResult(this);

        ((Activity) mContext).startActivityForResult(intent, REQ_PICK_FILES);
    }

    @Override
    public void onResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_PICK_FILES && resultCode == Activity.RESULT_OK) {
            if (data == null) return;

            Uri uri = data.getData();
            if (uri == null && data.getClipData() != null) {
                uri = data.getClipData().getItemAt(0).getUri();
            }

            unpackTempZip(uri, () -> mOnSuccess.run(), null);
        }
    }

    public void handleIncomingZip(Intent intent) {
        if (intent == null) {
            return;
        }

        Uri zipUri = null;

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            zipUri = intent.getData();
        } else if (Intent.ACTION_SEND.equals(intent.getAction())) {
            zipUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        }

        unpackTempZip(
                zipUri,
                () -> BackupSettingsPresenter.instance(mContext).showLocalRestoreDialogApi30(),
                error -> MessageHelpers.showLongMessage(mContext, "Failed to restore backup: " + error.getMessage())
        );
    }

    /**
     * Audit F-01: validate the archive, unpack to a staging dir, then swap with rollback.
     * Existing data is deleted only after the new data is successfully extracted,
     * and the temp zip is removed only after a confirmed success.
     */
    public void unpackTempZip(File tempZip) throws IOException {
        if (!tempZip.exists()) {
            return;
        }

        // Target folder: /Android/media/<package>/data
        File mediaDir = FileHelpers.getExternalMediaDirectory(mContext);
        File dataDir = new File(mediaDir, "data");

        // Validate the archive structure before touching any existing data
        boolean hasDataRoot = ZipHelper2.hasRootDir(tempZip, "data");

        File stagingDir = new File(mediaDir, "data_restore_staging");
        File oldDataBackup = new File(mediaDir, "data_backup_old");
        FileHelpers.delete(stagingDir);
        FileHelpers.delete(oldDataBackup);

        boolean oldMoved = false;
        boolean newInstalled = false;
        try {
            if (hasDataRoot) {
                // Unpack ZIP with data folder
                ZipHelper2.unzip(tempZip, stagingDir);
                File extractedData = new File(stagingDir, "data");
                if (!extractedData.isDirectory() || FileHelpers.isEmpty(extractedData)) {
                    throw new IOException("Invalid backup structure: 'data' folder is missing or empty");
                }
                oldMoved = !dataDir.exists() || dataDir.renameTo(oldDataBackup);
                if (!oldMoved) {
                    throw new IOException("Failed to move the current data directory");
                }
                newInstalled = extractedData.renameTo(dataDir);
                if (!newInstalled) {
                    throw new IOException("Failed to install the restored data");
                }
            } else {
                // Seems we've packed the contents of the data dir not data itself
                ZipHelper2.unzip(tempZip, stagingDir);
                if (FileHelpers.isEmpty(stagingDir)) {
                    throw new IOException("Invalid backup: archive is empty");
                }
                oldMoved = !dataDir.exists() || dataDir.renameTo(oldDataBackup);
                if (!oldMoved) {
                    throw new IOException("Failed to move the current data directory");
                }
                newInstalled = stagingDir.renameTo(dataDir);
                if (!newInstalled) {
                    throw new IOException("Failed to install the restored data");
                }
            }
        } catch (Exception e) {
            // Rollback: bring the previous data back on any failure
            if (newInstalled) FileHelpers.delete(dataDir);
            if (oldMoved && !dataDir.exists() && oldDataBackup.exists()) {
                oldDataBackup.renameTo(dataDir);
            }
            FileHelpers.delete(stagingDir);
            FileHelpers.delete(oldDataBackup);
            throw e instanceof IOException ? (IOException) e : new IOException("Restore failed", e);
        }

        // Success: cleanup. Keep the temp zip deletion here (only after confirmed success).
        FileHelpers.delete(oldDataBackup);
        FileHelpers.delete(stagingDir);
        tempZip.delete();
    }

    private void unpackTempZip(Uri zipUri, Runnable onSuccess, OnError onError) {
        if (zipUri == null) {
            MessageHelpers.showLongMessage(mContext, "No ZIP received");
            return;
        }

        // Cannot use async calls because the app usually isn't started yet.
        try {
            unpackTempZip(zipUri);
            if (onSuccess != null) {
                onSuccess.run();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            if (onError != null) {
                onError.onError(ex);
            } else {
                MessageHelpers.showLongMessage(mContext, "Failed to restore backup: " + ex.getMessage());
            }
        }
    }

    private void unpackTempZip(Uri zipUri) throws IOException {
        if (zipUri == null) {
            return;
        }

        File mediaDir = FileHelpers.getExternalMediaDirectory(mContext);

        // Copy ZIP from URI to the temporary file
        String backupZipName = getSavedBackupZipName();
        File tempZip = new File(mediaDir, backupZipName);
        copyUriToFile(zipUri, tempZip);

        unpackTempZip(tempZip);
    }

    private void copyUriToFile(Uri uri, File outFile) {
        // Audit F-22: streams must be closed even when the copy fails
        try (InputStream in = mContext.getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Failed to copyUriToFile", e);
        }
    }

    private GeneralData getGeneralData() {
        return GeneralData.instance(mContext);
    }

    private String createBackupZipNameWithTimestamp() {
        return mContext.getPackageName() + "_" + getTimeStamp() + ".zip";
    }

    private String getTimeStamp() {
        File timestampFile = getTimestampFile();
        if (timestampFile.exists()) {
            return FileHelpers.getFileContents(timestampFile);
        }

        String timestamp = DateHelper.createTimestamp();
        FileHelpers.stringToFile(timestamp, timestampFile);
        return timestamp;
    }

    private void deleteTimeStamp() {
        File timestampFile = getTimestampFile();
        if (timestampFile.exists()) {
            timestampFile.delete();
        }
    }

    private File getTimestampFile() {
        File mediaDir = FileHelpers.getExternalMediaDirectory(mContext);
        File timestampFile = new File(mediaDir, "timestamp.txt");
        return timestampFile;
    }

    private String getSavedBackupZipName() {
        String oldBackupZipName = getGeneralData().getBackupZipName();
        if (oldBackupZipName == null || !oldBackupZipName.endsWith(".zip")) {
            oldBackupZipName = createBackupZipNameWithTimestamp();
            getGeneralData().setBackupZipName(oldBackupZipName);
        }
        return oldBackupZipName;
    }
}
