package com.example.appaulestudio;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

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

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        SharedPreferences settings = getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("token",s);
        editor.commit();
        Log.e("myLog", "new token");

    }

    public void showNotification(String message){
        //accensione schermo
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = Build.VERSION.SDK_INT >= 20 ? pm.isInteractive() : pm.isScreenOn();
        if (!isScreenOn) {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "myApp:notificationLock");
            wl.acquire(3000);
        }

        //creo notification channel
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("gcmchn", "Giacomo_channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DESCRIPTION");
            channel.setVibrationPattern(new long[] { 1000, 1000, 1000, 1000, 1000});
            channel.setLightColor(Color.GREEN);
            channel.enableVibration(true);
            channel.enableLights(true);
            mNotificationManager.createNotificationChannel(channel);
        }

        //costruisco testo notifica
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
        if(message.contains("una nuova prenotazione")) body+="\nPremi sulla notifica per verificare la prenotazione";

        //costruisco notifica
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "gcmchn")
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(header)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true);

        //intent ad activity quando premo su notifica
        Intent intent=null;
        if(logged==false) intent = new Intent(getApplicationContext(), MainActivity.class);
        else if(message.contains("si è iscritto al gruppo") || message.contains("ha abbandonato il gruppo") || message.contains("ha aggiornato") || message.contains("stato rimosso") || message.contains("è stato rimosso"))
            intent = new Intent(getApplicationContext(), GroupActivity.class);
        else if(message.contains("non parteciperà alla prenotazione") || message.contains("una nuova prenotazione"))
            intent = new Intent(getApplicationContext(), PrenotazioniAttiveActivity.class);
        else intent = new Intent(getApplicationContext(), Home.class);

        //trigger notifica
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(id, mBuilder.build());
        id++;
    }

}