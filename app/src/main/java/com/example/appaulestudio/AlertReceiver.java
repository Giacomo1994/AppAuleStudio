package com.example.appaulestudio;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

public class AlertReceiver extends BroadcastReceiver {
    public int id=0;
    public Context context;
    SqliteManager database;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;

        if(intent.getAction().equals("StudyAround")) showNotification(); //se ascolta l'intent con action StudyAround mostra notifica
        else if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){ //se il telefono si riavvia reset l'alarm
            database=new SqliteManager(context);
            LinkedList<AlarmClass> allarmi_attivi=database.getAlarms();

            if(allarmi_attivi==null){
                Toast.makeText(context, "No alarms", Toast.LENGTH_LONG).show();
            }
            else {
                for(AlarmClass ac:allarmi_attivi){
                    Calendar adesso = Calendar.getInstance();
                    Calendar orario_allarme=Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date_allarme = null;
                    try {
                        date_allarme = df.parse(ac.getOrario_alarm());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    orario_allarme.setTime(date_allarme);
                    if(orario_allarme.after(adesso)==true) reset_allarme(ac.getId_prenotazione(),ac.getOrario_alarm());
                    //Toast.makeText(context, strOrario, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

// mostra notifica
    public void showNotification(){
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = Build.VERSION.SDK_INT >= 20 ? pm.isInteractive() : pm.isScreenOn(); // check if screen is on
        if (!isScreenOn) {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "myApp:notificationLock");
            wl.acquire(3000); //set your time in milliseconds
        }

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("gcmAlert", "giacoJacky", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DESCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "gcmAlert")
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("Attenzione! La tua prenotazione sta per terminare!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Intent i = new Intent(context.getApplicationContext(), PrenotazioniAttiveActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);
        mNotificationManager.notify(id, builder.build());
        id++;
    }

//reset alarm dopo reboot
    public void reset_allarme(int id_prenotazione, String orario_alarm){
        Calendar cal_allarme = Calendar.getInstance();

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date_allarme = null;
            try {
                date_allarme = df.parse(orario_alarm);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cal_allarme.setTime(date_allarme);


        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlertReceiver.class);
        intent.setAction("StudyAround");
        intent.putExtra("name", "Attenzione! La tua prenotazione sta per terminare!");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id_prenotazione, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal_allarme.getTimeInMillis(), pendingIntent);
    }

}
