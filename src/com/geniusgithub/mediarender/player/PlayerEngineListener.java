package com.geniusgithub.mediarender.player;

import com.geniusgithub.mediarender.center.DlnaMediaModel;



public interface PlayerEngineListener {
	
	public void onTrackPlay(DlnaMediaModel itemInfo); 

	public void onTrackStop(DlnaMediaModel itemInfo);
	
	public void onTrackPause(DlnaMediaModel itemInfo);	

	public void onTrackPrepareSync(DlnaMediaModel itemInfo);
	
	public void onTrackPrepareComplete(DlnaMediaModel itemInfo);
	
	public void onTrackStreamError(DlnaMediaModel itemInfo);
	
	public void onTrackPlayComplete(DlnaMediaModel itemInfo);
}
