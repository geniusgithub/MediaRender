package com.geniusgithub.mediarender.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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

import com.geniusgithub.mediarender.R;
import com.geniusgithub.mediarender.center.DLNAGenaEventBrocastFactory;
import com.geniusgithub.mediarender.center.DlnaMediaModel;
import com.geniusgithub.mediarender.center.DlnaMediaModelFactory;
import com.geniusgithub.mediarender.center.MediaControlBrocastFactory;
import com.geniusgithub.mediarender.player.AbstractTimer;
import com.geniusgithub.mediarender.player.CheckDelayTimer;
import com.geniusgithub.mediarender.player.SingleSecondTimer;
import com.geniusgithub.mediarender.player.PlayerEngineListener;
import com.geniusgithub.mediarender.player.VideoPlayEngineImpl;
import com.geniusgithub.mediarender.util.CommonLog;
import com.geniusgithub.mediarender.util.CommonUtil;
import com.geniusgithub.mediarender.util.DlnaUtils;
import com.geniusgithub.mediarender.util.LogFactory;

public class VideoActivity extends Activity implements MediaControlBrocastFactory.IMediaControlListener,
													OnBufferingUpdateListener, OnSeekCompleteListener, OnErrorListener{

	private static final CommonLog log = LogFactory.createLog();
	private final static int REFRESH_CURPOS = 0x0001;
	private final static int HIDE_TOOL = 0x0002;
	private final static int EXIT_ACTIVITY = 0x0003;
	private final static int REFRESH_SPEED = 0x0004;
	private final static int CHECK_DELAY = 0x0005;
	
	
	private final static int EXIT_DELAY_TIME = 2000;
	private final static int HIDE_DELAY_TIME = 3000;
	

	private UIManager mUIManager;
	private VideoPlayEngineImpl mPlayerEngineImpl;
	private VideoPlayEngineListener mPlayEngineListener;
	private MediaControlBrocastFactory mMediaControlBorcastFactory;
	private TranslateAnimation mHideDownTransformation;
	private TranslateAnimation mHideUpTransformation;
	private AlphaAnimation mAlphaHideTransformation;
	
	
	private DlnaMediaModel mMediaInfo = new DlnaMediaModel();	
	private Handler mHandler;
	private AbstractTimer mPlayPosTimer;
	private Context mContext;
	
	private AbstractTimer mNetWorkTimer;
	private CheckDelayTimer mCheckDelayTimer;
	
	private boolean isSurfaceCreate = false;
	private boolean isDestroy = false;
	
	private boolean isPause = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		log.e("onCreate");
		setContentView(R.layout.video_player_layout);
		initView();	
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
	protected void onResume() {	
		super.onResume();			
		
		log.e("VideoPlayerActivity onResume");
		if (isPause){
			isPause = false;
			play();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();	
		
		log.e("VideoPlayerActivity onPause");
		isPause = true;
		pause();
	}

	
	
	@Override
	protected void onStop() {
		super.onStop();
		
		log.e("VideoPlayerActivity onStop");
		
		onDestroy();
	}

	@Override
	protected void onDestroy() {
		log.e("onDestroy");
		isDestroy = true;
		mUIManager.unInit();
		mCheckDelayTimer.stopTimer();
		mNetWorkTimer.stopTimer();
		mMediaControlBorcastFactory.unregister();
		mPlayPosTimer.stopTimer();
		mPlayerEngineImpl.exit();
		super.onDestroy();

	}

	public void initView()
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
						break;
					case HIDE_TOOL:
						if (!mPlayerEngineImpl.isPause()){
							mUIManager.showControlView(false);
						}
						break;
					case EXIT_ACTIVITY:
						finish();
						break;
					case REFRESH_SPEED:
						refreshSpeed();
						break;
					case CHECK_DELAY:
						if (!isPause){
							checkDelay();
						}						
						break;
				}
			}
			
		};
		
		mPlayPosTimer.setHandler(mHandler, REFRESH_CURPOS);
		
		mNetWorkTimer = new SingleSecondTimer(this);
		mNetWorkTimer.setHandler(mHandler, REFRESH_SPEED);
		mCheckDelayTimer = new CheckDelayTimer(this);
		mCheckDelayTimer.setHandler(mHandler, CHECK_DELAY);

		mPlayerEngineImpl = new VideoPlayEngineImpl(this, mUIManager.holder);
		mPlayerEngineImpl.setOnBuffUpdateListener(this);
		mPlayerEngineImpl.setOnSeekCompleteListener(this);
		mPlayEngineListener = new VideoPlayEngineListener();
		mPlayerEngineImpl.setPlayerListener(mPlayEngineListener);
		
		mMediaControlBorcastFactory = new MediaControlBrocastFactory(mContext);
		mMediaControlBorcastFactory.register(this);
		
		mNetWorkTimer.startTimer();
		mCheckDelayTimer.startTimer();
		
		mHideDownTransformation = new TranslateAnimation(0.0f, 0.0f,0.0f,200.0f);  
    	mHideDownTransformation.setDuration(1000);
    	
    	mAlphaHideTransformation = new AlphaAnimation(1, 0);
    	mAlphaHideTransformation.setDuration(1000);
    	
    	mHideUpTransformation = new TranslateAnimation(0.0f, 0.0f,0.0f,-124.0f);
    	mHideUpTransformation.setDuration(1000);
	}
	
	
	private void refreshIntent(Intent intent){
		removeExitMessage();
		if (intent != null){
			mMediaInfo = DlnaMediaModelFactory.createFromIntent(intent);
		}

		mUIManager.updateMediaInfoView(mMediaInfo);
		if (isSurfaceCreate){
			mPlayerEngineImpl.playMedia(mMediaInfo);
		}else{
			delayToPlayMedia(mMediaInfo);
		}
		
		mUIManager.showPrepareLoadView(true);
		mUIManager.showLoadView(false);
		mUIManager.showControlView(false);
	}	

	private void delayToPlayMedia(final DlnaMediaModel mMediaInfo){
	
		log.e("delayToPlayMedia");
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if (!isDestroy){
					mPlayerEngineImpl.playMedia(mMediaInfo);
				}else{
					log.e("activity destroy...so don't playMedia...");
				}
			}
		}, 1000);
	}
	
	private void removeHideMessage(){
		mHandler.removeMessages(HIDE_TOOL);
	}
	
	private void delayToHideControlPanel(){
	//	log.e("delayToHideControlPanel");
		removeHideMessage();
		mHandler.sendEmptyMessageDelayed(HIDE_TOOL, HIDE_DELAY_TIME);
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
		log.e("play");
		mPlayerEngineImpl.play();
	}
	
	public void pause()
	{
		log.e("pause");
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
	//	log.e("checkDelay pos = " + pos);
		boolean ret = mCheckDelayTimer.isDelay(pos);
		if (ret){
			mUIManager.showLoadView(true);
		}else{
			mUIManager.showLoadView(false);
		}
		
		mCheckDelayTimer.setPos(pos);
		
	}
	
	public void seek(int pos){
		isSeekComplete = false;
		mPlayerEngineImpl.skipTo(pos);
		mUIManager.setSeekbarProgress(pos);
		
	}

	private class VideoPlayEngineListener implements PlayerEngineListener
	{

		@Override
		public void onTrackPlay(DlnaMediaModel itemInfo) {
		
			mPlayPosTimer.startTimer();
			DLNAGenaEventBrocastFactory.sendPlayStateEvent(mContext);	
			mUIManager.showPlay(false);
			mUIManager.showControlView(true);
		}

		@Override
		public void onTrackStop(DlnaMediaModel itemInfo) {
			log.e("onTrackStop");
			mPlayPosTimer.stopTimer();
			DLNAGenaEventBrocastFactory.sendStopStateEvent(mContext);
			mUIManager.showPlay(true);
			mUIManager.updateMediaInfoView(mMediaInfo);
			mUIManager.showControlView(true);
			mUIManager.showLoadView(false);
			isSeekComplete = true;
			delayToExit();
		}

		@Override
		public void onTrackPause(DlnaMediaModel itemInfo) {
			log.e("onTrackPause");
			mPlayPosTimer.stopTimer();
			DLNAGenaEventBrocastFactory.sendPauseStateEvent(mContext);
			mUIManager.showPlay(true);
			mUIManager.showControlView();
		}

		@Override
		public void onTrackPrepareSync(DlnaMediaModel itemInfo) {
			log.e("onTrackPrepareSync");
			mPlayPosTimer.stopTimer();
			DLNAGenaEventBrocastFactory.sendTranstionEvent(mContext);
		}

		@Override
		public void onTrackPrepareComplete(DlnaMediaModel itemInfo) {
			log.e("onTrackPrepareComplete");
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
	


	
	class UIManager implements OnClickListener, SurfaceHolder.Callback, OnSeekBarChangeListener{
		
		public View mPrepareView;
		public TextView mTVPrepareSpeed;
		
		public View mLoadView;
		public TextView mTVLoadSpeed;
		
		public View mControlView;	
		public View mToolView;
		public ImageView mImageViewIcon;
		public ImageButton mBtnPlay;
		public ImageButton mBtnPause;
		public SeekBar mSeekBar;
		public TextView mTVCurTime;
		public TextView mTVTotalTime;

		
		public ImageView mImageViewLoadProgress;
		public ImageView mImageViewPreProgress;
		
		
		private SurfaceView mSurfaceView;
		private SurfaceHolder holder = null;  
		
		
		public UIManager(){
			initView();
		}

		public void initView(){
			
			mPrepareView = findViewById(R.id.prepare_panel);
			mTVPrepareSpeed = (TextView) findViewById(R.id.tv_prepare_speed);
			
			mLoadView = findViewById(R.id.loading_panel);
			mTVLoadSpeed = (TextView) findViewById(R.id.tv_speed);
			
			mControlView = findViewById(R.id.control_panel);		
			mToolView = findViewById(R.id.toolview);
			mImageViewIcon = (ImageView) findViewById(R.id.iv_icon);
			
			mBtnPlay = (ImageButton) findViewById(R.id.btn_play);
			mBtnPause = (ImageButton) findViewById(R.id.btn_pause);
			mBtnPlay.setOnClickListener(this);
			mBtnPause.setOnClickListener(this);	
			mSeekBar = (SeekBar) findViewById(R.id.playback_seeker);
			mTVCurTime = (TextView) findViewById(R.id.tv_curTime);
			mTVTotalTime = (TextView) findViewById(R.id.tv_totalTime);
			
			setSeekbarListener(this);
			
			mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
			holder = mSurfaceView.getHolder();
		    holder.addCallback(this);  
		    holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		    

		 
		}

		
		public void unInit(){
			
		}
		
		public void showPrepareLoadView(boolean isShow){
			if (isShow){
				mPrepareView.setVisibility(View.VISIBLE);
			}else{
				mPrepareView.setVisibility(View.GONE);
			}
		}
		
		public void showControlView(boolean isShow){
			if (isShow){
				mImageViewIcon.setVisibility(View.VISIBLE);
				mToolView.setVisibility(View.VISIBLE);
			//	mControlView.setVisibility(View.VISIBLE);
				mPrepareView.setVisibility(View.GONE);
				delayToHideControlPanel();
			}else{
				if (mToolView.isShown()){
					mImageViewIcon.setVisibility(View.GONE);				
					//mControlView.startAnimation(mHideDownTransformation);
					mToolView.startAnimation(mHideDownTransformation);
					mToolView.setVisibility(View.GONE);
					//mControlView.setVisibility(View.GONE);
				}
			}
		}
		
		public void showControlView(){
			removeHideMessage();
	//		mControlView.setVisibility(View.VISIBLE);
			mImageViewIcon.setVisibility(View.VISIBLE);
			mToolView.setVisibility(View.VISIBLE);
		}
		
		public void showLoadView(boolean isShow){
			if (isShow){
				mLoadView.setVisibility(View.VISIBLE);
			}else{
				if (mLoadView.isShown()){
					//mLoadView.startAnimation(mHideUpTransformation);
					mLoadView.startAnimation(mAlphaHideTransformation);
					mLoadView.setVisibility(View.GONE);
				}
			}
		}
		
		private boolean isSeekbarTouch = false;	
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			log.e("surfaceCreated...");
			isSurfaceCreate = true;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			log.e("surfaceChanged  width = " + width + ", height = "  + height);
			
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			log.e("surfaceDestroyed");
			isSurfaceCreate = false;
		}

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
			mUIManager.showControlView(true);
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
			if (!isSeekbarTouch && !isDrag)
			{
			//	log.e("setSeekbarProgress time = " + time);
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
			//return mControlView.getVisibility() == View.VISIBLE ? true : false;
			return mToolView.getVisibility() == View.VISIBLE ? true : false;
		}
		
		public boolean isLoadViewShow(){
			if (mLoadView.getVisibility() == View.VISIBLE || 
					mPrepareView.getVisibility() == View.VISIBLE){
				return true;
			}
			
			return false;
		}
		
		public void showPlayErrorTip(){
			Toast.makeText(VideoActivity.this, R.string.toast_play_fail, Toast.LENGTH_SHORT).show();
		}
	}


	
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		int action = ev.getAction();
		int actionIdx = ev.getActionIndex();
		int actionMask = ev.getActionMasked();
		
	//	Log.d(TAG, "dispatchTouchEvent. action: "+action+", idx: "+actionIdx +", mask: "+actionMask);
		
		if(actionIdx == 0 && action == MotionEvent.ACTION_UP) {
			if(!mUIManager.isControlViewShow()) {	
				mUIManager.showControlView(true);	
				return true;
			}
		}
		
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		
		int keyCode = event.getKeyCode();
		int keyAction = event.getAction();
		
		switch(keyCode){
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			onTransdelCenter(keyAction);
			return true;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			onTransdelLeftRight(event, keyAction, true);
			return true;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			onTransdelLeftRight(event, keyAction, false);
			return true;
//		case KeyEvent.KEYCODE_MENU:
//			if(keyAction == KeyEvent.ACTION_UP){
//				startDialogActivity();
//			}
//			return true;
			default:
				break;
		}

		return super.dispatchKeyEvent(event);
	}
	
	
	private void onTransdelCenter(int keyAction){	
				switch (keyAction) {
				case KeyEvent.ACTION_UP:
					{
						if (!mUIManager.isControlViewShow())
						{
							mUIManager.showControlView(true);
							return ;
						}
						mUIManager.togglePlayPause();
					}
					break;
		
				default:
					break;
				}
		}
		
	
		private final static int INIT_SPEED = 10 * 1000;
		private final static int INIT_COUNT = 5;		
		private final static int SPEED_ACCELERATION = 2000;
		private   int speed = 0;
		private   int down_count = 0;
		
		private boolean isDrag = false;
		private int lastPos = 0;
		private void onTransdelLeftRight(KeyEvent event, int keyAction, boolean isLeft){
			mUIManager.showControlView(true);
			switch(keyAction){
			case KeyEvent.ACTION_DOWN:
			{
				isDrag = true;			
				if (down_count < INIT_COUNT){
					speed = INIT_SPEED;

				}else{
					speed = INIT_SPEED + (down_count - INIT_COUNT + 1) * SPEED_ACCELERATION;
				}
				down_count++;
				
				if (lastPos == 0){
					lastPos = mPlayerEngineImpl.getCurPosition();
				}					
		
				if(isLeft){
					lastPos -= speed;
				}else{
					lastPos += speed;
				}
			
		//		log.e("speed = " + speed);
				
				mUIManager.mSeekBar.setProgress(lastPos);
			}
				break;
			case KeyEvent.ACTION_UP:
			{
				log.e("ACTION_UP lastPos = " + lastPos);
				isDrag = false;
				seek(lastPos);
				lastPos = 0;
				speed = 0;
				down_count = 0;
			}
				break;
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
		log.e("onPlayCmd");
		if (!isPause){
			play();
		}
	}

	@Override
	public void onPauseCommand() {
		log.e("onPauseCmd");
		if (!isPause){
			pause();
		}
	}

	@Override
	public void onStopCommand() {
		log.e("onStopCmd");
		stop();
	}

	@Override
	public void onSeekCommand(int time) {
		log.e("onSeekCmd time = " + time);
		mUIManager.showControlView(true);
		seek(time);
	}

}
