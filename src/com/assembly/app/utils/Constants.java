package com.assembly.app.utils;

public class Constants {
	
	public static final String SERVER_URL = "http://api.assembly-app.com/versions/android";
	
	/**
	 * LinkedIn authorization constants
	 */
	public static final String API_KEY = "751masqj5asbjz";
	public static final String SECRET_KEY = "XmCUpcUh1CUNHOm7";
	public static final String STATE = "E3ZYKC1T6H2yP4z";
	public static final String REDIRECT_URI = "http://www.assembly-app.com";

	public static final String AUTHORIZATION_URL = "https://www.linkedin.com/uas/oauth2/authorization";
	public static final String ACCESS_TOKEN_URL = "https://www.linkedin.com/uas/oauth2/accessToken";
	public static final String USER_DATA_URL = "https://api.linkedin.com/v1/people/~?oauth2_access_token=";
	public static final String SECRET_KEY_PARAM = "client_secret";
	public static final String RESPONSE_TYPE_PARAM = "response_type";
	public static final String GRANT_TYPE_PARAM = "grant_type";
	public static final String GRANT_TYPE = "authorization_code";
	public static final String RESPONSE_TYPE_VALUE = "code";
	public static final String CLIENT_ID_PARAM = "client_id";
	public static final String STATE_PARAM = "state";
	public static final String SCOPE_PARAM = "scope";
	public static final String REDIRECT_URI_PARAM = "redirect_uri";
	
	public static final String LINKEDIN_PERMISSION_PROFILE = "r_basicprofile";
//	public static final String LINKEDIN_PERMISSION_CONTACT_INFO = "r_contactinfo";
//	public static final String LINKEDIN_PERMISSION_GROUPS = "rw_groups";
//	public static final String LINKEDIN_PERMISSION_NETWORK = "r_network";
	public static final String LINKEDIN_PERMISSION_EMAIL = "r_emailaddress";
	
	/**
	 * End of constants for LinkedIn
	 */
	
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	
	public static final String[] MONTHS = {
				"January",
				"February",
				"March",
				"April",
				"May",
				"June",
				"July",
				"August",
				"September",
				"October",
				"November",
				"December"
			};
	
	public static final String HASHTAGS_EXAMPLE = "#engineer #development #ui_design #management #training";
	
	/**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS =  60 * 1000 / 2; // 1m

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    
    /**
     * Smallest distance between location changes
     */
    public static final long SMALLEST_DISPLACEMENT_IN_METERS = 2 * 1000; // 2km
    
    public static final long ONE_MINUTE_IN_MILLISECONDS = 60 * 1000; // 1m
    
    public static final long ONE_HOUR_IN_MILLISECONDS = 60 * 60 * 1000; // 1h
    
    public static final long ONE_YEAR_IN_MILLISECONDS = 365 * 24 * 60 * 60 * 1000; // 365 days
    
    
    public static final String NO_INTERNET = "no_internet";
    
    public static final String ACTION_CONNECT = "connect";
    public static final String ACTION_DENY = "deny";
    
    public static final String DISTANCE_UNIT_KM = "km";
    public static final String DISTANCE_UNIT_ML = "mi";
    
    public static final int MESSAGE_TO_SHOW_COUNT = 20;
    
    public static final String DEFAULT_DISTANCE = "100000";
    
    /**
     * Google Play Services stuff
     */
    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	
	public static final String GCM_SENDER_ID = "658237988707"; 
}
