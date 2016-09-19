package com.assembly.app.data;

import org.json.JSONObject;

import com.assembly.app.utils.Constants;

public class Education {
	
	public int id;
	public String schoolName;
	public String fieldOfStudy;
	public String startDate;
	public String endDate;
	public String degree;
	public String activities;
	public String notes;
	
	public Education() {
		
	}
	
	public Education(JSONObject data) {
		this.id = data.optInt("id");
		this.schoolName = data.optString("schoolname", "");
		this.fieldOfStudy = data.optString("fieldofstudy", "");
		
		JSONObject startDateJson = data.optJSONObject("startdate");
		String startDateYear = startDateJson.optString("year", "");
		if (startDateYear.equals("null")) startDateYear = "";  
		this.startDate = Constants.MONTHS[startDateJson.optInt("month", 1) - 1] + " " + startDateYear;
		
		JSONObject endDateJson = data.optJSONObject("enddate");
		String endDateYear = endDateJson.optString("year", "");
		if (endDateYear.equals("null")) endDateYear = "";  
		this.endDate = Constants.MONTHS[endDateJson.optInt("month", 1) - 1] + " " + endDateYear;
		
		this.degree = data.optString("degree", "");
		this.activities = data.optString("activities", "");
		this.notes = data.optString("notes", "");
	}
}
