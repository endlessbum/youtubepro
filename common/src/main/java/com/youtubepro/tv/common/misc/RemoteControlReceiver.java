package com.youtubepro.tv.common.misc;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.liskovsoft.sharedutils.mylogger.Log;
import com.youtubepro.tv.common.utils.Utils;

public class RemoteControlReceiver extends BroadcastReceiver {
    private static final String TAG = RemoteControlReceiver.class.getSimpleName();
    public static final String EXTRA_RETRY_COUNT = "retry_count";
    private static final long RETRY_DELAY_MS = 15_000L;
    private static final int MAX_RETRIES = 1;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Initializing remote control listener...");

        // Fix unload from the memory on some devices?
        // NOTE: Starting from Android 12 (api 31) foreground service with type 'connectedDevice' not supported
        // Use 'mediaPlayback' type instead
        try {
            Utils.updateRemoteControlService(context);
        } catch (Exception e) {
            // ForegroundServiceStartNotAllowedException: startForegroundService() not allowed due to
            // mAllowStartForeground false (Android 12+): FGS can't start from background broadcasts.
            e.printStackTrace();

            // Audit F-06: don't fail silently - schedule one delayed retry instead.
            scheduleRetry(context, intent);
        }
    }

    /**
     * Audit F-06: on Android 12+ a foreground service can't be started from an arbitrary
     * background broadcast. Schedule a single delayed retry through AlarmManager
     * (no exact-alarm permission needed for inexact set()). The retry is allowed to
     * fail silently: max one retry, no loops.
     */
    private void scheduleRetry(Context context, Intent originalIntent) {
        int count = originalIntent.getIntExtra(EXTRA_RETRY_COUNT, 0) + 1;
        if (count > MAX_RETRIES) {
            Log.d(TAG, "Retry limit reached, giving up until the next system event");
            return;
        }

        Intent retry = new Intent(context, RemoteControlReceiver.class);
        retry.putExtras(originalIntent);
        retry.putExtra(EXTRA_RETRY_COUNT, count);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent operation = PendingIntent.getBroadcast(context, 0, retry, flags);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + RETRY_DELAY_MS, operation);
            Log.d(TAG, "FGS start rejected; retry scheduled in " + RETRY_DELAY_MS / 1000 + "s (attempt " + count + ")");
        }
    }
}
