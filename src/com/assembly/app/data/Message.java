package com.assembly.app.data;

import org.json.JSONObject;

public class Message {

	public String messageId;

	public long date;

	public String opponentId;
	public String opponentName;
	public String opponentUrl;

	public String message;

	public boolean isMine;
	public boolean isConnected;

	public int conversationId;
	public String groupId;
	public String senderId;
	public String topic;

	public boolean isNew;

	public Message() {

	}

	public Message(JSONObject data) {
		this.messageId = data.optString("messageID");
		this.date = data.optLong("date");
		this.opponentId = data.optString("opponentID");
		this.opponentName = data.optString("opponentName");
		this.opponentUrl = data.optString("opponentPicture");
		this.message = data.optString("message");

		this.isMine = data.optInt("my") == 1 ? true : false;
		this.isConnected = data.optInt("connected") == 1 ? true : false;
		this.isNew = data.optInt("new") == 1 ? true : false;

		this.conversationId = data.optInt("conversationID");
		this.groupId = data.optString("groupID");
		this.senderId = data.optString("senderID");
		this.topic = data.optString("topic");
	}

}
