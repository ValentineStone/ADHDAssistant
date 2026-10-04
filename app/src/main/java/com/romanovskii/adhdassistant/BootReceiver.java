package com.romanovskii.adhdassistant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Ensure we are handling the correct system broadcast
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            // 1. Check SharedPreferences to see if the alarm loop should be active
            SharedPreferences prefs = context.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
            boolean isAlarmEnabled = prefs.getBoolean("alarm_enabled", false); // defaults to false

            // 2. Abort if the user turned the alarm off before the reboot
            if (!isAlarmEnabled) {
                return;
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {

                // Safety Check: Verify exact alarm permissions (Android 12+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    return; // Abort if user revoked permissions while phone was on
                }

                Intent alarmIntent = new Intent(context, AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        alarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                // Set first post-boot alarm to fire 30 minutes from now
                long triggerTimeInMs = System.currentTimeMillis() + MainActivity.ALARM_INTERVAL;

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeInMs,
                        pendingIntent
                );
            }
        }
    }
}
