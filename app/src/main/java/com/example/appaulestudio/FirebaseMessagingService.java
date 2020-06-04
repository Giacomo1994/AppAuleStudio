package com.example.appaulestudio;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService{
    public int id=0;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String data = remoteMessage.getData().get("message");
        showNotification(data);
    }

    public void showNotification(String message){
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = Build.VERSION.SDK_INT >= 20 ? pm.isInteractive() : pm.isScreenOn(); // check if screen is on
        if (!isScreenOn) {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "myApp:notificationLock");
            wl.acquire(3000); //set your time in milliseconds
        }

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("gcmchn",
                    "Giacomo_channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DESCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "gcmchn")
                .setSmallIcon(R.drawable.notification) // notification icon
                .setContentTitle("StudyAround") // title for notification
                .setContentText(message)// message for notification
                .setAutoCancel(true); // clear notification after click
        Intent intent=null;
        if(message.contains("si è iscritto al gruppo") || message.contains("ha abbandonato il gruppo")
                || message.contains("ha aggiornato") || message.contains("docente ti ha rimosso") || message.contains("docente ha rimosso")) intent = new Intent(getApplicationContext(), GroupActivity.class);
        else if(message.contains("non parteciperà alla prenotazione")) intent = new Intent(getApplicationContext(), PrenotazioniAttiveActivity.class);
        else intent = new Intent(getApplicationContext(), Home.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(id, mBuilder.build());
        id++;
    }
}