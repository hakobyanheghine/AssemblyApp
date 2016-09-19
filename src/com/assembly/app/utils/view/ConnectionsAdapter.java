package com.assembly.app.utils.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.assembly.app.R;
import com.assembly.app.data.Connection;
import com.assembly.app.manager.AssemblyAppManager;
import com.assembly.app.utils.Constants;
import com.assembly.app.utils.Utils;
import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class ConnectionsAdapter extends ArrayAdapter<Connection> {

	private Context context;
	private boolean isGroupMemebers;
	
	public ConnectionsAdapter(Context context, int resource, List<Connection> objects, boolean isGroupMemebers) {
		super(context, resource, objects);
		this.context = context;
		this.isGroupMemebers = isGroupMemebers;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Connection child = (Connection) getItem(position);
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.item_connections_listview, parent, false);
		}
		((TextView) convertView.findViewById(R.id.connection_name_txt)).setText(child.firstName + " " + child.lastName);
		((TextView) convertView.findViewById(R.id.connection_position_txt)).setText(child.position);

		if (child.distance.isEmpty() || child.distance.equals(Constants.DEFAULT_DISTANCE) || !AssemblyAppManager.getInstance().hasEnabledGPS
				|| !AssemblyAppManager.getInstance().userData.showInConnections) {
			((TextView) convertView.findViewById(R.id.connection_distance_txt)).setText("-");
		} else {
			((TextView) convertView.findViewById(R.id.connection_distance_txt)).setText(child.distance + " " + AssemblyAppManager.getInstance().userData.userDistanceUnit);
		}

		((TextView) convertView.findViewById(R.id.connection_friends_txt)).setText(child.commonFriendsCount + "");
		if (isGroupMemebers || child.tags.isEmpty()) {
			((TextView) convertView.findViewById(R.id.connection_hashtags_container)).setVisibility(View.GONE);
		} else {
			((TextView) convertView.findViewById(R.id.connection_hashtags_container)).setVisibility(View.VISIBLE);
			((TextView) convertView.findViewById(R.id.connection_hashtags_container)).setText(child.tags);
			String tags = child.tags;
			ArrayList<int[]> hashtagSpans = Utils.getSpans(tags, '#');
			
			SpannableString commentsContent = new SpannableString(child.tags);
			for(int i = 0; i < hashtagSpans.size(); i++) {
		        int[] span = hashtagSpans.get(i);
		        int hashTagStart = span[0];
		        int hashTagEnd = span[1];

		        commentsContent.setSpan(new Hashtag(context), hashTagStart, hashTagEnd, 0);
		    }
			((TextView) convertView.findViewById(R.id.connection_hashtags_container)).setMovementMethod(LinkMovementMethod.getInstance());
			((TextView) convertView.findViewById(R.id.connection_hashtags_container)).setText(commentsContent);
		}
		
		final ImageView imageView = (ImageView) convertView.findViewById(R.id.connection_img);

		ImageLoader.getInstance().loadImage(child.pictureUrl, AssemblyAppManager.getInstance().targetSize70, null);
		ImageLoader.getInstance().displayImage(child.pictureUrl, imageView, AssemblyAppManager.getInstance().optionsWithLoading, new SimpleImageLoadingListener() {

			@Override
			public void onLoadingStarted(String imageUri, View view) {
				super.onLoadingStarted(imageUri, view);
				if (imageUri.isEmpty()) {
					imageView.setImageResource(R.drawable.no_img);
				} else {
					Glide.with(context).load(R.drawable.loading_1).into(imageView);
				}
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				super.onLoadingComplete(imageUri, view, loadedImage);
				if (imageUri.isEmpty()) {
					imageView.setImageResource(R.drawable.no_img);
				} else {
					Glide.clear(imageView);
					imageView.setImageBitmap(loadedImage);
				}
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				super.onLoadingFailed(imageUri, view, failReason);
				Glide.clear(imageView);
				imageView.setImageResource(R.drawable.no_img);
			}

			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				super.onLoadingCancelled(imageUri, view);
				Glide.clear(imageView);
				imageView.setImageResource(R.drawable.no_img);
			}

		});

		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AssemblyAppManager.getInstance().loadConnectionProfile((Activity) context, child.userId);
			}
		});
		
		return convertView;
	}
}
