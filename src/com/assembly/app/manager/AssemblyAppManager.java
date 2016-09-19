package com.assembly.app.manager;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.SparseArray;

import com.assembly.app.ConversationsActivity;
import com.assembly.app.HomeActivity;
import com.assembly.app.MainActivity;
import com.assembly.app.MessagesActivity;
import com.assembly.app.NewProfileActivity;
import com.assembly.app.R;
import com.assembly.app.SearchActivity;
import com.assembly.app.backend.API;
import com.assembly.app.backend.API.RequestObserver;
import com.assembly.app.data.Connection;
import com.assembly.app.data.Conversation;
import com.assembly.app.data.Group;
import com.assembly.app.data.Message;
import com.assembly.app.data.User;
import com.assembly.app.utils.Constants;
import com.assembly.app.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageSize;

public class AssemblyAppManager {

	private static AssemblyAppManager instance;

	private AssemblyAppManager() {

	}

	public static AssemblyAppManager getInstance() {
		if (instance == null) {
			instance = new AssemblyAppManager();
		}

		return instance;
	}

	public DisplayImageOptions optionsWithLoading = new DisplayImageOptions.Builder()
				.showImageOnFail(R.drawable.no_img)
				.showImageForEmptyUri(R.drawable.no_img)
				.showImageOnLoading(R.drawable.loading_1)
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.build();
	
	public DisplayImageOptions optionsWithoutLoading = new DisplayImageOptions.Builder()
				.showImageOnFail(R.drawable.no_img)
				.showImageForEmptyUri(R.drawable.no_img)
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.build();
	
	public ImageSize targetSize70 = new ImageSize(70, 70);
	public ImageSize targetSize32 = new ImageSize(32, 32);
	public ImageSize targetSize64 = new ImageSize(64, 64);
	public ImageSize targetSize128 = new ImageSize(128, 128);
	public ImageSize targetSize256 = new ImageSize(256, 256);

	public MainActivity mainActivity;
	public ProgressDialog loadingDialog;
	
	public ConversationsActivity currentConversationsActivity;
	public MessagesActivity currentMessagesActivity;
	public HomeActivity currentConnectionsActivity;

	public User userData;
	public User currentSelectedUser;
	public Group currentSelectedGroup;
	public Connection currentSelectedConnection;
	public Conversation currentSelectedConversation;
	public Conversation feedbackConversation = new Conversation("0", "Admin", "", "0", false, true, true);

	public int newMessagesCount;
	public int lastSelectedTab;
	
	public Timer messagesTimer;
	
	public boolean updateConnections;
	public boolean updateCurrentConversation;
	public boolean isNoInternetToastShown;
	public boolean showTopicDialog;
	public boolean showDrawerAfterSettings;
	public boolean hasEnabledGPS;
	public boolean updateSuggestionsAfterRequest;
	
	private boolean isMessageUpdateFinished = true;
	
	public void resetUserData() {
		messagesTimer.cancel();
		messagesTimer.purge();
		userData = null;
		currentSelectedUser = null;
		currentSelectedGroup = null;
		currentSelectedConnection = null;
		currentSelectedConversation = null;
		lastSelectedTab = 0;
	}

