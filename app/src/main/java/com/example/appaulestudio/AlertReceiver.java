package com.example.appaulestudio;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AlertReceiver extends BroadcastReceiver {
    public int id=0;
    public Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        if(intent.getAction().equals("StudyAround")) showNotification(); //se ascolta l'intent con action StudyAround mostra notifica
        else if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){ //se il telefono si riavvia reset l'alarm
            SharedPreferences settings = context.getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
            String strOrario=settings.getString("alarm_time", null);

            if(strOrario==null){
                //Toast.makeText(context, "No alarm", Toast.LENGTH_LONG).show();
            }
            else {
                Calendar adesso = Calendar.getInstance();
                Calendar orario_allarme=Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date_allarme = null;
                try {
                    date_allarme = df.parse(strOrario);
                } catch (ParseException e) {
                 e.printStackTrace();
                }
                orario_allarme.setTime(date_allarme);
                if(orario_allarme.after(adesso)==true) reset_allarme(strOrario);
                //Toast.makeText(context, strOrario, Toast.LENGTH_LONG).show();
            }
        }
    }

// mostra notifica
    public void showNotification(){
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("gcmAlert", "giacoJacky", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DESCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "gcmAlert")
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("StudyAround")
                .setContentText("Attenzione! La tua prenotazione sta per terminare!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Intent i = new Intent(context.getApplicationContext(), Home.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);
        mNotificationManager.notify(id, builder.build());
        id++;
    }

//reset alarm dopo reboot
    public void reset_allarme(String orario){
        Calendar cal_allarme = Calendar.getInstance();

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date_allarme = null;
            try {
                date_allarme = df.parse(orario);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cal_allarme.setTime(date_allarme);


        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlertReceiver.class);
        intent.setAction("StudyAround");
        intent.putExtra("name", "Attenzione! La tua prenotazione terminerà tra 5 minuti");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal_allarme.getTimeInMillis(), pendingIntent);
    }

}
