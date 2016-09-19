package com.assembly.app;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.assembly.app.backend.API;
import com.assembly.app.backend.API.RequestObserver;
import com.assembly.app.data.Connection;
import com.assembly.app.data.Conversation;
import com.assembly.app.data.Group;
import com.assembly.app.data.Message;
import com.assembly.app.data.User;
import com.assembly.app.manager.AssemblyAppManager;
import com.assembly.app.manager.FilesManager;
import com.assembly.app.manager.GCMClientManager;
import com.assembly.app.manager.PreferenceManager;
import com.assembly.app.utils.Constants;
import com.assembly.app.utils.L;
import com.assembly.app.utils.Utils;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class MainActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	
	public static final int REQUEST_CODE_GPS_SETTINGS = 100;

	private GoogleApiClient googleApiClient;
	private LocationRequest mLocationRequest;
	private Location mCurrentLocation;
	
	private Dialog loginWithLinkedInDialog;

	private boolean hasUserConnections;
	private boolean hasUserSuggestions;
	private boolean hasUserGroups;
	private boolean hasUserMessages;
	private boolean hasUserSettings;
	private boolean hasSavedLocation;
	
	private boolean isLastKnowLocationRecieved;
	private boolean waitForLocationUpdate;
	
	public boolean appUpdated;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
		L.debugLogEnabled = false;
		
		AssemblyAppManager.getInstance().mainActivity = this;
		PreferenceManager.getInstance().init(getApplicationContext());
		updateVersionCode();
		
		googleApiClient = new GoogleApiClient.Builder(this)
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(this)
			.addApi(LocationServices.API)
			.build();
		
		mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(Constants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setSmallestDisplacement(Constants.SMALLEST_DISPLACEMENT_IN_METERS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        
        if (checkPlayServices(false)) {
        	GCMClientManager gcmClientManager = new GCMClientManager(this);
			gcmClientManager.registerGCM();	
        }
		
		ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(this)
	        .threadPriority(Thread.NORM_PRIORITY - 2)
	        .denyCacheImageMultipleSizesInMemory()
	        .diskCacheFileNameGenerator(new Md5FileNameGenerator())
	        .diskCacheSize(10 * 1024 * 1024) // 50 Mb
	        .tasksProcessingOrder(QueueProcessingType.LIFO)
//	        .writeDebugLogs() // Remove for release app
	        .build());

		
		
		if (PreferenceManager.getInstance().getLinkedinMemberId().equals("") || PreferenceManager.getInstance().getUserId().equals("") || !Utils.checkLinkedInExpiration()) { 
			((TextView) findViewById(R.id.login_with_linkedin)).setOnClickListener(new OnClickListener() {
	
				@Override
				public void onClick(View v) {
					if (Utils.isNetworkAvailable()) {
						loginWithLinkedIn();
					} else {
						Utils.showToast(MainActivity.this, Utils.getStringByResourceId(R.string.no_internet));
					}
				}
			});
		} else {
			((TextView) findViewById(R.id.login_with_linkedin)).setVisibility(View.GONE);
			if (!isLastKnowLocationRecieved && !isGPSEnabled()) {
				showEnableGPSDialog();
			} else {
				Utils.showLoadingDialog(MainActivity.this);
				getUserData();
			}
		}
	}

	private void loginWithLinkedIn() {
		Utils.showLoadingDialog(this);

		loginWithLinkedInDialog = new Dialog(MainActivity.this);
		loginWithLinkedInDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		final WebView webView = new WebView(MainActivity.this);
		webView.requestFocus(View.FOCUS_DOWN);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				Utils.hideLoadingDialog();
				
				if (loginWithLinkedInDialog != null && !loginWithLinkedInDialog.isShowing()) {
					loginWithLinkedInDialog.show();
				}
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String authorizationUrl) {
				if (authorizationUrl.startsWith(Constants.REDIRECT_URI)) {
					Uri uri = Uri.parse(authorizationUrl);
					String stateToken = uri.getQueryParameter(Constants.STATE_PARAM);
					if (stateToken == null || !stateToken.equals(Constants.STATE)) {
						L.e("Authorize", "State token doesn't match");
						return true;
					}

					String authorizationToken = uri.getQueryParameter(Constants.RESPONSE_TYPE_VALUE);
					if (authorizationToken == null) {
						L.i("Authorize", "The user doesn't allow authorization.");
						if (loginWithLinkedInDialog != null && loginWithLinkedInDialog.isShowing()) {
							loginWithLinkedInDialog.dismiss();
						}
						return true;
					}
					L.i("Authorize", "Auth token received: " + authorizationToken);

					String accessTokenUrl = Utils.getAccessTokenUrl(authorizationToken);
					(new PostRequestAsyncTask()).execute(accessTokenUrl);

				} else {
					L.i("Authorize", "Redirecting to: " + authorizationUrl);
					webView.loadUrl(authorizationUrl);
				}
				return true;
			}
		});

		String authUrl = Utils.getAuthorizationUrl();
		L.i("Authorize", "Loading Auth Url: " + authUrl);
		webView.loadUrl(authUrl);
		loginWithLinkedInDialog.setContentView(webView);
	}
	
	private class PostRequestAsyncTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			Utils.showLoadingDialog(MainActivity.this);
		}

		@Override
		protected Boolean doInBackground(String... urls) {
			if (urls.length > 0) {
				String url = urls[0];
				JSONObject response = API.sendLinkedinAuthenticationRequestPost(url);
				
				if (response != null) {
					long expiresIn = response.optLong("expires_in", 0);
					long tokenRequestDate = System.currentTimeMillis() / 1000;
					
					String accessToken = response.optString("access_token", null);
					if (expiresIn > 0 && accessToken != null) {
						L.i("Authorize", "accessToken = " + accessToken);
						L.i("Authorize", "expiresIn = " + expiresIn);
						L.i("Authorize", "tokenRequestDate = " + tokenRequestDate);
						
						PreferenceManager.getInstance().setLinkedinToken(accessToken);
						PreferenceManager.getInstance().setLinkedinExpireDate(expiresIn);
						PreferenceManager.getInstance().setLinkedinTokenRequestDate(tokenRequestDate);
						
						JSONObject userDataResponse = API.sendLinkedinUserDataRequestGet(Utils.getLinkedInUserDataUrl(accessToken));
						if (userDataResponse != null) {
							PreferenceManager.getInstance().setLinkedinMemberId(userDataResponse.optString("id"));
							
							return true;
						}
					}
				}
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean status) {
			Utils.hideLoadingDialog();
			if (loginWithLinkedInDialog != null && loginWithLinkedInDialog.isShowing()) {
				loginWithLinkedInDialog.dismiss();
			}
			if (status) {
				((TextView) findViewById(R.id.login_with_linkedin)).setVisibility(View.GONE);
				Utils.showLoadingDialog(MainActivity.this);
				registerUser();
			} else {
				Utils.showToast(MainActivity.this, Utils.getStringByResourceId(R.string.smt_wrong_linkedin));
				Utils.sendErrorToBackend("LinkedIn Login: not successfull");
			}
		}

	};
	
	private void registerUser() {
		API.loginUser(PreferenceManager.getInstance().getLinkedinMemberId(), PreferenceManager.getInstance().getLinkedinToken(), new RequestObserver() {
			
			@Override
			public void onSuccess(JSONObject response) throws JSONException {
				if (response.optString("status", "").equals("OK")) {
					PreferenceManager.getInstance().setUserId(response.optString("data", ""));
					MainActivity.this.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							if (!isLastKnowLocationRecieved && !isGPSEnabled()) {
								Utils.hideLoadingDialog();
								showEnableGPSDialog();
							} else {
								getUserData();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(String response, Exception e) {
				if (response != null && response.equals(Constants.NO_INTERNET)) {
					Utils.hideLoadingDialog();
					Utils.showToast(AssemblyAppManager.getInstance().mainActivity, Utils.getStringByResourceId(R.string.no_internet));
				} else {
					Utils.sendErrorToBackend(e.toString());
				}
			}
		});
	}
	
	private void getUserData() {
		String userId = PreferenceManager.getInstance().getUserId();
		API.userId = userId;
		API.getProfile(userId, userId, new RequestObserver() {
			
			@Override
			public void onSuccess(JSONObject response) throws JSONException {
				if (response.optString("status", "").equals("OK")) {
					AssemblyAppManager.getInstance().userData = new User(response);
					saveUserLocation();
				}
			}
			
			@Override
			public void onError(String response, Exception e) {
				if (response != null && response.equals(Constants.NO_INTERNET)) {
					Utils.hideLoadingDialog();
					Utils.showToast(AssemblyAppManager.getInstance().mainActivity, Utils.getStringByResourceId(R.string.no_internet));
				} else {
					Utils.sendErrorToBackend(e.toString());
				}
			}
		});
	}
	
	private void initializeUserData() {
		String userId = PreferenceManager.getInstance().getUserId();
		
		API.getConnections(userId, new RequestObserver() {
			
			@Override
			public void onSuccess(JSONObject response) throws JSONException {
				if (response.optString("status", "").equals("OK")) {
					JSONObject data = response.optJSONObject("data");
					JSONArray dataTable = data.optJSONArray("dataTable");
					for (int i = 0; i < dataTable.length(); i++) {
						Connection connection = new Connection(dataTable.optJSONObject(i));
						if (connection.isFriendRequestSent) {
							AssemblyAppManager.getInstance().userData.incomingRequests.add(connection);
						} else {
							AssemblyAppManager.getInstance().userData.connections.add(connection);
						}
					}
					MainActivity.this.hasUserConnections = true;
					MainActivity.this.checkIfUserDataIsComplete();
				}
			}
			
			@Override
			public void onError(String response, Exception e) {
				if (response != null && response.equals(Constants.NO_INTERNET)) {
					Utils.hideLoadingDialog();
					Utils.showToast(AssemblyAppManager.getInstance().mainActivity, Utils.getStringByResourceId(R.string.no_internet));
				} else {
					Utils.sendErrorToBackend(e.toString());
				}
			}
		});
		
		API.getSuggestions(userId, new RequestObserver() {
			
			@Override
			public void onSuccess(JSONObject response) throws JSONException {
				if (response.optString("status", "").equals("OK")) {
					JSONObject data = response.optJSONObject("data");
					JSONArray dataTable = data.optJSONArray("dataTable");
					for (int i = 0; i < dataTable.length(); i++) {
						Connection suggestion = new Connection(dataTable.optJSONObject(i));
						AssemblyAppManager.getInstance().userData.suggestions.add(suggestion);
					}
					MainActivity.this.hasUserSuggestions = true;
					MainActivity.this.checkIfUserDataIsComplete();
				}
			}
			
			@Override
			public void onError(String response, Exception e) {
				if (response != null && response.equals(Constants.NO_INTERNET)) {
					Utils.hideLoadingDialog();
					Utils.showToast(AssemblyAppManager.getInstance().mainActivity, Utils.getStringByResourceId(R.string.no_internet));
				} else {
					Utils.sendErrorToBackend(e.toString());
				}
			}
		});
		
		API.getGroups(userId, new RequestObserver() {
			
			@Override
			public void onSuccess(JSONObject response) throws JSONException {
				if (response.optString("status", "").equals("OK")) {
					JSONArray data = response.optJSONArray("data");
					for (int i = 0; i < data.length(); i++) {
						Group group = new Group(data.optJSONObject(i));
						AssemblyAppManager.getInstance().userData.groups.add(group);
					}
					MainActivity.this.hasUserGroups = true;
					MainActivity.this.checkIfUserDataIsComplete();
				}
			}
			
			@Override
			public void onError(String response, Exception e) {
				if (response != null && response.equals(Constants.NO_INTERNET)) {
					Utils.hideLoadingDialog();
					Utils.showToast(AssemblyAppManager.getInstance().mainActivity, Utils.getStringByResourceId(R.string.no_internet));
				} else {
					Utils.sendErrorToBackend(e.toString());
				}
			}
		});
		
		API.getSettings(userId, new RequestObserver() {
			
			@Override
			public void onSuccess(JSONObject response) throws JSONException {
				if (response.optString("status", "").equals("OK")) {
					JSONObject data = response.optJSONObject("data");
					AssemblyAppManager.getInstance().userData.showInSuggestions = data.optBoolean("showInSuggestions", false);
					AssemblyAppManager.getInstance().userData.showInConnections = data.optBoolean("showInConnections", false);
					if (data.optBoolean("isKM", false)) {
						AssemblyAppManager.getInstance().userData.userDistanceUnit = Constants.DISTANCE_UNIT_KM;
					} else {
						AssemblyAppManager.getInstance().userData.userDistanceUnit = Constants.DISTANCE_UNIT_ML;
					}
					
					MainActivity.this.hasUserSettings = true;
					MainActivity.this.checkIfUserDataIsComplete();
				}
			}
			
			@Override
			public void onError(String response, Exception e) {
				if (response != null && response.equals(Constants.NO_INTERNET)) {
					Utils.hideLoadingDialog();
					Utils.showToast(AssemblyAppManager.getInstance().mainActivity, Utils.getStringByResourceId(R.string.no_internet));
				} else {
					Utils.sendErrorToBackend(e.toString());
				}
			}
		});
		
		if (!PreferenceManager.getInstance().getIsGCMRegistrationIdSent()) {
			try {
				API.setParam(userId, "token", PreferenceManager.getInstance().getGCMRegistrationId(), new RequestObserver() {
					
					@Override
					public void onSuccess(JSONObject response) throws JSONException {
						if (response.optString("status", "").equals("OK")) {
							PreferenceManager.getInstance().setIsGCMRegistrationIdSent(true);
						}
					}
					
					@Override
					public void onError(String response, Exception e) {
						if (response != null && response.equals(Constants.NO_INTERNET)) {
							Utils.hideLoadingDialog();
							Utils.showToast(AssemblyAppManager.getInstance().mainActivity, Utils.getStringByResourceId(R.string.no_internet));
						} else {
							Utils.sendErrorToBackend(e.toString());
						}
					}
				});
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
				Utils.sendErrorToBackend(e1.toString());
			}
		}
		
		MainActivity.this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				new MessageInitializerTask().execute();
			}
		});
	} 
	
	public void initializeMessages() {
		Conversation conversation;
		AssemblyAppManager.getInstance().newMessagesCount = 0;
		ArrayList<Message> allMessages = AssemblyAppManager.getInstance().userData.allMessages;
		for (int i = 0; i < allMessages.size(); i++) {
			if (AssemblyAppManager.getInstance().userData.allConversations.get(allMessages.get(i).opponentId) == null) {
				conversation = getConversationByMessage(allMessages.get(i));
				
				SparseArray<Conversation> converversations = new SparseArray<Conversation>();
				converversations.put(conversation.conversationId, conversation);
				AssemblyAppManager.getInstance().userData.allConversations.put(conversation.opponentId, converversations);
			} else if (AssemblyAppManager.getInstance().userData.allConversations.get(allMessages.get(i).opponentId).get(allMessages.get(i).conversationId) == null) {
				conversation = getConversationByMessage(allMessages.get(i));	
				
				AssemblyAppManager.getInstance().userData.allConversations.get(conversation.opponentId).put(conversation.conversationId, conversation);
			} else {
				conversation = AssemblyAppManager.getInstance().userData.allConversations.get(allMessages.get(i).opponentId).get(allMessages.get(i).conversationId);
				conversation.messages.add(allMessages.get(i));
				if (allMessages.get(i).isNew) {
					conversation.newMessageCount++;
					AssemblyAppManager.getInstance().newMessagesCount++;
				}
			}
		}
		
		MainActivity.this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				AssemblyAppManager.getInstance().initMessagesTimer();
				MainActivity.this.hasUserMessages = true;
				MainActivity.this.checkIfUserDataIsComplete();
			}
		});
	}
	
	public Conversation getConversationByMessage(Message message) {
		Conversation conversation = new Conversation();
		conversation.conversationId = message.conversationId;
		if (message.opponentId.startsWith("g")) {
			conversation.groupId = message.groupId;
			conversation.isGroupConversation = true;
		}
		conversation.opponentId = message.opponentId;
		conversation.opponentName = message.opponentName;
		conversation.opponentPictureUrl = message.opponentUrl;
		conversation.topic = message.topic;

		conversation.messages.add(message);
		
		if (message.isNew) {
			conversation.newMessageCount++;
			AssemblyAppManager.getInstance().newMessagesCount++;
		}
		
		return conversation;
	}
	
	private void saveUserLocation() {
		String latitude = "0";
		String logitude = "0";
		if (L.debugLogEnabled) {
			latitude = PreferenceManager.getInstance().getUserLatitude();
			logitude = PreferenceManager.getInstance().getUserLongitude();
		} else {
			latitude = PreferenceManager.getInstance().getUserLatitude();
			logitude = PreferenceManager.getInstance().getUserLongitude();
		}
		
		L.d("GooglePlayServices", "saveUserLocation: latitude = " + latitude);
		L.d("GooglePlayServices", "saveUserLocation: logitude = " + logitude);

		if (!latitude.isEmpty() && !logitude.isEmpty()) {
			if (latitude.equals("0") && logitude.equals("0")) {
				if (L.debugLogEnabled) {
					AssemblyAppManager.getInstance().hasEnabledGPS = true;
				} else {
					AssemblyAppManager.getInstance().hasEnabledGPS = false;
				}
			} else {
				AssemblyAppManager.getInstance().hasEnabledGPS = true;
			}
			API.saveLocation(latitude, logitude, PreferenceManager.getInstance().getUserId(), new RequestObserver() {
				
				@Override
				public void onSuccess(JSONObject response) throws JSONException {
					MainActivity.this.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							MainActivity.this.hasSavedLocation = true;
							initializeUserData();
						}
					});
				}
				
				@Override
				public void onError(String response, Exception e) {
					
				}
			});
		}
	}
	
	private void checkIfUserDataIsComplete() {
		if (hasUserConnections && hasUserSuggestions && hasUserGroups && hasUserMessages && hasUserSettings && hasSavedLocation) {
			startConnectionsActivity();
		}
	}
	
	private void startConnectionsActivity() {
		MainActivity.this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Utils.hideLoadingDialog();
				Intent connectionsActivity = new Intent(MainActivity.this, HomeActivity.class);
		        startActivity(connectionsActivity);
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if (googleApiClient != null) {
			googleApiClient.connect();
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		if (googleApiClient != null && googleApiClient.isConnected()) {
			googleApiClient.disconnect();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (!L.debugLogEnabled) {
			checkPlayServices(true);
		}
		
		if (googleApiClient != null && googleApiClient.isConnected()) {
			LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		L.d("onActivityResult", "requestCode = " + requestCode);
		L.d("onActivityResult", "resultCode = " + resultCode);
		
		if (requestCode == REQUEST_CODE_GPS_SETTINGS) {
			if (isGPSEnabled()) {
				if (googleApiClient != null && googleApiClient.isConnected()) {
					waitForLocationUpdate = true;
					LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
				} else {
					PreferenceManager.getInstance().setUserLatitude("0");
					PreferenceManager.getInstance().setUserLongitude("0");
					
					Utils.showLoadingDialog(MainActivity.this);
					getUserData();
				}
			} else {
				PreferenceManager.getInstance().setUserLatitude("0");
				PreferenceManager.getInstance().setUserLongitude("0");
				
				Utils.showLoadingDialog(MainActivity.this);
				getUserData();
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (AssemblyAppManager.getInstance().messagesTimer != null) {
				AssemblyAppManager.getInstance().messagesTimer.cancel();
			}
			finish();
            return true;
        }
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onBackPressed() {
		finish();
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		L.d("GooglePlayServices", "onConnectionFailed: " + result.toString());
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		L.d("GooglePlayServices", "onConnected");
		
		if (mCurrentLocation == null) {
			Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
			if (lastLocation != null) {
				mCurrentLocation = lastLocation;
				String latitude = String.valueOf(mCurrentLocation.getLatitude());
				String longitude = String.valueOf(mCurrentLocation.getLongitude());
				
				L.d("GooglePlayServices", "onConnected: Latitude = " + latitude);
				L.d("GooglePlayServices", "onConnected: Longitude = " + longitude);
				
				PreferenceManager.getInstance().setUserLatitude(latitude);
				PreferenceManager.getInstance().setUserLongitude(longitude);
				isLastKnowLocationRecieved = true;
			} else {
				isLastKnowLocationRecieved = false;
				L.d("GooglePlayServices", "lastLocation == null");
			}
		}
		
		LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
	}

	@Override
	public void onConnectionSuspended(int cause) {
		L.d("GooglePlayServices", "onConnectionSuspended");
	}

	@Override
	public void onLocationChanged(Location location) {
		L.d("GooglePlayServices", "onLocationChanged");
		
		// update user location in any case to avoid location changes
		mCurrentLocation = location;
		
		if (mCurrentLocation != null) {
			String latitude = String.valueOf(mCurrentLocation.getLatitude());
			String longitude = String.valueOf(mCurrentLocation.getLongitude());
			
			L.d("GooglePlayServices", "onLocationChanged: Latitude = " + latitude);
			L.d("GooglePlayServices", "onLocationChanged: Longitude = " + longitude);
			
			if (!PreferenceManager.getInstance().getUserLatitude().equals(latitude) && !PreferenceManager.getInstance().getUserLongitude().equals(longitude)) {
				// if the current location is not yet saved in backend
				PreferenceManager.getInstance().setUserLatitude(latitude);
				PreferenceManager.getInstance().setUserLongitude(longitude);
				
				LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
			}
			
			if (waitForLocationUpdate) {
				waitForLocationUpdate = false;
				Utils.showLoadingDialog(MainActivity.this);
				getUserData();
			}
		}
	}
	
	public boolean checkPlayServices(boolean showErrorDialog) {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (showErrorDialog && GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, this, Constants.PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            L.i("GooglePlayServices", "This device is not supported.");
	            Utils.sendErrorToBackend("GooglePlayServices: This device is not supported.");
	        }
	        return false;
	    }
	    return true;
	}
	
	private void updateVersionCode() {
		try {
			int oldVersion = PreferenceManager.getInstance().getVersionCode();
			int newVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
			if (oldVersion != newVersionCode) {
				PreferenceManager.getInstance().setVersionCode(newVersionCode);
				appUpdated = true;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private boolean isGPSEnabled() {
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		boolean isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		L.d("heghine", "isGPSEnabled = " + isEnabled);
		return isEnabled;
	}
	
	private void showEnableGPSDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		LayoutInflater inflater = MainActivity.this.getLayoutInflater();
		final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.dialog_logout, null);
		((TextView) view.findViewById(R.id.dialog_logout_txt)).setText(R.string.enable_gps);
		builder.setView(view).setPositiveButton(R.string.enagble, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, int id) {
				startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_CODE_GPS_SETTINGS);
			}
		}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
				
				PreferenceManager.getInstance().setUserLatitude("0");
				PreferenceManager.getInstance().setUserLongitude("0");
				
				Utils.showLoadingDialog(MainActivity.this);
				getUserData();
			}
		});

		builder.create().show();
	}
	
	private class MessageInitializerTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			AssemblyAppManager.getInstance().userData.allMessages = FilesManager.getInstance().getAllMessages();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			initializeMessages();
		}
	}
}
