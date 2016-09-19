package com.assembly.app.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.assembly.app.manager.PushNotificationManager;
import com.assembly.app.utils.L;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		L.d("GcmIntentService", "in GcmIntentService onHandleIntent");
		Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        
        if (!extras.isEmpty()) {
        	String messageType = gcm.getMessageType(intent);
        	if (messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE)) {
        		PushNotificationManager notfManager = new PushNotificationManager(this);
            	notfManager.sendNotification(extras);
            }
        }
        
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

}
