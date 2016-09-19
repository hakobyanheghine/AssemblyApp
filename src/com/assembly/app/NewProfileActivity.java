package com.assembly.app;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.assembly.app.backend.API;
import com.assembly.app.backend.API.RequestObserver;
import com.assembly.app.data.Conversation;
import com.assembly.app.data.User;
import com.assembly.app.manager.AssemblyAppManager;
import com.assembly.app.utils.Constants;
import com.assembly.app.utils.Utils;
import com.assembly.app.utils.view.Hashtag;
import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class NewProfileActivity extends Activity {
	private static final int COMMON_FREIND_IMAGE_WIDTH = 64;
	private static final int COMMON_FREIND_IMAGE_HEIGHT = 64;

	private User currentUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_new);

		currentUser = AssemblyAppManager.getInstance().currentSelectedUser;

		((TextView) findViewById(R.id.profile_user_name_txt)).setText(currentUser.firstName + " " + currentUser.lastName);
		((TextView) findViewById(R.id.profile_user_position_txt)).setText(currentUser.position);
		((TextView) findViewById(R.id.profile_user_location_txt)).setText(currentUser.location);
		if (currentUser.distance.isEmpty()) {
			((TextView) findViewById(R.id.profile_user_distance_txt)).setVisibility(View.GONE);
		} else if (!AssemblyAppManager.getInstance().hasEnabledGPS || !AssemblyAppManager.getInstance().userData.showInConnections) {
			((TextView) findViewById(R.id.profile_user_distance_txt)).setText("-");
		} else {
			((TextView) findViewById(R.id.profile_user_distance_txt)).setText(currentUser.distance);
		}


		if (currentUser.commonFriendsCount == 0) {
			((RelativeLayout) findViewById(R.id.profile_user_common_friends_container)).setVisibility(View.GONE);
		} else if (currentUser.commonFriendsCount == 1) {
			((TextView) findViewById(R.id.profile_friends_in_common_txt)).setText(currentUser.commonFriendsCount + " " + Utils.getStringByResourceId(R.string.friend_in_common));
		} else {
			((TextView) findViewById(R.id.profile_friends_in_common_txt)).setText(currentUser.commonFriendsCount + " " + Utils.getStringByResourceId(R.string.friends_in_common));
		}

		if (currentUser.isFriend) {
			((ImageView) findViewById(R.id.profile_connect_btn_img)).setBackgroundResource(R.drawable.ic_check);
			((TextView) findViewById(R.id.profile_connect_btn)).setTextColor(R.color.grey_4);
			((TextView) findViewById(R.id.profile_connect_btn)).setText(Utils.getStringByResourceId(R.string.connected));
		} else if (currentUser.isFriendRequestSent) {
			((ImageView) findViewById(R.id.profile_connect_btn_img)).setBackgroundResource(R.drawable.ic_requested);
			((TextView) findViewById(R.id.profile_connect_btn)).setText(Utils.getStringByResourceId(R.string.requested));
		} else {
			((TextView) findViewById(R.id.profile_connect_btn)).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// send connect request
					API.setConnect(AssemblyAppManager.getInstance().userData.userId, currentUser.userId, Constants.ACTION_CONNECT, new RequestObserver() {

						@Override
						public void onSuccess(JSONObject response) throws JSONException {
							if (response.optString("status", "").equals("OK")) {
								NewProfileActivity.this.runOnUiThread(new Runnable() {

									@Override
									public void run() {
										((TextView) findViewById(R.id.profile_connect_btn)).setOnClickListener(null);
										((ImageView) findViewById(R.id.profile_connect_btn_img)).setBackgroundResource(R.drawable.ic_requested);
										((TextView) findViewById(R.id.profile_connect_btn)).setText(Utils.getStringByResourceId(R.string.requested));
										currentUser.isFriendRequestSent = true;
									}
								});
							}
						}

						@Override
						public void onError(String response, Exception e) {
							Utils.sendErrorToBackend(e.toString());
						}
					});
				}
			});
		}

		((TextView) findViewById(R.id.profile_message_btn)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AssemblyAppManager.getInstance().currentSelectedConnection = AssemblyAppManager.getInstance().userData.getConnectionById(currentUser.userId);

				Conversation conversation = AssemblyAppManager.getInstance().userData.getConversationByOpponentId(currentUser.userId, 0);
				if (conversation != null) {
					AssemblyAppManager.getInstance().currentSelectedConversation = conversation;
				} else {
					AssemblyAppManager.getInstance().currentSelectedConversation = new Conversation(currentUser.userId, currentUser.firstName + " " + currentUser.lastName, currentUser.pictureUrl, "0", false, false, false);
				}

				startMessagesActivity();
			}
		});

		ImageLoader.getInstance().loadImage(currentUser.pictureUrl, AssemblyAppManager.getInstance().targetSize256, null);
		ImageLoader.getInstance().displayImage(currentUser.pictureUrl, (ImageView) findViewById(R.id.profile_user_img), AssemblyAppManager.getInstance().optionsWithLoading);

		addCommonFriendsImages();
		addExperience();
		addEducation();
		addInterests();

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle(currentUser.firstName + " " + currentUser.lastName);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void addCommonFriendsImages() {
		LinearLayout commonFriendsLayout = (LinearLayout) findViewById(R.id.profile_user_common_friends);
		for (int i = 0; i < currentUser.commonFriendsPictureUrl.size(); i++) {
			final ImageView commonFriendImage = new ImageView(this);

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(COMMON_FREIND_IMAGE_WIDTH, COMMON_FREIND_IMAGE_HEIGHT);
			params.setMargins(0, 0, 5, 0);
			commonFriendImage.setLayoutParams(params);
			commonFriendsLayout.addView(commonFriendImage);

			if (currentUser.commonFriendsPictureUrl.get(i).isEmpty()) {
				commonFriendImage.setImageResource(R.drawable.no_img);
			} else {
				Glide.with(this).load(R.drawable.loading_1).into(commonFriendImage);
				ImageLoader.getInstance().loadImage(currentUser.commonFriendsPictureUrl.get(i), AssemblyAppManager.getInstance().targetSize64, null);
				ImageLoader.getInstance().displayImage(currentUser.commonFriendsPictureUrl.get(i), commonFriendImage, AssemblyAppManager.getInstance().optionsWithLoading, new SimpleImageLoadingListener() {

							@Override
							public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
								super.onLoadingComplete(imageUri, view, loadedImage);
								Glide.clear(commonFriendImage);
								commonFriendImage.setImageBitmap(loadedImage);
							}

							@Override
							public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
								super.onLoadingFailed(imageUri, view, failReason);
								Glide.clear(commonFriendImage);
								commonFriendImage.setImageResource(R.drawable.no_img);
							}

							@Override
							public void onLoadingCancelled(String imageUri, View view) {
								super.onLoadingCancelled(imageUri, view);
								Glide.clear(commonFriendImage);
								commonFriendImage.setImageResource(R.drawable.no_img);
							}

				});
			}
		}
	}

	private void addEducation() {
		String tags = currentUser.educationTags;
		if (tags.isEmpty()) {
			((TextView) findViewById(R.id.profile_education_title_txt)).setVisibility(View.GONE);
			((TextView) findViewById(R.id.profile_education_container)).setVisibility(View.GONE);
		} else {
			ArrayList<int[]> hashtagSpans = Utils.getSpans(tags, '#');

			SpannableString commentsContent = new SpannableString(currentUser.educationTags);
			for (int i = 0; i < hashtagSpans.size(); i++) {
				int[] span = hashtagSpans.get(i);
				int hashTagStart = span[0];
				int hashTagEnd = span[1];

				commentsContent.setSpan(new Hashtag(this), hashTagStart, hashTagEnd, 0);
			}
			((TextView) findViewById(R.id.profile_education_container)).setMovementMethod(LinkMovementMethod.getInstance());
			((TextView) findViewById(R.id.profile_education_container)).setText(commentsContent);
		}
	}
	
	private void addExperience() {
		String tags = currentUser.experienceTags;
		if (tags.isEmpty()) {
			((TextView) findViewById(R.id.profile_experience_title_txt)).setVisibility(View.GONE);
			((TextView) findViewById(R.id.profile_experience_container)).setVisibility(View.GONE);
		} else {
			ArrayList<int[]> hashtagSpans = Utils.getSpans(tags, '#');

			SpannableString commentsContent = new SpannableString(currentUser.experienceTags);
			for (int i = 0; i < hashtagSpans.size(); i++) {
				int[] span = hashtagSpans.get(i);
				int hashTagStart = span[0];
				int hashTagEnd = span[1];

				commentsContent.setSpan(new Hashtag(this), hashTagStart, hashTagEnd, 0);
			}
			((TextView) findViewById(R.id.profile_experience_container)).setMovementMethod(LinkMovementMethod.getInstance());
			((TextView) findViewById(R.id.profile_experience_container)).setText(commentsContent);
		}
	}
	
	private void addInterests() {
		String tags = currentUser.interestsTags;
		if (tags.isEmpty()) {
			((TextView) findViewById(R.id.profile_interests_title_txt)).setVisibility(View.GONE);
			((TextView) findViewById(R.id.profile_interests_container)).setVisibility(View.GONE);
		} else {
			ArrayList<int[]> hashtagSpans = Utils.getSpans(tags, '#');

			SpannableString commentsContent = new SpannableString(currentUser.interestsTags);
			for (int i = 0; i < hashtagSpans.size(); i++) {
				int[] span = hashtagSpans.get(i);
				int hashTagStart = span[0];
				int hashTagEnd = span[1];

				commentsContent.setSpan(new Hashtag(this), hashTagStart, hashTagEnd, 0);
			}
			((TextView) findViewById(R.id.profile_interests_container)).setMovementMethod(LinkMovementMethod.getInstance());
			((TextView) findViewById(R.id.profile_interests_container)).setText(commentsContent);
		}
	}
	
	private void startMessagesActivity() {
		NewProfileActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Intent messagesActivity = new Intent(NewProfileActivity.this, MessagesActivity.class);
				startActivity(messagesActivity);
			}
		});
	}
}
