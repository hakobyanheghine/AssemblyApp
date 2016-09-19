package com.assembly.app;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.assembly.app.backend.API;
import com.assembly.app.backend.API.RequestObserver;
import com.assembly.app.data.Connection;
import com.assembly.app.data.Conversation;
import com.assembly.app.data.Group;
import com.assembly.app.manager.AssemblyAppManager;
import com.assembly.app.manager.FilesManager;
import com.assembly.app.utils.Utils;
import com.assembly.app.utils.view.ConnectionsAdapter;
import com.assembly.app.utils.view.GroupConversationsAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;

public class GroupActivity extends Activity {

	private Group currentGroup;
	private ConnectionsAdapter membersAdapter;
	private ConnectionsAdapter searchAdapter;
	private GroupConversationsAdapter messagesAdapter;
	private ListView membersList;
	
	private boolean isSearch;
	private TextView noSearchResults;
	private ArrayList<Connection> searchResults = new ArrayList<Connection>();
	
	private float downY;
	private float upY;
	private boolean isActionMove;
	
	private RelativeLayout groupDescriptionContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group);
		
		currentGroup = AssemblyAppManager.getInstance().currentSelectedGroup;
		
		noSearchResults = ((TextView) findViewById(R.id.group_no_results_txt));
		
		((TextView) findViewById(R.id.group_name_txt)).setText(currentGroup.name);
		((TextView) findViewById(R.id.group_description_txt)).setText(currentGroup.description);
		
		groupDescriptionContainer = (RelativeLayout) findViewById(R.id.group_description_container);
		
		ImageLoader.getInstance().loadImage(currentGroup.photoUrl, AssemblyAppManager.getInstance().targetSize256, null);
		ImageLoader.getInstance().displayImage(currentGroup.photoUrl, (ImageView) findViewById(R.id.group_img), AssemblyAppManager.getInstance().optionsWithLoading);
		
		final TextView groupMembersBtn = ((TextView) findViewById(R.id.group_members_btn));
		final TextView groupMessagesBtn = ((TextView) findViewById(R.id.group_messages_btn));
		final TextView groupNewConversationBtn = ((TextView) findViewById(R.id.group_new_conversation_btn));
		final EditText groupSearchEdit = ((EditText) findViewById(R.id.group_search_edit));
		
		groupMembersBtn.setTypeface(null, Typeface.BOLD);
		groupMembersBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				isSearch = false;
				noSearchResults.setVisibility(View.GONE);
				if (Utils.isTablet()) {
					groupMessagesBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.members_button_unselected_background_big);
					groupMembersBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.members_button_selected_background_big);
				} else {
					groupMessagesBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.members_button_unselected_background);
					groupMembersBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.members_button_selected_background);
				}
				
				groupMembersBtn.setTypeface(null, Typeface.BOLD);
				groupMessagesBtn.setTypeface(null, Typeface.NORMAL);
				
				groupSearchEdit.setVisibility(View.VISIBLE);
				groupNewConversationBtn.setVisibility(View.GONE);
				
				if (currentGroup.members.isEmpty()) {
					membersList.setVisibility(View.GONE);
				} else {
					membersList.setVisibility(View.VISIBLE);
					membersList.setAdapter(membersAdapter);
					membersAdapter.notifyDataSetChanged();
				}
			}
		});
		
		groupMembersBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = MotionEventCompat.getActionMasked(event);

				switch (action) {
				case (MotionEvent.ACTION_DOWN):
					downY = event.getY();
					return false;
				case (MotionEvent.ACTION_MOVE):
					isActionMove = true;
					return false;
				case (MotionEvent.ACTION_UP):
					upY  = event.getY();
					moveMembersUpOrDown();
					return false;
				case (MotionEvent.ACTION_CANCEL):
					return false;
				case (MotionEvent.ACTION_OUTSIDE):
					return false;
				default:
					return false;
				}
			}
		});

		groupMessagesBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				isSearch = false;
				noSearchResults.setVisibility(View.GONE);
				if (Utils.isTablet()) {
					groupMembersBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.members_button_unselected_background_big);
					groupMessagesBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.members_button_selected_background_big);
				} else {
					groupMembersBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.members_button_unselected_background);
					groupMessagesBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.members_button_selected_background);	
				}
				
				groupMembersBtn.setTypeface(null, Typeface.NORMAL);
				groupMessagesBtn.setTypeface(null, Typeface.BOLD);
				
				groupSearchEdit.setVisibility(View.GONE);
				groupNewConversationBtn.setVisibility(View.VISIBLE);
				
				if (currentGroup.conversations.isEmpty()) {
					membersList.setVisibility(View.GONE);
				} else {
					membersList.setVisibility(View.VISIBLE);
					membersList.setAdapter(messagesAdapter);
					messagesAdapter.notifyDataSetChanged();
				}
			}
		});
		
		groupNewConversationBtn.setVisibility(View.GONE);
		groupNewConversationBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AssemblyAppManager.getInstance().currentSelectedGroup = currentGroup;
				AssemblyAppManager.getInstance().currentSelectedConversation = new Conversation("g" + currentGroup.groupId, 
						AssemblyAppManager.getInstance().userData.firstName + " " + AssemblyAppManager.getInstance().userData.lastName, 
						AssemblyAppManager.getInstance().userData.pictureUrl, currentGroup.groupId, true, false, false);
				AssemblyAppManager.getInstance().showTopicDialog = true;
				startMessagesActivity();
			}
		});
		
		searchAdapter = new ConnectionsAdapter(this, R.layout.item_connections_listview, searchResults, true);
		groupSearchEdit.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().isEmpty()) {
					isSearch = false;
					noSearchResults.setVisibility(View.GONE);
					membersList.setAdapter(membersAdapter);
					membersAdapter.notifyDataSetChanged();
				}
			}
		});
		groupSearchEdit.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE ||
						(event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
					String searchText = groupSearchEdit.getEditableText().toString();
					if (!searchText.isEmpty()) {
						isSearch = true;
						searchInGroup(searchText);
						
						InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						inputMethodManager.hideSoftInputFromWindow(groupSearchEdit.getWindowToken(), 0);
						
						return true;
					}
				}
				return false;
			}
		});
		
		groupSearchEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethodManager.hideSoftInputFromWindow(groupSearchEdit.getWindowToken(), 0);
				}
			}
		});
		
		membersAdapter = new ConnectionsAdapter(this, R.layout.item_connections_listview, currentGroup.members, true);
		membersList = (ListView) findViewById(R.id.group_members_listview);
		membersList.setAdapter(membersAdapter);
		membersList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (membersList.getAdapter() instanceof ConnectionsAdapter) {
					if (isSearch) {
						AssemblyAppManager.getInstance().loadConnectionProfile(GroupActivity.this, searchResults.get(position).userId);
					} else {
						AssemblyAppManager.getInstance().loadConnectionProfile(GroupActivity.this, currentGroup.members.get(position).userId);
					}
				} else if (membersList.getAdapter() instanceof GroupConversationsAdapter) {
					AssemblyAppManager.getInstance().currentSelectedGroup = currentGroup;
					AssemblyAppManager.getInstance().currentSelectedConversation = currentGroup.conversations.get(position);
					makeConversationRead(currentGroup.conversations.get(position));
					messagesAdapter.notifyDataSetChanged();
					startMessagesActivity();
				}
			}
		});
		
		if (currentGroup.members.isEmpty() && currentGroup.conversations.isEmpty()) {
			membersList.setVisibility(View.GONE);
		}
		
		Collections.sort(currentGroup.conversations);
		messagesAdapter = new GroupConversationsAdapter(this, R.layout.item_conversations_listview, currentGroup.conversations);
		
		getActionBar().setTitle(currentGroup.name);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		messagesAdapter.notifyDataSetChanged();
	}
	
	private void startMessagesActivity() {
		GroupActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Intent messagesActivity = new Intent(GroupActivity.this, MessagesActivity.class);
				startActivity(messagesActivity);
			}
		});
	}

	private void makeConversationRead(final Conversation currentConversation) {
		GroupActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				for (int i = currentConversation.messages.size() - 1; i >= 0; i--) {
					if (currentConversation.messages.get(i).isNew) {
						currentConversation.messages.get(i).isNew = false;
					} else {
						break;
					}
				}
				AssemblyAppManager.getInstance().newMessagesCount -= currentConversation.newMessageCount;
				currentConversation.newMessageCount = 0;
				FilesManager.getInstance().saveMessagesToFileAsync();
			}
		});
	}
	
	private void searchInGroup(String searchText) {
		Utils.showLoadingDialog(this);
		try {
			API.search(AssemblyAppManager.getInstance().userData.userId, searchText, new RequestObserver() {
				
				@Override
				public void onSuccess(JSONObject response) throws JSONException {
					searchResults.clear();
					if (response.optString("status", "").equals("OK")) {
						JSONArray searchResultJson = response.optJSONObject("data").optJSONArray("dataTable");
						for (int i = 0; i < searchResultJson.length(); i++) {
							if (isGroupMember(searchResultJson.optJSONObject(i).optString("id", "0"))) {
								Connection connection = new Connection(searchResultJson.optJSONObject(i));
								searchResults.add(connection);
							}
						}
						
						GroupActivity.this.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								Utils.hideLoadingDialog();
								updateSearchResults();
								if (searchResults.isEmpty()) {
									noSearchResults.setVisibility(View.VISIBLE);
								} else {
									noSearchResults.setVisibility(View.GONE);
								}
							}
						});
					} else {
						GroupActivity.this.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								Utils.hideLoadingDialog();
								updateSearchResults();
								noSearchResults.setVisibility(View.VISIBLE);
							}
						});
					}
				}
				
				@Override
				public void onError(String response, Exception e) {
					searchResults.clear();
					GroupActivity.this.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							Utils.hideLoadingDialog();
							updateSearchResults();
							noSearchResults.setVisibility(View.VISIBLE);
						}
					});
				}
			});
		} catch (UnsupportedEncodingException e) {
			searchResults.clear();
			e.printStackTrace();
		}
	}
	
	private void updateSearchResults() {
		membersList.setAdapter(searchAdapter);
		searchAdapter.notifyDataSetChanged();
	}
	
	private boolean isGroupMember(String connectionId) {
		for (int i = 0; i < currentGroup.members.size(); i++) {
			if (currentGroup.members.get(i).userId.equals(connectionId)) {
				return true;
			}
		}
		
		return false;
	}
	
	private void moveMembersUpOrDown() {
		if (isActionMove) {
			if (upY <  downY) {
				// its action fling up
				groupDescriptionContainer.setVisibility(View.GONE);
			} else {
				// its action fling down
				groupDescriptionContainer.setVisibility(View.VISIBLE);
			}
		}
	}
}
