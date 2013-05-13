package com.geniusgithub.mediarender.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MediaRenderService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();		
		initRenderService();	
	}

	@Override
	public void onDestroy() {
		unInitRenderService();	
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	private void initRenderService(){
		
	}

	
	private void unInitRenderService(){
		
	}
}
