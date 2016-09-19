package com.assembly.app.data;

import java.util.ArrayList;
import org.json.JSONObject;

public class Group {
	
	public String groupId;
	public String name;
	public String description;
	public String photoUrl;
	public int friendsCount;
	
	public ArrayList<Connection> members = new ArrayList<Connection>();
	public ArrayList<Conversation> conversations = new ArrayList<Conversation>();
	
	public boolean isGroupDataLoaded;
	
	public Group() {
		
	}
	
	public Group(JSONObject data) {
		this.groupId = data.optString("id");
		this.name = data.optString("name");
		this.description = data.optString("desc");
		this.photoUrl = data.optString("photo");
		this.friendsCount = data.optInt("friends");
	}
	
	public Group(String title, String description) {
		this.name = title;
		this.description = description;
	}
}
