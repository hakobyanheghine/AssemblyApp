package com.assembly.app;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.assembly.app.backend.API;
import com.assembly.app.backend.API.RequestObserver;
import com.assembly.app.data.User;
import com.assembly.app.manager.AssemblyAppManager;
import com.assembly.app.utils.Constants;
import com.assembly.app.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SettingsActivity extends Activity {

	private User currentUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		currentUser = AssemblyAppManager.getInstance().userData;

		((TextView) findViewById(R.id.settings_profile_user_name_txt)).setText(currentUser.firstName + " " + currentUser.lastName);
		((TextView) findViewById(R.id.settings_profile_user_position_txt)).setText(currentUser.position);
		((TextView) findViewById(R.id.settings_profile_user_location_txt)).setText(currentUser.location);

		ImageLoader.getInstance().loadImage(currentUser.pictureUrl, AssemblyAppManager.getInstance().targetSize256, null);
		ImageLoader.getInstance().displayImage(currentUser.pictureUrl, (ImageView) findViewById(R.id.settings_profile_user_img), AssemblyAppManager.getInstance().optionsWithLoading);

		if (currentUser.userDistanceUnit.equals(Constants.DISTANCE_UNIT_KM)) {
			((CheckBox) findViewById(R.id.settings_preference_km_cb)).setChecked(true);
		} else {
			((CheckBox) findViewById(R.id.settings_preference_ml_cb)).setChecked(true);
		}
		
		((CheckBox) findViewById(R.id.settings_preference_ml_cb)).setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					((CheckBox) findViewById(R.id.settings_preference_km_cb)).setChecked(false);
					
					currentUser.userDistanceUnit = Constants.DISTANCE_UNIT_ML;
					updateSettings("isKM");
				} else if (!((CheckBox) findViewById(R.id.settings_preference_km_cb)).isChecked()) {
					((CheckBox) findViewById(R.id.settings_preference_km_cb)).setChecked(true);
				}
			}
		});

		((CheckBox) findViewById(R.id.settings_preference_km_cb)).setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					((CheckBox) findViewById(R.id.settings_preference_ml_cb)).setChecked(false);
					
					currentUser.userDistanceUnit = Constants.DISTANCE_UNIT_KM;
					updateSettings("isKM");
				} else if (!((CheckBox) findViewById(R.id.settings_preference_ml_cb)).isChecked()) {
					((CheckBox) findViewById(R.id.settings_preference_ml_cb)).setChecked(true);
				}
			}
		});
		
		if (currentUser.showInSuggestions) {
			((Switch) findViewById(R.id.settings_visibility_suggestions_sw)).setChecked(true);
		} else {
			((Switch) findViewById(R.id.settings_visibility_suggestions_sw)).setChecked(false);
		}
		
		((Switch) findViewById(R.id.settings_visibility_suggestions_sw)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				currentUser.showInSuggestions = isChecked;
				updateSettings("showInSuggestions");
			}
		});
		
		if (currentUser.showInConnections) {
			((Switch) findViewById(R.id.settings_visibility_connections_sw)).setChecked(true);
		} else {
			((Switch) findViewById(R.id.settings_visibility_connections_sw)).setChecked(false);
		}

		((Switch) findViewById(R.id.settings_visibility_connections_sw)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				currentUser.showInConnections = isChecked;
				updateSettings("showInConnections");
			}
		});

		getActionBar().setTitle(currentUser.firstName + " " + currentUser.lastName);
	}

	private void updateSettings(final String paramName) {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				try {
					String paramValue = "";
					if (paramName.equals("aboutMe")) {
						paramValue = currentUser.about;
					} else if (paramName.equals("showInSuggestions")) {
						paramValue = currentUser.showInSuggestions ? "true" : "false";
					} else if (paramName.equals("showInConnections")) {
						paramValue = currentUser.showInConnections ? "true" : "false";
					} else if (paramName.equals("isKM")) {
						paramValue = currentUser.userDistanceUnit.equals(Constants.DISTANCE_UNIT_KM) ? "true" : "false";
					}
					API.setParam(currentUser.userId, paramName, paramValue, new RequestObserver() {

						@Override
						public void onSuccess(JSONObject response) throws JSONException {
							if (response.optString("status", "").equals("OK")) {
								if (!paramName.equals("aboutMe")) {
									AssemblyAppManager.getInstance().updateConnections = true;
								}
							}
						}

						@Override
						public void onError(String response, Exception e) {
							Utils.sendErrorToBackend(e.toString());
						}
					});
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}, 1000);
	}
}
