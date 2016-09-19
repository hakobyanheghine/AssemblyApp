package com.assembly.app.data;

import org.json.JSONObject;

import com.assembly.app.manager.AssemblyAppManager;

public class Connection {
	public String userId;
	public String userLinkedinId;
	public String firstName;
	public String lastName;
	public String position;
	public String company;
	public String pictureUrl;
	public String tags;
	
	public boolean isFriend;
	public boolean isFriendRequestSent;
	
	public String distance;
	public int commonFriendsCount;
	
	public Connection() {
		this.userId = "0";
		this.userLinkedinId = "";
		this.firstName = AssemblyAppManager.getInstance().userData.firstName;
		this.lastName = AssemblyAppManager.getInstance().userData.lastName;
		this.position = "";
		this.company = "";
		this.pictureUrl = "";
		this.tags = "";
		this.distance = "";
		this.commonFriendsCount = 0;
		this.isFriend = false;
		this.isFriendRequestSent = false;
	}
	
	public Connection(JSONObject data) {
		this.userId = data.optString("id", "0");
		this.userLinkedinId = data.optString("linkedinId");
		this.firstName = data.optString("firstName");
		this.lastName = data.optString("lastName");
		this.position = data.optString("positionTitle");
		this.company = data.optString("companyName");
		this.pictureUrl = data.optString("pictureUrl");
		this.tags = data.optString("tags", "").equals("null") ? "" : data.optString("tags", "");
		this.distance = data.optString("distance").split(" ")[0];
		this.commonFriendsCount = data.optInt("friends");
		this.isFriend = data.optInt("myFriend") == 0 ? false : true;
		this.isFriendRequestSent = data.optInt("requested") == 0 ? false : true;
	}
}
