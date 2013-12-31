package com.geniusgithub.mediarender.music;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.geniusgithub.mediarender.BaseActivity;
import com.geniusgithub.mediarender.R;
import com.geniusgithub.mediarender.center.DLNAGenaEventBrocastFactory;
import com.geniusgithub.mediarender.center.DlnaMediaModel;
import com.geniusgithub.mediarender.center.DlnaMediaModelFactory;
import com.geniusgithub.mediarender.center.MediaControlBrocastFactory;
import com.geniusgithub.mediarender.music.lrc.LrcDownLoadHelper;
import com.geniusgithub.mediarender.music.lrc.LyricView;
import com.geniusgithub.mediarender.music.lrc.MusicUtils;
import com.geniusgithub.mediarender.player.AbstractTimer;
import com.geniusgithub.mediarender.player.CheckDelayTimer;
import com.geniusgithub.mediarender.player.MusicPlayEngineImpl;
import com.geniusgithub.mediarender.player.PlayerEngineListener;
import com.geniusgithub.mediarender.player.SingleSecondTimer;
import com.geniusgithub.mediarender.util.CommonLog;
import com.geniusgithub.mediarender.util.CommonUtil;
import com.geniusgithub.mediarender.util.DlnaUtils;
import com.geniusgithub.mediarender.util.LogFactory;

