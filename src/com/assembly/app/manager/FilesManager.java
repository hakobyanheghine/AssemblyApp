package com.assembly.app.manager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.assembly.app.data.Message;

public class FilesManager {

	public static final String MESSAGES_FILE_NAME = "messages";

	private static FilesManager instance;

	private FilesManager() {
		
	}

	public static FilesManager getInstance() {
		if (instance == null) {
			instance = new FilesManager();
		}

		return instance;
	}
	
	public void saveMessagesToFileAsync() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (AssemblyAppManager.getInstance().userData != null) {
					saveMessagesToFile(AssemblyAppManager.getInstance().userData.allMessages);
				}
			}
		}).start();
	}
	
	public void saveMessagesToFile(ArrayList<Message> messages) {
		JSONArray messagesJson = new JSONArray();
		try {

			for (int i = 0; i < messages.size(); i++) {
				JSONObject messageJson = new JSONObject();
				messageJson.put("messageID", messages.get(i).messageId);
				messageJson.put("message", messages.get(i).message);
				messageJson.put("date", messages.get(i).date);
				messageJson.put("opponentID", messages.get(i).opponentId);
				messageJson.put("opponentName", messages.get(i).opponentName);
				messageJson.put("opponentPicture", messages.get(i).opponentUrl);
				
				messageJson.put("my", messages.get(i).isMine ? 1 : 0);
				messageJson.put("connected", messages.get(i).isConnected ? 1 : 0);
				messageJson.put("new", messages.get(i).isNew ? 1 : 0);
				
				messageJson.put("conversationID", messages.get(i).conversationId);
				messageJson.put("groupID", messages.get(i).groupId);
				messageJson.put("senderID", messages.get(i).senderId);
				messageJson.put("topic", messages.get(i).topic);
				
				messagesJson.put(messageJson);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		writeToFile(messagesJson.toString());
	}
	
	public ArrayList<Message> getAllMessages() {
		ArrayList<Message> messages = new ArrayList<Message>();
		String messagesStr = readFromFile();
		JSONArray messagesJson = new JSONArray();
		try {
			messagesJson = new JSONArray(messagesStr);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < messagesJson.length(); i++) {
			Message message = new Message(messagesJson.optJSONObject(i));
			messages.add(message);
		}
		
		return messages;
	}
	
	private String readFromFile() {
		FileInputStream in = null;
		String messages = "";
		try {
			in = AssemblyAppManager.getInstance().mainActivity.openFileInput(MESSAGES_FILE_NAME);

			InputStreamReader inputStreamReader = new InputStreamReader(in);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}
			messages = sb.toString();
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			messages = "";
		} catch (IOException e) {
			e.printStackTrace();
			messages = "";
		} finally {
			try {
				in.close();
			} catch (Exception e) {
			}
		}
		
		return messages;
	}

	private void writeToFile(String messages) {
		FileOutputStream outputStream = null;
		try {
			outputStream = AssemblyAppManager.getInstance().mainActivity.openFileOutput(MESSAGES_FILE_NAME, Context.MODE_PRIVATE);
			outputStream.write(messages.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
			}
		}
	}

	
}
