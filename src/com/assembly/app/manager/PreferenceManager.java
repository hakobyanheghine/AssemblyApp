package com.assembly.app.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferenceManager {

	private static PreferenceManager instance;
	private SharedPreferences sharedPreferences;
	private Editor editor;

	public static final String NAME = "AssemblyApp";

	public static final String PREFERENCE_LINKEDIN_TOKEN = "linkedin_token";
	public static final String PREFERENCE_LINKEDIN_EXPIRE_DATE = "linkedin_expire_date";
	public static final String PREFERENCE_LINKEDIN_TOKEN_REQUEST_DATE = "linkedin_token_request_date";
	public static final String PREFERENCE_LINKEDIN_MEMBER_ID = "linkedin_member_id";
	public static final String PREFERENCE_USER_ID = "user_id";

	public static final String PREFERENCE_LAST_MESSAGE_ID = "last_message_id";

	public static final String PREFERENCE_USER_LATITUDE = "user_latitude";
	public static final String PREFERENCE_USER_LONGITUDE = "user_longitute";

	public static final String PREFERENCE_GCM_REGISTRATION_ID = "gcm_registration_id";
	public static final String PREFERENCE_IS_GCM_REGISTRATION_ID_SENT = "is_gcm_registration_id_sent";
	public static final String PREFERENCE_VERSION_CODE = "version_code";
	
	private static final String PREFERENCE_IS_FIRST_TIME = "is_first_time";

	private PreferenceManager() {

	}

	public static PreferenceManager getInstance() {
		if (instance == null) {
			instance = new PreferenceManager();
		}
		return instance;
	}

	public void init(Context context) {
		sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();
	}

	public String getLinkedinToken() {
		return sharedPreferences.getString(PREFERENCE_LINKEDIN_TOKEN, "");
	}

	public void setLinkedinToken(String value) {
		editor.putString(PREFERENCE_LINKEDIN_TOKEN, value).commit();
	}

	public long getLinkedinExpireDate() {
		return sharedPreferences.getLong(PREFERENCE_LINKEDIN_EXPIRE_DATE, 0);
	}

	public void setLinkedinExpireDate(long value) {
		editor.putLong(PREFERENCE_LINKEDIN_EXPIRE_DATE, value).commit();
	}
	
	public long getLinkedinTokenRequestDate() {
		return sharedPreferences.getLong(PREFERENCE_LINKEDIN_TOKEN_REQUEST_DATE, 0);
	}

	public void setLinkedinTokenRequestDate(long value) {
		editor.putLong(PREFERENCE_LINKEDIN_TOKEN_REQUEST_DATE, value).commit();
	}

	public String getLinkedinMemberId() {
		return sharedPreferences.getString(PREFERENCE_LINKEDIN_MEMBER_ID, "");
	}

	public void setLinkedinMemberId(String value) {
		editor.putString(PREFERENCE_LINKEDIN_MEMBER_ID, value).commit();
	}

	public String getUserId() {
		return sharedPreferences.getString(PREFERENCE_USER_ID, "");
	}

	public void setUserId(String value) {
		editor.putString(PREFERENCE_USER_ID, value).commit();
	}

	public String getLastMessageId() {
		return sharedPreferences.getString(PREFERENCE_LAST_MESSAGE_ID, "0");
	}

	public void setLastMessageId(String value) {
		editor.putString(PREFERENCE_LAST_MESSAGE_ID, value).commit();
	}

	public String getUserLatitude() {
		return sharedPreferences.getString(PREFERENCE_USER_LATITUDE, "0");
	}

	public void setUserLatitude(String value) {
		editor.putString(PREFERENCE_USER_LATITUDE, value).commit();
	}

	public String getUserLongitude() {
		return sharedPreferences.getString(PREFERENCE_USER_LONGITUDE, "0");
	}

	public void setUserLongitude(String value) {
		editor.putString(PREFERENCE_USER_LONGITUDE, value).commit();
	}

	public String getGCMRegistrationId() {
		return sharedPreferences.getString(PREFERENCE_GCM_REGISTRATION_ID, "");
	}

	public void setGCMRegistrationId(String id) {
		editor.putString(PREFERENCE_GCM_REGISTRATION_ID, id).commit();
	}
	
	public void setIsGCMRegistrationIdSent(boolean value) {
		editor.putBoolean(PREFERENCE_IS_GCM_REGISTRATION_ID_SENT, value).commit();
	}

	public boolean getIsGCMRegistrationIdSent() {
		return sharedPreferences.getBoolean(PREFERENCE_IS_GCM_REGISTRATION_ID_SENT, false);
	}

	public void setVersionCode(int value) {
		editor.putInt(PREFERENCE_VERSION_CODE, value).commit();
	}

	public int getVersionCode() {
		return sharedPreferences.getInt(PREFERENCE_VERSION_CODE, 1);
	}
	
	public void setIsFirstTime(boolean value) {
		editor.putBoolean(PREFERENCE_IS_FIRST_TIME, value).commit();
	}

	public boolean getIsFirstTime() {
		return sharedPreferences.getBoolean(PREFERENCE_IS_FIRST_TIME, true);
	}
}
