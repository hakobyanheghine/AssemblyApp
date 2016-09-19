package com.assembly.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
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
import com.assembly.app.utils.view.ConversationsAdapter;

public class ConversationsActivity extends Activity {

	private ConversationsAdapter conversationsAdapter;
	private ArrayList<Conversation> conversations = new ArrayList<Conversation>();
	private TextView newMessagesCountTxt;
	
	private ConversationsAdapter searchAdapter;
	private ArrayList<Conversation> searchResults = new ArrayList<Conversation>();
	private boolean isSearch;
	private TextView noSearchResults;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversations);

		noSearchResults = (TextView) findViewById(R.id.conversations_no_results_txt);
		
		getConversations();
		Collections.sort(conversations);

		conversationsAdapter = new ConversationsAdapter(this, R.layout.item_conversations_listview, conversations);
		final ListView messagesList = (ListView) findViewById(R.id.messages_listview);
		messagesList.setAdapter(conversationsAdapter);
		messagesList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Conversation currentConversation = null;
				if (isSearch) {
					currentConversation = AssemblyAppManager.getInstance().userData.allConversations.get(searchResults.get(position).opponentId).get(searchResults.get(position).conversationId);
				} else {
					currentConversation = AssemblyAppManager.getInstance().userData.allConversations.get(conversations.get(position).opponentId).get(conversations.get(position).conversationId);
				}
				
				makeConversationRead(currentConversation);
				conversationsAdapter.notifyDataSetChanged();
				AssemblyAppManager.getInstance().currentSelectedConversation = currentConversation;
				if (AssemblyAppManager.getInstance().currentSelectedConversation.isGroupConversation) {
					AssemblyAppManager.getInstance().currentSelectedGroup = AssemblyAppManager.getInstance().userData.getGroupById(currentConversation.groupId);
					if (AssemblyAppManager.getInstance().currentSelectedGroup.members.isEmpty()) {
						loadGroupMembers();
					} else {
						startMessagesActivity();
					}
				} else {
					Connection currentConnection = AssemblyAppManager.getInstance().userData.getConnectionById(currentConversation.opponentId);
					if (currentConnection == null) {
						loadConnectionProfile(currentConversation.opponentId);
					} else {
						AssemblyAppManager.getInstance().currentSelectedConnection = currentConnection; 
						startMessagesActivity();
					}
				}
			}
		});
		
		final EditText conversationsSearchEdit = ((EditText) findViewById(R.id.conversations_search_edit));
		searchAdapter = new ConversationsAdapter(this, R.layout.item_conversations_listview, searchResults);
		conversationsSearchEdit.addTextChangedListener(new TextWatcher() {
			
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
					messagesList.setAdapter(conversationsAdapter);
					conversationsAdapter.notifyDataSetChanged();
					noSearchResults.setVisibility(View.GONE);
				}
			}
		});
		conversationsSearchEdit.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE ||
						(event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
					String searchText = conversationsSearchEdit.getEditableText().toString();
					if (!searchText.isEmpty()) {
						isSearch = true;
						searchInMessages(searchText);
						messagesList.setAdapter(searchAdapter);
						searchAdapter.notifyDataSetChanged();
						
						if (searchResults.isEmpty()) {
							noSearchResults.setVisibility(View.VISIBLE);
						} else {
							noSearchResults.setVisibility(View.GONE);
						}
						
						InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						inputMethodManager.hideSoftInputFromWindow(conversationsSearchEdit.getWindowToken(), 0);
						
						return true;
					}
				}
				return false;
			}
		});

		getActionBar().setTitle(Utils.getStringByResourceId(R.string.messages));
	}

	@Override
	protected void onStart() {
		super.onStart();
		AssemblyAppManager.getInstance().currentConversationsActivity = this;
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		updateConversations();
	}

	@Override
	protected void onStop() {
		super.onStop();
		AssemblyAppManager.getInstance().currentConversationsActivity = null;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		View messagesView = menu.findItem(R.id.action_messages).getActionView();
		newMessagesCountTxt = ((TextView) messagesView.findViewById(R.id.messages_new_count));
		updateNewMessagesCount();
		
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_search) {
			return true;
		} else if (id == R.id.action_messages) {
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

	private void loadGroupMembers() {
		Utils.showLoadingDialog(this);
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
					startMessagesActivity();
				}
			}

			@Override
			public void onError(String response, Exception e) {
				Utils.sendErrorToBackend(e.toString());
			}
		});
	}

	private void startMessagesActivity() {
		ConversationsActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Utils.hideLoadingDialog();
				Intent messagesActivity = new Intent(ConversationsActivity.this, MessagesActivity.class);
				startActivity(messagesActivity);
			}
		});
	}

	private void getConversations() {
		HashMap<String, SparseArray<Conversation>> conversationMap = AssemblyAppManager.getInstance().userData.allConversations;
		for (String key : conversationMap.keySet()) {
			for (int i = 0; i < conversationMap.get(key).size(); i++) {
				int k = conversationMap.get(key).keyAt(i);
				Conversation conversation = conversationMap.get(key).get(k);
				if (conversation.isGroupConversation) {
					Group group = AssemblyAppManager.getInstance().userData.getGroupById(conversation.groupId);
					conversation.groupName = group.name;
					conversation.groupPictureUrl = group.photoUrl;
				}
				conversations.add(conversation);
			}
		}
	}

	public void updateConversations() {
		conversationsAdapter.clear();
		getConversations();
		Collections.sort(conversations);
		conversationsAdapter.notifyDataSetChanged();
	}

	private void makeConversationRead(final Conversation currentConversation) {
		ConversationsActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				for (int i = currentConversation.messages.size() - 1; i >= 0; i--) {
					if (currentConversation.messages.get(i).isMine) {
						continue;
					}
					if (currentConversation.messages.get(i).isNew) {
						currentConversation.messages.get(i).isNew = false;
					} else {
						break;
					}
				}
				AssemblyAppManager.getInstance().newMessagesCount -= currentConversation.newMessageCount;
				updateNewMessagesCount();
				currentConversation.newMessageCount = 0;
				FilesManager.getInstance().saveMessagesToFileAsync();
			}
		});
	}
	
	private void searchInMessages(String searchText) {
		searchResults.clear();
		searchText = searchText.toLowerCase();
		String[] searchtTexts = searchText.split(" ");
		String currentSearchText = ""; 
		
		// search, where conversation with at least one keyword is acceptable
//		for (int k = 0; k < searchtTexts.length; k++) {
//			currentSearchText = searchtTexts[k];
//			for (int i = 0; i < conversations.size(); i++) {
//				if ((conversations.get(i).isGroupConversation && conversations.get(i).groupName.toLowerCase().contains(currentSearchText)) ||
//						(!conversations.get(i).isGroupConversation && conversations.get(i).opponentName.toLowerCase().contains(currentSearchText))) {
//					searchResults.add(conversations.get(i));
//				} else {
//					for (int j = 0; j < conversations.get(i).messages.size(); j++) {
//						String message = conversations.get(i).messages.get(j).message.toLowerCase();
//						if (message.indexOf(currentSearchText) >= 0) {
//							searchResults.add(conversations.get(i));
//							break;
//							
//						}
//					}
//				}
//			}
//		}
		
		// search, where conversation with all keywords is acceptable
		int isFound;
		for (int i = 0; i < conversations.size(); i++) {
			isFound = 0;
			for (int j = 0; j < searchtTexts.length; j++) {
				currentSearchText = searchtTexts[j];
				
				if ((conversations.get(i).isGroupConversation && (conversations.get(i).groupName.toLowerCase().contains(currentSearchText) || conversations.get(i).topic.toLowerCase().contains(currentSearchText))) ||
						(!conversations.get(i).isGroupConversation && conversations.get(i).opponentName.toLowerCase().contains(currentSearchText))) {
					isFound++;
				} else {
					for (int k = 0; k < conversations.get(i).messages.size(); k++) {
						String message = conversations.get(i).messages.get(k).message.toLowerCase();
						if (message.indexOf(currentSearchText) >= 0) {
							isFound++;
							break;
						}
					}
				}
			}
			
			if (isFound == searchtTexts.length) {
				searchResults.add(conversations.get(i));
			}
		}
		
	}
	
	public void loadConnectionProfile(String profileId) {
		Utils.showLoadingDialog(ConversationsActivity.this);
		API.getProfile(profileId, AssemblyAppManager.getInstance().userData.userId, new RequestObserver() {
			
			@Override
			public void onSuccess(JSONObject response) throws JSONException {
				if (response.optString("status", "").equals("OK")) {
					AssemblyAppManager.getInstance().currentSelectedConnection = new Connection(response);
					startMessagesActivity();
				}
			}
			
			@Override
			public void onError(String response, Exception e) {
				Utils.sendErrorToBackend(e.toString());
			}
		});
	}
}
