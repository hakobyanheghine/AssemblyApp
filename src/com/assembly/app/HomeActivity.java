package com.assembly.app;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.assembly.app.backend.API;
import com.assembly.app.backend.API.RequestObserver;
import com.assembly.app.data.Connection;
import com.assembly.app.fragment.HomeFragment;
import com.assembly.app.manager.AssemblyAppManager;
import com.assembly.app.manager.PreferenceManager;
import com.assembly.app.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class HomeActivity extends FragmentActivity {

	public static final int MENU_ITEM_SETTINGS = 0;
	public static final int MENU_ITEM_EDIT_MY_PROFILE = 1;
	public static final int MENU_ITEM_FEEDBACK = 2;
	public static final int MENU_ITEM_TUTORIAL = 3;
	public static final int MENU_ITEM_LOG_OUT = 4;
	
	public static final String NOTIFICATION_USER_ID = "notification_user_id";
	public static final String NOTIFICATION_GROUP_ID = "notification_group_id";
	
	private String notificationUserId = "";
	private String notificationGroupId = "";
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private String[] menuItems = { "Settings", "Edit my profile", "Report Bugs", "Tutorial", "Log Out" };

	private TextView newMessagesCountTxt;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.drawer_list);

		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
		getActionBar().setTitle(Utils.getStringByResourceId(R.string.app_name));

		mDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menuItems));
		mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == MENU_ITEM_SETTINGS) {
					AssemblyAppManager.getInstance().showDrawerAfterSettings = true;
					startSettingsActivity();
				} else if (position == MENU_ITEM_EDIT_MY_PROFILE) {
					startEditMyProfileActivity();
				} else if (position == MENU_ITEM_FEEDBACK) {
					showFeedbackActivity();
				} else if (position == MENU_ITEM_TUTORIAL) {
					startTutorialActivity();
				} else if (position == MENU_ITEM_LOG_OUT) {
					mDrawerLayout.closeDrawers();
					logoutUser();
				}
			}
		});

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_action_navigation_menu, R.string.drawer_open, R.string.drawer_close) {
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			}

			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		
		ImageLoader.getInstance().loadImage(AssemblyAppManager.getInstance().userData.pictureUrl, AssemblyAppManager.getInstance().targetSize128, null);
		ImageLoader.getInstance().displayImage(AssemblyAppManager.getInstance().userData.pictureUrl, (ImageView) mDrawerLayout.findViewById(R.id.drawer_user_profile_img), AssemblyAppManager.getInstance().optionsWithLoading);
		((TextView) mDrawerLayout.findViewById(R.id.drawer_user_name_txt)).setText(AssemblyAppManager.getInstance().userData.firstName + " " + AssemblyAppManager.getInstance().userData.lastName);
		
		((LinearLayout) mDrawerLayout.findViewById(R.id.drawer_container)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			}
		});

		if (savedInstanceState == null) {
			Fragment fragment = new HomeFragment();
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.drawer_content_frame, fragment).addToBackStack(null).commit();
		}
		
		if (getIntent().getExtras() != null) {
			notificationUserId = getIntent().getExtras().getString(NOTIFICATION_USER_ID, "");
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		updateNewMessagesCount();
		mDrawerToggle.setDrawerIndicatorEnabled(true);
		AssemblyAppManager.getInstance().currentConnectionsActivity = this;
		
		if (PreferenceManager.getInstance().getIsFirstTime()) {
			PreferenceManager.getInstance().setIsFirstTime(false);
			startEditMyProfileActivity();
		}
		
		if (!notificationUserId.isEmpty()) {
			AssemblyAppManager.getInstance().loadConnectionProfile(HomeActivity.this, notificationUserId);
			notificationUserId = "";
		}
		
		if (!notificationGroupId.isEmpty()) {
			// launch group activity
		}
		
		if (AssemblyAppManager.getInstance().showDrawerAfterSettings) {
			AssemblyAppManager.getInstance().showDrawerAfterSettings = false;
			mDrawerLayout.openDrawer(Gravity.LEFT);
		}
		
		if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		AssemblyAppManager.getInstance().currentConnectionsActivity = null;
	}
	
	@Override
	public void onBackPressed() {
		finish();
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		final View messagesView = menu.findItem(R.id.action_messages).getActionView();
		newMessagesCountTxt = ((TextView) messagesView.findViewById(R.id.messages_new_count));
		updateNewMessagesCount();
		messagesView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent messagesActivity = new Intent(HomeActivity.this, ConversationsActivity.class);
				startActivity(messagesActivity);
			}
		});

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		int id = item.getItemId();
		if (id == R.id.action_search) {
			return true;
		} else if (id == R.id.action_messages) {
			Intent messagesActivity = new Intent(HomeActivity.this, ConversationsActivity.class);
			startActivity(messagesActivity);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void updateNewMessagesCount() {
		if (newMessagesCountTxt != null) {
			if (AssemblyAppManager.getInstance().newMessagesCount > 0) {
				newMessagesCountTxt.setVisibility(View.VISIBLE);
				newMessagesCountTxt.setText(AssemblyAppManager.getInstance().newMessagesCount + "");
			} else {
				newMessagesCountTxt.setVisibility(View.GONE);
			}
		}
	}
	
	public ActionBarDrawerToggle getmDrawerToggle() {
		return mDrawerToggle;
	}
	
	private void startSettingsActivity() {
		HomeActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Intent settingsActivity = new Intent(HomeActivity.this, SettingsActivity.class);
				startActivity(settingsActivity);
			}
		});
	}
	
	private void startMainActivity() {
		HomeActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Utils.hideLoadingDialog();
				Intent mainActivity = new Intent(HomeActivity.this, MainActivity.class);
				mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); 
				startActivity(mainActivity);
				finish();
			}
		});
	}
	
	private void logoutUser() {
		AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
		LayoutInflater inflater = HomeActivity.this.getLayoutInflater();
		final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.dialog_logout, null);
		builder.setView(view).setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, int id) {
				Utils.showLoadingDialog(HomeActivity.this);
				API.logoutUser(AssemblyAppManager.getInstance().userData.userLinkedinId, AssemblyAppManager.getInstance().userData.userId, new RequestObserver() {
					
					@Override
					public void onSuccess(JSONObject response) throws JSONException {
						if (response.optString("status", "").equals("OK")) {
							PreferenceManager.getInstance().setLinkedinMemberId("");
							AssemblyAppManager.getInstance().resetUserData();
							startMainActivity();
						}
					}
					
					@Override
					public void onError(String response, Exception e) {
						Utils.sendErrorToBackend(e.toString());
					}
				});
			}
		}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});

		builder.create().show();
	}
	
	public void showFeedbackActivity() {
		AssemblyAppManager.getInstance().currentSelectedConnection = new Connection();
		AssemblyAppManager.getInstance().currentSelectedConversation = AssemblyAppManager.getInstance().feedbackConversation;
		startMessagesActivity();
	}
	
	private void startMessagesActivity() {
		HomeActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Intent messagesActivity = new Intent(HomeActivity.this, MessagesActivity.class);
				startActivity(messagesActivity);
			}
		});
	}
	
	private void startTutorialActivity() {
		HomeActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Intent tutorialActivity = new Intent(HomeActivity.this, TutorialActivity.class);
				startActivity(tutorialActivity);
			}
		});
	}
	
	private void startEditMyProfileActivity() {
		HomeActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Intent registrationActivity = new Intent(HomeActivity.this, RegistrationActivity.class);
				startActivity(registrationActivity);
			}
		});
	}
}
