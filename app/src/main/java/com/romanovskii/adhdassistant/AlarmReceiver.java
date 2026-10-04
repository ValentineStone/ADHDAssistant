package com.romanovskii.adhdassistant;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // 1. PERFORM YOUR TASK (e.g., Play the Beep)
        ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 500);

        // 2. CHAIN THE NEXT ALARM (Schedule for 30 minutes from now)
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {

            // Check permission again for modern Android versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                return; // Stop loop if the user revoked permission in settings
            }

            Intent nextIntent = new Intent(context, AlarmReceiver.class);
            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            long nextTriggerTime = System.currentTimeMillis() + MainActivity.ALARM_INTERVAL;

            // Schedule the next link in the chain
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerTime,
                    nextPendingIntent
            );
        }
    }
}