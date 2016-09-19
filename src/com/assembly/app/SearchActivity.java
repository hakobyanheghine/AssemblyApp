package com.assembly.app;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.assembly.app.backend.API;
import com.assembly.app.backend.API.RequestObserver;
import com.assembly.app.data.Connection;
import com.assembly.app.manager.AssemblyAppManager;
import com.assembly.app.utils.Constants;
import com.assembly.app.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SearchActivity extends Activity {

	public static final int TAB_CONNECTION = 1;
	public static final int TAB_SUGGESTION = 2;
	
	
	private LinearLayout connectionsList;
	private LinearLayout suggestionsList;
	private String query;
	private TextView newMessagesCountTxt;
	private TextView noSearchResultsTxt;
	private TextView suggestionsTxt;
	private TextView connectionsTxt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		
		connectionsList = (LinearLayout) findViewById(R.id.connections_listview);
		suggestionsList = (LinearLayout) findViewById(R.id.suggestions_listview);
		noSearchResultsTxt = (TextView) findViewById(R.id.no_results_txt);
		suggestionsTxt = (TextView) findViewById(R.id.suggestion_txt);
		connectionsTxt = (TextView) findViewById(R.id.connections_txt);
		
		if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
			query = getIntent().getStringExtra(SearchManager.QUERY);
			try {
				search();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		getActionBar().setTitle(query);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			query = intent.getStringExtra(SearchManager.QUERY);
			try {
				search();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	private void search() throws UnsupportedEncodingException {
		Utils.showLoadingDialog(this);
		API.search(AssemblyAppManager.getInstance().userData.userId, query, new RequestObserver() {

			@Override
			public void onSuccess(JSONObject response) throws JSONException {
				AssemblyAppManager.getInstance().userData.searchResultsConnections.clear();
				AssemblyAppManager.getInstance().userData.searchResultsSuggestions.clear();
				if (response.optString("status", "").equals("OK")) {
					JSONArray searchResultJson = response.optJSONObject("data").optJSONArray("dataTable");
					for (int i = 0; i < searchResultJson.length(); i++) {
						Connection connection = new Connection(searchResultJson.optJSONObject(i));
						if (connection.isFriend) {
							AssemblyAppManager.getInstance().userData.searchResultsConnections.add(connection);
						} else {
							AssemblyAppManager.getInstance().userData.searchResultsSuggestions.add(connection);
						}
					}
					SearchActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							Utils.hideLoadingDialog();
							SearchActivity.this.updateConnections(AssemblyAppManager.getInstance().userData.searchResultsConnections, true);
							SearchActivity.this.updateConnections(AssemblyAppManager.getInstance().userData.searchResultsSuggestions, false);
							if (AssemblyAppManager.getInstance().userData.searchResultsConnections.isEmpty() && AssemblyAppManager.getInstance().userData.searchResultsSuggestions.isEmpty()) {
								showNoSearchResults();
							} else {
								hideNoSearchResults();
							}
						}
					});
				} else {
					SearchActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							Utils.hideLoadingDialog();
							SearchActivity.this.updateConnections(AssemblyAppManager.getInstance().userData.searchResultsConnections, true);
							SearchActivity.this.updateConnections(AssemblyAppManager.getInstance().userData.searchResultsSuggestions, false);
							showNoSearchResults();
						}
					});
				}
			}

			@Override
			public void onError(String response, Exception e) {
				Utils.sendErrorToBackend(e.toString());
				AssemblyAppManager.getInstance().userData.searchResultsConnections.clear();
				AssemblyAppManager.getInstance().userData.searchResultsSuggestions.clear();
				SearchActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Utils.hideLoadingDialog();
						SearchActivity.this.updateConnections(AssemblyAppManager.getInstance().userData.searchResultsConnections, true);
						SearchActivity.this.updateConnections(AssemblyAppManager.getInstance().userData.searchResultsSuggestions, false);
						showNoSearchResults();
					}
				});
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		View messagesView = menu.findItem(R.id.action_messages).getActionView();
		newMessagesCountTxt = ((TextView) messagesView.findViewById(R.id.messages_new_count));
		updateNewMessagesCount();
		messagesView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent messagesActivity = new Intent(SearchActivity.this, ConversationsActivity.class);
				startActivity(messagesActivity);
			}
		});
		
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_search) {
			return true;
		} else if (id == R.id.action_messages) {
			Intent messagesActivity = new Intent(SearchActivity.this, ConversationsActivity.class);
			startActivity(messagesActivity);
			return true;
		} else if (id == android.R.id.home) {
			super.onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void updateNewMessagesCount() {
		if (AssemblyAppManager.getInstance().newMessagesCount > 0) {
			newMessagesCountTxt.setVisibility(View.VISIBLE);
			newMessagesCountTxt.setText(AssemblyAppManager.getInstance().newMessagesCount + "");
		} else {
			newMessagesCountTxt.setVisibility(View.GONE);
		}
	}
	
	public void updateConnections(final ArrayList<Connection> connections, boolean isConnection) {
		if (isConnection) {
			connectionsList.removeAllViews();
		} else {
			suggestionsList.removeAllViews();
		}
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		for (int i = 0; i < connections.size(); i++) {
			RelativeLayout convertView = (RelativeLayout) inflater.inflate(R.layout.item_connections_listview, null, false);
			
			((TextView) convertView.findViewById(R.id.connection_name_txt)).setText(connections.get(i).firstName + " " + connections.get(i).lastName);
			((TextView) convertView.findViewById(R.id.connection_position_txt)).setText(connections.get(i).position);

			if (connections.get(i).distance.isEmpty() || connections.get(i).distance.equals(Constants.DEFAULT_DISTANCE) || !AssemblyAppManager.getInstance().hasEnabledGPS) {
				((TextView) convertView.findViewById(R.id.connection_distance_txt)).setText("-");
			} else {
				((TextView) convertView.findViewById(R.id.connection_distance_txt)).setText(connections.get(i).distance + " " + AssemblyAppManager.getInstance().userData.userDistanceUnit);
			}
			
			((TextView) convertView.findViewById(R.id.connection_friends_txt)).setText(connections.get(i).commonFriendsCount + "");
			
			((TextView) convertView.findViewById(R.id.connection_hashtags_container)).setVisibility(View.GONE);

			// code to download image
			ImageLoader.getInstance().loadImage(connections.get(i).pictureUrl, AssemblyAppManager.getInstance().targetSize70, null);
			ImageLoader.getInstance().displayImage(connections.get(i).pictureUrl, (ImageView) convertView.findViewById(R.id.connection_img), AssemblyAppManager.getInstance().optionsWithLoading);
			
			final int index = i;
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					AssemblyAppManager.getInstance().loadConnectionProfile(SearchActivity.this, connections.get(index).userId);
				}
			});
			
			if (isConnection) {
				connectionsList.addView(convertView);
			} else {
				suggestionsList.addView(convertView);
			}
		}
	}
	
	private void showNoSearchResults() {
		suggestionsTxt.setVisibility(View.GONE);
		connectionsTxt.setVisibility(View.GONE);
		noSearchResultsTxt.setVisibility(View.VISIBLE);
	}
	
	private void hideNoSearchResults() {
		suggestionsTxt.setVisibility(View.VISIBLE);
		connectionsTxt.setVisibility(View.VISIBLE);
		noSearchResultsTxt.setVisibility(View.GONE);
	}
}
