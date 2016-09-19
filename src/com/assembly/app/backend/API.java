package com.assembly.app.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.LinkedList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.assembly.app.utils.Constants;
import com.assembly.app.utils.L;
import com.assembly.app.utils.Utils;

public class API {
	
	public static final String LOGIN = "/login.aspx";
	public static final String LOGOUT = "/logout.aspx";
	public static final String SEARCH = "/search.aspx";
	public static final String SAVE_LOCATION = "/saveLocation.aspx";
	public static final String JOIN_GROUP = "/joinGroup.aspx";
	public static final String GET_MESSAGES = "/getMessages.aspx";
	public static final String GET_PROFILE = "/getProfile.aspx";
	public static final String GET_SETTINGS = "/getSettings.aspx";
	public static final String GET_SUGGESTIONS = "/getSuggestions.aspx";
	public static final String GET_CONNECTIONS = "/getConnections.aspx";
	public static final String GET_GROUPS = "/getGroups.aspx";
	public static final String GET_GROUP_MEMBERS = "/getGroupMembers.aspx";
	public static final String SET_CONNECT = "/setConnect.aspx";
	public static final String SET_PARAM = "/setParam.aspx";
	public static final String SET_TAGS = "/setTags.aspx";
	public static final String SEND_MESSAGE = "/sendMessage.aspx";
	public static final String SEND_ERROR = "/sendError.aspx";
	
    public static String userId = "";
	
	public static final String TAG = "API";

    private static RequestThread requestThread = new RequestThread();
    private static LinkedList<RequestData> requestStack = new LinkedList<RequestData>();

    static {
        requestThread.start();
    }
    
    public static void loginUser(String linkedinId, String accessToken, RequestObserver observer) {
    	sendAsyncRequestGet(LOGIN, "linkedinID=" + linkedinId + "&token=" + accessToken, observer);
    }
    
    public static void logoutUser(String linkedinId, String userId, RequestObserver observer) {
    	sendAsyncRequestGet(LOGOUT, "uid=" + userId + "&token=" + linkedinId, observer);
    }
    
    public static void search(String userId, String searchText, RequestObserver observer) throws UnsupportedEncodingException {
    	sendAsyncRequestGet(SEARCH, "uid=" + userId + "&search=" + URLEncoder.encode(searchText, "UTF-8"), observer);
    }

    public static void saveLocation(String latitude, String longitude, String userId, RequestObserver observer) {
    	sendAsyncRequestGet(SAVE_LOCATION, "latitude=" + latitude + "&longitude=" + longitude + "&userID=" + userId, observer);
    }
    
    public static void joinGroup(String userId, String groupCode, RequestObserver observer) throws UnsupportedEncodingException {
    	sendAsyncRequestGet(JOIN_GROUP, "code=" + URLEncoder.encode(groupCode, "UTF-8") + "&uid=" + userId, observer);
    }
    
    public static void getProfile(String profileId, String userId, RequestObserver observer) {
    	sendAsyncRequestGet(GET_PROFILE, "profileID=" + profileId + "&myID=" + userId, observer);
    }
    
    public static void getSettings(String userId, RequestObserver observer) {
    	sendAsyncRequestGet(GET_SETTINGS, "uid=" + userId, observer);
    }
    
    public static void getSuggestions(String userId, RequestObserver observer) {
    	sendAsyncRequestGet(GET_SUGGESTIONS, "uid=" + userId, observer);
    }
    
    public static void getConnections(String userId, RequestObserver observer) {
    	sendAsyncRequestGet(GET_CONNECTIONS, "uid=" + userId, observer);
    }
    
    public static void getGroups(String userId, RequestObserver observer) {
    	sendAsyncRequestGet(GET_GROUPS, "uid=" + userId, observer);
    }
    
    public static void getGroupMembers(String userId, String groupId, RequestObserver observer) {
    	sendAsyncRequestGet(GET_GROUP_MEMBERS, "uid=" + userId + "&groupID=" + groupId, observer);
    }
    
    public static void getMessages(String userId, String lastMessageId, RequestObserver observer) {
    	sendAsyncRequestGet(GET_MESSAGES, "uid=" + userId + "&lastID=" + lastMessageId, observer);
    }
    
    public static void setConnect(String userId, String connectionId, String action, RequestObserver observer) {
    	sendAsyncRequestGet(SET_CONNECT, "myID=" + userId + "&userID=" + connectionId + "&action=" + action, observer);
    }
    
    public static void setParam(String userId, String paramName, String paramValue, RequestObserver observer) throws UnsupportedEncodingException {
		sendAsyncRequestGet(SET_PARAM, "uid=" + userId + "&param=" + paramName + "&value=" + URLEncoder.encode(paramValue, "UTF-8"), observer); 
    }
    
