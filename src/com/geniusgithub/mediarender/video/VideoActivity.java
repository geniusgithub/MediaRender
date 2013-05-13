package com.geniusgithub.mediarender.video;

import com.geniusgithub.mediarender.R;

import android.app.Activity;
import android.os.Bundle;

public class VideoActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.video_player_layout);
	}

}
