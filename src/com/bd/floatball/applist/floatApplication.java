package com.bd.floatball.applist;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.StrictMode;
import android.util.Log;
import android.view.WindowManager;

public class floatApplication extends Application implements OnSharedPreferenceChangeListener{		

	private static final boolean DEBUG = false;
	private static final String TAG = floatApplication.class.getSimpleName();	
	private SharedPreferences prefs;	
	private static String selectPackName = null;
	private static int selectPid = 0;	
	private static int monitorpid = 0;
	private static String monitorpackname = null;
	public  static Process monitorprocess = null;	
	public final int selecteditembg = Color.GREEN;
	public final int runningitembg = Color.GRAY;	
	private static PackageManager packmg = null;
	private static List<ApplicationInfo> allapplist = null;
	private static ArrayList<String> allappname = null;
	private static ArrayList<Drawable> allappicon = null;
	
	private WindowManager.LayoutParams wmParams;
	public WindowManager.LayoutParams getMywmParams_float(){
		wmParams = new WindowManager.LayoutParams();
		return wmParams;
	}
	
	public WindowManager.LayoutParams getMywmParams_top(){
		wmParams = new WindowManager.LayoutParams();
		return wmParams;
	}

	public synchronized boolean initPackageManager(){		
		if(allappname == null) {
			allappname = new ArrayList<String>();
		}		

		if(allappicon == null) {
			allappicon = new ArrayList<Drawable>();
		}		
		
		if(allapplist == null){
			packmg = this.getApplicationContext().getPackageManager();
			if(packmg == null){
				Log.d(TAG,"FATAL, can not get PackageManager");
				return false;
			}
			
			allapplist = packmg.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
			//pm.getInstalledPackages(0);			
			if(allapplist == null){
				Log.d(TAG,"FATAL, getInstalledPackages failed");
				return false;
			}					
			
			for(ApplicationInfo e: allapplist){
					allappname.add(e.processName);
					allappicon.add(e.loadIcon(packmg));
			}
		}			
		return true;
	}
	
	private Drawable NewAppIconByName(String appname){
		packmg = this.getApplicationContext().getPackageManager();
		if(packmg == null){
			Log.d(TAG,"FATAL, can not get PackageManager");
			return null;
		}
		try {
			Drawable newicon = packmg.getApplicationIcon(appname);
			if(newicon != null){
				allappname.add(appname);
				allappicon.add(newicon);
			}			
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			Log.d(TAG,"NewAppIconByName() failed");
			return null;
		}
		return allappicon.get(allappname.indexOf(appname));
	}
		
	public Drawable getAppIconByName(String appname){

		if(!initPackageManager()) return null;
		
		int index = allappname.indexOf(appname);
		if(index < 0) return NewAppIconByName(appname);		
		return allappicon.get(index);
	}

	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		if (DEBUG) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectAll().penaltyLog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectAll().penaltyLog().penaltyDeath().build());
		}
		//this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//this.prefs.registerOnSharedPreferenceChangeListener(this);
		Log.d(TAG,"onCreate");
	}

	/* (non-Javadoc)
	 * @see android.app.Application#onTerminate()
	 */
	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.d(TAG,"onTerminate");
	}	
	
	private synchronized void ReadPrefs()
	{
		selectPackName = this.prefs.getString("dstappname", "");
		selectPid = Integer.parseInt(this.prefs.getString("dstapppid", "0"));	
	}
	
	/* (non-Javadoc)
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		ReadPrefs();
	}
	
	public String getSelectPackName()
	{
		return selectPackName;
	}

	public int getSelectPid()
	{
		return selectPid;
	}
	
	public synchronized boolean setSelectApp(String appname, int pid)
	{
		selectPackName = appname;
		selectPid = pid;
		return true;
	}
	
	public synchronized boolean resetSelectApp()
	{
		selectPackName = null;
		selectPid = 0;
		return true;
	}
	
	public String getMonitorPackName()
	{
		return monitorpackname;
	}

	public int getMonitorPid()
	{
		return monitorpid;
	}
	
	public synchronized boolean setMonitorApp(String appname, int pid, Process pro)
	{
		monitorpackname = appname;
		monitorpid = pid;
		startMonitorProcess(pro);
		return true;
	}
	
	public synchronized boolean resetMonitorApp()
	{
		monitorpackname = null;
		monitorpid = 0;
		stopMonitorProcess();
		return true;
	}
	
	private synchronized boolean startMonitorProcess(Process pro)
	{
		if(monitorprocess != pro)
		{
			if(monitorprocess != null){
				monitorprocess.destroy();
				Log.d(TAG,"stop old process, start new process");
			}
			monitorprocess = pro;
			Log.d(TAG,"start new process");
		}

		return true;
	}
	
	private synchronized boolean stopMonitorProcess()
	{
		if(monitorprocess != null)
		{
			monitorprocess.destroy();
			monitorprocess = null;
			Log.d(TAG,"stop old process");
		}
		return true;
	}
}
