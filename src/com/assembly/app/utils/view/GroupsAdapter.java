package com.assembly.app.utils.view;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.assembly.app.R;
import com.assembly.app.data.Group;
import com.assembly.app.manager.AssemblyAppManager;
import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class GroupsAdapter extends ArrayAdapter<Group> {

	private Context context;

	public GroupsAdapter(Context context, int resource, List<Group> objects) {
		super(context, resource, objects);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Group child = (Group) getItem(position);
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.item_connections_listview, parent, false);
		}
		((TextView) convertView.findViewById(R.id.connection_name_txt)).setText(child.name);
		((TextView) convertView.findViewById(R.id.connection_position_txt)).setVisibility(View.GONE);

		((TextView) convertView.findViewById(R.id.connection_group_desc_txt)).setVisibility(View.VISIBLE);
		((TextView) convertView.findViewById(R.id.connection_group_desc_txt)).setText(child.description);

		((TextView) convertView.findViewById(R.id.connection_distance_txt)).setVisibility(View.GONE);
		((TextView) convertView.findViewById(R.id.connection_friends_txt)).setText(child.friendsCount + "");

		((LinearLayout) convertView.findViewById(R.id.connection_location_container)).setVisibility(View.GONE);
		((TextView) convertView.findViewById(R.id.connection_group_members_txt)).setVisibility(View.VISIBLE);
		((TextView) convertView.findViewById(R.id.connection_group_members_txt)).setText(child.friendsCount + "");

		((TextView) convertView.findViewById(R.id.connection_hashtags_container)).setVisibility(View.GONE);
		
		final ImageView imageView = (ImageView) convertView.findViewById(R.id.connection_img);

		ImageLoader.getInstance().loadImage(child.photoUrl, AssemblyAppManager.getInstance().targetSize70, null);
		ImageLoader.getInstance().displayImage(child.photoUrl, imageView, AssemblyAppManager.getInstance().optionsWithLoading, new SimpleImageLoadingListener() {

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

		return convertView;
	}

}
