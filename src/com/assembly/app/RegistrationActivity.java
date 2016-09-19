package com.assembly.app;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.assembly.app.backend.API;
import com.assembly.app.data.User;
import com.assembly.app.manager.AssemblyAppManager;
import com.assembly.app.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class RegistrationActivity extends Activity {
	
	private User currentUser;
	
	private EditText experienceEdit;
	private EditText educationEdit;
	private EditText interestsEdit;
	
	private boolean experienceHasFocus;
	private boolean educationHasFocus;
	private boolean interestsHasFocus;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registration);
		
		currentUser = AssemblyAppManager.getInstance().userData;
		
		((TextView) findViewById(R.id.registration_user_name_txt)).setText(currentUser.firstName + " " + currentUser.lastName);
		((TextView) findViewById(R.id.registration_user_position_txt)).setText(currentUser.position);
		((TextView) findViewById(R.id.registration_user_location_txt)).setText(currentUser.location);
		
		experienceEdit = ((EditText) findViewById(R.id.registration_experience_edit));
		educationEdit = ((EditText) findViewById(R.id.registration_education_edit));
		interestsEdit = ((EditText) findViewById(R.id.registration_interests_edit));
		
		if (!currentUser.experienceTags.isEmpty()) {
			experienceEdit.setText(currentUser.experienceTags);
		}
		
		experienceEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() >= 2 && s.toString().charAt(s.length() - 1) == ' ' && s.toString().charAt(s.length() - 2) == '#') {
					s.delete(s.length() - 1, s.length());
				}
				
				if (experienceHasFocus && s.toString().charAt(s.length() - 1) == ' ') {
					s.append("#");
				}
				
				if (experienceHasFocus && s.length() == 0) {
					s.append("#");
				}
			}
		});
		
		experienceEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				experienceHasFocus = hasFocus;
				if (hasFocus) {
					if (experienceEdit.getText().length() == 0) {
						experienceEdit.getText().append("#");
					} else if (experienceEdit.getText().charAt(experienceEdit.getText().length() - 1) == ' ') {
						experienceEdit.getText().append("#");
					} 
				} else {
					deleteLastHashTag();
				}
			}
		});
		
		if (!currentUser.educationTags.isEmpty()) {
			educationEdit.setText(currentUser.educationTags);
		}
		
		educationEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() >= 2 && s.toString().charAt(s.length() - 1) == ' ' && s.toString().charAt(s.length() - 2) == '#') {
					s.delete(s.length() - 1, s.length());
				}
				
				if (educationHasFocus && s.toString().charAt(s.length() - 1) == ' ') {
					s.append("#");
				}
				
				if (educationHasFocus && s.length() == 0) {
					s.append("#");
				}
			}
		});
		
		educationEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				educationHasFocus = hasFocus;
				if (hasFocus) {
					if (educationEdit.getText().length() == 0) {
						educationEdit.getText().append("#");
					} else if (educationEdit.getText().charAt(educationEdit.getText().length() - 1) == ' ') {
						educationEdit.getText().append("#");
					} 
				} else {
					deleteLastHashTag();
				}
			}
		});
		
		if (!currentUser.interestsTags.isEmpty()) {
			interestsEdit.setText(currentUser.interestsTags);
		}
		
		interestsEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() >= 2 && s.toString().charAt(s.length() - 1) == ' ' && s.toString().charAt(s.length() - 2) == '#') {
					s.delete(s.length() - 1, s.length());
				}
				
				if (interestsHasFocus && (s.toString().charAt(s.length() - 1) == ' ' || s.toString().charAt(s.length() - 1) == '\n')) {
					s.append("#");
				}
				
				if (interestsHasFocus && s.length() == 0) {
					s.append("#");
				}
			}
		});
		
		interestsEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				interestsHasFocus = hasFocus;
				if (hasFocus) {
					if (interestsEdit.getText().length() == 0) {
						interestsEdit.getText().append("#");
					} else if (interestsEdit.getText().charAt(interestsEdit.getText().length() - 1) == ' ') {
						interestsEdit.getText().append("#");
					} 
				} else {
					deleteLastHashTag();
				}
			}
		});
		
		ImageLoader.getInstance().loadImage(currentUser.pictureUrl, AssemblyAppManager.getInstance().targetSize256, null);
		ImageLoader.getInstance().displayImage(currentUser.pictureUrl, (ImageView) findViewById(R.id.registration_user_img), AssemblyAppManager.getInstance().optionsWithLoading);
		
		getActionBar().setTitle(Utils.getStringByResourceId(R.string.my_profile));
	}
	
	@Override
	public void onBackPressed() {
		deleteLastHashTag();
		saveUserTags();
		if (!checkIfTagsAreComplete()) {
			showAlmostThereDialog();
		} else {
			super.onBackPressed();
		}
	}
	
	private void deleteLastHashTag() {
		if (interestsEdit != null && interestsEdit.getText().length() > 0 && interestsEdit.getText().charAt(interestsEdit.getText().length() - 1) == '#') {
			interestsEdit.getText().delete(interestsEdit.getText().length() - 1, interestsEdit.getText().length());
		}
		
		if (educationEdit != null && educationEdit.getText().length() > 0 && educationEdit.getText().charAt(educationEdit.getText().length() - 1) == '#') {
			educationEdit.getText().delete(educationEdit.getText().length() - 1, educationEdit.getText().length());
		}
		
		if (experienceEdit != null && experienceEdit.getText().length() > 0 && experienceEdit.getText().charAt(experienceEdit.getText().length() - 1) == '#') {
			experienceEdit.getText().delete(experienceEdit.getText().length() - 1, experienceEdit.getText().length());
		}
	}
	
	private boolean checkIfTagsAreComplete() {
		String experienceTags = "";
		String educationTags = "";
		String interestsTags = "";
		if (experienceEdit != null) {
			experienceTags = experienceEdit.getText().toString();
		}
		if (educationEdit != null) {
			educationTags = educationEdit.getText().toString();
		}
		if (interestsEdit != null) {
			interestsTags = interestsEdit.getText().toString();
		}
		
		int experienceTagsCount = 0, educationTagsCount = 0, interestsTagsCount = 0;
		if (!experienceTags.isEmpty() && !experienceTags.equals("#")) {
			experienceTagsCount = experienceTags.split(" ").length;
		} 
		if (!educationTags.isEmpty() && !educationTags.equals("#")) {
			educationTagsCount = educationTags.split(" ").length;
		}
		if (!interestsTags.isEmpty() && !interestsTags.equals("#")) {
			interestsTagsCount = interestsTags.split(" ").length;
		}
		
		return (experienceTagsCount + educationTagsCount + interestsTagsCount) >= 6;
	}
	
	private void showAlmostThereDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
		LayoutInflater inflater = RegistrationActivity.this.getLayoutInflater();
		final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.dialog_about_me, null);
		builder.setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, int id) {
//				Utils.showLoadingDialog(RegistrationActivity.this);
			}
		});

		builder.create().show();
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	public void saveUserTags() {
		String experienceTags = "";
		String educationTags = "";
		String interestsTags = "";
		if (experienceEdit != null) {
			experienceTags = experienceEdit.getText().toString().equals("#") ? "" : experienceEdit.getText().toString().trim();
		}
		if (educationEdit != null) {
			educationTags = educationEdit.getText().toString().equals("#") ? "" : educationEdit.getText().toString().trim();
		}
		if (interestsEdit != null) {
			interestsTags = interestsEdit.getText().toString().equals("#") ? "" : interestsEdit.getText().toString().trim();
		}
		
		if (!experienceTags.isEmpty() && experienceTags.charAt(experienceTags.length() - 1) == '#') {
			experienceTags = experienceTags.substring(0, experienceTags.length() - 1);
		}
		
		if (!educationTags.isEmpty() && educationTags.charAt(educationTags.length() - 1) == '#') {
			educationTags = educationTags.substring(0, educationTags.length() - 1);
		}
		
		if (!interestsTags.isEmpty() && interestsTags.charAt(interestsTags.length() - 1) == '#') {
			interestsTags = interestsTags.substring(0, interestsTags.length() - 1);
		}
		
		String userId = AssemblyAppManager.getInstance().userData.userId;
		AssemblyAppManager.getInstance().userData.experienceTags = experienceTags;
		AssemblyAppManager.getInstance().userData.educationTags = educationTags;
		AssemblyAppManager.getInstance().userData.interestsTags = interestsTags;
		
		try {
			API.setTags(userId, experienceTags, educationTags, interestsTags, null);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
