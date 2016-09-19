package com.assembly.app.utils;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import com.assembly.app.utils.L;
import com.assembly.app.R;
import com.assembly.app.backend.API;
import com.assembly.app.manager.AssemblyAppManager;
import com.assembly.app.manager.PreferenceManager;

public class Utils {
	public static String getAccessTokenUrl(String authorizationToken) {
		return Constants.ACCESS_TOKEN_URL + "?" + Constants.GRANT_TYPE_PARAM + "=" + Constants.GRANT_TYPE + "&" + 
				Constants.RESPONSE_TYPE_VALUE + "=" + authorizationToken + "&" + 
				Constants.CLIENT_ID_PARAM + "=" + Constants.API_KEY + "&" + 
				Constants.REDIRECT_URI_PARAM + "=" + Constants.REDIRECT_URI + "&" + 
				Constants.SECRET_KEY_PARAM + "=" + Constants.SECRET_KEY;
	}

	
	public static String getAuthorizationUrl() {
		return Constants.AUTHORIZATION_URL + "?" + Constants.RESPONSE_TYPE_PARAM + "=" + Constants.RESPONSE_TYPE_VALUE + "&" + 
				Constants.SCOPE_PARAM + "=" + Constants.LINKEDIN_PERMISSION_PROFILE 
				+ "%20" + Constants.LINKEDIN_PERMISSION_EMAIL + "&" +
				Constants.CLIENT_ID_PARAM + "=" + Constants.API_KEY + "&" + 
				Constants.STATE_PARAM + "=" + Constants.STATE + "&" + 
				Constants.REDIRECT_URI_PARAM + "=" + Constants.REDIRECT_URI;
	}
	
	public static String getLinkedInUserDataUrl(String accessToken) {
		return Constants.USER_DATA_URL + accessToken;
	}
	
	public static String getStringByResourceId(int id) {
		return AssemblyAppManager.getInstance().mainActivity.getString(id);
	}
	
	public static void showLoadingDialog(Activity activity) {
		hideLoadingDialog();
		AssemblyAppManager.getInstance().loadingDialog = ProgressDialog.show(activity, "", getStringByResourceId(R.string.loading), true, true, new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				if (AssemblyAppManager.getInstance().loadingDialog != null) {
					AssemblyAppManager.getInstance().loadingDialog.dismiss();
					AssemblyAppManager.getInstance().loadingDialog = null;
				}
			}
		});
	}
	
	public static void hideLoadingDialog() {
		if (AssemblyAppManager.getInstance().loadingDialog != null && AssemblyAppManager.getInstance().loadingDialog.isShowing()) {
			AssemblyAppManager.getInstance().loadingDialog.dismiss();
			AssemblyAppManager.getInstance().loadingDialog = null;
		}
	}
	
	public static void showToast(final Activity activity, final String text) {
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	public static String[] getDateAndTimeInDotFormatNew(long dateValue) {
		SimpleDateFormat df2 = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		Date date = new Date(dateValue);
		String dateText = df2.format(date);
		String[] dateAndTime = dateText.split(" ");
		
		return dateAndTime;
	}
	
	public static String[] getDateAndTimeInDotFormat(long dateValue) {
		SimpleDateFormat df2 = new SimpleDateFormat("dd.MM.yy HH:mm");
		Date date = new Date(dateValue);
		String dateText = df2.format(date);
		String[] dateAndTime = dateText.split(" ");
		
		return dateAndTime;
	}
	
	public static String[] getDateAndTimeInMonthFormat(long dateValue) {
		SimpleDateFormat df2 = new SimpleDateFormat("MMM d, yy 'at' HH:mm");
		Date date = new Date(dateValue);
		String dateText = df2.format(date);
		String[] dateAndTime = dateText.split("at");
		
		return dateAndTime;
	}
	
	public static void sendErrorToBackend(String error) {
		if (!L.debugLogEnabled) {
			try {
				API.sendError(API.userId, error, null);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public static boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) AssemblyAppManager.getInstance().mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		
		return networkInfo != null && networkInfo.isConnected();
	}
	
	public static boolean checkLinkedInExpiration() {
		long dateRequested = PreferenceManager.getInstance().getLinkedinTokenRequestDate(); // in seconds
		long currentTime = System.currentTimeMillis() / 1000; // in seconds
		long timeRemainingForTokenExpire = currentTime - dateRequested;
				
		if (PreferenceManager.getInstance().getLinkedinExpireDate() - timeRemainingForTokenExpire > 0) {
			return true;
		} 
		
		return false;
	}

	public static boolean isTablet() {
		return Utils.getStringByResourceId(R.string.device_type).equals("tablet");
	}
	
	public static ArrayList<int[]> getSpans(String body, char prefix) {
	    ArrayList<int[]> spans = new ArrayList<int[]>();

	    Pattern pattern = Pattern.compile(prefix + "\\w+[\\.\\w+]*");
	    Matcher matcher = pattern.matcher(body);

	    // Check all occurrences
	    while (matcher.find()) {
	        int[] currentSpan = new int[2];
	        currentSpan[0] = matcher.start();
	        currentSpan[1] = matcher.end();
	        spans.add(currentSpan);
	    }

	    return  spans;
	}
}