public class MusicActivity extends BaseActivity implements MediaControlBrocastFactory.IMediaControlListener,
									OnBufferingUpdateListener, 
									OnSeekCompleteListener,
									OnErrorListener,
									LrcDownLoadHelper.ILRCDownLoadCallback{

	private static final CommonLog log = LogFactory.createLog();
	private final static int REFRESH_CURPOS = 0x0001;
	private final static int EXIT_ACTIVITY = 0x0003;
	private final static int REFRESH_SPEED = 0x0004;
	private final static int CHECK_DELAY = 0x0005;
	private final static int LOAD_DRAWABLE_COMPLETE = 0x0006;
	private final static int UPDATE_LRC_VIEW = 0x0007;
	
	private final static int EXIT_DELAY_TIME = 3000;
	

	private UIManager mUIManager;
	private MusicPlayEngineImpl mPlayerEngineImpl;
	private MusicPlayEngineListener mPlayEngineListener;
	private MediaControlBrocastFactory mMediaControlBorcastFactory;
	private LrcDownLoadHelper mLrcDownLoadHelper;
	
	private Context mContext;
	private DlnaMediaModel mMediaInfo = new DlnaMediaModel();	
	private Handler mHandler;
	
	private AbstractTimer mPlayPosTimer;
	private AbstractTimer mNetWorkTimer;
	private CheckDelayTimer mCheckDelayTimer;
	
	private boolean isDestroy = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		log.e("onCreate");
		setContentView(R.layout.music_player_layout);
		setupsView();	
		initData();
		
		refreshIntent(getIntent());
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		log.e("onNewIntent");
		refreshIntent(intent);

		super.onNewIntent(intent);
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		mUIManager.unInit();
		mPlayerEngineImpl.exit();
		mLrcDownLoadHelper.unInit();
		mCheckDelayTimer.stopTimer();
		mNetWorkTimer.stopTimer();
		mMediaControlBorcastFactory.unregister();
		mPlayPosTimer.stopTimer();

		finish();
	}

	@Override
	protected void onDestroy() {
		log.e("onDestroy");
		isDestroy = true;
		
		super.onDestroy();

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		
		int keyCode = event.getKeyCode();
		int keyAction = event.getAction();
		
		switch(keyCode){
			case KeyEvent.KEYCODE_MENU:
				if (keyAction == KeyEvent.ACTION_UP){
					if (mUIManager.isLRCViewShow()){
						mUIManager.showLRCView(false);
					}else{
						mUIManager.showLRCView(true);
					}
					return true;
				}
				break;
		}
		
		return super.dispatchKeyEvent(event);
	}
	
	public void setupsView()
	{
		mContext = this;
		mUIManager = new UIManager();
	}
	
	public void initData(){
		mPlayPosTimer = new SingleSecondTimer(this);
		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what)
				{
					case REFRESH_CURPOS:					
						refreshCurPos();
						mUIManager.refreshLyrc(mPlayerEngineImpl.getCurPosition());
						break;
					case EXIT_ACTIVITY:
						finish();
						break;
					case REFRESH_SPEED:
						refreshSpeed();
						break;
					case CHECK_DELAY:
						checkDelay();				
						break;
					case LOAD_DRAWABLE_COMPLETE:
						Object object = msg.obj;
						Drawable drawable = null;
						if (object != null){
							drawable = (Drawable) object;
						}
						onLoadDrawableComplete(drawable);
						break;
					case UPDATE_LRC_VIEW:
						mUIManager.updateLyricView(mMediaInfo);
						break;
				}
			}
			
		};
		
		mPlayPosTimer.setHandler(mHandler, REFRESH_CURPOS);
		
		mNetWorkTimer = new SingleSecondTimer(this);
		mNetWorkTimer.setHandler(mHandler, REFRESH_SPEED);
		mCheckDelayTimer = new CheckDelayTimer(this);
		mCheckDelayTimer.setHandler(mHandler, CHECK_DELAY);

		mPlayerEngineImpl = new MusicPlayEngineImpl(this);
		mPlayerEngineImpl.setOnBuffUpdateListener(this);
		mPlayerEngineImpl.setOnSeekCompleteListener(this);
		mPlayerEngineImpl.setDataCaptureListener(mUIManager);
		mPlayEngineListener = new MusicPlayEngineListener();
		mPlayerEngineImpl.setPlayerListener(mPlayEngineListener);
		
		mMediaControlBorcastFactory = new MediaControlBrocastFactory(mContext);
		mMediaControlBorcastFactory.register(this);
		
		mLrcDownLoadHelper = new LrcDownLoadHelper();
		mLrcDownLoadHelper.init();
		
		mNetWorkTimer.startTimer();
		mCheckDelayTimer.startTimer();
		
		mUIManager.showLRCView(false);
	}
	
	
	private void refreshIntent(Intent intent){
		log.e("refreshIntent");
		removeExitMessage();
		if (intent != null){
			mMediaInfo = DlnaMediaModelFactory.createFromIntent(intent);
		}
			

		mUIManager.updateMediaInfoView(mMediaInfo);
		mPlayerEngineImpl.playMedia(mMediaInfo);
		LoaderHelper.syncDownLoadDrawable(mMediaInfo.getAlbumUri(), mHandler, LOAD_DRAWABLE_COMPLETE);
		
		mUIManager.showPrepareLoadView(true);
		mUIManager.showLoadView(false);
		mUIManager.showControlView(false);
		
		boolean need = checkNeedDownLyric(mMediaInfo);
		log.e("checkNeedDownLyric need = " + need);
		if (need){
			mLrcDownLoadHelper.syncDownLoadLRC(mMediaInfo.getTitle(), mMediaInfo.getArtist(), this);
		}		
		mUIManager.updateLyricView(mMediaInfo);

	}	
	
	
	private void removeExitMessage(){
		mHandler.removeMessages(EXIT_ACTIVITY);
	}
	
	private void delayToExit(){
		log.e("delayToExit");
		removeExitMessage();
		mHandler.sendEmptyMessageDelayed(EXIT_ACTIVITY, EXIT_DELAY_TIME);
	}
	
	
	public void play()
	{
		mPlayerEngineImpl.play();
	}
	
	public void pause()
	{
		mPlayerEngineImpl.pause();
	}
	
	public void stop()
	{
		mPlayerEngineImpl.stop();
	}
	
	public void refreshCurPos(){
		int pos = mPlayerEngineImpl.getCurPosition();
	
		mUIManager.setSeekbarProgress(pos);
		DLNAGenaEventBrocastFactory.sendSeekEvent(mContext, pos);
	}
	
	
	
	public void refreshSpeed(){
		if (mUIManager.isLoadViewShow()){
			float speed = CommonUtil.getSysNetworkDownloadSpeed();
			mUIManager.setSpeed(speed);
		}
	}
	
	public void checkDelay(){
		int pos = mPlayerEngineImpl.getCurPosition();

		boolean ret = mCheckDelayTimer.isDelay(pos);
		if (ret){
			mUIManager.showLoadView(true);
		}else{
			mUIManager.showLoadView(false);
		}
		
		mCheckDelayTimer.setPos(pos);
		
	}
	
	public void onLoadDrawableComplete(Drawable drawable){
		if (isDestroy || drawable == null){
			return ;
		}
		
		mUIManager.updateAlbumPIC(drawable);
		
	}
	
	public void seek(int pos){
		isSeekComplete = false;
		mPlayerEngineImpl.skipTo(pos);
		mUIManager.setSeekbarProgress(pos);
		
	}

	private class MusicPlayEngineListener implements PlayerEngineListener
	{

		@Override
		public void onTrackPlay(DlnaMediaModel itemInfo) {
		
			mPlayPosTimer.startTimer();
			DLNAGenaEventBrocastFactory.sendPlayStateEvent(mContext);	
			mUIManager.showPlay(false);
			mUIManager.showPrepareLoadView(false);
			mUIManager.showControlView(true);
		}

		@Override
		public void onTrackStop(DlnaMediaModel itemInfo) {

			mPlayPosTimer.stopTimer();
			DLNAGenaEventBrocastFactory.sendStopStateEvent(mContext);
			mUIManager.showPlay(true);
			mUIManager.updateMediaInfoView(mMediaInfo);
			mUIManager.showLoadView(false);
			isSeekComplete = true;
			delayToExit();
		}

		@Override
		public void onTrackPause(DlnaMediaModel itemInfo) {
	
			mPlayPosTimer.stopTimer();
			DLNAGenaEventBrocastFactory.sendPauseStateEvent(mContext);
			mUIManager.showPlay(true);
		}

		@Override
		public void onTrackPrepareSync(DlnaMediaModel itemInfo) {

			mPlayPosTimer.stopTimer();
			DLNAGenaEventBrocastFactory.sendTranstionEvent(mContext);
		}

		@Override
		public void onTrackPrepareComplete(DlnaMediaModel itemInfo) {

			mPlayPosTimer.stopTimer();
			int duration = mPlayerEngineImpl.getDuration();
			DLNAGenaEventBrocastFactory.sendDurationEvent(mContext, duration);	
			mUIManager.setSeekbarMax(duration);
			mUIManager.setTotalTime(duration);
			
		}
		
		@Override
		public void onTrackStreamError(DlnaMediaModel itemInfo) {
			log.e("onTrackStreamError");
			mPlayPosTimer.stopTimer();		
			mPlayerEngineImpl.stop();		
			mUIManager.showPlayErrorTip();
		}

		@Override
		public void onTrackPlayComplete(DlnaMediaModel itemInfo) {
			log.e("onTrackPlayComplete");
			mPlayerEngineImpl.stop();
		}

	

	}
	
	
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
	//	log.e("onBufferingUpdate --> percen = " + percent + ", curPos = " + mp.getCurrentPosition());
	  
		int duration = mPlayerEngineImpl.getDuration();
		int time = duration * percent / 100;
		mUIManager.setSeekbarSecondProgress(time);
	}

	private boolean isSeekComplete = false;
	@Override
	public void onSeekComplete(MediaPlayer mp) {
		isSeekComplete = true;
		log.e("onSeekComplete ...");
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mUIManager.showPlayErrorTip();
		log.e("onError what = " + what + ", extra = " + extra);
		return false;
	}

	@Override
	public void onPlayCommand() {
		play();
	}

	@Override
	public void onPauseCommand() {
		pause();
	}

	@Override
	public void onStopCommand() {
		stop();
	}

	@Override
	public void onSeekCommand(int time) {
		log.e("onSeekCmd time = " + time);
		seek(time);
	}



	
	
	
	
	
	
	
	
	
	
	/*---------------------------------------------------------------------------*/
	class UIManager implements OnClickListener, OnSeekBarChangeListener, OnDataCaptureListener{
		
		public View mPrepareView;
		public TextView mTVPrepareSpeed;
		
		public View mLoadView;
		public TextView mTVLoadSpeed;
		
		public View mControlView;	
		public TextView mTVSongName;
		public TextView mTVArtist;
		public TextView mTVAlbum;
	
		public ImageButton mBtnPlay;
		public ImageButton mBtnPause;
		public SeekBar mSeekBar;
		public TextView mTVCurTime;
		public TextView mTVTotalTime;
		public VisualizerView mVisualizerView;
		public ImageView mIVAlbum; 
		
		public TranslateAnimation mHideDownTransformation;
		public AlphaAnimation mAlphaHideTransformation;
		
		public View mSongInfoView;
		public LyricView mLyricView;
		public boolean lrcShow = false;
		
		
		public UIManager(){
			initView();
		}

		public void initView(){
			
			mPrepareView = findViewById(R.id.prepare_panel);
			mTVPrepareSpeed = (TextView) findViewById(R.id.tv_prepare_speed);
			
			mLoadView = findViewById(R.id.loading_panel);
			mTVLoadSpeed = (TextView) findViewById(R.id.tv_speed);
			
			mControlView = findViewById(R.id.control_panel);	
			mTVSongName = (TextView) findViewById(R.id.tv_title);
			mTVArtist = (TextView) findViewById(R.id.tv_artist);
			mTVAlbum = (TextView) findViewById(R.id.tv_album);
			
			mBtnPlay = (ImageButton) findViewById(R.id.btn_play);
			mBtnPause = (ImageButton) findViewById(R.id.btn_pause);
			mBtnPlay.setOnClickListener(this);
			mBtnPause.setOnClickListener(this);	
			mSeekBar = (SeekBar) findViewById(R.id.playback_seeker);
			mTVCurTime = (TextView) findViewById(R.id.tv_curTime);
			mTVTotalTime = (TextView) findViewById(R.id.tv_totalTime);
			mVisualizerView = (VisualizerView) findViewById(R.id.mp_freq_view);
			mIVAlbum = (ImageView) findViewById(R.id.iv_album);
			setSeekbarListener(this);
			
	    	mSongInfoView = findViewById(R.id.song_info_view);
	    	mLyricView = (LyricView) findViewById(R.id.lrc_view);
		    
			mHideDownTransformation = new TranslateAnimation(0.0f, 0.0f,0.0f,200.0f);  
	    	mHideDownTransformation.setDuration(1000);
	    	
	    	mAlphaHideTransformation = new AlphaAnimation(1, 0);
	    	mAlphaHideTransformation.setDuration(1000);
	    	
	    	updateAlbumPIC(getResources().getDrawable(R.drawable.mp_music_default));
		}

		
		public void unInit(){
			
		}

		private  int DRAW_OFFSET_Y = 200;
		public void updateLyricView(DlnaMediaModel mMediaInfo) {
			log.e("updateLyricView song:" + mMediaInfo.getTitle() + ", artist:" + mMediaInfo.getArtist());

			mLyricView.read(mMediaInfo.getTitle(), mMediaInfo.getArtist());
			int pos = 0;
			pos = mPlayerEngineImpl.getCurPosition();
			refreshLyrc(pos);
		}
		
		public void refreshLyrc(int pos){	
			if (pos > 0) {
				mLyricView.setOffsetY(DRAW_OFFSET_Y - mLyricView.selectIndex(pos)
						* (mLyricView.getSIZEWORD() + LyricView.INTERVAL - 1));
			} else {
				mLyricView.setOffsetY(DRAW_OFFSET_Y);
			}
			mLyricView.invalidate();
		}
		
		public boolean isLRCViewShow(){
			return lrcShow;
		}
		
		
		public void showLRCView(boolean bshow){
			lrcShow = bshow;
			if (bshow){
				mLyricView.setVisibility(View.VISIBLE);
				mSongInfoView.setVisibility(View.GONE);
			}else{
				mLyricView.setVisibility(View.GONE);
				mSongInfoView.setVisibility(View.VISIBLE);
			}
		}
		
		public void updateAlbumPIC(Drawable drawable){
			Bitmap bitmap = ImageUtils.createRotateReflectedMap(mContext, drawable);
			if (bitmap != null){
				mIVAlbum.setImageBitmap(bitmap);
			}
		}
		
		public void showPrepareLoadView(boolean isShow){
			if (isShow){
				mPrepareView.setVisibility(View.VISIBLE);		
			}else{
				mPrepareView.setVisibility(View.GONE);
			}
		}
		
		public void showControlView(boolean show){
			if (show){
				mControlView.setVisibility(View.VISIBLE);
			}else{
				mControlView.setVisibility(View.GONE);
			}
			
		}
		
		public void showLoadView(boolean isShow){
			if (isShow){
				mLoadView.setVisibility(View.VISIBLE);
			}else{
				if (mLoadView.isShown()){
					mLoadView.startAnimation(mAlphaHideTransformation);
					mLoadView.setVisibility(View.GONE);
				}
			}
		}
		
		private boolean isSeekbarTouch = false;	

		@Override
		public void onClick(View v) {

			switch(v.getId())
			{
				case R.id.btn_play:
					play();
					break;
				case R.id.btn_pause:
					pause();
					break;
			}
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
	
			mUIManager.setcurTime(progress);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			isSeekbarTouch = true;
		
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			isSeekbarTouch = false;			
			seek(seekBar.getProgress());
		}
		
		
		public void showPlay(boolean bShow)
		{
			if (bShow)
			{
				mBtnPlay.setVisibility(View.VISIBLE);
				mBtnPause.setVisibility(View.INVISIBLE);
			}else{
				mBtnPlay.setVisibility(View.INVISIBLE);
				mBtnPause.setVisibility(View.VISIBLE);
			}
		}
		
		public void togglePlayPause(){
			if (mBtnPlay.isShown()){
				play();
			}else{
				pause();
			}
		}
		
		public void setSeekbarProgress(int time)
		{
			if (!isSeekbarTouch)
			{
				mSeekBar.setProgress(time);	
			}
		}
		
		public void setSeekbarSecondProgress(int time)
		{
			mSeekBar.setSecondaryProgress(time);	
		}
		
		public void setSeekbarMax(int max){
			mSeekBar.setMax(max);
		}
		
		public void setcurTime(int curTime){
			String timeString = DlnaUtils.formateTime(curTime);
			mTVCurTime.setText(timeString);
		}
		
		public void setTotalTime(int totalTime){
			String timeString = DlnaUtils.formateTime(totalTime);
			mTVTotalTime.setText(timeString);
		}
		
		public void updateMediaInfoView(DlnaMediaModel mediaInfo){
			setcurTime(0);
			setTotalTime(0);
			setSeekbarMax(100);
			setSeekbarProgress(0);
	
			mTVSongName.setText(mediaInfo.getTitle());
			mTVArtist.setText(mediaInfo.getArtist());
			mTVAlbum.setText(mediaInfo.getAlbum());
		}
		
		public void setSpeed(float speed){
			String showString = (int)speed + "KB/" + getResources().getString(R.string.second);
			mTVPrepareSpeed.setText(showString);
			mTVLoadSpeed.setText(showString);
		}
		

		public void setSeekbarListener(OnSeekBarChangeListener listener)
		{
			mSeekBar.setOnSeekBarChangeListener(listener);
		}

		public boolean isControlViewShow(){
			return mControlView.getVisibility() == View.VISIBLE ? true : false;
		}
		
		public boolean isLoadViewShow(){
			if (mLoadView.getVisibility() == View.VISIBLE || 
					mPrepareView.getVisibility() == View.VISIBLE){
				return true;
			}
			
			return false;
		}
		
		public void showPlayErrorTip(){
			Toast.makeText(MusicActivity.this, R.string.toast_musicplay_fail, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onFftDataCapture(Visualizer visualizer, byte[] fft,
				int samplingRate) {
			mVisualizerView.updateVisualizer(fft);		
		}

		@Override
		public void onWaveFormDataCapture(Visualizer visualizer,
				byte[] waveform, int samplingRate) {
			mVisualizerView.updateVisualizer(waveform);
		}
	}

	private boolean checkNeedDownLyric(DlnaMediaModel mediaInfo) {
		String lyricPath = MusicUtils.getLyricFile(mediaInfo.getTitle(), mediaInfo.getArtist());
		if (lyricPath != null) {
			File f = new File(lyricPath);
			if (f.exists()) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public void lrcDownLoadComplete(boolean isSuccess, String song, String artist) {

		if (isSuccess && song.equals(mMediaInfo.getTitle()) && artist.equals(mMediaInfo.getArtist())){
			Message msg = mHandler.obtainMessage(UPDATE_LRC_VIEW);
			msg.sendToTarget();
		}
	}

}
