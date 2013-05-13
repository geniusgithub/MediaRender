package com.geniusgithub.mediarender;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.geniusgithub.mediarender.center.MediaRenderProxy;
import com.geniusgithub.mediarender.util.CommonLog;
import com.geniusgithub.mediarender.util.LogFactory;


/**
 * @author lance
 * @csdn  http://blog.csdn.net/geniuseoe2012
 * @github https://github.com/geniusgithub
 */
public class MainActivity extends Activity implements OnClickListener{

private static final CommonLog log = LogFactory.createLog();
	
	private Button mBtnStart;
	private Button mBtnReset;
	private Button mBtnStop;

	private MediaRenderProxy mRenderProxy;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		setupView();
		initData();
	}


	
	private void setupView(){
		mBtnStart = (Button) findViewById(R.id.btn_init);
    	mBtnReset = (Button) findViewById(R.id.btn_reset);
    	mBtnStop = (Button) findViewById(R.id.btn_exit);
	
    	mBtnStart.setOnClickListener(this);
    	mBtnReset.setOnClickListener(this);
    	mBtnStop.setOnClickListener(this);
	}
	
	private void initData(){
		mRenderProxy = MediaRenderProxy.getInstance();
	}



	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btn_init:
			start();
			break;
		case R.id.btn_reset:
			reset();
			break;
		case R.id.btn_exit:
			stop();
			break;
		}
	}
	
	
	private void start(){
		mRenderProxy.startEngine();
	}
	
	private void reset(){
		mRenderProxy.restartEngine();
	}
	
	private void stop(){
		mRenderProxy.stopEngine();
	}

}
