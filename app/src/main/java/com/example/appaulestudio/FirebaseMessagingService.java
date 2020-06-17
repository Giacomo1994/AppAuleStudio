package com.example.appaulestudio;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService{
    public int id=0;
    SharedPreferences preferences;
    boolean logged;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String data = remoteMessage.getData().get("message");
        preferences = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        logged = preferences.getBoolean("logged", false);
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
        String title="StudyAround";
        String header="";
        String body="";
        if(message.contains("posti liberi")){
            header=message;
            body="";
        }
        else{
            int interruzione=message.indexOf(":");
            header=message.substring(0,interruzione);
            body=message.substring(interruzione+2);
        }
        //se prenotazione di gruppo aggiungo "premi sulla notifica per verificare la prenotazione"

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "gcmchn")
                .setSmallIcon(R.drawable.notification) // notification icon
                .setContentTitle(header) // title for notification
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body)) //text expandable for notification
                .setAutoCancel(true); // clear notification after click
        Intent intent=null;
        if(logged==false)
            intent = new Intent(getApplicationContext(), MainActivity.class);
        else if(message.contains("si è iscritto al gruppo") || message.contains("ha abbandonato il gruppo") || message.contains("ha aggiornato") || message.contains("sei stato rimosso") || message.contains("è stato rimosso"))
            intent = new Intent(getApplicationContext(), GroupActivity.class);
        else if(message.contains("non parteciperà alla prenotazione"))
            intent = new Intent(getApplicationContext(), PrenotazioniAttiveActivity.class);
        else
            intent = new Intent(getApplicationContext(), Home.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(id, mBuilder.build());
        id++;
    }
}