package com.geniusgithub.mediarender.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.geniusgithub.mediarender.BaseActivity;
import com.geniusgithub.mediarender.R;
import com.geniusgithub.mediarender.center.DlnaMediaModel;
import com.geniusgithub.mediarender.center.DlnaMediaModelFactory;
import com.geniusgithub.mediarender.center.MediaControlBrocastFactory;
import com.geniusgithub.mediarender.player.AbstractTimer;
import com.geniusgithub.mediarender.player.SingleSecondTimer;
import com.geniusgithub.mediarender.util.CommonLog;
import com.geniusgithub.mediarender.util.CommonUtil;
import com.geniusgithub.mediarender.util.FileHelper;
import com.geniusgithub.mediarender.util.LogFactory;

public class ImageActivity extends BaseActivity  implements MediaControlBrocastFactory.IMediaControlListener,
													DownLoadHelper.IDownLoadCallback{

	private static final CommonLog log = LogFactory.createLog();
	
	private int mScreenWidth = 0;
	private int mScreenHeight = 0;
	
	private Handler mHandler;
	private UIManager mUIManager;
	private DownLoadHelper mDownLoadHelper;
	private DelCacheFileManager mDelCacheFileManager;
	
	private DlnaMediaModel mMediaInfo = new DlnaMediaModel();	
	private MediaControlBrocastFactory mMediaControlBorcastFactor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		log.e("onCreate");
		
		setContentView(R.layout.image_player_layout);
		
		initView();
		initData();
		refreshIntent(getIntent());
	}	
	
	@Override
	protected void onDestroy() {
		log.e("onDestroy");

		mMediaControlBorcastFactor.unregister();
		mDownLoadHelper.unInit();
		mDelCacheFileManager.start(FileManager.getSaveRootDir());
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		refreshIntent(intent);
	}

	private void initView(){
		mUIManager = new UIManager();
	}
	
	
	private static final int REFRESH_SPEED= 0x0001;
	private static final int EXIT_ACTIVITY = 0x0002;
	
	private void initData(){
		mScreenWidth = CommonUtil.getScreenWidth(this);
		mScreenHeight = CommonUtil.getScreenHeight(this);

		
		mMediaControlBorcastFactor = new MediaControlBrocastFactory(this);
		mMediaControlBorcastFactor.register(this);
		
		mDownLoadHelper = new DownLoadHelper();
		mDownLoadHelper.init();
		
		mDelCacheFileManager = new DelCacheFileManager();
		
		mHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				switch(msg.what){
				case EXIT_ACTIVITY:
					finish();
					break;
				}
			}
			
		};
		
	}
	
	
	private void refreshIntent(Intent intent){
		removeExitMessage();
		if (intent != null){
			mMediaInfo = DlnaMediaModelFactory.createFromIntent(intent);
		}

		
		String requesUrl = mMediaInfo.getUrl();
		String saveUri = FileManager.getSaveFullPath(requesUrl);
		if (null == saveUri || saveUri.length() < 1) {
			return ;
		}

		mUIManager.showProgress(true);
		mDownLoadHelper.syncDownLoadFile(mMediaInfo.getUrl(), FileManager.getSaveFullPath(requesUrl), this);
	}
	
	
	private void removeExitMessage(){
		mHandler.removeMessages(EXIT_ACTIVITY);
	}
	
	private static final int EXIT_DELAY_TIME = 2000;
	private void delayToExit(){
		removeExitMessage();
		mHandler.sendEmptyMessageDelayed(EXIT_ACTIVITY, EXIT_DELAY_TIME);
	}

	
	
	class UIManager{
		public ImageView mImageView;
		public View mLoadView;
		
		public Bitmap recycleBitmap;
		public boolean mIsScalBitmap = false;
		
		public UIManager(){
			initView();
		}
		
		
		private void initView(){
			mImageView = (ImageView) findViewById(R.id.imageview);
			mLoadView = findViewById(R.id.show_load_progress);
		}
		
		
		public void setBitmap(Bitmap bitmap){
			if (recycleBitmap != null && !recycleBitmap.isRecycled()) {
				mImageView.setImageBitmap(null);
				recycleBitmap.recycle();
				recycleBitmap = null;
			}
						
			if (mIsScalBitmap) {
				mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			} else {
				mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			}
						
			recycleBitmap = bitmap;
			mImageView.setImageBitmap(recycleBitmap);
		}
		
		public boolean isLoadViewShow(){
			if (mLoadView.getVisibility() == View.VISIBLE){
				return true;
			}
			
			return false;
		}
		
		public void showProgress(boolean bShow)
		{
			
			if (bShow){
				mLoadView.setVisibility(View.VISIBLE);
			} else{
				mLoadView.setVisibility(View.GONE);
			}

		}
		
		public void showLoadFailTip(){
			showToask(R.string.load_image_fail);
		}
		
		public void showParseFailTip(){
			showToask(R.string.parse_image_fail);
		}
		
		private void showToask(int tip) {
			Toast.makeText(ImageActivity.this, tip, Toast.LENGTH_SHORT).show();
		}
	}
	
	
	
	

	@Override
	public void downLoadResult(boolean isSuccess, String savePath) {
		
		onTransDelLoadResult(isSuccess, savePath);
	}


	private void onTransDelLoadResult(final boolean isSuccess,final String savePath){
		
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				mUIManager.showProgress(false);
				
				if (!isSuccess){
					mUIManager.showLoadFailTip();
					return ;
				}
				
				Bitmap bitmap = decodeOptionsFile(savePath);
				if (bitmap == null){
					mUIManager.showParseFailTip();
					return ;
				}
				
				mUIManager.setBitmap(bitmap);
			}
		});
		
		
	}
	
	
	
	public Bitmap decodeOptionsFile(String filePath) {
        try {
        	File file = new File(filePath);
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file),null,o);         
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale = 1;
            if (width_tmp <= mScreenWidth && height_tmp <= mScreenHeight)
            {
            	scale = 1;
            	mUIManager.mIsScalBitmap = false;
            }else{
            	double widthFit = width_tmp * 1.0 / mScreenWidth;
                double heightFit = height_tmp * 1.0 / mScreenHeight;
                double fit = widthFit > heightFit ? widthFit : heightFit; 
                scale = (int) (fit + 0.5);    
                mUIManager.mIsScalBitmap = true;
            }
            Bitmap bitmap = null;
            if(scale == 1)
            { 	
            	bitmap =  BitmapFactory.decodeStream(new FileInputStream(file));
            	if (bitmap != null){
            		log.e("scale = 1 bitmap.size = " + bitmap.getRowBytes() * bitmap.getHeight());
            	}         	
            }else{
            	BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, o2);
            	if (bitmap != null){
            		log.e("scale = " + o2.inSampleSize + " bitmap.size = " + bitmap.getRowBytes() * bitmap.getHeight());
            	}   
            }
            
            return bitmap;
            
        } catch (FileNotFoundException e) {
        	log.e("fileNotFoundException, e: " + e.toString());
        	  	
        }
        return null;
    }
	
	
	class DelCacheFileManager implements Runnable
	{
		private Thread mThread;
		private String mFilePath;
		
		public DelCacheFileManager()
		{
			
		}
		
		@Override
		public void run() {
			
			long time = System.currentTimeMillis();
			log.e("DelCacheFileManager run...");
			try {
				FileHelper.deleteDirectory(mFilePath);
			} catch (Exception e) {
				e.printStackTrace();
			}
			long interval = System.currentTimeMillis() - time;
			log.e("DelCacheFileManager del over, cost time = " + interval);
		}
		
		public boolean start(String directory)
		{		
			if (mThread != null)
			{
				if (mThread.isAlive())
				{
					return false;
				}			
			}
			mFilePath = directory;	
			mThread = new Thread(this);			
			mThread.start();	
			
			return true;
		}
		
	}





	@Override
	public void onPlayCommand() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPauseCommand() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopCommand() {
		log.e("onStopCommand");
		delayToExit();
	}

	@Override
	public void onSeekCommand(int time) {
		// TODO Auto-generated method stub
		
	}
	
}