    public static void setTags(String userId, String experienceTags, String educationTags, String interestsTags, RequestObserver observer) throws UnsupportedEncodingException {
		sendAsyncRequestGet(SET_TAGS, "userId=" + userId + "&experience=" + URLEncoder.encode(experienceTags, "UTF-8") + "&education=" + URLEncoder.encode(educationTags, "UTF-8") +
				"&interests=" + URLEncoder.encode(interestsTags, "UTF-8"), observer); 
    }
    
    public static void sendMessage(String fromId, String toId, String message, String groupId, String topic, RequestObserver observer) throws UnsupportedEncodingException {
    	if (groupId.isEmpty() && topic.isEmpty()) {
    		sendAsyncRequestGet(SEND_MESSAGE, "from=" + fromId + "&to=" + toId + "&message=" + URLEncoder.encode(message, "UTF-8"), observer);
    	} else {
    		sendAsyncRequestGet(SEND_MESSAGE, "from=" + fromId + "&to=" + toId + "&message=" + URLEncoder.encode(message, "UTF-8") + "&groupID=" + groupId + "&topic=" + URLEncoder.encode(topic, "UTF-8"), observer);
    	}
    }
    
    public static void sendError(String userId, String error, RequestObserver observer) throws UnsupportedEncodingException {
    	sendAsyncRequestGet(SEND_ERROR, "userID=" + userId + "&errors=" + URLEncoder.encode(error, "UTF-8"), observer);
    }
    
    /**
     *  Helper methods that send requests to back end
     */
    	
    private static void sendAsyncRequestGet(String command, String requestStr, RequestObserver observer) {
    	requestStr = Constants.SERVER_URL + command + "?" + requestStr;
        sendAsyncRequest(command, requestStr, RequestData.GET_METHOD, observer);
    }

	/**
	 * This function is working in AsyncTask's background function
	 * It's not working on thread in API
	 */
	public static JSONObject sendLinkedinAuthenticationRequestPost(String url) {
		L.i("API", "sendLinkedinAuthenticationRequestPost: url = " + url);
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpost = new HttpPost(url);
		try {
			HttpResponse response = httpClient.execute(httpost);
			if (response != null) {
				if (response.getStatusLine().getStatusCode() == 200) {
					String result = EntityUtils.toString(response.getEntity());
					L.i("API", "sendLinkedinAuthenticationRequestPost: result = " + result);
					JSONObject resultJson = new JSONObject(result);
					return resultJson;
				}
			}
		} catch (IOException e) {
			L.e("Authorize", "Error Http response " + e.getLocalizedMessage());
		} catch (ParseException e) {
			L.e("Authorize", "Error Parsing Http response " + e.getLocalizedMessage());
		} catch (JSONException e) {
			L.e("Authorize", "Error Parsing Http response " + e.getLocalizedMessage());
		}
		return null;
	}
	
	/**
	 * This function is working in AsyncTask's background function
	 * It's not working on thread in API
	 */
	public static JSONObject sendLinkedinUserDataRequestGet(String url) {
		L.i("API", "sendLinkedinUserDataRequestGet: url = " + url);
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpost = new HttpGet(url);
		
		try {
			HttpResponse response = httpClient.execute(httpost);
			if (response != null) {
				if (response.getStatusLine().getStatusCode() == 200) {
					String result = EntityUtils.toString(response.getEntity());
					L.i("API", "sendLinkedinUserDataRequestGet: result = " + result);
					
					String idIdentifier = "<id>";
					String firstNameIdentifier = "<first-name>";
					String lastNameIdentifier = "<last-name>";
					String headlineIdentifier = "<headline>";
					String id = result.substring(result.indexOf(idIdentifier) + idIdentifier.length(), result.indexOf("</id>"));
					String firstName = result.substring(result.indexOf(firstNameIdentifier) + firstNameIdentifier.length(), result.indexOf("</first-name>"));
					String lastName = result.substring(result.indexOf(lastNameIdentifier) + lastNameIdentifier.length(), result.indexOf("</last-name>"));
					String headline = result.substring(result.indexOf(headlineIdentifier) + headlineIdentifier.length(), result.indexOf("</headline>"));
					
					JSONObject resultJson = new JSONObject();
					resultJson.put("id", id);
					resultJson.put("first-name", firstName);
					resultJson.put("last-name", lastName);
					resultJson.put("headline", headline);
					
					return resultJson;
				}
			}
		} catch (IOException e) {
			L.e("Authorize", "Error Http response " + e.getLocalizedMessage());
		} catch (ParseException e) {
			L.e("Authorize", "Error Parsing Http response " + e.getLocalizedMessage());
		} catch (JSONException e) {
			L.e("Authorize", "Error Parsing Http response " + e.getLocalizedMessage());
		}
		return null;
	}

