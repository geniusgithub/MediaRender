package com.mipt.platinum;

import com.geniusgithub.mediarender.util.CommonLog;
import com.geniusgithub.mediarender.util.LogFactory;





public class DLNAInflectClass {
	
	private static final CommonLog log = LogFactory.createLog();
	
	private static final int MEDIA_RENDER_CTL_MSG_BASE = 0x100;
	/*----------------------------------------------------------------*/
	public static final int MEDIA_RENDER_CTL_MSG_SET_AV_URL = (MEDIA_RENDER_CTL_MSG_BASE+0);
	public static final int MEDIA_RENDER_CTL_MSG_STOP = (MEDIA_RENDER_CTL_MSG_BASE+1);
	public static final int MEDIA_RENDER_CTL_MSG_PLAY = (MEDIA_RENDER_CTL_MSG_BASE+2);
	public static final int MEDIA_RENDER_CTL_MSG_PAUSE = (MEDIA_RENDER_CTL_MSG_BASE+3);
	public static final int MEDIA_RENDER_CTL_MSG_SEEK = (MEDIA_RENDER_CTL_MSG_BASE+4);
	public static final int MEDIA_RENDER_CTL_MSG_SETVOLUME = (MEDIA_RENDER_CTL_MSG_BASE+5);
	public static final int MEDIA_RENDER_CTL_MSG_SETMUTE = (MEDIA_RENDER_CTL_MSG_BASE+6);
	public static final int MEDIA_RENDER_CTL_MSG_SETPLAYMODE = (MEDIA_RENDER_CTL_MSG_BASE+7);
	public static final int MEDIA_RENDER_CTL_MSG_PRE = (MEDIA_RENDER_CTL_MSG_BASE+8);
	public static final int MEDIA_RENDER_CTL_MSG_NEXT = (MEDIA_RENDER_CTL_MSG_BASE+9);
	/*----------------------------------------------------------------*/	
	
	
	
	/*----------------------------------------------------------------*/	
	// 爱奇艺
	public static final int  PRIVATE_SERVER_CTL_MSG_BASE = 0x200;
	public static final int  PRIVATE_SERVER_CTL_MSG_SENDMESSAGE = PRIVATE_SERVER_CTL_MSG_BASE+0;
	
	// IQiYi-JNI
	public static final int PRIVATE_SERVER_SENDMESSAGE_RESULT = (PRIVATE_SERVER_CTL_MSG_BASE+0);
	public static final int PRIVATE_SERVER_NOTIFY_MSG = (PRIVATE_SERVER_CTL_MSG_BASE+1); 
	
	public static final String PRIVATE_SERVER_CMD_INTENT_NAME="com.mipt.platinum.privateserver.cmd.intent";
	public static final String PRIVATE_SERVER_CMD="com.mipt.platinum.privateserver.cmd";
	public static final String PRIVATE_SERVER_MESSAGE_PARAM="com.mipt.platinum.privateserver.message.param";
	/*----------------------------------------------------------------*/
	
	
	/*----------------------------------------------------------------*/	
	/*
	 * 		往JNI设置的命令
	 * 
	 * */
	public static final int MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_DURATION = (MEDIA_RENDER_CTL_MSG_BASE+0);
	public static final int MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_POSITION = (MEDIA_RENDER_CTL_MSG_BASE+1);
	public static final int MEDIA_RENDER_TOCONTRPOINT_SET_MEDIA_PLAYINGSTATE = (MEDIA_RENDER_CTL_MSG_BASE+2);
	/*----------------------------------------------------------------*/
	public static final String RENDERER_TOCONTRPOINT_CMD_INTENT_NAME="com.mipt.platinum.tocontrolpointer.cmd.intent";
	public static final String GET_RENDERER_TOCONTRPOINT_CMD="get_dlna_renderer_tocontrolpointer.cmd";
	public static final String GET_PARAM_MEDIA_DURATION="get_param_media_duration";
	public static final String GET_PARAM_MEDIA_POSITION="get_param_media_position";
	public static final String GET_PARAM_MEDIA_PLAYINGSTATE="get_param_media_playingstate";
	/*----------------------------------------------------------------*/
	
	//播放状态的宏字符串来自dlna协议，不可随意修改
    public static final String MEDIA_PLAYINGSTATE_STOP="STOPPED";
    public static final String MEDIA_PLAYINGSTATE_PAUSE="PAUSED_PLAYBACK";
    public static final String MEDIA_PLAYINGSTATE_PLAYING="PLAYING";
    public static final String MEDIA_PLAYINGSTATE_TRANSTION="TRANSITIONING";
    public static final String MEDIA_PLAYINGSTATE_NOMEDIA="NO_MEDIA_PRESENT";
    
    /*----------------------------------------------------------------*/
    public static final String MEDIA_SEEK_TIME_TYPE_REL_TIME="REL_TIME";	//时间格式为：00:00:00
    public static final String MEDIA_SEEK_TIME_TYPE_TRACK_NR="TRACK_NR";
    
	
	public static interface ActionInflectCallback{
		public void onActionFromDMR(int cmd,String value,String data);
	}
	
	private static ActionInflectCallback mCallback;
	
	
	public static void onActionFromDMR(int cmd,String value,String data){
		if (mCallback != null){
			mCallback.onActionFromDMR(cmd, value, data);
		}
	}
	
	public static void setActionCallback(ActionInflectCallback callback){
		mCallback = callback;
	}
}
