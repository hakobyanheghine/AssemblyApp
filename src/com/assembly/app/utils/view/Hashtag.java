package com.assembly.app.utils.view;

import android.app.Activity;
import android.content.Context;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.assembly.app.manager.AssemblyAppManager;

public class Hashtag extends ClickableSpan {
	private Context context;

	public Hashtag(Context ctx) {
		super();
		context = ctx;
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setColor(ds.linkColor);
		ds.setARGB(255, 71, 151, 212);
	}

	@Override
	public void onClick(View widget) {
		TextView tv = (TextView) widget;
		Spanned s = (Spanned) tv.getText();
		int start = s.getSpanStart(this);
		int end = s.getSpanEnd(this);
		String theWord = s.subSequence(start + 1, end).toString();
		AssemblyAppManager.getInstance().startSearchActivity((Activity)context, "#" + theWord);
	}
	
}