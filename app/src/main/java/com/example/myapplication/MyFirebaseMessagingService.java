package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    private String CHANNEL_ID = "331313";
    private String title, message;
    private final int notifyID = 3335;
    private String TAG = this.getClass().getName();
    private LocalBroadcastManager broadcaster;

    @Override
    public void onCreate() {
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();



        Intent intent = new Intent(MainActivity.FB_DEMO_RECEIVER);
        intent.putExtra("title",data.get("title"));
        intent.putExtra("action",data.get("action"));
        intent.putExtra("body",data.get("body"));
        intent.putExtra("id",data.get("id"));
        broadcaster.sendBroadcast(intent);

        // sendNotification( data);

    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    private void sendNotification(Map<String, String> data) {

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivityOld.class);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 1, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "my_channel_01";// The id of the channel.
            CharSequence name = "DE_CHANNEL";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

            // Create a notification and set the notification channel.
            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle(data.get("title"))
                    .setContentText(data.get("body"))
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                    .build();



            if (manager != null) {
                manager.createNotificationChannel(mChannel);
                manager.notify(notifyID, notification);
            }


        } else {


            //

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(false)
                    .setContentIntent(contentIntent);

            // Add as notification
            int random = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            manager.notify(notifyID, builder.build());

        }
    }
}
