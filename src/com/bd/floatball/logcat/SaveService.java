package com.bd.floatball.logcat;

import android.app.IntentService;
import android.content.Intent;

public class SaveService extends IntentService {
	public SaveService() {
		super("saveService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		//Log.d("logcat", "handling intent");

		LogSaver saver = new LogSaver(this);
		saver.save();

		Lock.release();
	}
}
