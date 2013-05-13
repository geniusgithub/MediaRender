package com.geniusgithub.mediarender;

import android.app.Application;

import com.geniusgithub.mediarender.util.CommonLog;
import com.geniusgithub.mediarender.util.LogFactory;

public class RenderApplication  extends Application{

	private static final CommonLog log = LogFactory.createLog();

	private static RenderApplication mInstance;
	
	
	public synchronized static RenderApplication getInstance(){
		return mInstance;
	}
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
	
	}

	
	
	
	
}
