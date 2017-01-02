package io.nnsookwon.bathroom_wars;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by nnsoo on 1/1/2017.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent alertIntent = new Intent(context, AlertReceiver.class);
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            alarmManager.setInexactRepeating(AlarmManager.RTC,
                    System.currentTimeMillis() + AlarmManager.INTERVAL_DAY*12,
                    AlarmManager.INTERVAL_DAY*12, pendingIntent);
        }
    }
}
