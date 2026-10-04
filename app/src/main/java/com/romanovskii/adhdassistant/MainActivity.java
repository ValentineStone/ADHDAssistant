package com.romanovskii.adhdassistant;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button alarmBtn;

    public static final int ALARM_INTERVAL = 30 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarmBtn = findViewById(R.id.alarmBtn);

        alarmBtn.setOnClickListener(view -> {
            SharedPreferences prefs = getApplication().getApplicationContext().getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
            boolean isAlarmEnabled = prefs.getBoolean("alarm_enabled", false);

            if (isAlarmEnabled) {
                stopAlarm();
            } else {
                startAlarm();
            }
            updateButtonText();
        });

        updateButtonText();
    }

    void updateButtonText() {
        SharedPreferences prefs = getApplication().getApplicationContext().getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
        boolean isAlarmEnabled = prefs.getBoolean("alarm_enabled", false);

        if (isAlarmEnabled) {
            alarmBtn.setText("Stop alarm!");
        } else {
            alarmBtn.setText("Start alarm!");
        }
    }


    void startAlarm() {
        // 1. Get the AlarmManager system service
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            // 1. Android 12+ Safety Check: Verify if the app can schedule exact alarms
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    // Redirect user to the system settings page to turn on the permission
                    Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(intent);

                    Toast.makeText(this, "Please enable exact alarm permission", Toast.LENGTH_SHORT).show();
                    return; // Stop execution until permission is granted
                }
            }

            // 2. Create an Intent pointing to your BroadcastReceiver (e.g., AlarmReceiver.class)
            Intent intent = new Intent(this, AlarmReceiver.class);

            // 3. Wrap it in a PendingIntent
            // Note: Use PendingIntent.FLAG_IMMUTABLE or FLAG_MUTABLE depending on your needs (Android 12+)
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // 4. Set the trigger time (e.g., 10 seconds from now)
            long triggerTimeInMs = System.currentTimeMillis() + ALARM_INTERVAL;

            // SAVE THE STATE AS TRUE
            getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("alarm_enabled", true)
                    .apply();


            try {
                // 5. Schedule the exact alarm that triggers even in Doze mode
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeInMs,
                        pendingIntent
                );
                Toast.makeText(this, "Will alarm every 30 minutes", Toast.LENGTH_SHORT).show();
            } catch (SecurityException e) {
                // Defend against edge cases or vendor-specific bugs
                e.printStackTrace();
                Toast.makeText(this, "Failed to schedule exact alarm due to permission.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void stopAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            // 1. Recreate the EXACT same Intent structure used to set the alarm
            Intent intent = new Intent(this, AlarmReceiver.class);

            // 2. Recreate the EXACT same PendingIntent matching your Request Code (0)
            // We use FLAG_NO_CREATE to check if the alarm is currently active,
            // or FLAG_IMMUTABLE to safely fetch/target it.
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (pendingIntent != null) {
                // 3. Cancel the alarm in the system framework
                alarmManager.cancel(pendingIntent);

                // 4. Cancel the PendingIntent itself so the system cleans it up
                pendingIntent.cancel();

                // SAVE THE STATE AS FALSE
                getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("alarm_enabled", false)
                        .apply();

                Toast.makeText(this, "Alarm loop stopped!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No active alarm loop found.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}