	public void initMessagesTimer() {
		final Handler handler = new Handler();
		messagesTimer = new Timer();

		TimerTask doAsynchronousTask = new TimerTask() {

			@Override
			public void run() {
				
				if (isMessageUpdateFinished) {
					isMessageUpdateFinished = false;
					handler.post(new Runnable() {
						public void run() {
	
							final String lastLocalMessageId = PreferenceManager.getInstance().getLastMessageId();
							API.getMessages(userData.userId, lastLocalMessageId, new RequestObserver() {
	
								@Override
								public void onSuccess(JSONObject response) throws JSONException {
									if (response.optString("status", "").equals("OK")) {
										String lastMessageId = "";
										JSONArray messages = response.optJSONArray("messages");
										if (messages.length() > 0) {
											// add new messages 
											Conversation conversation;
											for (int i = 0; i < messages.length(); i++) {
												Message message = new Message(messages.optJSONObject(i));
												if (message.opponentId.startsWith("g") && AssemblyAppManager.getInstance().userData.getGroupById(message.groupId) == null) {
													continue;
												}
												if (!lastLocalMessageId.equals("0") && !message.isMine) {
													message.isNew = true;
												}
												
												if (AssemblyAppManager.getInstance().userData.allConversations.get(message.opponentId) == null) { // case 1: no conversation at all with opponent
													conversation = getConversationByMessage(message);
	
													SparseArray<Conversation> converversations = new SparseArray<Conversation>();
													converversations.put(conversation.conversationId, conversation);
													AssemblyAppManager.getInstance().userData.allConversations.put(conversation.opponentId, converversations);
												} else if (AssemblyAppManager.getInstance().userData.allConversations.get(message.opponentId).get(message.conversationId) == null) { // case 2: no conversation with given conversation id
													conversation = getConversationByMessage(message);
													
													AssemblyAppManager.getInstance().userData.allConversations.get(conversation.opponentId).put(conversation.conversationId, conversation);
												} else { // case 3: conversation with given conversation_id is available and has other messages
													conversation = AssemblyAppManager.getInstance().userData.allConversations.get(message.opponentId).get(message.conversationId);
													conversation.messages.add(message);
													if (!message.isMine) {
														conversation.isNewMessageMine = false;
														if (message.isNew) {
															conversation.newMessageCount++;
															newMessagesCount++;
														}
													} else {
														conversation.isNewMessageMine = true;
													}
												}
												AssemblyAppManager.getInstance().userData.allMessages.add(message);
	//											LocalDbManager.getInstance().insertMessage(message);
												
												if (updateCurrentConversation && currentMessagesActivity != null) {
													updateCurrentConversation = false;
													final Conversation conversationFinal = conversation;
													currentMessagesActivity.runOnUiThread(new Runnable() {
														
														@Override
														public void run() {
															currentMessagesActivity.updateCurrentConversation(conversationFinal);
														}
													});
												}
	
												if (i == messages.length() - 1) {
													lastMessageId = message.messageId;
												}
											}
											
											// set lastMessageId in Preferences
											if (!lastMessageId.isEmpty()) {
												PreferenceManager.getInstance().setLastMessageId(lastMessageId);
												FilesManager.getInstance().saveMessagesToFile(userData.allMessages);
											}
											
											// update UI
											if (currentConversationsActivity != null) {
												currentConversationsActivity.runOnUiThread(new Runnable() {
													
													@Override
													public void run() {
														if (currentConversationsActivity != null) {
															currentConversationsActivity.updateConversations();
															currentConversationsActivity.updateNewMessagesCount();
														}
													}
												});
											} 
											if (currentMessagesActivity != null) {
												currentMessagesActivity.runOnUiThread(new Runnable() {
													
													@Override
													public void run() {
														if (currentMessagesActivity != null) {
															currentMessagesActivity.updateMessages();
														}
													}
												});
											}
											if (currentConnectionsActivity != null) {
												currentConnectionsActivity.runOnUiThread(new Runnable() {
													
													@Override
													public void run() {
														if (currentConnectionsActivity != null) {
															currentConnectionsActivity.updateNewMessagesCount();
														}
													}
												});
											}
										}
										isMessageUpdateFinished = true;
									}
								}
	
								@Override
								public void onError(String response, Exception e) {
									if (response != null && response.equals(Constants.NO_INTERNET) && !isNoInternetToastShown) {
										isNoInternetToastShown = true;
										Utils.hideLoadingDialog();
										Utils.showToast(AssemblyAppManager.getInstance().mainActivity, Utils.getStringByResourceId(R.string.no_internet));
									} else {
										Utils.sendErrorToBackend(e.toString());
									}
								}
							});
						}
					});
				}
			}

		};

		messagesTimer.schedule(doAsynchronousTask, 0, 3000);// execute in every 3 s
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
		
		if (!message.isMine) {
			conversation.isNewMessageMine = false;
			if (message.isNew) {
				conversation.newMessageCount++;
				newMessagesCount++;
			}
		} else {
			conversation.isNewMessageMine = true;
		}
		
		return conversation;
	}
	
	public void loadConnectionProfile(final Activity currentActivity, String profileId) {
		Utils.showLoadingDialog(currentActivity);
		API.getProfile(profileId, AssemblyAppManager.getInstance().userData.userId, new RequestObserver() {
			
			@Override
			public void onSuccess(JSONObject response) throws JSONException {
				if (response.optString("status", "").equals("OK")) {
					AssemblyAppManager.getInstance().currentSelectedUser = new User(response);
					startProfileActivity(currentActivity);
				}
			}
			
			@Override
			public void onError(String response, Exception e) {
				Utils.sendErrorToBackend(e.toString());
			}
		});
	}
	
	public void startProfileActivity(final Activity currentActivity) {
		currentActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Utils.hideLoadingDialog();
				Intent profileActivity = new Intent(currentActivity, NewProfileActivity.class);
				currentActivity.startActivity(profileActivity);
			}
		});
	}
	
	public void startSearchActivity(final Activity currentActivity, final String searchWord) {
		currentActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Utils.hideLoadingDialog();
				Intent searchActivity = new Intent(currentActivity, SearchActivity.class);
				searchActivity.putExtra(SearchManager.QUERY, searchWord);
				searchActivity.setAction(Intent.ACTION_SEARCH);
				currentActivity.startActivity(searchActivity);
			}
		});
	}
}
