package com.assembly.app.manager;

import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;

import com.assembly.app.MainActivity;
import com.assembly.app.utils.Constants;
import com.assembly.app.utils.L;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMClientManager {
	private static final String TAG = "GCMClientManager";

	private MainActivity mActivity;
	private GoogleCloudMessaging gcm;
	private Context context;
	private String regId;

	public GCMClientManager(MainActivity activity) {
		mActivity = activity;
	}

	public void registerGCM() {
		if (mActivity.checkPlayServices(false)) {
			gcm = GoogleCloudMessaging.getInstance(mActivity);
			regId = getRegistrationId(context);

			if (regId.isEmpty()) {
				registerInBackground();
			}
		} else {
			L.i(TAG, "No valid Google Play Services APK found.");
		}
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing registration ID.
	 */
	private String getRegistrationId(Context context) {
		String registrationId = PreferenceManager.getInstance().getGCMRegistrationId();
		if (registrationId.isEmpty()) {
			L.i(TAG, "Registration not found.");
			return "";
		}
		
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		if (mActivity.appUpdated) {
			L.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * Stores the registration ID and app versionCode in the application's shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regId = gcm.register(Constants.GCM_SENDER_ID);
					msg = "Device registered, registration ID=" + regId;

					// Persist the GCM registration id - no need to register again
					// And to send it to backend after initialization
					PreferenceManager.getInstance().setGCMRegistrationId(regId);
					PreferenceManager.getInstance().setIsGCMRegistrationIdSent(false);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				L.i(TAG, msg);
			}

		}.execute(null, null, null);
	}

}
