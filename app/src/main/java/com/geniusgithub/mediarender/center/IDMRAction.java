package com.geniusgithub.mediarender.center;


public interface IDMRAction {
	public void onRenderAvTransport(String value, String data);
	public void onRenderPlay(String value, String data);
	public void onRenderPause(String value, String data);
	public void onRenderStop(String value, String data);
	public void onRenderSeek(String value, String data);
	public void onRenderSetMute(String value, String data);
	public void onRenderSetVolume(String value, String data);
}

