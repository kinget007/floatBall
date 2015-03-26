package com.bd.floatball.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.bd.floatball.R;
import com.bd.floatball.applist.BrowseRunningAppActivity;
import com.bd.floatball.applist.exitSys;
import com.bd.floatball.applist.floatApplication;

import java.util.ArrayList;

@SuppressLint("NewApi")
public class FloatService extends Service {
    public ArrayList<String> info = null;
    public ArrayList<String> info_index = null;
    Bundle bundle;
    Intent intentInfo;
	WindowManager wm = null;
	WindowManager.LayoutParams wmParams = null;
	View view;
	private float mTouchStartX;
	private float mTouchStartY;
	private float x;
	private float y;
	int state;
	Chronometer chronometer;
	TextView rss_tx;
	TextView pss_tx;
	TextView uss_tx;
	TextView cpu_tx;
	TextView pid_tx;
	ImageView iv;
	private float StartX;
	private float StartY;
	int delaytime=1000;
	Boolean flag  =  true;
	@Override
	public void onCreate() {
		Log.d("FloatService", "onCreate");
		super.onCreate();
		view = LayoutInflater.from(this).inflate(R.layout.floating, null);
		chronometer = (Chronometer)view.findViewById(R.id.chronometer);
		chronometer.setFormat("%s");
		chronometer.start();
		rss_tx = (TextView) view.findViewById(R.id.rss);
		pss_tx = (TextView) view.findViewById(R.id.pss);
		uss_tx = (TextView) view.findViewById(R.id.uss);
		cpu_tx = (TextView) view.findViewById(R.id.cpu);
		pid_tx = (TextView) view.findViewById(R.id.pid);

		rss_tx.setText("  "+"RSS:");
		pss_tx.setText("PSS");
		uss_tx.setText("USS");
		cpu_tx.setText("CPU");
		pid_tx.setText("PID");
		createView();
		handler.postDelayed(task, delaytime);
		exitSys.getInstance().addService(this);
	}

	private void createView() {
//		SharedPreferences shared = getSharedPreferences("float_flag",
//				Activity.MODE_PRIVATE);
//		SharedPreferences.Editor editor = shared.edit();
//		editor.putInt("float", 1);
//		editor.commit();
		wm = (WindowManager) getApplicationContext().getSystemService("window");
		wmParams = ((floatApplication)getApplication()).getMywmParams_float();
		wmParams.type = 2002;
		wmParams.flags |= 8;
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;
		  
		wmParams.x = 0;
		wmParams.y = 0;
		// set the height of floating
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.format = 1;
		
		wm.addView(view, wmParams);

		view.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				// get the position
				x = event.getRawX();
				y = event.getRawY(); // 25 is the height of status
				Log.i("currP", "currX" + x + "====currY" + y);
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					state = MotionEvent.ACTION_DOWN;
					StartX = x;
					StartY = y;
					
					mTouchStartX = event.getX();
					mTouchStartY = event.getY();
					Log.i("startP", "startX" + mTouchStartX + "====startY"
							+ mTouchStartY);
					break;
				case MotionEvent.ACTION_MOVE:
					state = MotionEvent.ACTION_MOVE;
					updateViewPosition();
					break;

				case MotionEvent.ACTION_UP:
					state = MotionEvent.ACTION_UP;
					upViewPosition();
					mTouchStartX = mTouchStartY = 0;
					showList();
					break;
				}
				return true;
			}
		});
	}

	public void showList() {		
		if (Math.abs(x - StartX) < 1.5 && Math.abs(y - StartY) < 1.5) {
			
			Intent intent = new Intent(FloatService.this,BrowseRunningAppActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent); 
        }
	}

	private Handler handler = new Handler();
	private Runnable task = new Runnable() {
		public void run() {
			// TODO Auto-generated method stub
			try {
				dataRefresh();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			handler.postDelayed(this, delaytime);
			wm.updateViewLayout(view, wmParams);
		}
	};

	public void dataRefresh(){
		if(BrowseRunningAppActivity.isStart_clickFlag() == true && !BrowseRunningAppActivity.info.equals(null)){
			rss_tx.setText("  "+"RSS:" + new java.text.DecimalFormat("#.00").format(Double.parseDouble(BrowseRunningAppActivity.info.get(BrowseRunningAppActivity.info_index.indexOf("RSS")))/1024));
			pss_tx.setText("PSS:" + new java.text.DecimalFormat("#.00").format(Double.parseDouble(BrowseRunningAppActivity.info.get(BrowseRunningAppActivity.info_index.indexOf("PSS")))/1024));
			uss_tx.setText("USS:"+ new java.text.DecimalFormat("#.00").format(Double.parseDouble(BrowseRunningAppActivity.info.get(BrowseRunningAppActivity.info_index.indexOf("USS")))/1024));
			cpu_tx.setText("CPU:"+ BrowseRunningAppActivity.info.get(BrowseRunningAppActivity.info_index.indexOf("CPU")));
			pid_tx.setText("PID:"+ BrowseRunningAppActivity.info.get(BrowseRunningAppActivity.info_index.indexOf("PID")));
		}
		else{
			rss_tx.setText("  "+"RSS: N/A" );
			pss_tx.setText("PSS: N/A" );
			uss_tx.setText("USS: N/A");
			cpu_tx.setText("CPU: N/A");
			pid_tx.setText("PID: N/A");
		}

	
	}

	private void updateViewPosition() {
		// updateViewPosition
		wmParams.x = (int) (x - mTouchStartX);
		wmParams.y = (int) (y - mTouchStartY);
		
		wm.updateViewLayout(view, wmParams);
	}
	
	private void upViewPosition() {
		if(x - StartX>0 && y-StartY > 0){
			wmParams.x = (int) 2000;
			wmParams.y = (int) 2000;
		}
		else if (x - StartX < 0 && y-StartY < 0 ){
			wmParams.x = (int) 0;
			wmParams.y = (int) 0;
		}
		else if (x - StartX < 0 && y-StartY > 0){
			wmParams.x = (int) 0;
			wmParams.y = (int) 2000;
		}
		else{
			wmParams.x = (int) 2000;
			wmParams.y = (int) 0;
		}
		wm.updateViewLayout(view, wmParams);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public void onStart(Intent intent, int startId) {
		Log.d("FloatService", "onStart");
		stopForeground(true);
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		handler.removeCallbacks(task);
		Log.d("FloatService", "onDestroy");
		wm.removeView(view);
		chronometer.stop();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}	
}
