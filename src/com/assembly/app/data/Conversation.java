package com.assembly.app.data;

import java.util.ArrayList;

import com.assembly.app.utils.Constants;

public class Conversation implements Comparable<Conversation> {

	public int conversationId;
	public String groupId = "0";
	public String groupName;
	public String groupPictureUrl;
	public String opponentId;
	public String opponentName;
	public String opponentPictureUrl;

	public String topic;

	public int newMessageCount;
	public boolean isNewMessageMine;
	
	public boolean isGroupConversation;
	public boolean isFeedback;
	
	public boolean isSynced = true;
	
	public int messagesToShowCount = Constants.MESSAGE_TO_SHOW_COUNT;

	public ArrayList<Message> messages = new ArrayList<Message>();
	public ArrayList<Message> messagesToShow = new ArrayList<Message>();

	public Conversation() {

	}

	public Conversation(String opponentId, String opponentName, String opponentPictureUrl, String groupId, boolean isGroupConversation, boolean isFeedback, boolean isSynced) {
		this.opponentId = opponentId;
		this.opponentName = opponentName;
		this.opponentPictureUrl = opponentPictureUrl;
		this.topic = "";
		this.groupId = groupId;
		this.isGroupConversation = isGroupConversation;
		this.isFeedback = isFeedback;
		this.isSynced = isSynced;
	}

	@Override
	public int compareTo(Conversation another) {
		if (this.messages.get(this.messages.size() - 1).date > another.messages.get(another.messages.size() - 1).date) {
			return -1;
		} else if (this.messages.get(this.messages.size() - 1).date < another.messages.get(another.messages.size() - 1).date) {
			return 1;
		} else {
			return 0;
		}
	}
}
