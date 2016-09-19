package com.assembly.app.fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.assembly.app.HomeActivity;
import com.assembly.app.R;
import com.assembly.app.backend.API;
import com.assembly.app.backend.API.RequestObserver;
import com.assembly.app.data.Connection;
import com.assembly.app.manager.AssemblyAppManager;
import com.assembly.app.manager.PreferenceManager;
import com.assembly.app.utils.Constants;
import com.assembly.app.utils.Utils;

public class HomeFragment extends Fragment implements ActionBar.TabListener {
	
	public static final int TAB_COUNT = 3;
	public static final int TAB_SUGGESTION = 0;
	public static final int TAB_CONNECTION = 1;
	public static final int TAB_GROUP = 2;

	private AppSectionsPagerAdapter mAppSectionsPagerAdapter;
	private ViewPager mViewPager;

	private Fragment fragmentSuggestions = ConnectionsFragment.newInstance(ConnectionsFragment.TYPE_SUGGESTION);
	private Fragment fragmentConnections = ConnectionsFragment.newInstance(ConnectionsFragment.TYPE_CONNECTION);
	private Fragment fragmentGroups = new GroupsFragment();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_home, container, false);
		final ActionBar actionBar = getActivity().getActionBar();
		
		mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getActivity().getSupportFragmentManager());

		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		
		mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
		mViewPager.setAdapter(mAppSectionsPagerAdapter);
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});

		if (actionBar.getTabCount() == 0) {
			actionBar.addTab(actionBar.newTab().setText(R.string.tab_suggestions).setTabListener(this));
			actionBar.addTab(actionBar.newTab().setText(R.string.tab_connections).setTabListener(this));
			actionBar.addTab(actionBar.newTab().setText(R.string.tab_groups).setTabListener(this));
		}
		
		actionBar.selectTab(actionBar.getTabAt(AssemblyAppManager.getInstance().lastSelectedTab));
		
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		((HomeActivity) getActivity()).getmDrawerToggle().setDrawerIndicatorEnabled(true);

		if (AssemblyAppManager.getInstance().updateConnections) {
			AssemblyAppManager.getInstance().updateConnections = false;
			Utils.showLoadingDialog(getActivity());
			getUserConnectionsAndSuggestions();
		}
	}
	
	public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

		public AppSectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case TAB_SUGGESTION:
				return fragmentSuggestions;
			case TAB_CONNECTION:
				return fragmentConnections;
			case TAB_GROUP:
				return fragmentGroups;
			default:
				Fragment fragmentDefault = ConnectionsFragment.newInstance(ConnectionsFragment.TYPE_SUGGESTION);
				return fragmentDefault;
			}
		}

		@Override
		public int getCount() {
			return TAB_COUNT;
		}
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		AssemblyAppManager.getInstance().lastSelectedTab = tab.getPosition();
		
		if (tab.getPosition() == TAB_SUGGESTION && AssemblyAppManager.getInstance().updateSuggestionsAfterRequest) {
			AssemblyAppManager.getInstance().updateSuggestionsAfterRequest = false;
			if (fragmentSuggestions != null) {
				((ConnectionsFragment) fragmentSuggestions).updateConnectionsAndSuggestions();
			}
		}
		
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	
	private void getUserConnectionsAndSuggestions() {
		String userId = PreferenceManager.getInstance().getUserId();
		API.getConnections(userId, new RequestObserver() {
			
			@Override
			public void onSuccess(JSONObject response) throws JSONException {
				if (response.optString("status", "").equals("OK")) {
					JSONObject data = response.optJSONObject("data");
					JSONArray dataTable = data.optJSONArray("dataTable");
					AssemblyAppManager.getInstance().userData.incomingRequests.clear();
					AssemblyAppManager.getInstance().userData.connections.clear();
					for (int i = 0; i < dataTable.length(); i++) {
						Connection connection = new Connection(dataTable.optJSONObject(i));
						if (connection.isFriendRequestSent) {
							AssemblyAppManager.getInstance().userData.incomingRequests.add(connection);
						} else {
							AssemblyAppManager.getInstance().userData.connections.add(connection);
						}
					}
					
					HomeFragment.this.getActivity().runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							((ConnectionsFragment) fragmentConnections).updateConnectionsAndSuggestions();
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
		
		API.getSuggestions(userId, new RequestObserver() {
			
			@Override
			public void onSuccess(JSONObject response) throws JSONException {
				if (response.optString("status", "").equals("OK")) {
					JSONObject data = response.optJSONObject("data");
					JSONArray dataTable = data.optJSONArray("dataTable");
					AssemblyAppManager.getInstance().userData.suggestions.clear();
					for (int i = 0; i < dataTable.length(); i++) {
						Connection suggestion = new Connection(dataTable.optJSONObject(i));
						AssemblyAppManager.getInstance().userData.suggestions.add(suggestion);
					}
					
					Utils.hideLoadingDialog();
					HomeFragment.this.getActivity().runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							((ConnectionsFragment) fragmentSuggestions).updateConnectionsAndSuggestions();
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
}
