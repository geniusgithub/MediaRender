package com.geniusgithub.mediarender;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


public class DeviceUpdateBrocastFactory {

	public static final String PARAM_DEV_UPDATE="com.geniusgithub.PARAM_DEV_UPDATE";
	
	public static interface IDevUpdateListener {
		public void onUpdate();
	}
	
	
	private DeviceUpdateBrocastReceiver mReceiver;
	private Context mContext;
	
	public DeviceUpdateBrocastFactory(Context context){
		mContext = context;
	}
	
	
	public void register(IDevUpdateListener listener){
		if (mReceiver == null){
			mReceiver = new DeviceUpdateBrocastReceiver();
			mReceiver.setListener(listener);	
			mContext.registerReceiver(mReceiver, new IntentFilter(PARAM_DEV_UPDATE));
		}
	}

	public void unregister()
	{
		if (mReceiver != null){
			mContext.unregisterReceiver(mReceiver);
			mReceiver = null;
		}
	}
	
	public static void sendDevUpdateBrocast(Context context){
		Intent intent = new Intent();
		intent.setAction(PARAM_DEV_UPDATE);
		context.sendBroadcast(intent);
	}
	
}
