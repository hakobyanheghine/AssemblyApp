package com.assembly.app.manager;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.assembly.app.HomeActivity;
import com.assembly.app.MainActivity;
import com.assembly.app.R;
import com.assembly.app.utils.L;


public class PushNotificationManager {
	public static final int MESSAGE_NOTIFICATION_ID = 100;
	public static final int REQUEST_NOTIFICATION_ID = 101;
	
	private static final String EXTRA_NOTIFICATION_TITLE = "title";
	private static final String EXTRA_NOTIFICATION_DESC = "message";
	private static final String EXTRA_NOTIFICATION_TYPE = "type";
	private static final String EXTRA_NOTIFICATION_USER_ID = "pageID";

	private IntentService intentService;
	
	public PushNotificationManager(IntentService intentService) {
		this.intentService = intentService;
	}

	public void sendNotification(Bundle extras) {
		String title = extras.getString(EXTRA_NOTIFICATION_TITLE, "Assembly");
		int type = extras.getInt(EXTRA_NOTIFICATION_TYPE, MESSAGE_NOTIFICATION_ID);
		String message = extras.getString(EXTRA_NOTIFICATION_DESC);
		String userId = extras.getString(EXTRA_NOTIFICATION_USER_ID, "");
		String page = extras.getString("page", "");
		
		L.d("heghine", "extras = " + extras.toString());
		
		Intent intent = new Intent(intentService, MainActivity.class);
		if (!page.isEmpty() && userId.equals(page)) {
			intent.putExtra(HomeActivity.NOTIFICATION_USER_ID, userId);
		}
		
		sendNotification(type, title, message, intent);
	}
	
	public void sendNotification(int type, String title, String msg, Intent intent) {
		NotificationManager mNotificationManager = (NotificationManager) intentService.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(intentService, 0, intent, 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(intentService)
						.setSmallIcon(R.drawable.ic_launcher)
						.setContentTitle(title)
						.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
						.setContentText(msg);

		mBuilder.setAutoCancel(true);
		mBuilder.setDefaults(Notification.DEFAULT_SOUND);
		mBuilder.setContentIntent(contentIntent);
		
		mNotificationManager.notify(type, mBuilder.build());
	}
	
}
