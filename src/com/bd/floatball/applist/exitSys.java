package com.bd.floatball.applist;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.app.Service;

public class exitSys extends Application {

	private List<Activity> activityList = new LinkedList<Activity>();
	private List<Service> serviceList = new LinkedList<Service>();

	private static exitSys instance;

	private exitSys() {
	}

	// 单例模式中获取唯一的ExitApplication 实例
	public static exitSys getInstance() {
		if (null == instance) {
			instance = new exitSys();
		}
		return instance;

	}

	// 添加Activity 到容器中
	public void addActivity(Activity activity) {
		activityList.add(activity);
	}
	
	public void addService(Service service) {
		serviceList.add(service);
	}

	// 遍历所有Activity 并finish

	public void exit() {

		for (Service service : serviceList) {
			service.stopSelf();
		}
		
		for (Activity activity : activityList) {
			activity.finish();
		}

		System.exit(0);

	}
}