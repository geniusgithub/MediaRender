package com.geniusgithub.mediarender;

import android.app.Application;

import com.geniusgithub.mediarender.util.CommonLog;
import com.geniusgithub.mediarender.util.LogFactory;

public class RenderApplication  extends Application{

	private static final CommonLog log = LogFactory.createLog();

	private static RenderApplication mInstance;

	private DeviceInfo mDeviceInfo;
	
	
	public synchronized static RenderApplication getInstance(){
		return mInstance;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		log.e("RenderApplication onCreate");
		
		mInstance = this;
		mDeviceInfo = new DeviceInfo();
	}

	public void updateDevInfo(String name, String uuid){
		mDeviceInfo.dev_name = name;
		mDeviceInfo.uuid = uuid;
	}
	
	public void setDevStatus(boolean flag){
		mDeviceInfo.status = flag;
		DeviceUpdateBrocastFactory.sendDevUpdateBrocast(this);
	}
	
	public DeviceInfo getDevInfo(){
		return mDeviceInfo;
	}
	
}
