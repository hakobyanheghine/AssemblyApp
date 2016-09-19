package com.assembly.app.utils.view;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.assembly.app.ConversationsActivity;
import com.assembly.app.R;
import com.assembly.app.data.Conversation;
import com.assembly.app.data.Message;
import com.assembly.app.manager.AssemblyAppManager;
import com.assembly.app.utils.Constants;
import com.assembly.app.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ConversationsAdapter extends ArrayAdapter<Conversation> {

	private Context context;
	
	public ConversationsAdapter(Context context, int resource, List<Conversation> objects) {
		super(context, resource, objects);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Conversation child = (Conversation) getItem(position);
		final Message lastMessage = child.messages.get(child.messages.size() - 1);
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.item_conversations_listview, parent, false);
		}

		String opponentPictureUrl = "";
		String opponentName = "";
		if (child.isGroupConversation) {
			opponentPictureUrl = child.groupPictureUrl;
			opponentName = child.groupName;
		} else {
			opponentPictureUrl = child.opponentPictureUrl;
			opponentName = child.opponentName;
		}
		
		((TextView) convertView.findViewById(R.id.message_user_name_txt)).setText(opponentName);
		if (child.isGroupConversation) {
			((TextView) convertView.findViewById(R.id.message_description_txt)).setText(child.topic);
		} else {
			((TextView) convertView.findViewById(R.id.message_description_txt)).setText(lastMessage.message);
		}

		if (child.newMessageCount > 0) {
			((TextView) convertView.findViewById(R.id.message_count_txt)).setVisibility(View.VISIBLE);
			((TextView) convertView.findViewById(R.id.message_count_txt)).setText(child.newMessageCount + "");
			((RelativeLayout) convertView.findViewById(R.id.message_container)).setBackgroundColor(context.getResources().getColor(R.color.grey_6));
		} else {
			((TextView) convertView.findViewById(R.id.message_count_txt)).setVisibility(View.GONE);
			((RelativeLayout) convertView.findViewById(R.id.message_container)).setBackgroundColor(context.getResources().getColor(R.color.white));
		}
		
		long dateValue = child.messages.get(child.messages.size() - 1).date * 1000;
		String[] dateAndTime = Utils.getDateAndTimeInDotFormat(dateValue);
		
		long millisecondsPassed = System.currentTimeMillis() - dateValue;
		
		if (millisecondsPassed > Constants.ONE_YEAR_IN_MILLISECONDS) {
			((TextView) convertView.findViewById(R.id.message_time_txt)).setText(dateAndTime[0]);
		} else if (millisecondsPassed > Constants.ONE_HOUR_IN_MILLISECONDS) {
			((TextView) convertView.findViewById(R.id.message_time_txt)).setText(dateAndTime[0].substring(0, dateAndTime[0].lastIndexOf(".")));
		} else if (millisecondsPassed > Constants.ONE_MINUTE_IN_MILLISECONDS) {
			((TextView) convertView.findViewById(R.id.message_time_txt)).setText((millisecondsPassed / 1000 / 60) + "m");
		} else {
			((TextView) convertView.findViewById(R.id.message_time_txt)).setText((millisecondsPassed / 1000) + "s");
		}
		
		if (!child.isGroupConversation) {
			((ImageView) convertView.findViewById(R.id.message_user_img)).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					AssemblyAppManager.getInstance().loadConnectionProfile((ConversationsActivity) context, child.opponentId);
				}
			});
		}
		ImageLoader.getInstance().loadImage(opponentPictureUrl, AssemblyAppManager.getInstance().targetSize70, null);
		ImageLoader.getInstance().displayImage(opponentPictureUrl, (ImageView) convertView.findViewById(R.id.message_user_img), AssemblyAppManager.getInstance().optionsWithLoading);

		return convertView;
	}
}
