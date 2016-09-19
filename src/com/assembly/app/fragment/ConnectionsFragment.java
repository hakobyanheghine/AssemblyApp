package com.assembly.app.fragment;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.assembly.app.R;
import com.assembly.app.backend.API;
import com.assembly.app.backend.API.RequestObserver;
import com.assembly.app.data.Connection;
import com.assembly.app.manager.AssemblyAppManager;
import com.assembly.app.manager.PreferenceManager;
import com.assembly.app.utils.Constants;
import com.assembly.app.utils.Utils;
import com.assembly.app.utils.view.ConnectionsAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ConnectionsFragment extends Fragment {

	public static final String CURRENT_TYPE = "current_type";
	
	public static final int TYPE_SUGGESTION = 1;
	public static final int TYPE_CONNECTION = 2;

	private ConnectionsAdapter connectionsAdapter;
	private ListView itemsList;
	private LinearLayout incomingRequestContainer;
	private ScrollView incomingRequestContainerScroll;
	private TextView noVisibilityInSuggestions;
	private ArrayList<Connection> connections = new ArrayList<Connection>();
	private ArrayList<Connection> incomingRequests = new ArrayList<Connection>();
	private int currentType;

	public static final ConnectionsFragment newInstance(int type) {
		ConnectionsFragment connectionsFragment = new ConnectionsFragment();
		Bundle connectionsBundle = new Bundle(1);
	    connectionsBundle.putInt(CURRENT_TYPE, type);
	    connectionsFragment.setArguments(connectionsBundle);
	    return connectionsFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentType = getArguments().getInt(CURRENT_TYPE);
		getItemsListByType();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_connections, container, false);

		((TextView) rootView.findViewById(R.id.connections_join_group_txt)).setVisibility(View.GONE);
		

		noVisibilityInSuggestions = ((TextView) rootView.findViewById(R.id.connections_no_visibility_in_suggestions_txt));
		
		incomingRequestContainerScroll = ((ScrollView) rootView.findViewById(R.id.incoming_requests_container_scroll));
		incomingRequestContainer = ((LinearLayout) rootView.findViewById(R.id.incoming_requests_container));

		if (currentType == TYPE_CONNECTION) {
			incomingRequests = AssemblyAppManager.getInstance().userData.incomingRequests;
			initializeIncomingRequests();
		} else {
			incomingRequestContainerScroll.setVisibility(View.GONE);
		}
		
		connectionsAdapter = new ConnectionsAdapter(getActivity(), R.layout.item_connections_listview, connections, false);
		itemsList = (ListView) rootView.findViewById(R.id.connections_listview);
		itemsList.setAdapter(connectionsAdapter);
		itemsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				AssemblyAppManager.getInstance().loadConnectionProfile(getActivity(), connections.get(position).userId);
			}
		});
		
		if (currentType == TYPE_SUGGESTION) {
			if (AssemblyAppManager.getInstance().userData.showInSuggestions) {
				itemsList.setVisibility(View.VISIBLE);
				noVisibilityInSuggestions.setVisibility(View.GONE);
			} else {
				itemsList.setVisibility(View.GONE);
				noVisibilityInSuggestions.setVisibility(View.VISIBLE);
			}
		} else {
			itemsList.setVisibility(View.VISIBLE);
			noVisibilityInSuggestions.setVisibility(View.GONE);
		}

		return rootView;
	}

	private void getItemsListByType() {
		if (AssemblyAppManager.getInstance().userData == null) {
			return;
		}
		
		if (currentType == TYPE_SUGGESTION) {
			ArrayList<Connection> suggestions = AssemblyAppManager.getInstance().userData.suggestions;
			for (int i = 0; i < suggestions.size(); i++) {
				connections.add(suggestions.get(i));
			}
		} else if (currentType == TYPE_CONNECTION) {
			ArrayList<Connection> userConnections = AssemblyAppManager.getInstance().userData.connections;
			for (int i = 0; i < userConnections.size(); i++) {
				connections.add(userConnections.get(i));
			}
		}
	}

	public void updateConnectionsAndSuggestions() {
		if (connectionsAdapter != null && itemsList != null) {
			connectionsAdapter.clear();
			getItemsListByType();
			connectionsAdapter.notifyDataSetChanged();
		
			if (currentType == TYPE_SUGGESTION) {
				if (AssemblyAppManager.getInstance().userData.showInSuggestions) {
					itemsList.setVisibility(View.VISIBLE);
					noVisibilityInSuggestions.setVisibility(View.GONE);
				} else {
					itemsList.setVisibility(View.GONE);
					noVisibilityInSuggestions.setVisibility(View.VISIBLE);
				}
			} else {
				itemsList.setVisibility(View.VISIBLE);
				noVisibilityInSuggestions.setVisibility(View.GONE);
			}
		}
	}
	
	private void initializeIncomingRequests() {
		if (incomingRequests.isEmpty()) {
			incomingRequestContainerScroll.setVisibility(View.GONE);
		} else {
			incomingRequestContainer.removeAllViews();
			incomingRequestContainerScroll.setVisibility(View.VISIBLE);
			if (incomingRequests.size() <= 1) {
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				params.setMargins(40, 35, 40, 0);
				incomingRequestContainerScroll.setLayoutParams(params);
			}

			for (int i = 0; i < incomingRequests.size(); i++) {
				LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				RelativeLayout incomingRequestView = (RelativeLayout) layoutInflater.inflate(R.layout.item_incoming_request, null);

				((TextView) incomingRequestView.findViewById(R.id.incoming_request_name_txt)).setText(incomingRequests.get(i).firstName + " " + incomingRequests.get(i).lastName);
				ImageLoader.getInstance().loadImage(incomingRequests.get(i).pictureUrl, AssemblyAppManager.getInstance().targetSize64, null);
				ImageLoader.getInstance().displayImage(incomingRequests.get(i).pictureUrl, ((ImageView) incomingRequestView.findViewById(R.id.incoming_request_img)), AssemblyAppManager.getInstance().optionsWithLoading);

				final int index = i;
				((ImageView) incomingRequestView.findViewById(R.id.incoming_request_check_img)).setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Utils.showLoadingDialog(ConnectionsFragment.this.getActivity());
						API.setConnect(AssemblyAppManager.getInstance().userData.userId, incomingRequests.get(index).userId, Constants.ACTION_CONNECT, new RequestObserver() {

							@Override
							public void onSuccess(JSONObject response) throws JSONException {
								if (response.optString("status", "").equals("OK")) {
									getUserConnectionsAndSuggestions();
								}
							}

							@Override
							public void onError(String response, Exception e) {
								Utils.sendErrorToBackend(e.toString());
							}
						});
					}
				});

				((ImageView) incomingRequestView.findViewById(R.id.incoming_request_cancel_img)).setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Utils.showLoadingDialog(ConnectionsFragment.this.getActivity());
						API.setConnect(AssemblyAppManager.getInstance().userData.userId, incomingRequests.get(index).userId, Constants.ACTION_DENY, new RequestObserver() {
							
							@Override
							public void onSuccess(JSONObject response) throws JSONException {
								if (response.optString("status", "").equals("OK")) {
									ConnectionsFragment.this.getActivity().runOnUiThread(new Runnable() {

										@Override
										public void run() {
											Utils.hideLoadingDialog();
											AssemblyAppManager.getInstance().userData.incomingRequests.remove(index);
											initializeIncomingRequests();
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
				});

				incomingRequestView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						AssemblyAppManager.getInstance().loadConnectionProfile(getActivity(), incomingRequests.get(index).userId);
					}
				});
				
				incomingRequestContainer.addView(incomingRequestView);
			}
		}
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
					
					Utils.hideLoadingDialog();
					ConnectionsFragment.this.getActivity().runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							updateConnectionsAndSuggestions();
							initializeIncomingRequests();
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
					AssemblyAppManager.getInstance().updateSuggestionsAfterRequest = true;
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
