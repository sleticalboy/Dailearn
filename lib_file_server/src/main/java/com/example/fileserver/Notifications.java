package com.example.fileserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
public class Notifications {

  private static final String TAG = "Notifications";

  static final int notifyId = 101;
  static final String channelId = "downloader";

  private Notifications() {
    //no instance
  }

  public static void createAllChannels(Context context) {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
    Log.d(TAG, "createAllChannels() channel: " + channelId);
    NotificationChannel channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT);
    channel.setSound(Uri.EMPTY, Notification.AUDIO_ATTRIBUTES_DEFAULT);
    NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    mgr.createNotificationChannel(channel);
  }

  public static Notification build(Context context, CharSequence title, CharSequence content) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      return new NotificationCompat.Builder(context, channelId)
        .setPriority(NotificationCompat.PRIORITY_MIN)
        .setCategory(Notification.CATEGORY_SERVICE)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(content)
        .build();
    } else {
      Notification notification = new Notification();
      notification.priority = Notification.PRIORITY_MIN;
      notification.category = Notification.CATEGORY_SERVICE;
      notification.icon = R.mipmap.ic_launcher;
      return notification;
    }
  }
}
