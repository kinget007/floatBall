package com.bd.floatball.applist;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.bd.floatball.R;
import com.bd.floatball.service.TopFloatService;

public class MainActivity extends Activity implements OnClickListener{
	
	private static String TAG = "AM_MEMORYIPROCESS" ;
	
	private ActivityManager mActivityManager = null ;
	
	private TextView tvAvailMem  ;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	

        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);
        setContentView(R.layout.main);
        
        Intent service = new Intent();
		service.setClass(MainActivity.this, TopFloatService.class);	
		service.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startService(service);
		
        tvAvailMem = (TextView)findViewById(R.id.tvAvailMemory) ;
        
        mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        String availMemStr = getSystemAvaialbeMemorySize();
        availMemStr = getSystemAvaialbeMemorySize();
        Log.i(TAG, "The Availabel Memory Size is"+availMemStr); 
        tvAvailMem.setText(availMemStr); 
		exitSys.getInstance().addActivity(this);
    }
    
    
    //获得系统可用内存信息
    private String getSystemAvaialbeMemorySize(){
    	//获得MemoryInfo对象
        MemoryInfo memoryInfo = new MemoryInfo() ;
       
//        获得系统可用内存，保存在MemoryInfo对象上
        mActivityManager.getMemoryInfo(memoryInfo) ;
        long memSize = memoryInfo.availMem ;
        
        //字符类型转换
        String availMemStr = formateFileSize(memSize);
        
        return availMemStr ;
    }
    
    @SuppressWarnings("unused")
	private void wastmem(ArrayList<byte[]> memory){
    	byte[] b = new byte[1024 * 1024 *10];
		memory.add(b);
		String s = getSystemAvaialbeMemorySize();
		Toast.makeText(MainActivity.this, "Memory use  "+memory.size() *10+" M" + s, Toast.LENGTH_SHORT).show();
		Log.i("Memory ", "Memory use  "+memory.size()*10 +" M" + s);
    }
  
    //调用系统函数，字符串转换 long -String KB/MB
    @SuppressLint("NewApi")
	private String formateFileSize(long size){
    	return Formatter.formatFileSize(MainActivity.this, size); 
    }


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}