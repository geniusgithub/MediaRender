package com.geniusgithub.mediarender.center;

import com.geniusgithub.mediarender.util.CommonLog;
import com.geniusgithub.mediarender.util.LogFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;



public class MediaControlBrocastReceiver extends BroadcastReceiver{

	private static final CommonLog log = LogFactory.createLog();
	private MediaControlBrocastFactory.IMediaControlListener mMediaControlListener;

	@Override
	public void onReceive(Context context, Intent intent) {

		  String action = intent.getAction(); 
		  if (action != null && mMediaControlListener != null){
			  TransdelControlCommand(intent);
		  }
	}
	
	public void setMediaControlListener(MediaControlBrocastFactory.IMediaControlListener listener)
	{
		mMediaControlListener = listener;
	}
	
	private void TransdelControlCommand(Intent intent){
		int time  = 0;
		String action = intent.getAction(); 
		if (action.equalsIgnoreCase(MediaControlBrocastFactory.MEDIA_RENDERER_CMD_PLAY)){
			mMediaControlListener.onPlayCommand();
		}else if (action.equalsIgnoreCase(MediaControlBrocastFactory.MEDIA_RENDERER_CMD_PAUSE)){
			mMediaControlListener.onPauseCommand();
		}else if (action.equalsIgnoreCase(MediaControlBrocastFactory.MEDIA_RENDERER_CMD_STOP)){
			mMediaControlListener.onStopCommand();
		}else if (action.equalsIgnoreCase(MediaControlBrocastFactory.MEDIA_RENDERER_CMD_SEEKPS)){	
			time  = intent.getIntExtra(MediaControlBrocastFactory.PARAM_CMD_SEEKPS, 0);
			mMediaControlListener.onSeekCommand(time);
		}
			
	}
	
}
