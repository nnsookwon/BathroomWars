package io.nnsookwon.bathroom_wars;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by nnsoo on 12/30/2016.
 */

public class AlertReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String appName = context.getResources().getString(R.string.app_name);
        String reminder = context.getResources().getString(R.string.reminder);
        createNotification(context, appName, reminder, reminder);
    }

    public void createNotification(Context context, String msg, String msgText, String msgAlert) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.logo_24_24)
                .setContentTitle(msg)
                .setTicker(msgAlert)
                .setContentText(msgText);

        builder.setContentIntent(pendingIntent);
        builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
        builder.setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }
}
