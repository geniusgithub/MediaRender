package com.geniusgithub.mediarender.center;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class MediaControlBrocastFactory {

	public static interface IMediaControlListener {
		public void onPlayCommand();
		public void onPauseCommand();
		public void onStopCommand();
		public void onSeekCommand(int time);	
	}
	
	
	private MediaControlBrocastReceiver mMediaControlReceiver;
	private Context mContext;
	
	public MediaControlBrocastFactory(Context context){
		mContext = context;
	}
	
	
	public void register(IMediaControlListener listener){
		if (mMediaControlReceiver == null){
			mMediaControlReceiver = new MediaControlBrocastReceiver();
			mMediaControlReceiver.setMediaControlListener(listener);	
			
			mContext.registerReceiver(mMediaControlReceiver, new IntentFilter(MediaControlBrocastFactory.MEDIA_RENDERER_CMD_PLAY));
			mContext.registerReceiver(mMediaControlReceiver, new IntentFilter(MediaControlBrocastFactory.MEDIA_RENDERER_CMD_PAUSE));
			mContext.registerReceiver(mMediaControlReceiver, new IntentFilter(MediaControlBrocastFactory.MEDIA_RENDERER_CMD_STOP));
			mContext.registerReceiver(mMediaControlReceiver, new IntentFilter(MediaControlBrocastFactory.MEDIA_RENDERER_CMD_SEEKPS));

		}
	}

	public void unregister()
	{
		if (mMediaControlReceiver != null){
			mContext.unregisterReceiver(mMediaControlReceiver);
			mMediaControlReceiver = null;
		}
	}
	
	
	
	public static final String MEDIA_RENDERER_CMD_PLAY="com.geniusgithub.control.play_command"; 
	public static final String MEDIA_RENDERER_CMD_PAUSE="com.geniusgithub.control.pause_command";
	public static final String MEDIA_RENDERER_CMD_STOP="com.geniusgithub.control.stop_command"; 
	public static final String MEDIA_RENDERER_CMD_SEEKPS="com.geniusgithub.control.seekps_command"; 
		
	public static final String PARAM_CMD_SEEKPS="get_param_seekps";
	
	
	
	public static void sendPlayBrocast(Context context){
		Intent intent = new Intent(MEDIA_RENDERER_CMD_PLAY);
		context.sendBroadcast(intent);
	}
	
	public static void sendPauseBrocast(Context context){
		Intent intent = new Intent(MEDIA_RENDERER_CMD_PAUSE);
		context.sendBroadcast(intent);
	}
	
	public static void sendStopBorocast(Context context){
		Intent intent = new Intent(MEDIA_RENDERER_CMD_STOP);
		context.sendBroadcast(intent);
	}
	
	public static void sendSeekBrocast(Context context, int seekPos){
		Intent intent = new Intent(MEDIA_RENDERER_CMD_SEEKPS);
		intent.putExtra(PARAM_CMD_SEEKPS, seekPos);
		context.sendBroadcast(intent);
	}

}
