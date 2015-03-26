package com.bd.floatball.applist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.bd.floatball.R;
import com.bd.floatball.mode.RunningAppInfo;
import com.bd.floatball.service.FloatService;

public class BrowseRunningAppActivity extends Activity {

	@SuppressWarnings("unused")
	private ActivityManager mActivityManager = null ;
	private static String TAG = "BrowseRunningAppActivity";
	private ListView listview = null;

	private List<RunningAppInfo> mlistAppInfo = null;
    
	private PackageManager pm;
	
	public static ArrayList<String> info = null;
	public static ArrayList<String> info_index = null;
	private static boolean start_clickFlag = false;
	private static final String TOP_CMD = "top";
	private static boolean monstopFlag = false;
	private static DoCmdThread worker;
	private static Process cmdProcess;
	private static BufferedReader readerforworker;

	BrowseRunningAppAdapter listadapter = null;
	ArrayList<String> listappname = null;
	ArrayList<Integer> listapppid = null;
	ArrayList<Integer> listappuid = null;
	ArrayList<String> listlabel = null;
	floatApplication myapplication = null;
	ListView listviewofapps = null;
	View lastselectedview = null;
	String focusedname = null;
	ActivityManager am = null;

	private boolean isappinfoupdate = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Black_NoTitleBar);
		setContentView(R.layout.browse_app_list);

		listview = (ListView) findViewById(R.id.listviewApp);
		
		mlistAppInfo = new ArrayList<RunningAppInfo>();
		
		listview.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v,int position, long id) {			
					if(!monstopFlag){
				    Log.d(TAG, "long  click item");
					float_memory_Start(v);
					String selectname = (String) mlistAppInfo.get(position).getPkgName();
					Log.d(TAG, selectname);

					if (selectname == null) {
						Log.d(TAG, "selectname is null");
						monstopFlag = true;
						return false;
					}
                    //启动第三方应用 
                        PackageManager packageManager = getPackageManager();
                        Intent intent=new Intent();
                        intent =packageManager.getLaunchIntentForPackage(selectname);
                        startActivity(intent);
                        finishActivity();

					if (myapplication == null) {
						myapplication = (floatApplication) getApplication();
					}
					if (myapplication != null) {
						String[] parasforprocess = { TOP_CMD };
						if (cmdProcess != null) {
							cmdProcess.destroy();
							cmdProcess = null;
						}
						if ((cmdProcess = startNewProcess(
								parasforprocess, null)) == null) {
							return false;
						}
						if ((readerforworker = getReaderFromProcess(cmdProcess)) == null) {
							return false;
						}

						ArrayList<String> parastothread = new ArrayList<String>();
						parastothread.add(TOP_CMD);
						parastothread.add(selectname);
						parastothread.add(""+ mlistAppInfo.get(position).getPid());
						String[] arrayParamter = new String[mlistAppInfo.size()];
						parastothread.toArray(arrayParamter);
						AsyncTask<String, Integer, String> threadobj = startRecordeInfo(arrayParamter, selectname, ""+ myapplication.getSelectPid());

						if (threadobj != null) {
							if (!threadobj.isCancelled()) {
								myapplication.setMonitorApp(selectname,myapplication.getSelectPid(),cmdProcess);
								setStart_clickFlag(true);
							}
						}
					}
					monstopFlag = true;

				} else {
					clickButonStop();
					float_memory_Stop(v);
                        finishActivity();
                        popupMessage("Stop", "存储路径：/Android/data/com.bd.floatball/files/xxx.cvs");
					monstopFlag = false;
				}
				return false;
			}
		});

		Intent intent = getIntent();
		int pid = intent.getIntExtra("EXTRA_PROCESS_ID", -1);
		
		if ( pid != -1) {
			mlistAppInfo =querySpecailPIDRunningAppInfo(intent, pid);
		}
		else{
		    mlistAppInfo = queryAllRunningAppInfo(); 
		}
		BrowseRunningAppAdapter browseAppAdapter = new BrowseRunningAppAdapter(this, mlistAppInfo);
		listview.setAdapter(browseAppAdapter);
		exitSys.getInstance().addActivity(this);
	}

    public void finishActivity(){
        this.finish();
    }

	public void float_memory_Start(View v) {
		Intent service = new Intent();
		service.setClass(BrowseRunningAppActivity.this, FloatService.class);
		startService(service);
	}

	public void float_memory_Stop(View v) {
		Intent serviceStop = new Intent();
		serviceStop.setClass(BrowseRunningAppActivity.this, FloatService.class);
		stopService(serviceStop);
	}


	private Process startNewProcess(String[] cmd, String workdirectory) {
		Process newprocess = null;
		String wd = workdirectory;
		if (wd == null) {
			wd = getApplicationContext().getFilesDir().getAbsolutePath();
		}
		if (wd != null) {
			try {
				ProcessBuilder builder = new ProcessBuilder(cmd);
				builder.directory(new File(wd));
				builder.redirectErrorStream(true);
				newprocess = builder.start();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return newprocess;
	}

	private BufferedReader getReaderFromProcess(Process process) {
		BufferedReader bufreader = null;
		InputStream in = null;

		if (process != null) {
			in = process.getInputStream();
			if (in != null) {
				InputStreamReader reader = new InputStreamReader(in);
				bufreader = new BufferedReader(reader);
			}
		}
		return bufreader;
	}

	private AsyncTask<String, Integer, String> startRecordeInfo(String[] cmd,
			String dstname, String pid) {
		Log.d(TAG, "new thread");
		worker = new DoCmdThread();
		return worker.execute(cmd);
	}

	private String splitAppName(String ori) {
		String simplename = null;
		if (ori != null) {
			if (!ori.contains(".")) {
				simplename = ori;
			} else {
				String[] nameparts = ori.split("\\.");
				int cn = nameparts.length;
				if (cn > 0) {
					simplename = "";
				}
				if (cn > 2) {
					simplename += nameparts[cn - 2];
					simplename += ".";
				}
				if (cn > 1) {
					simplename += nameparts[cn - 1];
				}
			}
		}
		return simplename;
	}

	private void setStart_clickFlag(boolean b) {
		start_clickFlag = b;
	}

	private void clickButonStop() {
		Log.d(TAG, "click buttonStop");
		if (myapplication == null) {
			myapplication = (floatApplication) getApplication();
		}
		if (myapplication != null) {
			myapplication.resetMonitorApp();
			myapplication.resetSelectApp();
			if (worker != null) {
				worker = null;
			}
			cmdProcess = null;
		}
		setStart_clickFlag(false);
	}

	private void popupMessage(String title, String msg) {
		Toast tst = Toast.makeText(getApplicationContext(), msg,Toast.LENGTH_LONG);
		tst.setGravity(Gravity.CENTER, 0, 0);
		LinearLayout toastView = (LinearLayout) tst.getView();
		ImageView imageView = new ImageView(getApplicationContext());
		imageView.setImageResource(R.drawable.ic_launcher);
		toastView.addView(imageView, 0);
		tst.show();
	}

	public static boolean isStart_clickFlag() {
		return start_clickFlag;
	}
	
	private void setListViewStatus(BrowseRunningAppAdapter adpater) {

		String selectappname = null;
		String monitorappname = null;

		if (myapplication == null) {
			myapplication = (floatApplication) getApplication();
		}
		if (myapplication != null) {
			selectappname = myapplication.getSelectPackName();
			monitorappname = myapplication.getMonitorPackName();
		}

		if (monitorappname != null) {
			myapplication.resetSelectApp();
			if (listappname.contains(monitorappname)) {
				Log.d(TAG, "monitored app:" + monitorappname + " is running");
				adpater.setBackgroundColorByName(monitorappname, Color.GREEN);
				adpater.setUpdateIconFlag(isappinfoupdate);
				focusedname = monitorappname;
				return;
			} else {
				Log.d(TAG, "monitored app:" + monitorappname + " is not running");
				myapplication.resetMonitorApp();
				clickButonStop();
				focusedname = null;
				return;
			}
		}
		if (selectappname != null) {
			if (listappname.contains(selectappname)) {
				Log.d(TAG, "selected app:" + selectappname + " is running");
				adpater.setBackgroundColorByName(selectappname, Color.GRAY);
				adpater.setUpdateIconFlag(isappinfoupdate);
				focusedname = selectappname;
				return;
			} else {
				Log.d(TAG, "selected app:" + selectappname + " is not running");
				myapplication.resetSelectApp();
				focusedname = null;
				return;
			}
		}
		Log.d(TAG, "NO SELECT NAME OR RUNNING APP, IN setListViewStatus");
	}

	private void setListViewSelection() {
		if (focusedname != null) {
			int indexoflastselected = listappname.indexOf(focusedname);
//			Log.d(TAG, "total num:" + listviewofapps.getCount() + " focus at position:" + indexoflastselected + " focus app:" + focusedname);
			listviewofapps.setSelection(indexoflastselected);
			String simplename = splitAppName(focusedname);
			if (simplename != null) {
				popupMessage(TAG, simplename + "is selected");
			}
		} 

	}

	private List<RunningAppInfo> queryAllRunningAppInfo() {
		pm = this.getPackageManager();
		List<ApplicationInfo> listAppcations = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		Collections.sort(listAppcations,new ApplicationInfo.DisplayNameComparator(pm));// 排序
		Map<String, ActivityManager.RunningAppProcessInfo> pgkProcessAppMap = new HashMap<String, ActivityManager.RunningAppProcessInfo>();

		ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
			int pid = appProcess.pid; 
			String processName = appProcess.processName;
			Log.i(TAG, "processName: " + processName + "  pid: " + pid);
			String[] pkgNameList = appProcess.pkgList;
			for (int i = 0; i < pkgNameList.length; i++) {
				String pkgName = pkgNameList[i];
				Log.i(TAG, "packageName " + pkgName + " at index " + i+ " in process " + pid);
				pgkProcessAppMap.put(pkgName, appProcess);
			}
		}
		List<RunningAppInfo> runningAppInfos = new ArrayList<RunningAppInfo>(); // 保存过滤查到的AppInfo

		for (ApplicationInfo app : listAppcations) {
			if (pgkProcessAppMap.containsKey(app.packageName)) {
				int pid = pgkProcessAppMap.get(app.packageName).pid;
				String processName = pgkProcessAppMap.get(app.packageName).processName;
				if(!processName.startsWith("com.android.") &&
						!processName.startsWith("com.sonyericsson.") &&
						!processName.startsWith("com.sec.") &&
						!processName.startsWith("com.samsung.") &&
						!processName.startsWith("com.google.") &&
						!processName.startsWith("com.huawei.") &&
						!processName.startsWith("com.htc") &&
						!processName.startsWith("com.zte") &&
						!processName.startsWith("com.miui.") &&
						!processName.startsWith("com.qualcomm.") &&
						!processName.startsWith("android.process") &&
						!processName.startsWith("com.baidu")  &&
						!processName.contains("system") &&					
						!processName.contains("device") &&
						!processName.endsWith(".home")){
					runningAppInfos.add(getAppInfo(app, pid, processName));
				}
			}
		}

		return runningAppInfos;

	}
	
	private List<RunningAppInfo> querySpecailPIDRunningAppInfo(Intent intent , int pid) {


		String[] pkgNameList = intent.getStringArrayExtra("EXTRA_PKGNAMELIST");
		String processName = intent.getStringExtra("EXTRA_PROCESS_NAME");		
		pm = this.getPackageManager();
		List<RunningAppInfo> runningAppInfos = new ArrayList<RunningAppInfo>(); // 保存过滤查到的AppInfo

		for(int i = 0 ; i<pkgNameList.length ;i++){
		   ApplicationInfo appInfo;
		  try {
			appInfo = pm.getApplicationInfo(pkgNameList[i], 0);
			if(!processName.startsWith("com.android.") &&
					!processName.startsWith("com.sonyericsson.") &&
					!processName.startsWith("com.sec.") &&
					!processName.startsWith("com.samsung.") &&
					!processName.startsWith("com.google.") &&
					!processName.startsWith("com.huawei.") &&
					!processName.startsWith("com.htc") &&
					!processName.startsWith("com.zte") &&
					!processName.startsWith("com.miui.") &&
					!processName.startsWith("com.qualcomm.") &&
					!processName.startsWith("android.process") &&
					!processName.contains("system") &&					
					!processName.contains("device") &&
					!processName.startsWith("com.baidu.") &&
					!processName.endsWith(".home")){
				runningAppInfos.add(getAppInfo(appInfo, pid, processName));
			}
		  }
		  catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  } 
		}
		return runningAppInfos ;
	}
	
	private RunningAppInfo getAppInfo(ApplicationInfo app, int pid, String processName) {
		RunningAppInfo appInfo = new RunningAppInfo();
		appInfo.setAppLabel((String) app.loadLabel(pm));
		appInfo.setAppIcon(app.loadIcon(pm));
		appInfo.setPkgName(app.packageName);
		appInfo.setPid(pid);
		appInfo.setProcessName(processName);
		appInfo.setCachesize(new PackageStats(app.packageName).cacheSize + "");
		appInfo.setCodesize(new PackageStats(app.packageName).codeSize + "");
		appInfo.setDatasize(new PackageStats(app.packageName).dataSize + "");
		
		return appInfo;
	}
	

	class DoCmdThread extends AsyncTask<String, Integer, String> {

		private String TAGTHREAD = "DoCmdThread";
		private String pid = null;
		private ArrayList<String> filters = null;
		private File fileOutCSV = null;

		boolean titleflag = false;

		ArrayList<String> keys = null;
		ArrayList<Integer> keyindex = null;
		ArrayList<String> values = null;

		ArrayList<String> valuesBuff = null;
		@SuppressWarnings("unused")
		private int accounting = 0;

		BufferedWriter writer = null;
		private long starttime = 0;
		private String starttimestr = null;
		private long stoptime = 0;
		private String stoptimestr = null;

		public boolean isExternalStorageWritable() {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				return true;
			}
			return false;
		}

		private boolean createOutputfile(String filename) {
			String filepath;

			if (isExternalStorageWritable() == false) {
//				Log.d(TAG, "external storage is not available now");
				return false;
			}
			File dir = getExternalFilesDir(null);
			fileOutCSV = new File(dir, filename);
			if (fileOutCSV != null) {
				filepath = fileOutCSV.getPath();
				Log.d(TAG, "create file name:" + filepath);
				return true;
			}
			return false;
		}

		private boolean initThread(String _dstname, String _pid) {
			pid = _pid;

			if (keys == null) {
				keys = new ArrayList<String>();
				keys.add("USS");
				keys.add("PSS");
				keys.add("RSS");
				keys.add("CPU");
				keys.add("PID");
			}
			if (info_index == null) {
				info_index = new ArrayList<String>();
				info_index.add("USS");
				info_index.add("PSS");
				info_index.add("RSS");
				info_index.add("CPU");
				info_index.add("PID");
			}
			if (keyindex == null) {
				keyindex = new ArrayList<Integer>();
				keyindex.add(-1);
				keyindex.add(-1);
				keyindex.add(-1);
				keyindex.add(-1);
				keyindex.add(-1);
			}
			if (values == null) {
				values = new ArrayList<String>();
				values.add("0");
				values.add("0");
				values.add("0");
				values.add("0");
				values.add(pid);
			}
			if (info == null) {
				info = new ArrayList<String>();
				info.add("0");
				info.add("0");
				info.add("0");
				info.add("0");
				info.add(pid);
			}

			filters = new ArrayList<String>();
			String simplename = splitAppName(_dstname);
			String[] nameparts = simplename.split("\\.");
			int i = nameparts.length;
			int j = 1;
			for (--i; i >= 0; --i, ++j) {
				if (j > 2)
					break;
				filters.add(nameparts[i]);
			}
			
			if (myapplication == null) {
				myapplication = (floatApplication) getApplication();
			}
			Time t = new Time();
			t.setToNow();
			starttime = t.toMillis(false);
			starttimestr = "" + t.year + "-" + (t.month + 1) + "-" + t.monthDay + "-" + t.hour + "-" + t.minute + "-" + t.second;
			String outputfilename = simplename + "." + _pid + "." + (t.month + 1) + "-" + (t.monthDay - 5) + "-" + t.hour + "-" + t.minute + "-" + t.second + ".csv";
			if (!createOutputfile(outputfilename)) {
				return false;
			}

			try {
				writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(fileOutCSV), "utf8"));
				if (writer == null) {
					return false;
				}

				String filestartcontent = "USS(kb),PSS(kb),RSS(kb),CPU(%),PID,android.phone";
				outputToFile(0, simplename);
				outputToFile(0, filestartcontent);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				Log.d(TAGTHREAD, "Exception:" + e.getClass().getSimpleName());
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				Log.d(TAGTHREAD, "Exception:" + e.getClass().getSimpleName());
				return false;
			}
			return true;
		}

		private boolean isTitleLine(String line) {
			int indexcpu = keys.indexOf("CPU");
			int indexpid = keys.indexOf("PID");
			if (line.contains(keys.get(indexcpu)) && line.contains(keys.get(indexpid))) {
				if (titleflag == false) {
					line = line.trim();
					String[] allfields = line.split(" +");

					for (int i = 0; i < allfields.length; ++i) {
						if (allfields[i].contains(keys.get(indexcpu))) {
							keyindex.set(indexcpu, i);
						} else if (allfields[i].contains(keys.get(indexpid))) {
							keyindex.set(indexpid, i);
						}
					}
					if (keyindex.get(indexcpu) >= 0 && keyindex.get(indexpid) >= 0) {						
						String logfields = "";
						for (String e : allfields) {
							logfields = logfields + e + ",";
						}
//						Log.d(TAG, "title fileds:" + logfields);
						return true;
					}
				}
				return true;
			}
			return false;
		}

		private boolean isDstAppLine(String line) {
			int cmparetime = filters.size();
			for (int i = 0; i < cmparetime; ++i) {
				if (!line.contains(filters.get(i))) {
					return false;
				} else {
					if (i == (cmparetime - 1))
						return true;
				}
			}
			return false;
		}

		private boolean readDstAppMem() {
			if (am == null) {
				am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			}
			int[] pids = { 0 };
			pids[0] = Integer.parseInt(pid);
			Debug.MemoryInfo[] myinfos = am.getProcessMemoryInfo(pids);
			Debug.MemoryInfo myinfo = myinfos[0];
			int indexrss = keys.indexOf("RSS");
			int indexpss = keys.indexOf("PSS");
			int indexuss = keys.indexOf("USS");
			values.set(indexrss, "" + myinfo.getTotalSharedDirty());
			values.set(indexpss, "" + myinfo.getTotalPss());
			values.set(indexuss, "" + myinfo.getTotalPrivateDirty());
			return true;
		}

		@SuppressWarnings("unchecked")
		private boolean outputToFile(int flag, String srcstr) {
			info = (ArrayList<String>) values.clone();
			int maxnum = 100;
			if (valuesBuff == null) {
				valuesBuff = new ArrayList<String>();
			}

			if (flag == 0 && srcstr != null) {
				if (writer == null)
					return false;
				try {
					writer.write(srcstr);
					writer.newLine();
					writer.flush();
					return true;
				} catch (IOException e) {
					e.printStackTrace();
					Log.d(TAG, "write error, string: " + srcstr);
					return false;
				}
			}

			if (flag == 0 && srcstr == null) {
				String debuginfo = "info line:";
				for (int i = 0; i < values.size(); ++i) {
					debuginfo = debuginfo + " " + values.get(i);
					if(values.get(keys.indexOf("CPU")).equals("0")){
						continue;
					}
					else{
						valuesBuff.add(values.get(i));	
					}					
				}
				Log.d(TAG, debuginfo);
				if (valuesBuff.size() > maxnum) {
					flag = 1;
				}
			}
			if (flag != 0) {
				if (writer == null)
					return false;
				try {
					int j = 0;
					for (String e : valuesBuff) {
						++j;
						writer.write(e + ",");
						if (j % values.size() == 0)
							writer.newLine();
					}
					writer.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
					return false;
				}
				valuesBuff.clear();
			}

			
			return true;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		private boolean readDstAppStatus(String line) {
			line = line.trim();
			String[] allfields = line.split(" +");

			String logfields = "";
			for (String e : allfields) {
				logfields = logfields + e + ",";
			}
			Log.d(TAG, "dstapp fileds:" + logfields);

			int cnt = values.size();
			int i = keys.indexOf("CPU");
			for (; i < cnt; ++i) {
				int index = keyindex.get(i);
				if (index > (allfields.length - 1))
					return false;
				allfields[index] = allfields[index].replaceAll("%", "");
				values.set(i, allfields[index]);
			}
			if (i == cnt) {
				return true;
			}
			return false;
		}

		@Override
		protected String doInBackground(String... args) {			
			if (!initThread(args[1], args[2])) {
				Log.d(TAGTHREAD, "initThread failed");
				return "fail";
			}

			if (writer == null) {
				Log.d(TAGTHREAD, "writer is null");
				return "fail";
			}

			if (cmdProcess != null && readerforworker != null) {
				String line;
				try {
					line = readerforworker.readLine();
					titleflag = false;
					boolean isiterative = false;

					while (line != null) {
						if (isTitleLine(line)) {
							if (isiterative == false) {
								Log.d(TAG, "begin iteration");
								isiterative = true;
							} else {
								Log.d(TAG,"last iteration read cpu failed,so add 0");
								values.set(keys.indexOf("PID"), pid);
								values.set(keys.indexOf("CPU"), "0");
								accounting++;
								readDstAppMem();
								if (outputToFile(0, null)) {
									;
								} else {
									Log.d(TAG,"zero info,write file failed, now exit thread");
									if (writer != null) {
										writer.flush();
									}
									return "file output error";
								}
							}
							line = readerforworker.readLine();
							continue;
						}
						if (isiterative && isDstAppLine(line)) {
							if (readDstAppStatus(line)) {
								accounting++;
								readDstAppMem();
								if (outputToFile(0, null)) {
									isiterative = false;
								} else {
									Log.d(TAG,"cpu info, write file failed, now exit thread");
									if (writer != null) {
										writer.flush();
									}
									return "file output error";
								}
							} else {
								isiterative = false;
							}
						}
						line = readerforworker.readLine();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (writer != null)
				try {
					outputToFile(1, null);
					outputSummery();
					writer.close();
					writer = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			return null;
		}

		private void outputSummery() {
			long millisecond = 0;
			long second = 0;
			long minute = 0;
			String timediffstr = "";

			Time t = new Time();
			t.setToNow();
			stoptime = t.toMillis(false);
			stoptimestr = "" + t.year + "-" + (t.month + 1) + "-" + t.monthDay
					+ "-" + t.hour + "-" + t.minute + "-" + t.second;

			long timediff = stoptime - starttime;
			Log.d(TAG, "timediff: " + timediff);
			millisecond = timediff;
			if (millisecond > 1000) {
				second = millisecond / 1000;
				millisecond = timediff % 1000;
				if (second > 59) {
					minute = second / 60;
					second = second % 60;					
				}
			}

			if (minute > 0) {
				timediffstr = minute + "Mins";
			}
			if (second > 0) {
				timediffstr = timediffstr + second + "Secs";
			}
			if (millisecond > 0) {
				timediffstr = timediffstr + millisecond + "Millisecs";
			}

			if (writer != null) {
				try {
					writer.newLine();
					writer.write("Summery");					
					writer.newLine();
					writer.write("start   :" + starttimestr);
					writer.newLine();
					writer.write("stop    :" + stoptimestr );
					writer.newLine();
					writer.write("duration:" + timediffstr);
					writer.newLine();					
					writer.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.d(TAG, "thread result: " + result);
			clickButonStop();

			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					writer = null;
				}
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
	}

	class DataCenter extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... arg0) {
			boolean initresult = false;
			Log.d(TAG, "doInBackground");
			if (myapplication == null) {
				myapplication = (floatApplication) getApplication();
			}
			if (myapplication != null) {
				initresult = myapplication.initPackageManager();
			}
			if (initresult) {
				return "succ";
			}
			return "fail";
		}

		@Override
		protected void onPostExecute(String result) {

			super.onPostExecute(result);
			if (result.equals("succ")) {
				isappinfoupdate = true;
				listadapter = new BrowseRunningAppAdapter(BrowseRunningAppActivity.this,mlistAppInfo);
				setListViewStatus(listadapter);
				listviewofapps.setAdapter(listadapter);
				setListViewSelection();
			}
		}
	}

}