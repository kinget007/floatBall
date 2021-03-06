package com.bd.floatball.mode;

import android.graphics.drawable.Drawable;

public class RunningAppInfo {
  
	private String appLabel;  
	private Drawable appIcon ;  
	private String pkgName ;  
	
	private int pid ;
	private String processName ; 
	
	private String cachesize; 	  
	private String datasize ; 
	private String codesize ; 
	
	public RunningAppInfo(){}
	
	public String getAppLabel() {
		return appLabel;
	}
	public void setAppLabel(String appName) {
		this.appLabel = appName;
	}
	public Drawable getAppIcon() {
		return appIcon;
	}
	public void setAppIcon(Drawable appIcon) {
		this.appIcon = appIcon;
	}
	public String getPkgName(){
		return pkgName ;
	}
	public void setPkgName(String pkgName){
		this.pkgName=pkgName ;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getCachesize() {
		return cachesize;
	}

	public void setCachesize(String cachesize) {
		this.cachesize = cachesize;
	}

	public String getDatasize() {
		return datasize;
	}

	public void setDatasize(String datasize) {
		this.datasize = datasize;
	}

	public String getCodesize() {
		return codesize;
	}

	public void setCodesize(String codesize) {
		this.codesize = codesize;
	}
	
	
}
