package com.assembly.app;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.assembly.app.backend.API;
import com.assembly.app.backend.API.RequestObserver;
import com.assembly.app.data.Connection;
import com.assembly.app.data.Conversation;
import com.assembly.app.data.Group;
import com.assembly.app.data.Message;
import com.assembly.app.manager.AssemblyAppManager;
import com.assembly.app.manager.FilesManager;
import com.assembly.app.utils.Constants;
import com.assembly.app.utils.Utils;
import com.assembly.app.utils.view.MessagesAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MessagesActivity extends Activity {

	private Conversation currentConversation;
	private Group currentGroup;
	private Connection currentConnection;
	private MessagesAdapter messagesAdapter;
	private ListView messagesList;
	private TextView loadMoreMessages;
	
	private boolean isSendingMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_messages);
		
		AssemblyAppManager.getInstance().currentMessagesActivity = this;
		
		currentConversation = AssemblyAppManager.getInstance().currentSelectedConversation;
		if (currentConversation.isGroupConversation) {
			currentGroup = AssemblyAppManager.getInstance().currentSelectedGroup;
		} else {
			currentConnection = AssemblyAppManager.getInstance().currentSelectedConnection;
		}

		getMessagesToShow(currentConversation.messagesToShowCount);
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		loadMoreMessages = (TextView) inflater.inflate(R.layout.item_load_more_messages, null, false);
		loadMoreMessages.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				currentConversation.messagesToShowCount += Constants.MESSAGE_TO_SHOW_COUNT;
				getMessagesToShow(currentConversation.messagesToShowCount);
				messagesAdapter.notifyDataSetChanged();
				
				messagesList.setSelection(Constants.MESSAGE_TO_SHOW_COUNT);
				
				if (currentConversation.messagesToShow.size() == currentConversation.messages.size()) {
					messagesList.removeHeaderView(loadMoreMessages);
				}
			}
		});
		
		messagesAdapter = new MessagesAdapter(this, R.layout.item_messages_friend_1, currentConversation.messagesToShow);
		messagesList = (ListView) findViewById(R.id.messages_listview);
		if (currentConversation.messagesToShow.size() < currentConversation.messages.size()) {
			messagesList.addHeaderView(loadMoreMessages);
		}
		messagesList.setAdapter(messagesAdapter);
		messagesList.post(new Runnable() {
			
			@Override
			public void run() {
				messagesList.setSelection(currentConversation.messagesToShow.size() - 1);
			}
		});
		
		
		
		if (currentConversation.messagesToShow.isEmpty()) {
			String[] dateAndTime = Utils.getDateAndTimeInDotFormatNew(System.currentTimeMillis());
			((TextView) findViewById(R.id.messages_date_txt)).setText(dateAndTime[0]);
		} else {
			long dateValue = currentConversation.messagesToShow.get(currentConversation.messagesToShow.size() - 1).date * 1000;
			String[] dateAndTime = Utils.getDateAndTimeInDotFormatNew(dateValue);
			((TextView) findViewById(R.id.messages_date_txt)).setText(dateAndTime[0]);
		}
		
		((Button) findViewById(R.id.messages_send_message_btn)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EditText messageEditText = ((EditText) findViewById(R.id.messages_send_message_edit));
				String message = messageEditText.getText().toString();
				if (!message.isEmpty() && !isSendingMessage) {
					isSendingMessage = true;
					messageEditText.setText("");
					InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethodManager.hideSoftInputFromWindow(messageEditText.getWindowToken(), 0);
					
					String fromId = AssemblyAppManager.getInstance().userData.userId;
					String toId = currentConversation.isGroupConversation ? "c" + currentConversation.conversationId : currentConversation.opponentId;
					String groupId = currentConversation.isGroupConversation ? currentConversation.groupId : "";
					String topic = currentConversation.isGroupConversation ? currentConversation.topic : "";
					if (currentConversation.isFeedback) {
						Message newMessage = new Message();
						newMessage.messageId = "";
						newMessage.message = message;
						newMessage.isMine = true;
						
						newMessage.groupId = "0";
						newMessage.topic = "";
						
						newMessage.opponentId = "0";
						newMessage.opponentName = "Admin";
						newMessage.opponentUrl = "";
						
						newMessage.date = System.currentTimeMillis() / 1000;
						currentConversation.messages.add(newMessage);
						updateMessages();
					}
					if (!currentConversation.isSynced) {
						AssemblyAppManager.getInstance().updateCurrentConversation = true;
					}
					try {
						API.sendMessage(fromId, toId, message, groupId, topic, new RequestObserver() {
							
							@Override
							public void onSuccess(JSONObject response) throws JSONException {
								isSendingMessage = false;
							}
							
							@Override
							public void onError(String response, Exception e) {
								
							}
						});
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						isSendingMessage = false;
					}
				}
			}
		});
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		
		LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View actionBarView = inflator.inflate(R.layout.action_bar_custom_view, null);

		String pictureUrl = "";
		if (currentConversation.isGroupConversation) {
			pictureUrl = currentGroup.photoUrl;
			actionBar.setTitle(currentConversation.topic);
		} else {
			pictureUrl = currentConnection.pictureUrl;
			actionBar.setTitle(currentConversation.opponentName);
		}
		
		if (!currentConversation.isGroupConversation) {
			((ImageView) actionBarView.findViewById(R.id.action_bar_custom_img)).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					AssemblyAppManager.getInstance().loadConnectionProfile(MessagesActivity.this, currentConnection.userId);
				}
			});
		}

		ImageLoader.getInstance().loadImage(pictureUrl, AssemblyAppManager.getInstance().targetSize128, null);
		ImageLoader.getInstance().displayImage(pictureUrl, (ImageView) actionBarView.findViewById(R.id.action_bar_custom_img), AssemblyAppManager.getInstance().optionsWithLoading);

		ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		actionBarView.setLayoutParams(layoutParams);
		actionBar.setCustomView(actionBarView);
		
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if (AssemblyAppManager.getInstance().showTopicDialog) {
			AssemblyAppManager.getInstance().showTopicDialog = false;
			showTopicDialog();
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		AssemblyAppManager.getInstance().currentMessagesActivity = null;
	}
	
	public void updateCurrentConversation(Conversation newConversation) {
		currentConversation = newConversation;
		messagesAdapter = new MessagesAdapter(this, R.layout.item_messages_friend_1, currentConversation.messagesToShow);
		messagesList.setAdapter(messagesAdapter);
		
		if (currentConversation.isGroupConversation && AssemblyAppManager.getInstance().currentSelectedGroup != null) {
			if (currentConversation.groupId.equals(AssemblyAppManager.getInstance().currentSelectedGroup.groupId)) {
				AssemblyAppManager.getInstance().currentSelectedGroup.conversations.add(0, currentConversation);
			}
		}
	}
	
	public void updateMessages() {
		getMessagesToShow(20);
		messagesAdapter.notifyDataSetChanged(); 
		messagesList.setSelection(currentConversation.messagesToShow.size() - 1);
		makeConversationRead();
	}
	
	private void makeConversationRead() {
		MessagesActivity.this.runOnUiThread(new Runnable() {
			
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
	
	public void showTopicDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MessagesActivity.this);
		LayoutInflater inflater = MessagesActivity.this.getLayoutInflater();
		final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.dialog_conversation_topic_feedback, null);
		builder.setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, int id) {
				String topic = ((EditText) view.findViewById(R.id.message_edit)).getText().toString();
				if (!topic.isEmpty()) {
					currentConversation.topic = topic;
					getActionBar().setTitle(topic);
				}
			}
		}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});

		builder.create().show();
	}

	public void getMessagesToShow(int count) {
		currentConversation.messagesToShow.clear();
		int start = 0;
		if (count > currentConversation.messages.size() - 1) {
			start = 0;
		} else {
			start = currentConversation.messages.size() - 1 - count;
		}
		for (int i = start; i < currentConversation.messages.size(); i++) {
			currentConversation.messagesToShow.add(currentConversation.messages.get(i));
		}
	}
}
