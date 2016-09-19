package com.assembly.app.utils.view;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.assembly.app.R;
import com.assembly.app.data.Message;
import com.assembly.app.utils.Utils;

public class MessagesAdapter extends ArrayAdapter<Message> {

	private Context context;
	private View rootView;

	public MessagesAdapter(Context context, int resource, List<Message> objects) {
		super(context, resource, objects);
		this.context = context;
		
		rootView = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Message message = (Message) this.getItem(position);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (message.isMine) {
			convertView = inflater.inflate(R.layout.item_messages_mine_1, parent, false);
		} else {
			convertView = inflater.inflate(R.layout.item_messages_friend_1, parent, false);
		}

		if (message.groupId.equals("0")) {
			((TextView) convertView.findViewById(R.id.chat_message_text)).setText(message.message);
		} else {
			((TextView) convertView.findViewById(R.id.chat_message_text)).setText(message.opponentName + ":\n" + message.message);
		}
		
		String[] dateAndTime = Utils.getDateAndTimeInDotFormatNew(message.date * 1000);
		
		((TextView) rootView.findViewById(R.id.messages_date_txt)).setText(dateAndTime[0]);
		((TextView) convertView.findViewById(R.id.chat_message_date_txt)).setText(dateAndTime[1]);
		
		return convertView;
	}
}
