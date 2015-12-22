package com.geniusgithub.mediarender.player;


public interface IBasePlayEngine {
	public void play();
	public void pause();
	public void stop();
	public void skipTo(int time);
}
