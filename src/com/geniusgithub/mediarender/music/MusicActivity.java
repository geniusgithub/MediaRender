package com.geniusgithub.mediarender.music;

import com.geniusgithub.mediarender.R;

import android.app.Activity;
import android.os.Bundle;

public class MusicActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.music_player_layout);
	}
}
