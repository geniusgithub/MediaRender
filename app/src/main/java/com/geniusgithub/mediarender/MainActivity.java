package com.geniusgithub.mediarender;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.geniusgithub.mediarender.center.MediaRenderProxy;
import com.geniusgithub.mediarender.datastore.LocalConfigSharePreference;
import com.geniusgithub.mediarender.util.CommonLog;
import com.geniusgithub.mediarender.util.DlnaUtils;
import com.geniusgithub.mediarender.util.LogFactory;


/**
 * @author lance
 * @csdn  http://blog.csdn.net/geniuseoe2012
 * @github https://github.com/geniusgithub
 */
public class MainActivity extends BaseActivity implements OnClickListener, DeviceUpdateBrocastFactory.IDevUpdateListener{

private static final CommonLog log = LogFactory.createLog();
	
	private Button mBtnStart;
	private Button mBtnReset;
	private Button mBtnStop;
	
	private Button mBtnEditName;
	private EditText mETName;
	private TextView mTVDevInfo;


	private MediaRenderProxy mRenderProxy;
	private RenderApplication mApplication;
	private DeviceUpdateBrocastFactory mBrocastFactory;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		setupView();
		initData();
	}


	
	
	@Override
	protected void onDestroy() {
		unInitData();	
		super.onDestroy();
	}




	private void setupView(){
		mBtnStart = (Button) findViewById(R.id.btn_init);
    	mBtnReset = (Button) findViewById(R.id.btn_reset);
    	mBtnStop = (Button) findViewById(R.id.btn_exit);
    	mBtnEditName = (Button) findViewById(R.id.bt_dev_name);
    	mBtnStart.setOnClickListener(this);
    	mBtnReset.setOnClickListener(this);
    	mBtnStop.setOnClickListener(this);
    	mBtnEditName.setOnClickListener(this);
    	
    	mTVDevInfo = (TextView) findViewById(R.id.tv_dev_info);
    	mETName = (EditText) findViewById(R.id.et_dev_name);
    	
	}
	
	private void initData(){
		mApplication = RenderApplication.getInstance();
		mRenderProxy = MediaRenderProxy.getInstance();
		mBrocastFactory = new DeviceUpdateBrocastFactory(this);
		
		String dev_name = DlnaUtils.getDevName(this);
		mETName.setText(dev_name);
		mETName.setEnabled(false);
		
		updateDevInfo(mApplication.getDevInfo());
		mBrocastFactory.register(this);
	}

	private void unInitData(){
		mBrocastFactory.unregister();
	}

	private void updateDevInfo(DeviceInfo object){
		String status = object.status ? "open" : "close";
		String text = "status: " + status + "\n" +
						"friend name: " + object.dev_name + "\n" + 
					   "uuid: " + object.uuid;
		mTVDevInfo.setText(text);
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
			finish();
			break;
		case R.id.bt_dev_name:
			change();
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
	
	private void change(){
		if (mETName.isEnabled()){
			mETName.setEnabled(false);
			DlnaUtils.setDevName(this, mETName.getText().toString());
		}else{
			mETName.setEnabled(true);
		}
	}



	@Override
	public void onUpdate() {
		updateDevInfo(mApplication.getDevInfo());
	}

}
