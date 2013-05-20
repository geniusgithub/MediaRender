package com.geniusgithub.mediarender.center;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.geniusgithub.mediarender.jni.PlatinumJniProxy;
import com.geniusgithub.mediarender.jni.PlatinumReflection;
import com.geniusgithub.mediarender.util.CommonLog;
import com.geniusgithub.mediarender.util.LogFactory;



public class DLNAGenaEventBrocastReceiver extends BroadcastReceiver{

	private static final CommonLog log = LogFactory.createLog();

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action == null){
			return ;
		}
		
		if (PlatinumReflection.RENDERER_TOCONTRPOINT_CMD_INTENT_NAME.equalsIgnoreCase(action)){
			onTransdelGenaEvent(intent);
		}
	}
	
	
	private void onTransdelGenaEvent(Intent intent){
		
		int cmd = intent.getIntExtra(PlatinumReflection.GET_RENDERER_TOCONTRPOINT_CMD, 0);
		
		switch(cmd){
			case 0:
				break;
			case PlatinumReflection.MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_DURATION:
				String duration = intent.getStringExtra(PlatinumReflection.GET_PARAM_MEDIA_DURATION);
				PlatinumJniProxy.responseGenaEvent(PlatinumReflection.MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_DURATION, duration, null);
				break;
			case PlatinumReflection.MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_POSITION:
				String positionto = intent.getStringExtra(PlatinumReflection.GET_PARAM_MEDIA_POSITION);
				PlatinumJniProxy.responseGenaEvent(PlatinumReflection.MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_POSITION,
						positionto,null);
				break;
			case PlatinumReflection.MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_PLAYINGSTATE:
				String playingstate = intent.getStringExtra(PlatinumReflection.GET_PARAM_MEDIA_PLAYINGSTATE);
				PlatinumJniProxy.responseGenaEvent(PlatinumReflection.MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_PLAYINGSTATE,
						playingstate,null);

				
				if (playingstate.equalsIgnoreCase(PlatinumReflection.MEDIA_PLAYINGSTATE_STOP)){
					PlatinumJniProxy.responseGenaEvent(PlatinumReflection.MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_POSITION, "00:00:00", null);
				}						
				break;
		}
	}

}