    private static RequestData sendAsyncRequest(String command, String requestStr, int resuestMethod, RequestObserver observer) {
        RequestData requestData = new RequestData();
        requestData.requestObserver = observer;
        requestData.requestStr = requestStr;
        requestData.command = command;
        requestData.requestMethod = resuestMethod;
        synchronized (requestStack) {
            requestStack.add(requestData);
            requestStack.notifyAll();
        }
        return requestData;
    }

    public static String convertResponseToString(HttpResponse response) {
        StringBuilder sb = new StringBuilder();
        HttpEntity entity = response.getEntity();
        try {
            if (entity != null) {
                InputStream instream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(instream));

                String line = null;
                try {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    instream.close();
                }
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String resultString = sb.toString();
        return resultString;
    }

    private static class RequestThread extends Thread {

        private DefaultHttpClient httpClient;

        public RequestThread() {
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (requestStack.size() == 0) {
                        synchronized (requestStack) {
                            requestStack.wait();
                        }
                    }
                    if (requestStack.size() != 0) {
                        RequestData requestData;
                        synchronized (requestStack) {
                            requestData = requestStack.get(0);
                        }
                        if (httpClient == null) {
                            httpClient = new DefaultHttpClient();
                        }
                        HttpRequestBase request = null;
                        if (requestData.requestMethod == RequestData.GET_METHOD) {
                            request = new HttpGet(requestData.requestStr);
                        } else if (requestData.requestMethod == RequestData.POST_METHOD) {
                            request = new HttpPost(requestData.requestStr);
                            ((HttpPost) request).setEntity(requestData.params);
                        }
                        L.i(TAG, "request = " + requestData.requestStr);

                        HttpResponse response = null;
                        int statusCode = -1;
                        String responseStr = null;
                        try {
                        	
                        	L.i(TAG, "request = " + requestData.requestStr);
                        	
                            if (!Utils.isNetworkAvailable()) {
                            	throw new UnknownHostException("no internet connection");
                            }
                            
                            response = httpClient.execute(request);
                            statusCode = response.getStatusLine().getStatusCode();

                            L.i(TAG, "statusCode = " + statusCode);

                            if (statusCode >= 500) {
                                throw new IOException("statusCode: " + statusCode);
                            }

                            responseStr = convertResponseToString(response);
                            
                            L.i(TAG, "response = " + responseStr);

                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(responseStr);
                            } catch (JSONException e) {
                                JSONArray jsonArray = new JSONArray(responseStr);
                                jsonObject = new JSONObject();
                                jsonObject.put("values", jsonArray);
                            }
                            if (requestData.requestObserver != null) {
                            	if (jsonObject.optString("status", "").equals("OK")) {
                            		requestData.requestObserver.onSuccess(jsonObject);
                            	} else {
                            		requestData.requestObserver.onError(responseStr, new Exception(responseStr));
                            	}
                            }

                            synchronized (requestStack) {
                                requestStack.remove(requestData);
                            }
                        } catch (ClientProtocolException e) {
                            e.printStackTrace();
                            synchronized (requestStack) {
                                requestStack.remove(requestData);
                            }
                            if (requestData.requestObserver != null) {
                                requestData.requestObserver.onError(responseStr, e);
                            }
                        } catch (UnknownHostException e) {
                        	e.printStackTrace();
                            synchronized (requestStack) {
                                requestStack.remove(requestData);
                            }
                            if (requestData.requestObserver != null) {
                                requestData.requestObserver.onError(Constants.NO_INTERNET, e);
                            }
						} catch (IOException e) {
                            e.printStackTrace();
                            synchronized (requestStack) {
                                requestStack.remove(requestData);
                            }
                            if (requestData.requestObserver != null) {
                                requestData.requestObserver.onError(responseStr, e);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            synchronized (requestStack) {
                                requestStack.remove(requestData);
                            }
                            if (requestData.requestObserver != null) {
                                requestData.requestObserver.onError(responseStr, e);
                            }
                        }
                    }
                    if (Thread.interrupted())
                        break;
                }
            } catch (InterruptedException e) {
                L.d(TAG, "api is interapted " + e);
            }
        }
    }

    public static class RequestData {
        public final static int POST_METHOD = 1;
        public final static int GET_METHOD = 2;

        public RequestObserver requestObserver;
        public int requestMethod = GET_METHOD;
        public String requestStr;
        public String command;
        public HttpEntity params;

        public void setRequestObserver(RequestObserver requestObserver) {
            this.requestObserver = requestObserver;
        }
    }

    public static interface RequestObserver {
        public void onSuccess(JSONObject response) throws JSONException;

        public void onError(String response, Exception e);
    }

}
