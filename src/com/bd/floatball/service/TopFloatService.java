package com.bd.floatball.service;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bd.floatball.R;
import com.bd.floatball.applist.BrowseRunningAppActivity;
import com.bd.floatball.applist.MainActivity;
import com.bd.floatball.applist.exitSys;
import com.bd.floatball.applist.floatApplication;
import com.bd.floatball.logcat.LogActivity;

public class TopFloatService extends Service implements OnClickListener,
		OnKeyListener {
	WindowManager wm = null;
	WindowManager.LayoutParams ballWmParams = null;
	private View ballView;
	private View menuView;
	private float StartX;
	private float StartY;
	private float mTouchStartX;
	private float mTouchStartY;
	private float x;
	private float y;
	private RelativeLayout menuLayout;
	private Button floatImage;
	private PopupWindow pop;
	private LinearLayout menuTop;
	private Button m_ON;
	private Button m_OFF;
	private Button cut_Screen;
	@SuppressWarnings("unused")
	private Button speed;
	private Button APP;
	@SuppressWarnings("unused")
	private Button ViewALLPro;
	@SuppressWarnings("unused")
	private Button aboutInfo;
	private Button logcat;
	private Button shutdown;
	ArrayList<byte[]> memory = new ArrayList<byte[]>();

	@Override
	public void onCreate() {
		super.onCreate();
		// 加载辅助球布局
		ballView = LayoutInflater.from(this).inflate(R.layout.floatball, null);
		floatImage = (Button) ballView.findViewById(R.id.float_image);
		setUpFloatMenuView();
		createView();
		exitSys.getInstance().addService(this);
	}

	/**
	 * 窗口菜单初始化
	 */
	private void setUpFloatMenuView() {
		menuView = LayoutInflater.from(this).inflate(R.layout.floatmenu, null);
		menuLayout = (RelativeLayout) menuView.findViewById(R.id.menu);
		menuTop = (LinearLayout) menuView.findViewById(R.id.lay_main);
		m_ON = (Button) menuView.findViewById(R.id.Monitor_memON);
		m_OFF = (Button) menuView.findViewById(R.id.Monitor_memOFF);
		cut_Screen = (Button) menuView.findViewById(R.id.cut_Screen);
		speed = (Button) menuView.findViewById(R.id.edit_speed);
		APP = (Button) menuView.findViewById(R.id.ViewAllRunningPrograms);
		ViewALLPro = (Button) menuView.findViewById(R.id.ViewSystemProcessInformation);
        aboutInfo = (Button) menuView.findViewById(R.id.btn_ex2);
		logcat = (Button) menuView.findViewById(R.id.logcat);
		shutdown = (Button) menuView.findViewById(R.id.shutdown);
		menuLayout.setOnClickListener(this);
		menuLayout.setOnKeyListener(this);
		menuTop.setOnClickListener(this);

		m_ON.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent service = new Intent();
				service.setClass(TopFloatService.this, FloatService.class);
				startService(service);
				pop.dismiss();
				floatImage.setVisibility(0x00000000);
			}
		});

		m_OFF.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent serviceStop = new Intent();
				serviceStop.setClass(TopFloatService.this, FloatService.class);
				stopService(serviceStop);
				pop.dismiss();
				floatImage.setVisibility(0x00000000);
			}
		});

		APP.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(TopFloatService.this,BrowseRunningAppActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				pop.dismiss();
				floatImage.setVisibility(0x00000000);
			}
		});
		
		cut_Screen.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View v) {
//				pop.dismiss();
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.CHINA);
//				String fname = Environment.getExternalStorageDirectory().getPath() + sdf.format(new Date()) + ".png";
//				View view = v.getRootView();
//				view.setDrawingCacheEnabled(true);
//				view.buildDrawingCache();
//				Bitmap bitmap = view.getDrawingCache();
//				if (bitmap != null) {
//					Log.v(STORAGE_SERVICE, "bitmap got!");
//					try {
//						FileOutputStream out = new FileOutputStream(fname);
//						bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//						Log.v(STORAGE_SERVICE, "file" + fname + "output done.");
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				} else {
//					Log.v(STORAGE_SERVICE, "bitmap is NULL!");
//				}
//				floatImage.setVisibility(0x00000000);								
			}
		});
		
		logcat.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(TopFloatService.this,LogActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				pop.dismiss();
				floatImage.setVisibility(0x00000000);
			}
		});

        aboutInfo.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TopFloatService.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                pop.dismiss();
                floatImage.setVisibility(0x00000000);
            }
        });
		
		shutdown.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub	
				exitSys.getInstance().exit();
			}
		});

	}	 

	/**
	 * 通过MyApplication创建view，并初始化显示参数
	 */
	private void createView() {
		wm = (WindowManager) getApplicationContext().getSystemService("window");
		ballWmParams = ((floatApplication) getApplication()).getMywmParams_top();
		ballWmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		ballWmParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		ballWmParams.gravity = Gravity.LEFT | Gravity.TOP;
		ballWmParams.x = 0;
		ballWmParams.y = 0;
		ballWmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		ballWmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		ballWmParams.format = PixelFormat.RGBA_8888;
		// 添加显示层
		wm.addView(ballView, ballWmParams);

		// 注册触碰事件监听器
		floatImage.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				x = event.getRawX();
				y = event.getRawY();
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					StartX = x;
					StartY = y;
					mTouchStartX = (int) event.getX();
					mTouchStartY = (int) event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					updateViewPosition_();
					break;
				case MotionEvent.ACTION_UP:
					updateViewPosition_();
					mTouchStartX = mTouchStartY = 0;
					showView();
					break;
				}
				return true;
			}

		});
	}

	public void showView() {
		if (Math.abs(x - StartX) < 1.5 && Math.abs(y - StartY) < 1.5) {
			floatImage.setVisibility(0x00000004);
			DisplayMetrics dm = getResources().getDisplayMetrics();
			pop = new PopupWindow(menuView, dm.widthPixels, dm.heightPixels);
			pop.showAtLocation(ballView, Gravity.CENTER, 0, 0);
			pop.update();
		}
	}

	private void updateViewPosition_() {
		ballWmParams.x = (int) (x - mTouchStartX);
		ballWmParams.y = (int) (y - mTouchStartY);
		wm.updateViewLayout(ballView, ballWmParams);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@SuppressLint("ShowToast")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.lay_main:
			Toast.makeText(getApplicationContext(), "icee", 1000).show();
			break;

		default:
			if (pop != null && pop.isShowing()) {
				pop.dismiss();
				floatImage.setVisibility(0x00000000);
			}
			break;
		}

	}

	@SuppressLint("ShowToast")
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		Toast.makeText(getApplicationContext(), "keyCode:" + keyCode, 1000)
				.show();
		switch (keyCode) {
		case KeyEvent.KEYCODE_HOME:
			pop.dismiss();
			break;
		default:
			break;
		}
		return true;
	}

}
