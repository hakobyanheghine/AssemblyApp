package com.assembly.app.data;

import org.json.JSONObject;

import com.assembly.app.R;
import com.assembly.app.utils.Constants;
import com.assembly.app.utils.Utils;

public class Position {
	
	public int id;
	public String title;
	public String description;
	public String startDate;
	public String endDate;
	public String companyId;
	public String companyName;
	public boolean isCurrent;
	
	public Position() {
		
	}
	
	public Position(JSONObject data) {
		this.id = data.optInt("id");
		this.title = data.optString("title", "");
		this.description = data.optString("summary", "");
		
		JSONObject startDateJson = data.optJSONObject("startdate");
		String startDateYear = startDateJson.optString("year", "");
		if (startDateYear.equals("null")) startDateYear = "";  
		this.startDate = Constants.MONTHS[startDateJson.optInt("month", 1) - 1] + " " + startDateYear; 
		
		if (data.optString("iscurrent", "").equalsIgnoreCase(Constants.TRUE)) {
			this.isCurrent = true;
			this.endDate = Utils.getStringByResourceId(R.string.present);
		} else {
			this.isCurrent = false;
			JSONObject endDateJson = data.optJSONObject("enddate");
			String endDateYear = endDateJson.optString("year", "");
			if (endDateYear.equals("null")) endDateYear = "";  
			this.endDate = Constants.MONTHS[endDateJson.optInt("month", 1) - 1] + " " + endDateYear;
		}
		
		this.companyId = data.optJSONObject("company").optString("id");
		this.companyName = data.optJSONObject("company").optString("name", "");
	}
}
