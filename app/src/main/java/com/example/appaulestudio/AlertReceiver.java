package com.example.appaulestudio;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class AlertReceiver extends BroadcastReceiver {
    public int id=0;
    public Context context;
    public String message;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        if(intent.getAction().equals("StudyAround")) showNotification(intent);
    }

    public void showNotification(Intent intent){
        String message=intent.getStringExtra("name");
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("gcmAlert", "giacoJacky", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DESCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "gcmAlert")
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("StudyAround")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Intent i = new Intent(context.getApplicationContext(), Home.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);
        mNotificationManager.notify(0, builder.build());
    }

}
