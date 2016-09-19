package com.assembly.app.fragment;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.assembly.app.GroupActivity;
import com.assembly.app.R;
import com.assembly.app.backend.API;
import com.assembly.app.backend.API.RequestObserver;
import com.assembly.app.data.Connection;
import com.assembly.app.data.Conversation;
import com.assembly.app.data.Group;
import com.assembly.app.manager.AssemblyAppManager;
import com.assembly.app.utils.Utils;
import com.assembly.app.utils.view.GroupsAdapter;

public class GroupsFragment extends Fragment {

	private GroupsAdapter groupsAdapter;
	private ListView itemsList;
	private ArrayList<Group> groups = new ArrayList<Group>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getItemsListByType();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_connections, container, false);
		
		((ScrollView) rootView.findViewById(R.id.incoming_requests_container_scroll)).setVisibility(View.GONE);
		((TextView) rootView.findViewById(R.id.connections_no_visibility_in_suggestions_txt)).setVisibility(View.GONE);
		
		((TextView) rootView.findViewById(R.id.connections_join_group_txt)).setVisibility(View.VISIBLE);
		((TextView) rootView.findViewById(R.id.connections_join_group_txt)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				createJoinGroupDialog().show();
			}
		});

		groupsAdapter = new GroupsAdapter(getActivity(), R.layout.item_connections_listview, groups);
		itemsList = (ListView) rootView.findViewById(R.id.connections_listview);
		itemsList.setAdapter(groupsAdapter);
		itemsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (AssemblyAppManager.getInstance().currentSelectedGroup != null) {
					if (AssemblyAppManager.getInstance().currentSelectedGroup.groupId == groups.get(position).groupId) {
						if (AssemblyAppManager.getInstance().currentSelectedGroup.isGroupDataLoaded) {
							startGroupActivity();
						} else {
							loadGroupData();
						}
					} else if (groups.get(position).isGroupDataLoaded) {
						AssemblyAppManager.getInstance().currentSelectedGroup = groups.get(position);
						startGroupActivity();
					} else {
						AssemblyAppManager.getInstance().currentSelectedGroup = groups.get(position);
						loadGroupData();
					}
				} else {
					AssemblyAppManager.getInstance().currentSelectedGroup = groups.get(position);
					loadGroupData();
				}
			}
		});

		return rootView;
	}

	private void getItemsListByType() {
		ArrayList<Group> userGroups = AssemblyAppManager.getInstance().userData.groups;
		for (int i = 0; i < userGroups.size(); i++) {
			groups.add(userGroups.get(i));
		}
	}

	private void loadGroupData() {
		Utils.showLoadingDialog(getActivity());
		API.getGroupMembers(AssemblyAppManager.getInstance().userData.userId, AssemblyAppManager.getInstance().currentSelectedGroup.groupId, new RequestObserver() {

			@Override
			public void onSuccess(JSONObject response) throws JSONException {
				if (response.optString("status", "").equals("OK")) {
					AssemblyAppManager.getInstance().currentSelectedGroup.members.clear();
					JSONArray groupMembersJson = response.optJSONArray("data");
					for (int i = 0; i < groupMembersJson.length(); i++) {
						Connection connection = new Connection(groupMembersJson.optJSONObject(i));
						AssemblyAppManager.getInstance().currentSelectedGroup.members.add(connection);
					}
					AssemblyAppManager.getInstance().currentSelectedGroup.isGroupDataLoaded = true;
					startGroupActivity();
				}
			}

			@Override
			public void onError(String response, Exception e) {
				Utils.sendErrorToBackend(e.toString());
			}
		});

		
		HashMap<String, SparseArray<Conversation>> conversations = AssemblyAppManager.getInstance().userData.allConversations;
		for (String key : conversations.keySet()) {
			for (int i = 0; i < conversations.get(key).size(); i++) {
				int k = conversations.get(key).keyAt(i);
				if (conversations.get(key).get(k).isGroupConversation && conversations.get(key).get(k).groupId.equals(AssemblyAppManager.getInstance().currentSelectedGroup.groupId)) {
					AssemblyAppManager.getInstance().currentSelectedGroup.conversations.add(conversations.get(key).get(k));
				}
			}
		}
	}

	private void startGroupActivity() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Utils.hideLoadingDialog();
				Intent groupActivity = new Intent(getActivity(), GroupActivity.class);
				startActivity(groupActivity);
			}
		});
	}

	public AlertDialog createJoinGroupDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.dialog_join_group, null);
		builder.setView(view).setPositiveButton(R.string.join, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, int id) {
				String groupCode = ((EditText) view.findViewById(R.id.join_group_edit)).getText().toString();
				if (groupCode.isEmpty()) {
					GroupsFragment.this.getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							Utils.showToast(getActivity(), Utils.getStringByResourceId(R.string.join_group_code_not_valid));
						}
					});
				} else {
					((EditText) view.findViewById(R.id.join_group_edit)).setText("");
					try {
						API.joinGroup(AssemblyAppManager.getInstance().userData.userId, groupCode, new RequestObserver() {

							@Override
							public void onSuccess(JSONObject response) throws JSONException {
								if (response.optString("status").equals("OK") && response.optJSONArray("data").length() > 0) {
									Group group = new Group(response.optJSONArray("data").getJSONObject(0));
									AssemblyAppManager.getInstance().userData.groups.add(group);
									GroupsFragment.this.getActivity().runOnUiThread(new Runnable() {

										@Override
										public void run() {
											dialog.dismiss();
											groupsAdapter.clear();
											getItemsListByType();
											groupsAdapter.notifyDataSetChanged();
											Utils.showToast(getActivity(), Utils.getStringByResourceId(R.string.join_group_success));
										}
									});
								} else {
									GroupsFragment.this.getActivity().runOnUiThread(new Runnable() {

										@Override
										public void run() {
											Utils.showToast(getActivity(), Utils.getStringByResourceId(R.string.join_group_failure));
										}
									});
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
			}
		}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});

		return builder.create();
	}
}
