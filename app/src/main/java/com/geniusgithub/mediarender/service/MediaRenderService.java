package com.geniusgithub.mediarender.service;

import com.geniusgithub.mediarender.RenderApplication;
import com.geniusgithub.mediarender.center.DMRCenter;
import com.geniusgithub.mediarender.center.DMRWorkThread;
import com.geniusgithub.mediarender.center.IBaseEngine;
import com.geniusgithub.mediarender.center.DLNAGenaEventBrocastFactory;
import com.geniusgithub.mediarender.jni.PlatinumJniProxy;
import com.geniusgithub.mediarender.jni.PlatinumReflection;
import com.geniusgithub.mediarender.jni.PlatinumReflection.ActionReflectionListener;
import com.geniusgithub.mediarender.util.CommonLog;
import com.geniusgithub.mediarender.util.CommonUtil;
import com.geniusgithub.mediarender.util.DlnaUtils;
import com.geniusgithub.mediarender.util.LogFactory;



import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class MediaRenderService extends Service implements IBaseEngine{

	private static final CommonLog log = LogFactory.createLog();
	
	public static final String START_RENDER_ENGINE = "com.geniusgithub.start.engine";
	public static final String RESTART_RENDER_ENGINE = "com.geniusgithub.restart.engine";


	private DMRWorkThread mWorkThread;
	
	private ActionReflectionListener mListener;
	private DLNAGenaEventBrocastFactory mMediaGenaBrocastFactory;
	
	private Handler mHandler;
	private static final int START_ENGINE_MSG_ID = 0x0001;
	private static final int RESTART_ENGINE_MSG_ID = 0x0002;
	
	private static final int DELAY_TIME = 1000;
	
	private MulticastLock mMulticastLock;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();		
		initRenderService();	
		log.e("MediaRenderService onCreate");
	}

	@Override
	public void onDestroy() {
		unInitRenderService();	
		log.e("MediaRenderService onDestroy");
		super.onDestroy();
	
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if (intent != null){
			String actionString = intent.getAction();
			if (actionString != null){		
				if (actionString.equalsIgnoreCase(START_RENDER_ENGINE)){
					delayToSendStartMsg();
				}else if (actionString.equalsIgnoreCase(RESTART_RENDER_ENGINE)){
					delayToSendRestartMsg();
				}
			}
		}	
	
		return super.onStartCommand(intent, flags, startId);
		
	}
	
	
	private void initRenderService(){

		mListener = new DMRCenter(this);
		PlatinumReflection.setActionInvokeListener(mListener);
		mMediaGenaBrocastFactory = new DLNAGenaEventBrocastFactory(this);
		mMediaGenaBrocastFactory.registerBrocast();
		mWorkThread = new DMRWorkThread(this);
		
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what){
				case START_ENGINE_MSG_ID:
					startEngine();
					break;
				case RESTART_ENGINE_MSG_ID:
					restartEngine();
					break;
				}
			}
			
		};
		
		mMulticastLock = CommonUtil.openWifiBrocast(this);
		log.e("openWifiBrocast = "  +  mMulticastLock != null ? true : false);
	}

	
	private void unInitRenderService(){
		stopEngine();
		removeStartMsg();
		removeRestartMsg();
		mMediaGenaBrocastFactory.unRegisterBrocast();
		if (mMulticastLock != null){
			mMulticastLock.release();
			mMulticastLock = null;
			log.e("closeWifiBrocast");
		}
	}

	private void delayToSendStartMsg(){
		removeStartMsg();
		mHandler.sendEmptyMessageDelayed(START_ENGINE_MSG_ID, DELAY_TIME);
	}
	
	private void delayToSendRestartMsg(){
		removeStartMsg();
		removeRestartMsg();
		mHandler.sendEmptyMessageDelayed(RESTART_ENGINE_MSG_ID, DELAY_TIME);
	}
	
	private void removeStartMsg(){
		mHandler.removeMessages(START_ENGINE_MSG_ID);
	}
	
	private void removeRestartMsg(){
		mHandler.removeMessages(RESTART_ENGINE_MSG_ID);	
	}
	
	
	@Override
	public boolean startEngine() {
		awakeWorkThread();
		return true;
	}

	@Override
	public boolean stopEngine() {
		mWorkThread.setParam("", "");
		exitWorkThread();
		return true;
	}

	@Override
	public boolean restartEngine() {
		String friendName = DlnaUtils.getDevName(this);
		String uuid = DlnaUtils.creat12BitUUID(this);
		mWorkThread.setParam(friendName, uuid);
		if (mWorkThread.isAlive()){
			mWorkThread.restartEngine();
		}else{
			mWorkThread.start();
		}
		return true;
	}

	private void awakeWorkThread(){
		String friendName = DlnaUtils.getDevName(this);
		String uuid = DlnaUtils.creat12BitUUID(this);
		mWorkThread.setParam(friendName, uuid);
		
		
		if (mWorkThread.isAlive()){
			mWorkThread.awakeThread();
		}else{
			mWorkThread.start();
		}
	}
	
	private void exitWorkThread(){
		if (mWorkThread != null && mWorkThread.isAlive()){
			mWorkThread.exit();
			long time1 = System.currentTimeMillis();
			while(mWorkThread.isAlive()){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			long time2 = System.currentTimeMillis();
			log.e("exitWorkThread cost time:" + (time2 - time1));
			mWorkThread = null;
		}
	}


}
