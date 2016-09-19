package com.assembly.app.data;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.assembly.app.utils.Constants;

import android.util.SparseArray;

public class User {

	public String userId;
	public String userLinkedinId;
	public String firstName;
	public String lastName;
	public String about;
	public String position;
	public String company;
	public String location;
	public String pictureUrl;
	public String experienceTags;
	public String educationTags;
	public String interestsTags;
	
	public boolean isFriend;
	public boolean isFriendRequestSent;
	
	public String distance;
	public int commonFriendsCount;
	public ArrayList<String> commonFriendsPictureUrl = new ArrayList<String>();
	
	public String userDistanceUnit = Constants.DISTANCE_UNIT_KM;
	public boolean showInSuggestions;
	public boolean showInConnections;
	
	public ArrayList<Group> groups = new ArrayList<Group>();
	public ArrayList<Connection> connections = new ArrayList<Connection>();
	public ArrayList<Connection> suggestions = new ArrayList<Connection>();
	public ArrayList<Connection> incomingRequests = new ArrayList<Connection>();
	public ArrayList<Connection> searchResultsConnections = new ArrayList<Connection>();
	public ArrayList<Connection> searchResultsSuggestions = new ArrayList<Connection>();
	public ArrayList<Position> positions = new ArrayList<Position>();
	public ArrayList<Education> educations = new ArrayList<Education>();
	
	public HashMap<String, SparseArray<Conversation>> allConversations = new HashMap<String, SparseArray<Conversation>>();
	public ArrayList<Message> allMessages = new ArrayList<Message>();

	public User() {

	}

	public User(JSONObject data) {
		this.userId = data.optString("id");
		this.userLinkedinId = data.optString("linkedinId");
		this.firstName = data.optString("firstName");
		this.lastName = data.optString("lastName");
		this.about = data.optString("headlineInApp");
		this.position = data.optString("headline");
		this.company = data.optString("companyName");
		this.location = data.optString("location");
		this.pictureUrl = data.optString("pictureUrl");
		this.distance = data.optString("distance");
		this.isFriend = data.optInt("myFriend") == 0 ? false : true;
		this.isFriendRequestSent = data.optInt("requested") == 0 ? false : true;
		this.experienceTags = data.optString("experienceTags", "").equals("null") ? "" : data.optString("experienceTags", "");
		this.educationTags = data.optString("educationTags", "").equals("null") ? "" : data.optString("educationTags", "");
		this.interestsTags = data.optString("interestsTags", "").equals("null") ? "" : data.optString("interestsTags", "");
		
		JSONArray commonFriends = data.optJSONArray("friends");
		for (int i = 0; i < commonFriends.length(); i++) {
			if (commonFriends.optJSONObject(i) != null) {
				this.commonFriendsPictureUrl.add(commonFriends.optJSONObject(i).optString("pictureUrl"));
			}
		}
		this.commonFriendsCount = this.commonFriendsPictureUrl.size();
		
		JSONArray positionsJson = data.optJSONArray("positions");
		for (int i = 0; i < positionsJson.length(); i++) {
			if (positionsJson.optJSONObject(i) != null) {
				Position position = new Position(positionsJson.optJSONObject(i));
				positions.add(position);
			}
		}
		
		JSONArray educationsJson = data.optJSONArray("educations");
		for (int i = 0; i < educationsJson.length(); i++) {
			if (educationsJson.optJSONObject(i) != null) {
				Education education = new Education(educationsJson.optJSONObject(i));
				educations.add(education);
			}
		}
	}

	public User(String userId, String firstName, String lastName, String position, String company) {
		this.userId = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.position = position;
		this.company = company;
	}
	
	public Group getGroupById(String groupId) {
		for (int i = 0; i < groups.size(); i++) {
			if (groups.get(i).groupId.equals(groupId)) {
				return groups.get(i); 
			}
		}
		
		return null;
	}
	
	public Connection getConnectionById(String connectionId) {
		for (int i = 0; i < connections.size(); i++) {
			if (connections.get(i).userId.equals(connectionId)) {
				return connections.get(i); 
			}
		}
		
		for (int i = 0; i < suggestions.size(); i++) {
			if (suggestions.get(i).userId.equals(connectionId)) {
				return suggestions.get(i); 
			}
		}
		
		for (int i = 0; i < searchResultsConnections.size(); i++) {
			if (searchResultsConnections.get(i).userId.equals(connectionId)) {
				return searchResultsConnections.get(i); 
			}
		}
		
		for (int i = 0; i < searchResultsSuggestions.size(); i++) {
			if (searchResultsSuggestions.get(i).userId.equals(connectionId)) {
				return searchResultsSuggestions.get(i); 
			}
		}
		
		return null;
	}
	
	public Conversation getConversationByOpponentId(String opponentId, int conversationId) {
		for (String key : allConversations.keySet()) {
			if (allConversations.get(key) != null && allConversations.get(key).get(conversationId) != null 
					&& !allConversations.get(key).get(conversationId).isGroupConversation 
					&& allConversations.get(key).get(conversationId).opponentId.equals(opponentId)) {
				return allConversations.get(key).get(conversationId);
			}
		}
		
		return null;
	}
	
}
