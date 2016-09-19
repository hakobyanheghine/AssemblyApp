package com.assembly.app;

import com.assembly.app.manager.AssemblyAppManager;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class TutorialActivity extends Activity {
	
	private int currentTutorialSlide;
	
	private int[] tutorialSlides = {
		R.drawable.tutorial_1,
		R.drawable.tutorial_2,
		R.drawable.tutorial_3,
		R.drawable.tutorial_4,
		R.drawable.tutorial_5
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_tutorial);
		
		((RelativeLayout) findViewById(R.id.tutorial_next)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (currentTutorialSlide + 1 < tutorialSlides.length) {
					currentTutorialSlide++;
					ImageLoader.getInstance().displayImage("drawable://" + tutorialSlides[currentTutorialSlide], (ImageView) findViewById(R.id.tutorial_container), AssemblyAppManager.getInstance().optionsWithoutLoading);
				} else {
					finish();
				}
			} 
		});
		
		((RelativeLayout) findViewById(R.id.tutorial_back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (currentTutorialSlide - 1 >= 0) {
					currentTutorialSlide--;
					ImageLoader.getInstance().displayImage("drawable://" + tutorialSlides[currentTutorialSlide], (ImageView) findViewById(R.id.tutorial_container), AssemblyAppManager.getInstance().optionsWithoutLoading);
				} else {
					finish();
				}
			}
		});
	}

}
